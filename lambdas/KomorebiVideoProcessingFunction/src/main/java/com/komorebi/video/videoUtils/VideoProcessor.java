package com.komorebi.video.videoUtils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.UUID;

public class VideoProcessor {

    private final String audioPath;
    private final String videoPath;

    public VideoProcessor(String audioPath, String videoPath){
        this.audioPath = audioPath;
        this.videoPath = videoPath;
    }

    public String processVideo(LambdaLogger logger) throws Exception {
        String muxedVideoPath = this.buildOutputPath("mp4");
        String command = this.buildMuxCommand(muxedVideoPath);
        Shell.execute(command, logger);
        return muxedVideoPath;
    }

    private String buildMuxCommand(String outputPath){
        String command = String.format(
            "ffmpeg -i %s -i %s -acodec copy -vcodec copy %s",
            this.audioPath,
            this.videoPath,
            outputPath
        );
        return command;
    }

    private String buildOutputPath(String extension){
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        String outputPath = String.format("/tmp/%s.%s", uuidAsString, extension);
        return outputPath;
    }
}
