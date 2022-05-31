package com.komorebi.video.awsUtils;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Location;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;


public class S3Helper {

    private final AmazonS3 s3Client;

    public S3Helper(){
        this.s3Client = AmazonS3ClientBuilder.standard().build();
    }

    public String downloadS3ObjectAsFile(String objectURI, LambdaLogger logger) throws Exception {
        String localPath = String.format("/tmp/%s", parseFileName(objectURI, logger));
        File localFile = new File(localPath);
        S3Location loc = parseObjectURI(objectURI, logger);
        GetObjectRequest objectRequest = new GetObjectRequest(loc.getBucketName(), loc.getPrefix());
        s3Client.getObject(objectRequest, localFile);
        if (!(localFile.exists() && localFile.canRead())){
            throw new Exception("Error while downloading an S3 object as file");
        }
        return localPath;
    }

    public void uploadLocalFileToS3(String destinationURI, String localPath, LambdaLogger logger) throws Exception{
        S3Location loc = parseObjectURI(destinationURI, logger);
        File localFile = new File(localPath);
        try{
            InputStream stream = new FileInputStream(localFile);
            PutObjectRequest request = new PutObjectRequest(
                    loc.getBucketName(),
                    loc.getPrefix(),
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

    private S3Location parseObjectURI(String objectURI, LambdaLogger logger) throws Exception{
        S3Location loc = new S3Location();
        Pattern pattern = Pattern.compile("s3://([^/]+)/(.*)");
        Matcher matcher = pattern.matcher(objectURI);
        if (matcher.find()){
            loc.setBucketName(matcher.group(1));
            loc.setPrefix(matcher.group(2));
        } else{
            throw new Exception("Unable to parse S3 object URI");
        }
        logger.log("Parsed bucket: " + loc.getBucketName() + "\n");
        logger.log("Parsed object key " + loc.getPrefix() + "\n");
        return loc;
    }

    private String parseFileName(String objectURI, LambdaLogger logger)throws Exception{
        String fileName;
        Pattern pattern = Pattern.compile("s3://.+/(.*)");
        Matcher matcher = pattern.matcher(objectURI);
        if (matcher.find()){
            fileName = matcher.group(1);
        } else{
            throw new Exception("Unable to parse S3 object URI");
        }
        logger.log("FILE NAME : " + fileName + "\n");
        return fileName;
    }
}
