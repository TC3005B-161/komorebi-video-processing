package com.komorebi.video.videoUtils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class VideoProcessor {

    public static void extractVideo(String inputPath, String videoPath, LambdaLogger logger) throws Exception {
        String command = String.format(
            "ffmpeg -i %s -an -vcodec copy %s",
            inputPath,
            videoPath);
        Shell.execute(command, logger);
    }

    public static void extractAudio(String inputPath, String audioPath, LambdaLogger logger) throws Exception{
        String command = String.format(
            "ffmpeg -i %s -vn -acodec copy %s",
            inputPath,
            audioPath);
        Shell.execute(command, logger);
    }

    public static void splitAudio(String inputPath, String secondHalfPath, String tms, LambdaLogger logger) throws Exception{
        String command = String.format(
            "./ffmpeg -i %s -ss %s -q:a 0 -map a %s",
            inputPath,
            tms,
            secondHalfPath);
        Shell.execute(command, logger);
    }

    public static void concatAudios(String firstAudio, String secondAudio, String outputPath, LambdaLogger logger) throws Exception {
        String command = String.format(
            "echo -e 'file %s\nfile %s' > /tmp/input.txt && " +
            "./ffmpeg -f concat -i /tmp/input.txt -c copy %s && " +
            "rm /tmp/input.txt",
            firstAudio,
            secondAudio,
            outputPath
        );
        Shell.execute(command, logger);
    }

    public static void muxVideo(String videoPath, String audioPath, String outputPath, LambdaLogger logger) throws Exception {
        String command = String.format(
                "ffmpeg -i %s -i %s -acodec copy -vcodec copy %s",
                audioPath,
                videoPath,
                outputPath);
        Shell.execute(command, logger);
    }

    public static void extractThumbnail(String videoPath, String outputPath, LambdaLogger logger) throws Exception {
        String command = String.format(
            "ffmpeg -i %s -ss 00:00:01.000 -vframes 1 %s",
            videoPath,
            outputPath
        );
        Shell.execute(command, logger);
    }

    // TODO Cleanup files
}
