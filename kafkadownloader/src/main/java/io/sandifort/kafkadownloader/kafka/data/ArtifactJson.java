/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sandifort.kafkadownloader.kafka.data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.c0ps.maveneasyindex.Artifact;

import java.io.IOException;

public class ArtifactJson {


    private static final long serialVersionUID = -1L;
    private static final String CENTRAL = "https://repo.maven.apache.org/maven2/";

    private static boolean isNullOrCentral(Artifact a) {
        return a.repository == null || CENTRAL.equals(a.repository);
    }

    public static class ArtifactDeserializer extends JsonDeserializer<Artifact> {

        @Override
        public Artifact deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JacksonException {
            var a = new Artifact();
            var json = p.getValueAsString();

            var idx = json.indexOf('@');
            var coord = idx == -1 ? json : json.substring(0, idx);

            a.repository = idx == -1 //
                    ? CENTRAL
                    : dec(json.substring(idx + 1));

            var parts = coord.split(":");
            if (parts.length != 5) {
                throw new RuntimeException("Cannot parse artifact: " + json);
            }

            a.groupId = dec(parts[0]);
            a.artifactId = dec(parts[1]);
            a.version = dec(parts[2]);
            a.packaging = dec(parts[3]);
            try {
                a.releaseDate = Long.parseLong(parts[4]);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot parse artifact: " + json, e);
            }
            return a;
        }
    }

    public static class ArtifacSerializer extends JsonSerializer<Artifact> {

        @Override
        public void serialize(Artifact a, JsonGenerator gen, SerializerProvider provider) throws IOException {
            var sb = new StringBuilder() //
                    .append(enc(a.groupId)).append(":")//
                    .append(enc(a.artifactId)).append(":")//
                    .append(enc(a.version)).append(":") //
                    .append(enc(a.packaging)).append(":") //
                    .append(a.releaseDate);

            if (!isNullOrCentral(a)) {
                sb.append('@').append(enc(a.repository));
            }

            var s = sb.toString();
            gen.writeString(s);
        }
    }

    private static String enc(String s) {
        return s.replace("@", "%40").replace(":", "%3A");
    }

    private static String dec(String s) {
        return s.replace("%40", "@").replace("%3A", ":");
    }

}