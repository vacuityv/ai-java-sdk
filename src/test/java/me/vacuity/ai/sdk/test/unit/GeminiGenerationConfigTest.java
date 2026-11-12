package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ImageConfig;
import me.vacuity.ai.sdk.gemini.entity.Schema;
import me.vacuity.ai.sdk.gemini.entity.SpeakerVoiceConfig;
import me.vacuity.ai.sdk.gemini.entity.SpeechConfig;
import me.vacuity.ai.sdk.gemini.entity.MultiSpeakerVoiceConfig;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The GenerationConfig fields added in 2.6.0, plus the topK type correction.
 * <p>
 * Checked against the v1beta discovery document
 * ({@code generativelanguage.googleapis.com/$discovery/rest?version=v1beta}),
 * which is the machine-readable contract rather than a prose page, and then
 * against the live API.
 *
 * @author vacuity
 */
public class GeminiGenerationConfigTest {

    private final ObjectMapper mapper = GeminiClient.defaultObjectMapper();

    /**
     * topK is int32 in the discovery document. It was declared Float here
     * before 2.6.0, which let a caller build a request the server refuses:
     * {@code Invalid value at 'generation_config.top_k' (TYPE_INT32), 5.5}.
     * A whole-valued float was always accepted, so nothing on the wire was
     * wrong — the type simply permitted an impossible value.
     */
    @Test
    public void topKIsAnIntegerAndSerializesWithoutADecimalPoint() throws Exception {
        JsonNode sent = mapper.valueToTree(ChatRequest.builder()
                .model("gemini-3-flash-preview")
                .generationConfig(ChatRequest.GenerationConfig.builder().topK(5).build())
                .build());

        JsonNode topK = sent.get("generationConfig").get("topK");
        assertTrue(topK.isInt(), "topK must serialize as an int, was " + topK);
        assertEquals(5, topK.asInt());
        assertEquals("5", topK.toString(), "no decimal point on the wire");
    }

    @Test
    public void responseSchemaSerializesWithItsWireNames() throws Exception {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put("ok", Schema.builder().type("BOOLEAN").build());
        properties.put("why", Schema.builder().type("STRING")
                .description("the reason").maxLength(200L).build());

        JsonNode sent = mapper.valueToTree(ChatRequest.builder()
                .model("gemini-3-flash-preview")
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .responseMimeType("application/json")
                        .responseSchema(Schema.builder()
                                .type("OBJECT")
                                .properties(properties)
                                .required(Collections.singletonList("ok"))
                                .propertyOrdering(Arrays.asList("ok", "why"))
                                .build())
                        .build())
                .build());

        JsonNode schema = sent.get("generationConfig").get("responseSchema");
        assertEquals("OBJECT", schema.get("type").asText());
        assertEquals("BOOLEAN", schema.get("properties").get("ok").get("type").asText());
        assertEquals(200, schema.get("properties").get("why").get("maxLength").asLong());
        assertEquals("ok", schema.get("required").get(0).asText());
        // propertyOrdering is honoured by the server, so it must survive serialization
        assertEquals("ok", schema.get("propertyOrdering").get(0).asText());
        assertEquals("why", schema.get("propertyOrdering").get(1).asText());
        assertNull(schema.get("nullable"), "unset fields must not be sent");
    }

    /**
     * {@code enum} is a Java keyword, so the field is named enumValues and
     * mapped by annotated accessors. If that mapping broke, the allowed values
     * would silently never be sent.
     */
    @Test
    public void schemaEnumSerializesAsEnum() throws Exception {
        JsonNode sent = mapper.valueToTree(Schema.builder()
                .type("STRING")
                .enumValues(Arrays.asList("LOW", "HIGH"))
                .build());

        assertEquals("LOW", sent.get("enum").get(0).asText());
        assertNull(sent.get("enumValues"), "the Java field name must not reach the wire");
    }

    @Test
    public void schemaEnumDeserializesFromEnum() throws Exception {
        Schema schema = mapper.readValue(
                "{\"type\":\"STRING\",\"enum\":[\"LOW\",\"HIGH\"]}", Schema.class);

        assertEquals(Arrays.asList("LOW", "HIGH"), schema.getEnumValues());
    }

    /**
     * responseJsonSchema is typed loosely because the discovery document calls
     * it {@code any} — a plain Map must pass straight through, including the
     * {@code additionalProperties} that responseSchema has no field for.
     */
    @Test
    public void responseJsonSchemaPassesAMapThrough() throws Exception {
        Map<String, Object> inner = new LinkedHashMap<>();
        inner.put("ok", Collections.singletonMap("type", "boolean"));
        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        jsonSchema.put("type", "object");
        jsonSchema.put("properties", inner);
        jsonSchema.put("additionalProperties", false);

        JsonNode sent = mapper.valueToTree(ChatRequest.builder()
                .model("gemini-3-flash-preview")
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .responseJsonSchema(jsonSchema).build())
                .build());

        JsonNode schema = sent.get("generationConfig").get("responseJsonSchema");
        assertEquals("object", schema.get("type").asText());
        assertEquals(false, schema.get("additionalProperties").asBoolean());
    }

    @Test
    public void imageConfigSerializes() throws Exception {
        JsonNode sent = mapper.valueToTree(ChatRequest.GenerationConfig.builder()
                .imageConfig(ImageConfig.builder().aspectRatio("16:9").imageSize("2K").build())
                .build());

        assertEquals("16:9", sent.get("imageConfig").get("aspectRatio").asText());
        assertEquals("2K", sent.get("imageConfig").get("imageSize").asText());
    }

    @Test
    public void speechConfigNestsThePrebuiltVoice() throws Exception {
        JsonNode sent = mapper.valueToTree(ChatRequest.GenerationConfig.builder()
                .speechConfig(SpeechConfig.voice("Kore"))
                .build());

        assertEquals("Kore", sent.get("speechConfig").get("voiceConfig")
                .get("prebuiltVoiceConfig").get("voiceName").asText(),
                "the voice name sits three levels down: " + sent);
        assertNull(sent.get("speechConfig").get("multiSpeakerVoiceConfig"));
    }

    @Test
    public void multiSpeakerSpeechConfigSerializes() throws Exception {
        JsonNode sent = mapper.valueToTree(ChatRequest.GenerationConfig.builder()
                .speechConfig(SpeechConfig.builder()
                        .languageCode("en-US")
                        .multiSpeakerVoiceConfig(MultiSpeakerVoiceConfig.builder()
                                .speakerVoiceConfigs(Arrays.asList(
                                        SpeakerVoiceConfig.of("Alice", "Kore"),
                                        SpeakerVoiceConfig.of("Bob", "Puck")))
                                .build())
                        .build())
                .build());

        JsonNode speakers = sent.get("speechConfig").get("multiSpeakerVoiceConfig")
                .get("speakerVoiceConfigs");
        assertEquals(2, speakers.size());
        assertEquals("Alice", speakers.get(0).get("speaker").asText());
        assertEquals("Puck", speakers.get(1).get("voiceConfig")
                .get("prebuiltVoiceConfig").get("voiceName").asText());
        assertEquals("en-US", sent.get("speechConfig").get("languageCode").asText());
    }

    /**
     * cachedContent is a top-level request field, not part of generationConfig
     * — the discovery document puts it on GenerateContentRequest.
     */
    @Test
    public void cachedContentIsATopLevelField() throws Exception {
        JsonNode sent = mapper.valueToTree(ChatRequest.builder()
                .model("gemini-2.5-flash")
                .cachedContent("cachedContents/abc123")
                .build());

        assertEquals("cachedContents/abc123", sent.get("cachedContent").asText());
        assertNull(sent.get("generationConfig"),
                "cachedContent must not pull an empty generationConfig along");
    }

    @Test
    public void unsetNewFieldsStayOffTheWire() throws Exception {
        JsonNode sent = mapper.valueToTree(ChatRequest.builder()
                .model("gemini-3-flash-preview")
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .maxOutputTokens(16).build())
                .build());

        assertNull(sent.get("cachedContent"));
        JsonNode config = sent.get("generationConfig");
        for (String absent : new String[]{
                "responseSchema", "responseJsonSchema", "imageConfig", "speechConfig", "topK"}) {
            assertNull(config.get(absent), "unset field must not be sent: " + absent);
        }
    }
}
