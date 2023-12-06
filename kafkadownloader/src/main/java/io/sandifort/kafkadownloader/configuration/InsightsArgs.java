package io.sandifort.kafkadownloader.configuration;

import com.beust.jcommander.Parameter;

public class InsightsArgs {

    @Parameter(names = "--mavenRepoDirectory", arity = 1, description = "absolute path to the maven input repository ")
    public String mavenRepoDirectory;
    @Parameter(names = "--mavenCmdPath", arity = 1, description = "absolute path to the mvn cmd")
    public String mavenCmdPath;

    @Parameter(names = "--mavenHomePath", arity = 1, description = "absolute path to the maven home")
    public String mavenHomePath;

    @Parameter(names = "--outputDirectory", arity = 1, description = "absolute path to the output directory")
    public String outputDirectory;


}
