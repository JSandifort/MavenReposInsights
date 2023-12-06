FROM maven:3.9.5-eclipse-temurin-11-alpine

# Set environment variables
ENV MAVEN_REPO_DIRECTORY /path/to/maven/repo
ENV MAVEN_CMD_PATH /usr/share/maven/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV OUTPUT_DIRECTORY /path/to/output

# Create the output directory
RUN mkdir -p $OUTPUT_DIRECTORY

# Define the volume
VOLUME $OUTPUT_DIRECTORY

WORKDIR /usr/local/runme

COPY ../target/runner-1.0.jar server.jar

ENTRYPOINT exec java $JAVA_OPTS -jar server.jar $0 $@
