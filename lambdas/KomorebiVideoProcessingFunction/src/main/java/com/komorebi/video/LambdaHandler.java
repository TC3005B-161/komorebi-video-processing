package com.komorebi.video;

import com.amazonaws.lambda.thirdparty.com.google.gson.Gson;
import com.amazonaws.lambda.thirdparty.com.google.gson.GsonBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.komorebi.video.awsUtils.S3Helper;
import com.komorebi.video.videoUtils.VideoProcessor;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LambdaHandler {

    private final S3Helper s3;
    private final Gson gson;


    public LambdaHandler(){
        s3 = new S3Helper();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void handleRequest(InputStream jsonStream, Context context) throws Exception{
        LambdaLogger logger = context.getLogger();

        String jsonEvent = IOUtils.toString(jsonStream, StandardCharsets.UTF_8);
        logger.log("RECEIVED EVENT: " +  jsonEvent);

        ProcessVideoInput processData = gson.fromJson(jsonEvent, ProcessVideoInput.class);
        logger.log("PARSED EVENT: " + gson.toJson(processData));

        String audioLocalFile = s3.downloadS3ObjectAsFile(processData.getAudioS3URI(), logger);
        String videoLocalFile = s3.downloadS3ObjectAsFile(processData.getVideoS3URI(), logger);

        VideoProcessor videoProcessor = new VideoProcessor(audioLocalFile, videoLocalFile);
        String recordingLocalFile = videoProcessor.processVideo(logger);

        s3.uploadLocalFileToS3(processData.getVideoS3URI(), recordingLocalFile, logger);

        logger.log("Execution finished");
    }

}
