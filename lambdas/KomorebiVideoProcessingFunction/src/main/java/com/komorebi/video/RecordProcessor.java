package com.komorebi.video;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.komorebi.video.DBUtils.RecordingDynamoDBHelper;
import com.komorebi.video.DBUtils.Recording;
import com.komorebi.video.DBUtils.RecordingDTO;
import com.komorebi.video.videoUtils.FileManager;
import com.komorebi.video.videoUtils.S3Helper;
import com.komorebi.video.videoUtils.VideoProcessor;

public class RecordProcessor {

    private final ProcessVideoInput processData;
    private final Recording recordingItem;
    private final RecordingDynamoDBHelper dynamo;
    private final RecordingDTO recordingDTO;
    private final S3Helper s3;
    private final LambdaLogger logger;

    public RecordProcessor(ProcessVideoInput processData, S3Helper s3, RecordingDynamoDBHelper dynamo, LambdaLogger logger) throws Exception{
        this.processData = processData;
        this.dynamo = dynamo;
        this.logger = logger;
        this.s3 = s3;

        this.recordingItem = dynamo.getRecording(processData.getRecordingID());
        this.recordingDTO = new RecordingDTO(recordingItem);
    }

    public void process() throws Exception{
        FileManager amazonConnectAudio = this.getAmazonConnectAudio();

        List<String> komorebiFiles = this.getKomorebiAudioAndVideo();
        String komorebiAudioACW = komorebiFiles.get(0);
        String komorebiVideoFull = komorebiFiles.get(1);

        FileManager muxedVideo = this.getMuxedVideo(amazonConnectAudio, komorebiAudioACW, komorebiVideoFull);

        this.getThumbnailImage(muxedVideo);

        dynamo.setRecordingAsProcessed(recordingItem);
    }

    private FileManager getAmazonConnectAudio() throws Exception{
        String audioInputS3Path = s3.getKeyFromUniquePrefix(
                EnvironmentVariables.getInstance().AC_AUDIO_BUCKET,
                recordingDTO.buildACRecordingPrefix(),
                logger);

        FileManager amazonAudioInput = new FileManager(
                EnvironmentVariables.getInstance().AC_AUDIO_BUCKET,
                audioInputS3Path,
                logger);

        s3.downloadS3ObjectAsFile(amazonAudioInput);
        return amazonAudioInput;
    }

    private List<String> getKomorebiAudioAndVideo() throws Exception {
        FileManager komorebiInput = new FileManager(
                        EnvironmentVariables.getInstance().VIDEO_INPUT_BUCKET,
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

        List<String> files = new ArrayList<>();
        files.add(komorebiAudioACW);
        files.add(komorebiVideoFull);

        return files;
    }

    private FileManager getMuxedVideo(FileManager amazonAudioInput, String komorebiAudioACW, String komorebiVideoFull) throws Exception{
        String mergedAudios = FileManager.buildOutputPath("wav");
        VideoProcessor.concatAudios(amazonAudioInput.getLocalFilePath(), komorebiAudioACW, mergedAudios, logger);

        FileManager muxedVideo = new FileManager(
                EnvironmentVariables.getInstance().VIDEO_OUTPUT_BUCKET,
                recordingDTO.buildKomorebiOutputVideoPath(),
                logger);
        VideoProcessor.muxVideo(komorebiVideoFull, mergedAudios, muxedVideo.getLocalFilePath(), logger);
        s3.uploadLocalFileToS3(muxedVideo, logger);

        return muxedVideo;
    }

    private FileManager getThumbnailImage(FileManager muxedVideo) throws Exception{
        FileManager thumbnail = new FileManager(
                EnvironmentVariables.getInstance().VIDEO_OUTPUT_BUCKET,
                recordingDTO.buildKomorebiOutputThumbnailPath(),
                logger);

        VideoProcessor.extractThumbnail(muxedVideo.getLocalFilePath(), thumbnail.getLocalFilePath(), logger);
        s3.uploadLocalFileToS3(thumbnail, logger);
        return thumbnail;
    }

}
