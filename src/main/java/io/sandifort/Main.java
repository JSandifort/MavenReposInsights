package io.sandifort;


import dev.c0ps.franz.Kafka;
import io.sandifort.kafka.ReadInput;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.sandifort.kafka.utils.KafkaUtils.getKafkaInstance;

public class Main {
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);
    private static final Kafka KAFKA = getKafkaInstance();


    public static void main(String[] args) {

        Properties properties = new Properties();
        File initialFile = new File("app.properties");
        InputStream targetStream = null;

        try {
            targetStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            properties.load(targetStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String mavenRepoDirectory =  properties.getProperty("mavenRepoDirectory");
        String mavenCmdPath = properties.getProperty("mavenCmdPath");
        String mavenHomePath = properties.getProperty("mavenHomePath");
        String outputDirectory = properties.getProperty("outputDirectory");

        new ReadInput(KAFKA, IS_RUNNING, mavenRepoDirectory, mavenCmdPath, mavenHomePath, outputDirectory).run();

        EXEC.shutdown();
        KAFKA.stop();


    }
}