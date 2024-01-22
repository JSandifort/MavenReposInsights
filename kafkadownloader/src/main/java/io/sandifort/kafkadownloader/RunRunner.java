package io.sandifort.kafkadownloader;

import dev.c0ps.diapper.Runner;
import dev.c0ps.diapper.VmArgs;

public class RunRunner {

    public static void main(String[] args) {
        VmArgs.log(args);
        var runner = new Runner("io.sandifort.kafkadownloader.configuration", "...");
        runner.run(args);
    }
}
