package com.komorebi.video;

public class EnvironmentVariables {

    public static EnvironmentVariables instance = null;

    public final String AC_AUDIO_BUCKET;
    public final String VIDEO_INPUT_BUCKET;
    public final String VIDEO_OUTPUT_BUCKET;

    public EnvironmentVariables(){
        AC_AUDIO_BUCKET = System.getenv("AC_AUDIO_BUCKET_NAME");
        VIDEO_INPUT_BUCKET = System.getenv("INPUT_BUCKET_NAME");
        VIDEO_OUTPUT_BUCKET = System.getenv("OUTPUT_BUCKET_NAME");
    }

    public static EnvironmentVariables getInstance(){
        if (instance == null){
            instance = new EnvironmentVariables();
        }
        return instance;
    }
}
