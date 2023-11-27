package io.sandifort.kafka.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.c0ps.franz.KafkaConnector;
import dev.c0ps.franz.KafkaImpl;
import dev.c0ps.io.JsonUtils;
import dev.c0ps.io.JsonUtilsImpl;
import io.sandifort.kafka.data.Coordinate;
import io.sandifort.kafka.data.CoordinateJson;

public class KafkaUtils {

    // configuration is assuming that the server auto-creates unknown topics
    private static final String KAFKA_URL = "localhost:19092";
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
        m.addSerializer(Coordinate.class, new CoordinateJson.CoordinateDataSerializer());
        m.addDeserializer(Coordinate.class, new CoordinateJson.CoordinateDataDeserializer());

        // instead of instantiation, consider using dev.c0ps.io.ObjectMapperBuilder
        return new ObjectMapper().registerModule(m);
    }
}