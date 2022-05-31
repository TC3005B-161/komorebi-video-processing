package com.komorebi.video;

public class ProcessVideoInput {

    private String audioS3URI;
    private String videoS3URI;

    public String getAudioS3URI() {
        return audioS3URI;
    }

    public void setAudioS3URI(String audioS3URI) {
        this.audioS3URI = audioS3URI;
    }

    public String getVideoS3URI() {
        return videoS3URI;
    }

    public void setVideoS3URI(String videoS3URI) {
        this.videoS3URI = videoS3URI;
    }
}
