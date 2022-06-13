package com.komorebi.video.videoUtils;

import java.io.File;import java.io.FileWriter;import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.LambdaLogger;import com.komorebi.video.EnvironmentVariables;

public class VideoProcessor {

    public final static String PROGRAM = "ffmpeg";

    public static void extractVideo(String inputPath, String videoPath, LambdaLogger logger) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(PROGRAM);
        command.add("-i");
        command.add(inputPath);
        command.add("-an");
        command.add("-vcodec");
        command.add("copy");
        command.add(videoPath);
        Shell.execute(command, logger);
    }

    public static void extractAudio(String inputPath, String audioPath, LambdaLogger logger) throws Exception{
        List<String> command = new ArrayList<>();
        command.add(PROGRAM);
        command.add("-i");
        command.add(inputPath);
        command.add("-vn");
        command.add("-acodec");
        command.add("pcm_s16le");
        command.add("-ar");
        command.add(EnvironmentVariables.getInstance().AUDIO_FREQUENCY);
        command.add(audioPath);
        Shell.execute(command, logger);
    }

    public static void splitAudio(String inputPath, String secondHalfPath, String tms, LambdaLogger logger) throws Exception{
        List<String> command = new ArrayList<>();
        command.add(PROGRAM);
        command.add("-i");
        command.add(inputPath);
        command.add("-ss");
        command.add(tms);
        command.add("-q:a");
        command.add("0");
        command.add("-map");
        command.add("a");
        command.add(secondHalfPath);
        Shell.execute(command, logger);
    }

    public static void concatAudios(String firstAudio, String secondAudio, String outputPath, LambdaLogger logger) throws Exception {
        String tmpFile = "/tmp/input.txt";
        FileWriter writer = new FileWriter(tmpFile);
        writer.append(String.format("file %s\n", firstAudio));
        writer.append(String.format("file %s\n", secondAudio));
        writer.flush();
        writer.close();

        List<String> concatAudiosCommand = new ArrayList<>();
        concatAudiosCommand.add(PROGRAM);
        concatAudiosCommand.add("-f");
        concatAudiosCommand.add("concat");
        concatAudiosCommand.add("-safe");
        concatAudiosCommand.add("0");
        concatAudiosCommand.add("-i");
        concatAudiosCommand.add(tmpFile);
        concatAudiosCommand.add("-c");
        concatAudiosCommand.add("copy");
        concatAudiosCommand.add(outputPath);
        Shell.execute(concatAudiosCommand, logger);

        File file = new File(tmpFile);
        file.delete();
    }

    public static void muxVideo(String videoPath, String audioPath, String outputPath, LambdaLogger logger) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(PROGRAM);
        command.add("-i");
        command.add(audioPath);
        command.add("-i");
        command.add(videoPath);
        command.add("-acodec");
        command.add("aac");
        command.add("-vcodec");
        command.add("copy");
        command.add(outputPath);
        Shell.execute(command, logger);
    }

    public static void extractThumbnail(String videoPath, String outputPath, LambdaLogger logger) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(PROGRAM);
        command.add("-i");
        command.add(videoPath);
        command.add("-ss");
        command.add("00:00:01.000");
        command.add("-vframes");
        command.add("1");
        command.add(outputPath);
        Shell.execute(command, logger);
    }

    public static void cleanupFiles(String... files){
        for (String filePath: files){
            File file = new File(filePath);
            file.delete();
        }
    }
}
