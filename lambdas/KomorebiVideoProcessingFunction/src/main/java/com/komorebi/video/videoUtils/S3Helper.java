package com.komorebi.video.videoUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.io.File;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.kms.model.NotFoundException;import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.*;


/**
* Abstracts the S3 related logic and SDK calls. All the downloads and uploads are made to a from storage (i.e. file
* system), not from memory. To accomplish this, the class is tightly coupled with the FileManager class, in order to
* map an S3 object with a local file path in a consistent way.
*/
public class S3Helper {

    private final AmazonS3 s3Client;

    public S3Helper(){
        this.s3Client = AmazonS3ClientBuilder.standard().build();
    }

    /**
    * Useful for finding the exact key of an object if a unique prefix is available.
    * @param S3Bucket - String The S3 bucket in which the object is stored.
    * @param prefix - String A unique prefix which is not necessarily the full path.
    * @param logger - LambdaLogger
    * @return String with the full path of the object with the given unique prefix.
    * @throws NotFoundException - If there is not a file with the given prefix, or if it is duplicated.
    */
    public String getKeyFromUniquePrefix(String S3Bucket, String prefix, LambdaLogger logger) throws NotFoundException{
        logger.log("S3 LIST OBJECTS. BUCKET: " + S3Bucket + " PREFIX: " + prefix + "\n");
        ListObjectsV2Request listRequest = new ListObjectsV2Request().
            withBucketName(S3Bucket).
            withPrefix(prefix);

        ListObjectsV2Result listResult = s3Client.listObjectsV2(listRequest);
        List<S3ObjectSummary> objectSummaries = listResult.getObjectSummaries();
        if (objectSummaries.size() != 1){
            logger.log(String.format("FOUND %d OBJECTS", objectSummaries.size()));
            throw new NotFoundException("S3 prefix is not unique or non existent " + prefix);
        }

        return objectSummaries.get(0).getKey();
    }

    /**
    *
    * @param file
    * @throws Exception
    */
    public void downloadS3ObjectAsFile(FileManager file) throws IllegalStateException {
        File localFile = new File(file.getLocalFilePath());
        GetObjectRequest objectRequest = new GetObjectRequest(file.getS3Bucket(), file.getS3Path());
        s3Client.getObject(objectRequest, localFile);
        if (!(localFile.exists() && localFile.canRead())){
            throw new IllegalStateException("Error while downloading an S3 object as file");
        }
    }

    /**
    *
    * @param file
    * @param logger
    * @throws Exception
    */
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
            throw new Exception("File not found while trying to upload to S3", e);
        } catch(SdkClientException e){
            logger.log("AWS SDK error while uploading to S3");
            throw new Exception("AWS SDK error while uploading to S3");
        }
    }

}
