package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.ImageConfig;
import me.vacuity.ai.sdk.gemini.entity.Schema;
import me.vacuity.ai.sdk.gemini.entity.SpeechConfig;
import me.vacuity.ai.sdk.gemini.enums.Modality;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The GenerationConfig fields added in 2.6.0, against the live API.
 * <p>
 * Each was checked first against the v1beta discovery document — the
 * machine-readable contract — and these tests confirm the SDK sends them in a
 * form the API accepts and acts on. Four fields the discovery document also
 * lists were deliberately NOT added, and
 * {@link #fieldsDeliberatelyNotModelledStillBehaveAsMeasured()} records why.
 *
 * @author vacuity
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiGenerationConfigLiveSmokeTest {

    private static final String TEXT_MODEL = "gemini-3-flash-preview";

    private GeminiClient client() {
        return new GeminiClient(System.getenv("GEMINI_API_KEY"), Duration.ofSeconds(180));
    }

    private static ChatMessage user(String text) {
        ChatMessageContentPart part = new ChatMessageContentPart();
        part.setText(text);
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setParts(Collections.singletonList(part));
        return message;
    }

    private static String textOf(ChatResponse response) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessageContentPart part : response.getCandidates().get(0)
                .getContent().getParts()) {
            if (part.getText() != null) {
                sb.append(part.getText());
            }
        }
        return sb.toString();
    }

    /**
     * responseSchema must actually constrain the output, not merely be
     * accepted. Thinking is turned off so the whole budget goes to the answer.
     */
    @Test
    public void responseSchemaConstrainsTheOutputToItsShape() throws Exception {
        Map<String, Schema> properties = new LinkedHashMap<>();
        properties.put("ok", Schema.builder().type("BOOLEAN").build());
        properties.put("why", Schema.builder().type("STRING").build());

        ChatRequest.ThinkingConfig noThinking = new ChatRequest.ThinkingConfig();
        noThinking.setThinkingBudget(0);

        ChatResponse response = client().chat(ChatRequest.builder()
                .model(TEXT_MODEL)
                .contents(Collections.singletonList(user("Is the sky blue? Answer the schema.")))
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .responseMimeType("application/json")
                        .maxOutputTokens(512)
                        .thinkingConfig(noThinking)
                        .responseSchema(Schema.builder()
                                .type("OBJECT")
                                .properties(properties)
                                .required(Collections.singletonList("ok"))
                                .propertyOrdering(Arrays.asList("ok", "why"))
                                .build())
                        .build())
                .build());

        String text = textOf(response).trim();
        Map<?, ?> parsed = new ObjectMapper().readValue(text, Map.class);
        assertTrue(parsed.containsKey("ok"), "the schema's required key must be present: " + text);
        assertTrue(parsed.get("ok") instanceof Boolean, "ok must be a boolean: " + text);
        assertTrue(text.indexOf("\"ok\"") < text.indexOf("\"why\""),
                "propertyOrdering must be honoured: " + text);
    }

    /** topK is int32; the SDK now types it so, and the server takes it. */
    @Test
    public void integerTopKIsAccepted() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model(TEXT_MODEL)
                .contents(Collections.singletonList(user("Say ok")))
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .topK(5).maxOutputTokens(16).build())
                .build());

        assertNotNull(response.getCandidates());
    }

    @Test
    public void imageConfigReachesAnImageModel() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model("gemini-3-pro-image")
                .contents(Collections.singletonList(user("a plain blue circle on white")))
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .responseModalities(Collections.singletonList(Modality.IMAGE))
                        .imageConfig(ImageConfig.builder()
                                .aspectRatio("16:9").imageSize("2K").build())
                        .build())
                .build());

        boolean sawImage = false;
        for (ChatMessageContentPart part : response.getCandidates().get(0)
                .getContent().getParts()) {
            if (part.getInlineData() != null) {
                sawImage = true;
            }
        }
        assertTrue(sawImage, "an IMAGE modality request with imageConfig must return an image");
    }

    /**
     * An invalid aspect ratio is the server's complaint — the SDK keeps no
     * allowlist, so a new ratio needs no release.
     */
    @Test
    public void anUnknownAspectRatioIsRejectedByTheServer() {
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(ChatRequest.builder()
                                .model("gemini-3-pro-image")
                                .contents(Collections.singletonList(user("a circle")))
                                .generationConfig(ChatRequest.GenerationConfig.builder()
                                        .responseModalities(
                                                Collections.singletonList(Modality.IMAGE))
                                        .imageConfig(ImageConfig.builder()
                                                .aspectRatio("bogus").build())
                                        .build())
                                .build()));

        assertTrue(e.getMessage().contains("aspect_ratio"),
                "the server should name the field: " + e.getMessage());
    }

    /**
     * The audio container differs by model, so the test reads the part's mime
     * type rather than asserting one.
     */
    @Test
    public void speechConfigProducesAudio() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model("gemini-2.5-flash-preview-tts")
                .contents(Collections.singletonList(user("Say: hello there")))
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .responseModalities(Collections.singletonList(Modality.AUDIO))
                        .speechConfig(SpeechConfig.voice("Kore"))
                        .build())
                .build());

        String mime = null;
        for (ChatMessageContentPart part : response.getCandidates().get(0)
                .getContent().getParts()) {
            if (part.getInlineData() != null) {
                mime = part.getInlineData().getMimeType();
            }
        }
        assertNotNull(mime, "an AUDIO modality request must return inline audio");
        assertTrue(mime.startsWith("audio/"), "got mime " + mime);
    }

    /**
     * cachedContent, end to end: the cache is created through the
     * cachedContents endpoint, which this SDK does not wrap, and then used
     * through the SDK's own request field.
     */
    @Test
    public void cachedContentIsUsedAndReportedBack() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 220; i++) {
            sb.append("You are reviewing a Java SDK. Follow these rules exactly "
                    + "and never deviate. ");
        }
        String name = createCache(sb.toString());
        assertNotNull(name, "the cache must be created before the field can be tested");
        try {
            ChatResponse response = client().chat(ChatRequest.builder()
                    .model("gemini-2.5-flash")
                    .contents(Collections.singletonList(user("Reply with: ok")))
                    .cachedContent(name)
                    .build());

            Integer cached = response.getUsageMetadata().getCachedContentTokenCount();
            assertNotNull(cached, "a cached call must report cachedContentTokenCount");
            assertTrue(cached > 0, "the cached prefix must be counted; got " + cached);
            assertTrue(response.getUsageMetadata().getPromptTokenCount() >= cached,
                    "cached tokens are part of the prompt count, not on top of it");
        } finally {
            deleteCache(name);
        }
    }

    /**
     * Four fields the discovery document lists were left out on purpose. This
     * test records what the API did with them on the models reachable here, so
     * the decision can be revisited from evidence rather than reread from the
     * same document.
     * <p>
     * {@code enableAffectiveDialog} is rejected outright. The other three are
     * accepted and change nothing observable on a text model — they belong to
     * the Live API or to modalities not reachable from generateContent — so
     * modelling them would have meant shipping fields nobody could verify.
     */
    @Test
    public void fieldsDeliberatelyNotModelledStillBehaveAsMeasured() throws Exception {
        assertEquals(400, rawStatus("{\"enableAffectiveDialog\":true,\"maxOutputTokens\":16}"),
                "enableAffectiveDialog is still rejected on " + TEXT_MODEL
                        + "; if it is now accepted, reconsider adding it");
        for (String config : new String[]{
                "{\"translationConfig\":{\"targetLanguageCode\":\"zh\"},\"maxOutputTokens\":32}",
                "{\"audioTranscriptionConfig\":{\"mode\":\"SMART\"},\"maxOutputTokens\":16}",
                "{\"responseFormat\":{\"text\":{}},\"maxOutputTokens\":16}"}) {
            assertEquals(200, rawStatus(config),
                    "still accepted but without observable effect: " + config);
        }
    }

    // ---- raw helpers, for the parts of the API this SDK does not wrap

    private static int rawStatus(String generationConfig) throws Exception {
        String body = "{\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"Say ok\"}]}],"
                + "\"generationConfig\":" + generationConfig + "}";
        HttpURLConnection connection = post(
                "https://generativelanguage.googleapis.com/v1alpha/models/"
                        + TEXT_MODEL + ":generateContent", body);
        int status = connection.getResponseCode();
        connection.disconnect();
        return status;
    }

    private static String createCache(String text) throws Exception {
        String body = "{\"model\":\"models/gemini-2.5-flash\",\"ttl\":\"300s\","
                + "\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\""
                + text.replace("\"", "\\\"") + "\"}]}]}";
        HttpURLConnection connection = post(
                "https://generativelanguage.googleapis.com/v1beta/cachedContents", body);
        if (connection.getResponseCode() != 200) {
            connection.disconnect();
            return null;
        }
        Map<?, ?> created = new ObjectMapper().readValue(connection.getInputStream(), Map.class);
        connection.disconnect();
        return (String) created.get("name");
    }

    private static void deleteCache(String name) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(
                "https://generativelanguage.googleapis.com/v1beta/" + name).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("x-goog-api-key", System.getenv("GEMINI_API_KEY"));
        connection.getResponseCode();
        connection.disconnect();
    }

    private static HttpURLConnection post(String url, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(180000);
        connection.setRequestProperty("x-goog-api-key", System.getenv("GEMINI_API_KEY"));
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return connection;
    }

    /** Unused but kept so the import list documents the response shape. */
    @SuppressWarnings("unused")
    private List<ChatMessageContentPart> parts(ChatResponse response) {
        return response.getCandidates().get(0).getContent().getParts();
    }
}
