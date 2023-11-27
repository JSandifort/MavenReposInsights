package io.sandifort.kafka.utils;

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
import java.util.List;

public class CsvWriterUtils {

    public final String csvSeparator = ",";
    public final String outputDirectory;
    public final String outputPomsFilePath;
    public final String outputRepositoriesFilePath;
    public final String outputDependenciesFilePath;

    public CsvWriterUtils (String outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.outputPomsFilePath = outputDirectory + "poms.csv";
        this.outputDependenciesFilePath = outputDirectory + "dependencies.csv";
        this.outputRepositoriesFilePath = outputDirectory + "repositories.csv";
    }
    public void writeToCsv(Model model, InvocationResult result) {
        Path filePath = Paths.get(outputPomsFilePath);
        if (Files.notExists(filePath)) {
            writeModelCsvFirstLine();
        }

        List<String> values = getValuesAsStrings(model, result);

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.APPEND);
            writeRepositoriesToCsv(model.getId(), model.getRepositories());
            writeDependenciesToCsv(model.getId(), model.getDependencies());
        } catch (IOException e) {
            //throw new RuntimeException(e);
            //skip
        }
    }

    public void writeRepositoriesToCsv(String pomId, List<Repository> repositories) {
        if(repositories.isEmpty()) return;
        Path filePath = Paths.get(outputRepositoriesFilePath);
        if (Files.notExists(filePath)) {
            writeRepositoriesCsvFirstLine();
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

    public void writeDependenciesToCsv(String pomId, List<Dependency> dependencies) {
        if(dependencies.isEmpty()) return;
        Path filePath = Paths.get(outputDependenciesFilePath);
        if (Files.notExists(filePath)) {
            writeDependenciesCsvFirstLine();
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

    private static List<String> getValuesAsStrings(Model model, InvocationResult result) {
        List<String> values = new ArrayList<>();
        values.add(model.getId()); // For referencing repositories
        values.add(model.getModelVersion());
        values.add(model.getParent() != null ? model.getParent().getId() : "");
        values.add(model.getGroupId());
        values.add(model.getArtifactId());
        values.add(model.getVersion());
        values.add(model.getName());
        values.add(model.getUrl());
        values.add(model.getInceptionYear());
        values.add(model.getOrganization() != null ? model.getOrganization().getName() : "");
        values.add(String.valueOf(model.getDependencies().size()));
        values.add(String.valueOf(model.getRepositories().size()));
        values.add(String.valueOf(result.getExitCode())); // exitcode 0 means success (https://maven.apache.org/shared/maven-invoker/apidocs/org/apache/maven/shared/invoker/InvocationResult.html) // A non-zero value indicates a build failure. Note: This value is undefined if getExecutionException() reports an exception.
        if(result.getExecutionException() != null){
            values.add(String.valueOf(result.getExecutionException().getMessage()));
        } else {
            values.add(String.valueOf(result.getExecutionException()));
        }
        return values;
    }

    public static List<String> getValuesAsStrings(String pomId, Repository repository) {
        List<String> values = new ArrayList<>();
        values.add(repository.getId());
        values.add(pomId);
        values.add(repository.getName());
        values.add(repository.getUrl());
        return values;
    }

    public static List<String> getValuesAsStrings(String pomId, Dependency dependency) {
        List<String> values = new ArrayList<>();
        values.add(pomId);
        values.add(dependency.getGroupId());
        values.add(dependency.getArtifactId());
        values.add(dependency.getScope());
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
        values.add("inceptionYear");
        values.add("organization");
        values.add("dependenciesAmount");
        values.add("repositoriesAmount");
        values.add("dependenciesResolvingExitCode");
        values.add("dependenciesResolvingExecutionException");

        try {
            Files.write(filePath, buildCsvText(values).getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeRepositoriesCsvFirstLine(){
        Path filePath = Paths.get(outputRepositoriesFilePath);
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

    public void writeDependenciesCsvFirstLine(){
        Path filePath = Paths.get(outputDependenciesFilePath);
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
