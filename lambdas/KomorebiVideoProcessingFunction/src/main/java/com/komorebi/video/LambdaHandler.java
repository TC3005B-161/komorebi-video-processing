package com.komorebi.video;

import com.amazonaws.lambda.thirdparty.com.google.gson.Gson;
import com.amazonaws.lambda.thirdparty.com.google.gson.GsonBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.komorebi.video.DBUtils.DynamoDBHelper;import com.komorebi.video.DBUtils.Recording;import com.komorebi.video.DBUtils.RecordingDTO;import com.komorebi.video.videoUtils.S3Helper;
import com.komorebi.video.videoUtils.VideoProcessor;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import java.io.File;import java.util.ArrayList;
import java.util.List;

public class LambdaHandler {

    private final S3Helper s3;
    private final Gson gson;
    private LambdaLogger logger;
    private DynamoDBHelper dynamo;


    public LambdaHandler() throws Exception {
        s3 = new S3Helper();
        gson = new GsonBuilder().setPrettyPrinting().create();
        dynamo = new DynamoDBHelper();
    }

    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) throws Exception {
        LambdaLogger logger = context.getLogger();
        this.logger = logger;
        logger.log("RECEIVED EVENT: " +  gson.toJson(sqsEvent) + "\n");

        List<SQSBatchResponse.BatchItemFailure> batchItemFailures = new ArrayList<>();

        for (SQSEvent.SQSMessage message : sqsEvent.getRecords()){
            String messageId = message.getMessageId();
            logger.log("PROCESSING MESSAGE ID: " + messageId + "\n");

            try {
                ProcessVideoInput processData = gson.fromJson(message.getBody(), ProcessVideoInput.class);
                logger.log("MESSAGE BODY: " +  gson.toJson(processData) + "\n");
                processRecord(processData);

            } catch (Exception e){
                logger.log("FAILED PROCESSING FOR MESSAGE ID: " +  messageId + "\n");
                batchItemFailures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }

        logger.log("Execution finished");
        return new SQSBatchResponse(batchItemFailures);
    }

    public void processRecord(ProcessVideoInput processData) throws Exception {
        EnvironmentVariables vars = EnvironmentVariables.getInstance();

        Recording recordingItem = dynamo.getRecording(processData.getRecordingID());
        RecordingDTO recordingDTO = new RecordingDTO(recordingItem);

        String audioInputS3Path = s3.getKeyFromUniquePrefix(
                vars.AC_AUDIO_BUCKET,
                recordingDTO.buildACRecordingPrefix(),
                logger);

        FileManager amazonAudioInput = new FileManager(
                vars.AC_AUDIO_BUCKET,
                audioInputS3Path,
                logger);

        s3.downloadS3ObjectAsFile(amazonAudioInput);

        FileManager komorebiInput = new FileManager(
                vars.VIDEO_INPUT_BUCKET,
                recordingDTO.buildKomorebiInputRecordingPath(),
                logger);

        s3.downloadS3ObjectAsFile(komorebiInput);

        String komorebiAudioFull = FileManager.buildOutputPath("wav");
        VideoProcessor.extractAudio(komorebiInput.getLocalFilePath(), komorebiAudioFull, logger);

        String komorebiVideoFull = FileManager.buildOutputPath("mp4");
        VideoProcessor.extractVideo(komorebiInput.getLocalFilePath(), komorebiVideoFull, logger);

        String komorebiAudioACW = FileManager.buildOutputPath("wav");
        VideoProcessor.splitAudio(
                komorebiAudioFull,
                komorebiAudioACW,
                processData.getCallEndingTimestamp(),
                logger);

        String mergedAudios = FileManager.buildOutputPath("wav");
        VideoProcessor.concatAudios(amazonAudioInput.getLocalFilePath(), komorebiAudioACW, mergedAudios, logger);

        FileManager muxedVideo = new FileManager(
                vars.VIDEO_OUTPUT_BUCKET,
                recordingDTO.buildKomorebiOutputVideoPath(),
                logger);
        VideoProcessor.muxVideo(komorebiVideoFull, mergedAudios, muxedVideo.getLocalFilePath(), logger);
        s3.uploadLocalFileToS3(muxedVideo, logger);

        FileManager thumbnail = new FileManager(
                vars.VIDEO_OUTPUT_BUCKET,
                recordingDTO.buildKomorebiOutputThumbnailPath(),
                logger);
        VideoProcessor.extractThumbnail(muxedVideo.getLocalFilePath(), thumbnail.getLocalFilePath(), logger);
        s3.uploadLocalFileToS3(thumbnail, logger);

        dynamo.setRecordingAsProcessed(recordingItem);
    }

}
