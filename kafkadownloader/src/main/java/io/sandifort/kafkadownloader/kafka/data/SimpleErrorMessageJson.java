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

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.c0ps.maveneasyindex.Artifact;
import dev.c0ps.mx.infra.kafka.SimpleErrorMessage;

import javax.swing.*;
import java.io.IOException;

public class SimpleErrorMessageJson {

    private static final long serialVersionUID = -1L;
    private static final String CENTRAL = "https://repo.maven.apache.org/maven2/";

    private static boolean isNullOrCentral(Artifact a) {
        return a.repository == null || CENTRAL.equals(a.repository);
    }

    public static class SimpleErrorMessageDeserializer extends JsonDeserializer<SimpleErrorMessage<Artifact>> {

        @Override
        public SimpleErrorMessage<Artifact> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

            p.nextToken();
            var objField = p.getValueAsString();
            p.nextToken();
            var objContent = p.getValueAsString();
            p.nextToken();
            var stacktraceField = p.getValueAsString();
            p.nextToken();
            var stacktraceContent = p.getValueAsString();

            // Parse Artifact
            var artifact = new Artifact();
            var idx = objContent.indexOf('@');
            var coord = idx == -1 ? objContent : objContent.substring(0, idx);

            artifact.repository = idx == -1 //
                    ? CENTRAL
                    : dec(objContent.substring(idx + 1));

            var parts = coord.split(":");
            if (parts.length != 5) {
                throw new RuntimeException("Cannot parse artifact: " + objContent);
            }

            artifact.groupId = dec(parts[0]);
            artifact.artifactId = dec(parts[1]);
            artifact.version = dec(parts[2]);
            artifact.packaging = dec(parts[3]);
            try {
                artifact.releaseDate = Long.parseLong(parts[4]);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot parse artifact: " + objContent, e);
            }


            var simpleErrorMessage = new SimpleErrorMessage<Artifact>();
            simpleErrorMessage.obj = artifact;
            simpleErrorMessage.stacktrace = stacktraceContent;

            return simpleErrorMessage;
        }
    }

    public static class SimpleErrorMessageSerializer extends JsonSerializer<SimpleErrorMessage> {

        @Override
        public void serialize(SimpleErrorMessage simpleErrorMessage, JsonGenerator gen, SerializerProvider provider) throws IOException {
            var artifact = simpleErrorMessage.obj instanceof Artifact ? (Artifact) simpleErrorMessage.obj : null;
            if (artifact == null) {
                return;
            }

            var sb = new StringBuilder() //
                    .append(enc(artifact.groupId)).append(":")//
                    .append(enc(artifact.artifactId)).append(":")//
                    .append(enc(artifact.version)).append(":") //
                    .append(enc(artifact.packaging)).append(":") //
                    .append(artifact.releaseDate);

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