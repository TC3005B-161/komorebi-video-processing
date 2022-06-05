package com.komorebi.video;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.lambda.thirdparty.com.google.gson.Gson;
import com.amazonaws.lambda.thirdparty.com.google.gson.GsonBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import com.komorebi.video.DBUtils.RecordingDynamoDBHelper;
import com.komorebi.video.videoUtils.S3Helper;


public class LambdaHandler {

    private final S3Helper s3;
    private final Gson gson;
    private final RecordingDynamoDBHelper dynamo;


    public LambdaHandler() throws Exception {
        s3 = new S3Helper();
        gson = new GsonBuilder().setPrettyPrinting().create();
        dynamo = new RecordingDynamoDBHelper();
    }

    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) throws Exception {
        LambdaLogger logger = context.getLogger();
        logger.log("RECEIVED EVENT: " +  gson.toJson(sqsEvent) + "\n");

        List<SQSBatchResponse.BatchItemFailure> batchItemFailures = new ArrayList<>();

        for (SQSEvent.SQSMessage message : sqsEvent.getRecords()){
            String messageId = message.getMessageId();
            logger.log("PROCESSING MESSAGE ID: " + messageId + "\n");

            try {
                ProcessVideoInput processData = gson.fromJson(message.getBody(), ProcessVideoInput.class);
                logger.log("MESSAGE BODY: " +  gson.toJson(processData) + "\n");
                RecordProcessor processor = new RecordProcessor(processData, s3, dynamo, logger);
                processor.process();

            } catch (Exception e){
                logger.log("FAILED PROCESSING FOR MESSAGE ID: " +  messageId + "\n");
                batchItemFailures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }

        logger.log("Execution finished");
        return new SQSBatchResponse(batchItemFailures);
    }
}
