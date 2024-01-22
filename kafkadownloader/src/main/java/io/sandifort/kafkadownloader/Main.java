package io.sandifort.kafkadownloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import dev.c0ps.franz.Kafka;
import dev.c0ps.maveneasyindex.ArtifactModule;
import io.sandifort.kafkadownloader.configuration.SimpleErrorModule;
import io.sandifort.kafkadownloader.kafka.ReadInput;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main implements Runnable {

    private final Kafka kafka;
    private final AtomicBoolean isRunning;
    private final ExecutorService exec;
    private final ReadInput readInput;

    @Inject
    public Main(Kafka kafka, AtomicBoolean isRunning, ExecutorService exec, ReadInput readInput) {
        this.kafka = kafka;
        this.isRunning = isRunning;
        this.exec = exec;
        this.readInput = readInput;
    }

    @Override
    public void run() {
        var list = new HashSet<SimpleModule>();
        list.add(new ArtifactModule());
        list.add(new SimpleErrorModule());

        var om = new ObjectMapper().registerModules(list);
        var injector = Guice.createInjector();
        injector.injectMembers(om);

        readInput.run();

        exec.shutdown();
        kafka.stop();
    }
}