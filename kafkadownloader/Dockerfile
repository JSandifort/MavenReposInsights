FROM maven:3.9.5-eclipse-temurin-21-alpine

# Set environment variables
ENV MAVEN_REPO_DIRECTORY /inside/m2
ENV MAVEN_CMD_PATH /usr/share/maven/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV OUTPUT_DIRECTORY /output

WORKDIR /usr/local/runme

COPY ../target/kafkadownloader-1.0-jar-with-dependencies.jar  server.jar

ENTRYPOINT exec java $JAVA_OPTS -jar server.jar $0 $@
