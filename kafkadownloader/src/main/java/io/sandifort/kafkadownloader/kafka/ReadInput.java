package io.sandifort.kafkadownloader.kafka;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.c0ps.franz.Kafka;
import dev.c0ps.franz.KafkaErrors;
import dev.c0ps.maveneasyindex.Artifact;
import dev.c0ps.mx.infra.kafka.SimpleErrorMessage;
import io.sandifort.kafkadownloader.kafka.handlers.MavenErrorHandler;
import io.sandifort.kafkadownloader.kafka.utils.CsvWriterUtils;
import io.sandifort.kafkadownloader.kafka.utils.MavenRepositoryUtils;
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
    private final KafkaErrors kafkaErrors;
    private final AtomicBoolean isRunning;
    private final String mavenRepoDirectory;
    private final String mavenCmdPath;
    private final String mavenHomePath;
    private final String outputDirectory;
    private final CsvWriterUtils csvWriterUtils;
    public static final String INPUT_TOPIC = "maven-explorer.downloaded";
    public static final String SEPERATE_LOCAL_REPOSITORY = "local";

    @Inject
    public ReadInput(Kafka kafka, KafkaErrors kafkaErrors, AtomicBoolean isRunning, @Named("MavenRepoDirectory") String mavenRepoDirectory, @Named("mavenCmdPath") String mavenCmdPath, @Named("mavenHomePath") String mavenHomePath, @Named("outputDirectory") String outputDirectory, CsvWriterUtils csvWriterUtils) {
        this.kafka = kafka;
        this.kafkaErrors = kafkaErrors;
        this.isRunning = isRunning;
        this.mavenRepoDirectory = mavenRepoDirectory;
        this.mavenCmdPath = mavenCmdPath;
        this.mavenHomePath = mavenHomePath;
        this.outputDirectory = outputDirectory;
        this.csvWriterUtils = csvWriterUtils;
    }

    public void run() {

////        // Dependencies successfully resolved
//        kafka.subscribe(ReadInput.INPUT_TOPIC, Artifact.class, (artifact, l) -> {
//
//            System.out.println("Writing to " + outputDirectory);
//            // Try to find POM file based on coordinates
//            var utils = new MavenRepositoryUtils(new File(mavenRepoDirectory));
//            var pomFile = utils.getLocalPomFile(artifact);
//
//            // Extract information from POM file
//            MavenXpp3Reader reader = new MavenXpp3Reader();
//            try {
//                Model model = reader.read(new FileReader(pomFile));
//
//                //TODO: put model in database/csv file
//                csvWriterUtils.writeToCsv(model, artifact);
//
//            } catch (XmlPullParserException e) {
////                throw new RuntimeException(e);
//            } catch (FileNotFoundException e) {
////                throw new RuntimeException(e);
//            } catch (IOException e) {
////                throw new RuntimeException(e);
//            }
//
//            System.out.printf("Message via TRef: %s\n", artifact);
//        });

        kafkaErrors.subscribeErrors(ReadInput.INPUT_TOPIC, SimpleErrorMessage.class, this::processError);

        // consume incoming messages until canceled
        while (isRunning.get()) {
//            kafka.poll();
            kafkaErrors.pollAllErrors();
        }
    }

    private void processError(SimpleErrorMessage<Artifact> simpleErrorMessage){
        var artifact = simpleErrorMessage.obj;
        if (artifact == null) {
            return;
        }

        // Try to find POM file based on coordinates
        var utils = new MavenRepositoryUtils(new File(mavenRepoDirectory));
        var pomFile = utils.getLocalPomFile(artifact);
        //TODO: get pom file

        // Try to resolve dependencies again for failed donwdloads
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
            csvWriterUtils.writeToErrorCsv(model, artifact, mavenErrorHandler.getErrorOutputWithoutNewLines());

        } catch (XmlPullParserException e) {
//                throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
        } catch (IOException e) {
//                throw new RuntimeException(e);
        }

        System.out.printf("Message via TRef: %s\n", artifact);
    }

    private static InvocationRequest getCheckDependenciesInvocationRequest(File pomFile, MavenErrorHandler
            mavenErrorHandler) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals(Arrays.asList("dependency:tree")); //TODO: determine and set right maven command to resolve dependencies
        // Set custom settings.xml file to avoid having credentials in the default settings.xml file
        // Set local repository to a separate directory to avoid caching
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
