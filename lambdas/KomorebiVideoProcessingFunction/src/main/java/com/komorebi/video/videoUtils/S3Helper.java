package com.komorebi.video.videoUtils;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.*;import com.komorebi.video.FileManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.io.File;


public class S3Helper {

    private final AmazonS3 s3Client;

    public S3Helper(){
        this.s3Client = AmazonS3ClientBuilder.standard().build();
    }

    public String getKeyFromUniquePrefix(String S3Bucket, String prefix, LambdaLogger logger) throws Exception{
        ListObjectsV2Request listRequest = new ListObjectsV2Request().
            withBucketName(S3Bucket).
            withPrefix(prefix);

        ListObjectsV2Result listResult = s3Client.listObjectsV2(listRequest);
        List<S3ObjectSummary> objectSummaries = listResult.getObjectSummaries();
        if (objectSummaries.size() != 1){
            throw new Exception("Prefix is not unique or non existent");
        }

        return objectSummaries.get(0).getKey();
    }

    public void downloadS3ObjectAsFile(FileManager file) throws Exception {
        File localFile = new File(file.getLocalFilePath());
        GetObjectRequest objectRequest = new GetObjectRequest(file.getS3Bucket(), file.getS3Path());
        s3Client.getObject(objectRequest, localFile);
        if (!(localFile.exists() && localFile.canRead())){
            throw new Exception("Error while downloading an S3 object as file");
        }
    }

    public void uploadLocalFileToS3(FileManager file, LambdaLogger logger) throws Exception{
        File localFile = new File(file.getLocalFilePath());
        try{
            InputStream stream = new FileInputStream(localFile);
            PutObjectRequest request = new PutObjectRequest(
                    file.getS3Bucket(),
                    file.getS3Path(),
                    stream,
                    new ObjectMetadata()
            );
            s3Client.putObject(request);
        } catch (FileNotFoundException e){
            logger.log("File not found while trying to upload to S3");
            throw e;
        } catch(SdkClientException e){
            logger.log("AWS SDK error while uploading to S3");
            throw e;
        }
    }

}
