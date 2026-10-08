package me.vacuity.ai.sdk.test.unit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.decision.request.DecisionQuestion;
import me.vacuity.ai.sdk.openai.decision.request.DecisionRequest;
import me.vacuity.ai.sdk.openai.decision.response.Decision;
import me.vacuity.ai.sdk.openai.decision.response.DecisionAnswer;
import me.vacuity.ai.sdk.openai.file.entity.OpenaiFile;
import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.request.EditImageRequest;
import me.vacuity.ai.sdk.openai.image.constant.ImageStreamEventConstant;
import me.vacuity.ai.sdk.openai.image.response.ImageResponse;
import me.vacuity.ai.sdk.openai.image.response.ImageStreamEvent;
import me.vacuity.ai.sdk.openai.video.entity.VideoJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Drives OpenaiClient over a real HTTP connection to a local server.
 * <p>
 * This exists because of the 1.10.0 regression: the Lombok upgrade dropped the
 * no-args constructor from every {@code @Builder} class, and Jackson could no
 * longer construct any of these response types. Nothing in the offline
 * serialization tests caught it — they only ever built request bodies. These
 * tests deserialize through the real Retrofit + Jackson converter path, which
 * is where the failure actually surfaced in production.
 * <p>
 * No vendor API is contacted; the server binds to 127.0.0.1 on an ephemeral port.
 */
public class OpenaiWireIntegrationTest {

    private HttpServer server;
    private String baseUrl;
    private volatile String responseBody = "{}";
    private volatile String responseContentType = "application/json";
    private final java.util.concurrent.atomic.AtomicReference<String> capturedBody =
            new java.util.concurrent.atomic.AtomicReference<>();

    @BeforeEach
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    public void stopServer() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws java.io.IOException {
        try (java.io.InputStream in = exchange.getRequestBody()) {
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buf.write(chunk, 0, read);
            }
            capturedBody.set(new String(buf.toByteArray(), StandardCharsets.UTF_8));
        }
        byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", responseContentType);
        exchange.sendResponseHeaders(200, out.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(out);
        }
    }

    private OpenaiClient client() {
        return new OpenaiClient("test-key-not-a-real-secret", Duration.ofSeconds(10), baseUrl);
    }

    @Test
    public void createImageDeserializes() {
        responseBody = "{\"created\":1712345678,"
                + "\"data\":[{\"url\":\"https://example.invalid/a.png\",\"revised_prompt\":\"a cat\"}],"
                + "\"usage\":{\"total_tokens\":42,\"input_tokens\":10,\"output_tokens\":32}}";

        ImageResponse response = client().createImage(
                CreateImageRequest.builder().prompt("a cat").n(1).build());

        assertNotNull(response, "image response must deserialize");
        assertEquals(1712345678, response.getCreated());
        assertEquals(1, response.getData().size());
        assertEquals("https://example.invalid/a.png", response.getData().get(0).getUrl());
        assertNotNull(response.getUsage());
    }

    @Test
    public void base64ImageResponseDeserializes() {
        responseBody = "{\"created\":1,\"data\":[{\"b64_json\":\"aGVsbG8=\"}]}";

        ImageResponse response = client().createImage(
                CreateImageRequest.builder().prompt("x").build());

        assertEquals("aGVsbG8=", response.getData().get(0).getB64Json());
    }

    @Test
    public void videoJobDeserializes() {
        responseBody = "{\"id\":\"video_1\",\"object\":\"video\",\"status\":\"queued\","
                + "\"model\":\"sora-1.0-turbo\",\"progress\":0}";

        VideoJob job = client().retrieveVideo("video_1");

        assertNotNull(job);
        assertEquals("video_1", job.getId());
        assertEquals("queued", job.getStatus());
    }

    @Test
    public void fileDeserializes() {
        responseBody = "{\"id\":\"file_1\",\"object\":\"file\",\"bytes\":120,"
                + "\"created_at\":1712345678,\"filename\":\"a.jsonl\",\"purpose\":\"batch\"}";

        OpenaiFile file = client().retrieveFile("file_1");

        assertNotNull(file);
        assertEquals("file_1", file.getId());
        assertEquals("a.jsonl", file.getFilename());
    }

    // ---------------------------------------------------- image edit multipart

    private java.io.File tempPng() throws Exception {
        java.io.File f = java.io.File.createTempFile("edit-", ".png");
        f.deleteOnExit();
        java.nio.file.Files.write(f.toPath(), new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        return f;
    }

    /**
     * editImage builds its multipart body by hand, so a field can exist on the
     * request object and still never reach the wire. moderation shipped in
     * 1.9.17 and was silently dropped for exactly that reason.
     */
    @Test
    public void editImageSendsEveryConfiguredField() throws Exception {
        responseBody = "{\"created\":1,\"data\":[{\"b64_json\":\"aGk=\"}]}";

        EditImageRequest request = EditImageRequest.builder()
                .prompt("make it pop")
                .model("gpt-image-1")
                .n(2)
                .quality("high")
                .size("1024x1024")
                .responseFormat("b64_json")
                .user("user-1")
                .inputFidelity("high")
                .moderation("low")
                .background("transparent")
                .outputFormat("webp")
                .outputCompression(80)
                .build();

        // 显式转型：editImage(req, File, File) 与 editImage(req, File, List) 对 null 有歧义
        client().editImage(request, tempPng(), (java.io.File) null);

        String body = capturedBody.get();
        for (String part : new String[]{
                "prompt", "model", "n", "quality", "size", "response_format",
                "user", "input_fidelity", "moderation",
                "background", "output_format", "output_compression"}) {
            assertTrue(body.contains("name=\"" + part + "\""),
                    "multipart body is missing form part: " + part);
        }
        assertTrue(body.contains("transparent"), "background value must be sent");
        assertTrue(body.contains("webp"), "output_format value must be sent");
        assertTrue(body.contains("80"), "output_compression value must be sent");
    }

    @Test
    public void editImageOmitsUnsetFields() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().editImage(EditImageRequest.builder().prompt("only prompt").build(),
                tempPng(), (java.io.File) null);

        String body = capturedBody.get();
        assertTrue(body.contains("name=\"prompt\""));
        for (String absent : new String[]{"moderation", "background", "output_format"}) {
            assertFalse(body.contains("name=\"" + absent + "\""),
                    "unset field must not be sent: " + absent);
        }
    }

    // ------------------------------------------------------- image streaming

    @Test
    public void streamCreateImageParsesPartialAndCompletedEvents() {
        responseContentType = "text/event-stream";
        responseBody = ""
                + "data: {\"type\":\"image_generation.partial_image\",\"b64_json\":\"cGFydDA=\","
                + "\"partial_image_index\":0,\"size\":\"1024x1024\",\"quality\":\"high\","
                + "\"background\":\"transparent\",\"output_format\":\"webp\",\"created_at\":1}\n\n"
                + "data: {\"type\":\"image_generation.completed\",\"b64_json\":\"ZG9uZQ==\","
                + "\"size\":\"1024x1024\",\"quality\":\"high\",\"created_at\":2,"
                + "\"usage\":{\"input_tokens\":10,\"output_tokens\":50,\"total_tokens\":60,"
                + "\"input_tokens_details\":{\"image_tokens\":4,\"text_tokens\":6}}}\n\n"
                + "data: [DONE]\n\n";

        java.util.List<ImageStreamEvent> events = new java.util.ArrayList<>();
        client().streamCreateImage(CreateImageRequest.builder().prompt("a cat").partialImages(1).build())
                .blockingSubscribe(events::add);

        assertEquals(2, events.size());

        ImageStreamEvent partial = events.get(0);
        assertEquals(ImageStreamEventConstant.GENERATION_PARTIAL_IMAGE, partial.getType());
        assertEquals("cGFydDA=", partial.getB64Json());
        assertEquals(0, partial.getPartialImageIndex());
        assertEquals("webp", partial.getOutputFormat());
        assertEquals("transparent", partial.getBackground());
        assertFalse(partial.isCompleted());

        ImageStreamEvent done = events.get(1);
        assertEquals(ImageStreamEventConstant.GENERATION_COMPLETED, done.getType());
        assertTrue(done.isCompleted());
        assertNotNull(done.getUsage());
        assertEquals(60, done.getUsage().getTotalTokens());
        assertEquals(4, done.getUsage().getInputTokensDetails().getImageTokens());
    }

    @Test
    public void streamCreateImageSetsStreamFlagAndPartialImages() {
        responseContentType = "text/event-stream";
        responseBody = "data: [DONE]\n\n";

        client().streamCreateImage(
                        CreateImageRequest.builder().prompt("x").partialImages(3).build())
                .blockingSubscribe(e -> { }, t -> { });

        String body = capturedBody.get();
        assertTrue(body.contains("\"stream\":true"), "stream flag must be sent, got: " + body);
        assertTrue(body.contains("\"partial_images\":3"), "partial_images must be sent, got: " + body);
    }

    @Test
    public void streamEditImageSendsStreamPartsInMultipart() throws Exception {
        responseContentType = "text/event-stream";
        responseBody = "data: [DONE]\n\n";

        client().streamEditImage(
                        EditImageRequest.builder().prompt("edit me").partialImages(2).build(),
                        tempPng(), (java.io.File) null)
                .blockingSubscribe(e -> { }, t -> { });

        String body = capturedBody.get();
        assertTrue(body.contains("name=\"stream\""), "stream part missing");
        assertTrue(body.contains("name=\"partial_images\""), "partial_images part missing");
    }

    // ------------------------------------------------------------- decisions

    /**
     * Fixture captured verbatim from a real /v1/decisions call, so the parsing
     * assertions below are pinned to the shape the server actually returns.
     */
    private static final String REAL_DECISION_RESPONSE =
            "{\"model\":\"gpt-6-luna\",\"answers\":["
            + "{\"type\":\"predicate\",\"name\":\"is_bug\",\"probability\":1.0},"
            + "{\"type\":\"choice\",\"name\":\"area\",\"choice\":\"mobile\",\"probabilities\":["
            + "{\"value\":\"mobile\",\"probability\":1.0},"
            + "{\"value\":\"backend\",\"probability\":0.0},"
            + "{\"value\":\"docs\",\"probability\":0.0}],\"confidence\":1.0},"
            + "{\"type\":\"score\",\"name\":\"severity\",\"score\":1.94,\"probabilities\":["
            + "{\"value\":0,\"label\":\"low\",\"probability\":0.01},"
            + "{\"value\":1,\"label\":\"medium\",\"probability\":0.04},"
            + "{\"value\":2,\"label\":\"high\",\"probability\":0.95}],\"confidence\":0.91}],"
            + "\"usage\":{\"input_tokens\":410,"
            + "\"input_tokens_details\":{\"cached_tokens\":0,\"cache_write_tokens\":0},"
            + "\"output_tokens\":0,\"output_tokens_details\":{\"reasoning_tokens\":0},"
            + "\"total_tokens\":410}}";

    private DecisionRequest sampleDecision() {
        return DecisionRequest.builder()
                .model("gpt-6-luna")
                .input("The app crashes when I open settings.")
                .questions(java.util.Arrays.asList(
                        DecisionQuestion.predicate("is_bug", "Does this describe a defect?"),
                        DecisionQuestion.choice("area", "Which area?", java.util.Arrays.asList(
                                DecisionQuestion.Choice.of("mobile", "iOS or Android"),
                                DecisionQuestion.Choice.of("backend", "server side"))),
                        DecisionQuestion.score("severity", "How severe?", java.util.Arrays.asList(
                                DecisionQuestion.Level.of("low", "cosmetic"),
                                DecisionQuestion.Level.of("high", "unusable")))))
                .build();
    }

    @Test
    public void decisionRequestUsesTheDocumentedShape() throws Exception {
        responseBody = REAL_DECISION_RESPONSE;

        client().createDecision(sampleDecision());

        com.fasterxml.jackson.databind.JsonNode body =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());

        assertEquals("gpt-6-luna", body.get("model").asText());
        assertTrue(body.get("input").isTextual());
        assertEquals(3, body.get("questions").size());

        assertEquals("predicate", body.get("questions").get(0).get("type").asText());
        assertFalse(body.get("questions").get(0).has("choices"),
                "a predicate question must not carry choices");

        assertEquals("choice", body.get("questions").get(1).get("type").asText());
        assertEquals("mobile", body.get("questions").get(1).get("choices").get(0).get("value").asText());

        assertEquals("score", body.get("questions").get(2).get("type").asText());
        assertEquals("low", body.get("questions").get(2).get("levels").get(0).get("label").asText());
    }

    @Test
    public void decisionResponseParsesAllThreeAnswerTypes() {
        responseBody = REAL_DECISION_RESPONSE;

        Decision decision = client().createDecision(sampleDecision());

        assertEquals("gpt-6-luna", decision.getModel());
        assertEquals(3, decision.getAnswers().size());

        DecisionAnswer predicate = decision.answer("is_bug");
        assertEquals(1.0, predicate.getProbability());
        assertFalse(predicate.isRefusal());

        DecisionAnswer choice = decision.answer("area");
        assertEquals("mobile", choice.getChoice());
        assertEquals(1.0, choice.getConfidence());
        assertEquals(3, choice.getProbabilities().size());
        assertEquals("backend", choice.getProbabilities().get(1).getValue());

        DecisionAnswer score = decision.answer("severity");
        // score is a probability-weighted average of level indexes, not an index
        assertEquals(1.94, score.getScore());
        assertEquals("high", score.getProbabilities().get(2).getLabel());
        assertEquals(2, score.getProbabilities().get(2).getValue());

        // only input tokens are billed on this endpoint
        assertEquals(410, decision.getUsage().getInputTokens());
        assertEquals(0, decision.getUsage().getOutputTokens());
        assertEquals(0, decision.getUsage().getInputTokensDetails().getCachedTokens());
    }

    @Test
    public void decisionRefusalIsDetectableBeforeReadingValues() {
        responseBody = "{\"model\":\"gpt-6-luna\",\"answers\":["
                + "{\"type\":\"refusal\",\"name\":\"is_bug\"}],"
                + "\"usage\":{\"input_tokens\":5,\"total_tokens\":5}}";

        Decision decision = client().createDecision(sampleDecision());
        DecisionAnswer answer = decision.answer("is_bug");

        assertTrue(answer.isRefusal());
        assertNull(answer.getProbability(), "a refusal carries no value fields");
        assertNull(answer.getChoice());
        assertNull(answer.getScore());
    }

    @Test
    public void unknownAnswerByNameIsNull() {
        responseBody = REAL_DECISION_RESPONSE;
        Decision decision = client().createDecision(sampleDecision());
        assertNull(decision.answer("no_such_question"));
    }
}
