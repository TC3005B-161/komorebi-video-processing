package com.komorebi.video.videoUtils;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class FileManager {

    private final String s3Path;
    private final String s3Bucket;
    private final String s3Directory;
    private final String s3FileName;
    private final String fileExtension;
    private final String localFilePath;

    private final String PARSE_FILE_NAME_FROM_PATH = ".+/([^.]+).+";
    private final String PARSE_FILE_DIR_FROM_PATH = "(.+)/.+";
    private final String PARSE_FILE_EXT_FROM_PATH = ".+/[^.]+.(.+)";

    public FileManager(String s3Bucket, String s3Path, LambdaLogger logger) throws Exception{
        this.s3Path = s3Path;
        this.s3Bucket = s3Bucket;
        this.s3Directory = FileManager.parseFromRegex(this.s3Path, PARSE_FILE_DIR_FROM_PATH, logger);
        this.s3FileName = FileManager.parseFromRegex(this.s3Path, PARSE_FILE_NAME_FROM_PATH, logger);
        this.fileExtension = FileManager.parseFromRegex(this.s3Path, PARSE_FILE_EXT_FROM_PATH, logger);
        this.localFilePath = FileManager.buildOutputPath(this.fileExtension);
    }

    private static String parseFromRegex(String inputStr, String regex, LambdaLogger logger) throws Exception{
        String group;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.find()){
            group = matcher.group(1);
        } else{
            throw new Exception(String.format("Unable to parse regex %s from input %s", regex, inputStr));
        }
        logger.log("Parsed the following group " + group + " from string " + inputStr + " with regex " + regex + "\n");
        return group;
    }

    public static String buildOutputPath(String extension){
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        String outputPath = String.format("/tmp/%s.%s", uuidAsString, extension);
        return outputPath;
    }

    public String getS3Path() {
        return s3Path;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Directory() {
        return s3Directory;
    }

    public String getS3FileName() {
        return s3FileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }
}
