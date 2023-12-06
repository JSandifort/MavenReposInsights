package io.sandifort.runner;


import dev.c0ps.diapper.VmArgs;

public class Runner {

    public static void main(String[] args){
        VmArgs.log(args);
        var runner = new dev.c0ps.diapper.Runner("io.sandifort");
        runner.run(args);
    }
}
