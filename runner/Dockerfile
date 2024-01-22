FROM maven:3.9.5-eclipse-temurin-21-alpine

# Set environment variables
ENV MAVEN_REPO_DIRECTORY /path/to/maven/repo
ENV MAVEN_CMD_PATH /usr/share/maven/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV OUTPUT_DIRECTORY /output

# Create the output directory
RUN mkdir -p $OUTPUT_DIRECTORY
RUN mkdir -p /inside/m2/repository

# Define the volume
VOLUME $OUTPUT_DIRECTORY
VOLUME /inside/m2/repository

WORKDIR /usr/local/runme

COPY settings.xml /usr/local/runme/
COPY ../target/runner-1.0-jar-with-dependencies.jar server.jar

ENTRYPOINT exec java $JAVA_OPTS -jar server.jar $0 $@
