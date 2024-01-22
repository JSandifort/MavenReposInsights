# MavenReposInsights

MavenReposInsights is a Java project focused on finding the impact of Maven Central's external repository usage. 

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Java JDK (version 8 or later)
- Maven (for building the project)
- Docker (optional, for running the project in a container)

### Installing and Running

#### Cloning the Repository

1. Clone the repository to your local machine:
   ```
   git clone git@github.com:JSandifort/MavenReposInsights.git
   ```

#### Building the Project

1. Navigate to the root directory of the project:
   ```
   cd MavenReposInsights
   ```

2. Clean and package the project using Maven:
   ```
   mvn clean package
   ```

#### Running the Project

1. To run the project directly using Java, navigate to the runner folder:
   ```
   cd runner
   ```
2. Run the project using the following command:
   ```
   java -jar target/runner1.0-jar-with-dependencies.jar [parameters]
   ```
   Replace `[parameters]` with the necessary command-line arguments.

#### Using Docker (Recommended)

1. Build the Docker image from the runner directory based on the Dockerfile:
   ```
   docker build -t mavenreposinsights .
   ```

2. Run the Docker image, or modify the `docker-compose.yml` file to run the image:
   ```
   docker run mavenreposinsights [parameters]
   ```
    Replace `[parameters]` with the necessary command-line arguments. Make sure to volume mount the maven repository directory and the output directory to the container. See the `docker-compose.yml` file for an example.


3. To run the project using Docker Compose, use the following command:
   ```
   docker-compose up
   ```
   

### Parameters

In order to run the project, you need to specify at least the following parameters:
- `--run` : Specifies the main class to run
- `--logLevel` : Specifies the log level to use e.g. DEBUG, INFO, WARN, ERROR
- `--mavenRepoDirectory` : Absolute path to the maven input repository
- `--mavenCmdPath` : Absolute path to the mvn cmd
- `--mavenHomePath` : Absolute path to the maven home
- `--outputDirectory` : Absolute path to the output directory
- `--shouldSubscribeErrors`: Indicates whether to subscribe to error lane (true) or to subscribe to the normal lane (false)

Example:
```
docker run mavenreposinsights --run=io.sandifort.kafkadownloader.Main --logLevel=DEBUG --mavenRepoDirectory=/inside/m2 --mavenCmdPath=/usr/share/maven/bin/mvn --mavenHomePath=/usr/share/maven --outputDirectory=/output --shouldSubscribeErrors=false
```

## License

This project is licensed under the Apache License - see the LICENSE file for details.
