package io.sandifort.kafkadownloader.kafka.utils;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.c0ps.maveneasyindex.Artifact;
import jdk.jfr.StackTrace;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.shared.invoker.InvocationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsvWriterUtils {

    public final String csvSeparator = ",";
    public final String outputDirectory;
    public final String outputPomsFilePath;
    public final String outputErrorPomsFilePath;
    public final String outputRepositoriesFilePath;
    public final String outputErrorRepositoriesFilePath;
    public final String outputDependenciesFilePath;
    public final String outputErrorDependenciesFilePath;

    @Inject
    public CsvWriterUtils (@Named("outputDirectory") String outputDirectory) {
        this.outputDirectory = outputDirectory.replace("\"", "");
        this.outputPomsFilePath = outputDirectory.replace("\"", "") + "/poms.csv";
        this.outputDependenciesFilePath = outputDirectory.replace("\"", "") + "/dependencies.csv";
        this.outputRepositoriesFilePath = outputDirectory.replace("\"", "") + "/repositories.csv";
        this.outputErrorPomsFilePath = outputDirectory.replace("\"", "") + "/errorPoms.csv";
        this.outputErrorRepositoriesFilePath = outputDirectory.replace("\"", "") + "/errorRepositories.csv";
        this.outputErrorDependenciesFilePath = outputDirectory.replace("\"", "") + "/errorDependencies.csv";
    }

    public void writeToErrorCsv(Model model, Artifact artifact, String mavenErrorMessage) {
        Path filePath = Paths.get(outputErrorPomsFilePath);
        if (Files.notExists(filePath)) {
            writeErrorModelCsvFirstLine();
        }
        System.out.println("csv ERROR Writing to " + outputDirectory);

        List<String> values = getValuesAsStrings(model, artifact, mavenErrorMessage);

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.APPEND);
            writeRepositoriesToCsv(model.getId(), model.getRepositories(), outputErrorRepositoriesFilePath);
            writeDependenciesToCsv(model.getId(), model.getDependencies(), outputErrorDependenciesFilePath);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            //skip
        }
    }

    public void writeToCsv(Model model, Artifact artifact) {
        System.out.println("csv Writing to " + outputDirectory);

        Path filePath = Paths.get(outputPomsFilePath);
        if (Files.notExists(filePath)) {
            writeModelCsvFirstLine();
        }

        List<String> values = getValuesAsStrings(model, artifact);

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.APPEND);
            writeRepositoriesToCsv(model.getId(), model.getRepositories(), outputRepositoriesFilePath);
            writeDependenciesToCsv(model.getId(), model.getDependencies(), outputDependenciesFilePath);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            //skip
        }
    }

    public void writeRepositoriesToCsv(String pomId, List<Repository> repositories, String outputDirectory) {
        if(repositories.isEmpty()) return;
        Path filePath = Paths.get(outputDirectory);
        if (Files.notExists(filePath)) {
            writeRepositoriesCsvFirstLine(outputDirectory);
        }

        for (Repository repository : repositories) {
            List<String> values = new ArrayList<>();
            values.addAll(getValuesAsStrings(pomId, repository));
            try {
                Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
//            throw new RuntimeException(e);
                //skip
            }
        }


    }

    public void writeDependenciesToCsv(String pomId, List<Dependency> dependencies, String outputDirectory) {
        if(dependencies.isEmpty()) return;
        Path filePath = Paths.get(outputDirectory);
        if (Files.notExists(filePath)) {
            writeDependenciesCsvFirstLine(outputDirectory);
        }

        for (Dependency dependency : dependencies) {
            List<String> values = new ArrayList<>();
            values.addAll(getValuesAsStrings(pomId, dependency));
            try {
                Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
//            throw new RuntimeException(e);
                //skip
            }
        }
    }

    private static String cleanString(String string){
        if (string == null) return "";
        return string.replace(",", ";");
    }

    private static List<String> getValuesAsStrings(Model model, Artifact artifact, String mavenErrorMessage) {
        var releaseDate = new Date(artifact.releaseDate);
        var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        // give a timezone reference for formatting (see comment at the bottom)
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        List<String> values = new ArrayList<>();
        values.add(cleanString(model.getId())); // For referencing repositories
        values.add(cleanString(model.getModelVersion()));
        values.add(cleanString(model.getParent() != null ? model.getParent().getId() : ""));
        values.add(cleanString(model.getGroupId()));
        values.add(cleanString(model.getArtifactId()));
        values.add(cleanString(model.getVersion()));
        values.add(cleanString(model.getName()));
        values.add(cleanString(model.getUrl()));
        values.add(cleanString(sdf.format(releaseDate))); // convert from unix epoch
        values.add(cleanString(model.getOrganization() != null ? model.getOrganization().getName() : ""));
        values.add(cleanString(String.valueOf(model.getDependencies().size())));
        values.add(cleanString(String.valueOf(model.getRepositories().size())));
        values.add(cleanString(mavenErrorMessage));

        return values;
    }

    private static List<String> getValuesAsStrings(Model model, Artifact artifact) {
        var releaseDate = new Date(artifact.releaseDate);
        var sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        // give a timezone reference for formatting (see comment at the bottom)
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        List<String> values = new ArrayList<>();
        values.add(cleanString(model.getId())); // For referencing repositories
        values.add(cleanString(model.getModelVersion()));
        values.add(cleanString(model.getParent() != null ? model.getParent().getId() : ""));
        values.add(cleanString(model.getGroupId()));
        values.add(cleanString(model.getArtifactId()));
        values.add(cleanString(model.getVersion()));
        values.add(cleanString(model.getName()));
        values.add(cleanString(model.getUrl()));
        values.add(cleanString(sdf.format(releaseDate))); // convert from unix epoch
        values.add(cleanString(model.getOrganization() != null ? model.getOrganization().getName() : ""));
        values.add(cleanString(String.valueOf(model.getDependencies().size())));
        values.add(cleanString(String.valueOf(model.getRepositories().size())));
        return values;
    }

    public static List<String> getValuesAsStrings(String pomId, Repository repository) {
        List<String> values = new ArrayList<>();
        values.add(cleanString(repository.getId()));
        values.add(cleanString(pomId));
        values.add(cleanString(repository.getName()));
        values.add(cleanString(repository.getUrl()));
        return values;
    }

    public static List<String> getValuesAsStrings(String pomId, Dependency dependency) {
        List<String> values = new ArrayList<>();
        values.add(cleanString(pomId));
        values.add(cleanString(dependency.getGroupId()));
        values.add(cleanString(dependency.getArtifactId()));
        values.add(cleanString(dependency.getScope()));
        return values;
    }

    public String buildCsvText(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : list) {
            stringBuilder.append(string);
            stringBuilder.append(csvSeparator);
        }
        stringBuilder.append(System.lineSeparator());
        return stringBuilder.toString();
    }

    public void writeErrorModelCsvFirstLine(){
        Path filePath = Paths.get(outputErrorPomsFilePath);
        if (Files.notExists(filePath)) {
            try {
                filePath.toFile().createNewFile();
            } catch (IOException e) {

                throw new RuntimeException(e);

            }
        }

        List<String> values = new ArrayList<>();
        values.add("id");
        values.add("modelVersion");
        values.add("parent");
        values.add("groupId");
        values.add("artifactId");
        values.add("version");
        values.add("name");
        values.add("url");
        values.add("releaseDate");
        values.add("organization");
        values.add("dependenciesAmount");
        values.add("repositoriesAmount");
        values.add("mavenErrorMessage");

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeModelCsvFirstLine(){
        Path filePath = Paths.get(outputPomsFilePath);
        if (Files.notExists(filePath)) {
            try {
                filePath.toFile().createNewFile();
            } catch (IOException e) {

                throw new RuntimeException(e);

            }
        }

        List<String> values = new ArrayList<>();
        values.add("id");
        values.add("modelVersion");
        values.add("parent");
        values.add("groupId");
        values.add("artifactId");
        values.add("version");
        values.add("name");
        values.add("url");
        values.add("releaseDate");
        values.add("organization");
        values.add("dependenciesAmount");
        values.add("repositoriesAmount");

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeRepositoriesCsvFirstLine(String outputDirectory){
        Path filePath = Paths.get(outputDirectory);
        if (Files.notExists(filePath)) {
            try {
                filePath.toFile().createNewFile();
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }

        List<String> values = new ArrayList<>();
        values.add("id");
        values.add("pomId");
        values.add("name");
        values.add("url");

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeDependenciesCsvFirstLine(String outputDirectory){
        Path filePath = Paths.get(outputDirectory);
        if (Files.notExists(filePath)) {
            try {
                filePath.toFile().createNewFile();
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }

        List<String> values = new ArrayList<>();
        values.add("pomId");
        values.add("groupId");
        values.add("artifactId");
        values.add("scope");

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
