package io.sandifort.kafkadownloader.kafka.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.c0ps.franz.KafkaConnector;
import dev.c0ps.franz.KafkaImpl;
import dev.c0ps.io.JsonUtils;
import dev.c0ps.io.JsonUtilsImpl;
import dev.c0ps.maveneasyindex.Artifact;
import dev.c0ps.mx.infra.kafka.SimpleErrorMessage;
import io.sandifort.kafkadownloader.kafka.data.*;
import io.sandifort.kafkadownloader.kafka.data.ArtifactJson;
import io.sandifort.kafkadownloader.kafka.data.SimpleErrorMessageJson;

public class KafkaUtils {

    // configuration is assuming that the server auto-creates unknown topics
    private static final String KAFKA_URL = "http://api.sandifort.io:19094";
    private static final String KAFKA_GROUP_ID = "maven-explorer";

    public static KafkaImpl getKafkaInstance() {
        return new KafkaImpl(initJsonUtils(), initConnector(), true);
    }

    private static KafkaConnector initConnector() {
        return new KafkaConnector(KAFKA_URL, KAFKA_GROUP_ID);
    }

    private static JsonUtils initJsonUtils() {
        var om = initObjectMapper();
        return new JsonUtilsImpl(om);
    }

    private static ObjectMapper initObjectMapper() {
        // register a (de-)serializer for the data structure of the example
        var m = new SimpleModule();
        m.addSerializer(SimpleErrorMessage.class, new SimpleErrorMessageJson.SimpleErrorMessageSerializer());
        m.addDeserializer(SimpleErrorMessage.class, new SimpleErrorMessageJson.SimpleErrorMessageDeserializer() );

        m.addSerializer(Artifact.class, new ArtifactJson.ArtifacSerializer());
        m.addDeserializer(Artifact.class, new ArtifactJson.ArtifactDeserializer() );

        // instead of instantiation, consider using dev.c0ps.io.ObjectMapperBuilder
        return new ObjectMapper().registerModule(m);
    }
}