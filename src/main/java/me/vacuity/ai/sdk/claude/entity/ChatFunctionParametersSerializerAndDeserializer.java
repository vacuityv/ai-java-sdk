package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;

import java.io.IOException;

public class ChatFunctionParametersSerializerAndDeserializer {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private final static JsonSchemaConfig config = JsonSchemaConfig.vanillaJsonSchemaDraft4();
    private final static JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(MAPPER, config);


    private ChatFunctionParametersSerializerAndDeserializer() {
    }

    public static class Serializer extends JsonSerializer<Class<?>> {

        private Serializer() {
        }

        @Override
        public void serialize(Class<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                try {
                    JsonNode schema = jsonSchemaGenerator.generateJsonSchema(value);
                    gen.writeObject(schema);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate JSON Schema", e);
                }
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<Class<?>> {

        private Deserializer() {
        }

        @Override
        public Class<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // todo: need fixed here
            return null;
        }
    }
}





