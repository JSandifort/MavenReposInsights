package io.sandifort.kafkadownloader.kafka.handlers;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import java.io.IOException;

public class MavenErrorHandler implements InvocationOutputHandler {

    private final StringBuilder errorOutput = new StringBuilder();
    private boolean firstLine = true;

    public MavenErrorHandler() {
    }


    @Override
    public void consumeLine(String line) throws IOException {
        //filter only error and fatal lines

        if (line.contains("[ERROR]") || line.contains("[FATAL]")) {
            if (line.startsWith("To see") || line.startsWith("Re-run") || line.startsWith("For more")) {
                firstLine = false;
            }
            if (firstLine) {
                var cleanedLine = line.replace(",", ";");
                cleanedLine = line.replace(",", ";");
                errorOutput.append(cleanedLine).append("\n");
            }

        }
        System.out.println(line);
    }

    public String getErrorOutputWithoutNewLines() {
        return errorOutput.toString().replace("\n", " | ");
    }
}
