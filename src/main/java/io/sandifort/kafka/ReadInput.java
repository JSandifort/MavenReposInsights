package io.sandifort.kafka;

import dev.c0ps.franz.Kafka;
import dev.c0ps.maveneasyindex.Artifact;
import io.sandifort.kafka.data.Coordinate;
import io.sandifort.kafka.handlers.MavenErrorHandler;
import io.sandifort.kafka.utils.CsvWriterUtils;
import io.sandifort.kafka.utils.MavenRepositoryUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadInput {
    private final Kafka kafka;
    private final AtomicBoolean isRunning;
    private final String mavenRepoDirectory;
    private final String mavenCmdPath;
    private final String mavenHomePath;
    private final String outputDirectory;
    public static final String INPUT_TOPIC = "maven-explorer.downloaded";
    public static final String SEPERATE_LOCAL_REPOSITORY = "local";

    public ReadInput(Kafka kafka, AtomicBoolean isRunning, String mavenRepoDirectory, String mavenCmdPath, String mavenHomePath, String outputDirectory) {
        this.kafka = kafka;
        this.isRunning = isRunning;
        this.mavenRepoDirectory = mavenRepoDirectory;
        this.mavenCmdPath = mavenCmdPath;
        this.mavenHomePath = mavenHomePath;
        this.outputDirectory = outputDirectory;
    }

    public void run() {
        var csvUtils = new CsvWriterUtils(outputDirectory);

        kafka.subscribe(ReadInput.INPUT_TOPIC, Coordinate.class, (in, l) -> {

            // Try to find POM file based on coordinates
            var artifact = new Artifact(in.groupId, in.artifactId, in.version, "pom");
            var utils = new MavenRepositoryUtils(new File(mavenRepoDirectory));
            var pomFile = utils.getLocalPomFile(artifact);
            //TODO: get pom file

            // Before we invoke we must delete local repository to avoid caching
            try {
                if(new File(SEPERATE_LOCAL_REPOSITORY).exists()){
                    FileUtils.deleteDirectory(new File(SEPERATE_LOCAL_REPOSITORY));
                }

                //create new local repository
                FileUtils.forceMkdir(new File(SEPERATE_LOCAL_REPOSITORY));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                // skip, directory does not exist
            }

            // Try to resolve dependencies
            var mavenErrorHandler = new MavenErrorHandler();
            InvocationRequest request = getCheckDependenciesInvocationRequest(pomFile, mavenErrorHandler);

            Invoker invoker = getMavenInvoker();
            InvocationResult result = null;
            try {
                result = invoker.execute(request);
            } catch (MavenInvocationException e) {
//                throw new RuntimeException(e);
            }

            // Extract information from POM file
            MavenXpp3Reader reader = new MavenXpp3Reader();
            try {
                Model model = reader.read(new FileReader(pomFile));
                //TODO: put model in database/csv file
                csvUtils.writeToCsv(model, result, mavenErrorHandler.getErrorOutputWithoutNewLines());

            } catch (XmlPullParserException e) {
//                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
            } catch (IOException e) {
//                throw new RuntimeException(e);
            }

            System.out.printf("Message via TRef: %s\n", in);
        });

        // consume incoming messages until canceled
        while (isRunning.get()) {
            kafka.poll();
        }
    }

    private static InvocationRequest getCheckDependenciesInvocationRequest(File pomFile, MavenErrorHandler mavenErrorHandler) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Arrays.asList("dependency:tree")); //TODO: determine and set right maven command to resolve dependencies
        // Set custom settings.xml file to avoid having credentials in the default settings.xml file
        request.setUserSettingsFile(new File("custom_settings.xml"));
        // Set local repository to a separate directory to avoid caching
        request.setLocalRepositoryDirectory(new File(SEPERATE_LOCAL_REPOSITORY));
        request.setErrorHandler(mavenErrorHandler);
        request.setOutputHandler(mavenErrorHandler);
        return request;
    }

    private Invoker getMavenInvoker() {
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenExecutable(new File(mavenCmdPath));
        invoker.setMavenHome(new File(mavenHomePath));
        invoker.setLocalRepositoryDirectory(new File(mavenRepoDirectory, "repository"));
        return invoker;
    }
}
