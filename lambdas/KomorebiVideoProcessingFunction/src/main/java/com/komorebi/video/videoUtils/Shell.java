package com.komorebi.video.videoUtils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Shell {

    public static void execute(String command, LambdaLogger logger) throws Exception{
        logger.log("About to execute the following command: " + command);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        try {

            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();

            BufferedReader outputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line = outputReader.readLine();
            while (line != null){
                output.append(line);
                line = outputReader.readLine();
            }

            int exitCode = process.waitFor();
            logger.log(output.append("\n").toString());
            if (exitCode != 0){
                throw new Exception("Error while processing the video and the audio");
            }

        } catch(IOException | InterruptedException exc){
            throw new Exception("Unable to process the video and audio");
        }

        logger.log("Successful execution of shell command");
    }
}
