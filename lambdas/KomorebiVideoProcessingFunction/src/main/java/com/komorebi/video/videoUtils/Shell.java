package com.komorebi.video.videoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;import java.util.List;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

class Shell {

    public static void execute(List<String> command, LambdaLogger logger) throws Exception{
        logger.log("About to execute the following command: " + command + "\n");

        try {
            ProcessBuilder processBuilder = new ProcessBuilder()
                .redirectErrorStream(true)
                .command(command);

            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();

            BufferedReader outputReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.defaultCharset())
            );

            int exitCode = process.waitFor();

            String line = outputReader.readLine();
            while (line != null){
                output.append(line);
                line = outputReader.readLine();
            }

            if (exitCode != 0){
                logger.log(output.append("\n").toString());
                throw new Exception("Error while processing the video and the audio");
            }

        } catch(IOException | InterruptedException exc){
            logger.log("Error while executing shell command: " + exc.getMessage() + "\n");
            throw new Exception("Unable to process the video and audio", exc);
        }
        logger.log("Successful execution of shell command\n");
    }
}
