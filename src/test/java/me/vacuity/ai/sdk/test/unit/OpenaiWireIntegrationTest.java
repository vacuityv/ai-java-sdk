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

    // ------------------------------------------------- GPT-Image-2.6 (2.1.0)

    /**
     * The new generations-only knobs. These go out as JSON, so the risk is the
     * snake_case wire names rather than a forgotten multipart part.
     */
    @Test
    public void createImageSendsGifFramingAndComplexity() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().createImage(CreateImageRequest.builder()
                .prompt("a waving cat")
                .model("gpt-image-2.6-sunburst")
                .outputFormat("gif")
                .complexity("low")
                .frameCount(12)
                .frameDurationMs(120)
                .build());

        com.fasterxml.jackson.databind.JsonNode sent =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());
        assertEquals("gif", sent.get("output_format").asText());
        assertEquals("low", sent.get("complexity").asText());
        assertEquals(12, sent.get("frame_count").asInt(), "frame_count must reach the wire");
        assertEquals(120, sent.get("frame_duration_ms").asInt(),
                "frame_duration_ms must reach the wire");
    }

    /**
     * svg is a nested object, not a string — the authoring model lives under
     * svg.model, and the API rejects an SVG request that omits it.
     */
    @Test
    public void createImageSendsSvgModelAsNestedObject() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().createImage(CreateImageRequest.builder()
                .prompt("a flat icon of a bicycle")
                .model("gpt-image-2.6-flare")
                .outputFormat("svg")
                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model("gpt-6.1-terra"))
                .build());

        com.fasterxml.jackson.databind.JsonNode sent =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());
        assertTrue(sent.get("svg").isObject(), "svg must serialize as an object");
        assertEquals("gpt-6.1-terra", sent.get("svg").get("model").asText());
    }

    /**
     * Verbatim body from a real gpt-image-2.6 GIF generation. Covers the echoed
     * request settings, output_tokens_details and data[].generation_id, none of
     * which the 2.0.0 ImageResponse could hold.
     */
    @Test
    public void gifResponseParsesEchoedSettingsAndGenerationId() {
        responseBody = "{\"background\":\"opaque\",\"output_format\":\"gif\","
                + "\"quality\":\"low\",\"size\":\"512x512\","
                + "\"usage\":{\"input_tokens\":41,"
                + "\"input_tokens_details\":{\"image_tokens\":0,\"text_tokens\":41},"
                + "\"output_tokens\":555,"
                + "\"output_tokens_details\":{\"image_tokens\":555,\"text_tokens\":0},"
                + "\"total_tokens\":596},"
                + "\"data\":[{\"b64_json\":\"R0lGODlh\",\"generation_id\":\"gen_abc123\"}]}";

        ImageResponse response = client().createImage(
                CreateImageRequest.builder().prompt("x").outputFormat("gif").build());

        assertEquals("opaque", response.getBackground());
        assertEquals("gif", response.getOutputFormat());
        assertEquals("low", response.getQuality());
        assertEquals("512x512", response.getSize());
        assertEquals("gen_abc123", response.getData().get(0).getGenerationId());
        assertNotNull(response.getUsage().getOutputTokensDetails(),
                "output_tokens_details must deserialize");
        assertEquals(555, response.getUsage().getOutputTokensDetails().getImageTokens());
        assertEquals(0, response.getUsage().getOutputTokensDetails().getTextTokens());
        assertNull(response.getSvgUsage(), "a GIF response carries no svg_usage");
    }

    /**
     * Verbatim body from a real SVG generation. svg_usage is billed separately
     * at the authoring model's own rates, so it must not be folded into usage.
     */
    @Test
    public void svgResponseParsesSvgUsageSeparatelyFromImageUsage() {
        responseBody = "{\"background\":\"transparent\",\"output_format\":\"svg\","
                + "\"quality\":\"low\",\"size\":\"1312x1199\","
                + "\"svg_usage\":{\"input_tokens\":2194,\"model\":\"gpt-6.1-terra\","
                + "\"output_tokens\":2592,\"reasoning_tokens\":1226,\"total_tokens\":4786},"
                + "\"usage\":{\"input_tokens\":81,"
                + "\"input_tokens_details\":{\"image_tokens\":0,\"text_tokens\":81},"
                + "\"output_tokens\":903,"
                + "\"output_tokens_details\":{\"image_tokens\":903,\"text_tokens\":0},"
                + "\"total_tokens\":984},"
                + "\"data\":[{\"b64_json\":\"PHN2Zz48L3N2Zz4=\"}]}";

        // svg.model is required by the server, and now by the SDK's own
        // pre-flight check, so the request has to be a valid one even though
        // this test only cares about parsing the response.
        ImageResponse response = client().createImage(
                CreateImageRequest.builder().prompt("x").outputFormat("svg")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model("gpt-6.1-terra"))
                        .build());

        assertEquals("svg", response.getOutputFormat());
        assertNotNull(response.getSvgUsage(), "svg_usage must deserialize");
        assertEquals("gpt-6.1-terra", response.getSvgUsage().getModel());
        assertEquals(2194, response.getSvgUsage().getInputTokens());
        assertEquals(2592, response.getSvgUsage().getOutputTokens());
        assertEquals(1226, response.getSvgUsage().getReasoningTokens());
        assertEquals(4786, response.getSvgUsage().getTotalTokens());
        // The two usage blocks are independent — reading one must not shadow the other.
        assertEquals(984, response.getUsage().getTotalTokens());
    }

    /**
     * complexity and markup travel in the hand-built multipart body, which is
     * exactly where moderation went missing between 1.9.17 and 1.11.0.
     */
    @Test
    public void editImageSendsComplexityAndMarkup() throws Exception {
        responseBody = "{\"created\":1,\"data\":[{\"b64_json\":\"aGk=\"}]}";

        EditImageRequest request = EditImageRequest.builder()
                .prompt("follow the annotations")
                .model("gpt-image-2.6-sunburst")
                .complexity("high")
                .markup(tempPng())
                .safetyIdentifier("sid-1")
                .build();

        client().editImage(request, tempPng(), (java.io.File) null);

        String body = capturedBody.get();
        assertTrue(body.contains("name=\"complexity\""),
                "multipart body is missing form part: complexity");
        assertTrue(body.contains("high"), "complexity value must be sent");
        assertTrue(body.contains("name=\"markup\""),
                "multipart body is missing file part: markup");
        assertTrue(body.contains("name=\"safety_identifier\""),
                "multipart body is missing form part: safety_identifier");
        assertTrue(body.contains("sid-1"), "safety_identifier value must be sent");
    }

    /**
     * The API rejects markup together with mask. Failing locally beats paying
     * for a round trip to find out.
     */
    @Test
    public void markupTogetherWithMaskIsRejectedLocally() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        EditImageRequest request = EditImageRequest.builder()
                .prompt("x")
                .markup(tempPng())
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> client().editImage(request, tempPng(), tempPng()));
    }

    @Test
    public void unsetGifAndSvgFieldsStayOffTheWire() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().createImage(CreateImageRequest.builder().prompt("plain png").build());

        String body = capturedBody.get();
        for (String absent : new String[]{"complexity", "frame_count", "frame_duration_ms", "svg"}) {
            assertFalse(body.contains("\"" + absent + "\""),
                    "unset field must not be sent: " + absent);
        }
    }

    // ------------------------------------------- GIF 编辑 / svg.reasoning (2.2.0)

    /**
     * frame_count and frame_duration_ms travel in the hand-built multipart
     * body, the same place complexity and markup had to be wired by hand.
     */
    @Test
    public void editImageSendsGifFramingFields() throws Exception {
        responseBody = "{\"created\":1,\"data\":[{\"b64_json\":\"R0lGODlh\"}]}";

        client().editImage(EditImageRequest.builder()
                .prompt("make the circle pulse")
                .model("gpt-image-2.6-flare")
                .outputFormat("gif")
                .frameCount(6)
                .frameDurationMs(150)
                .build(), tempPng(), (java.io.File) null);

        String body = capturedBody.get();
        assertTrue(body.contains("name=\"frame_count\""),
                "multipart body is missing form part: frame_count");
        assertTrue(body.contains("name=\"frame_duration_ms\""),
                "multipart body is missing form part: frame_duration_ms");
        assertTrue(body.contains("\r\n\r\n6\r\n"), "frame_count value must be sent");
        assertTrue(body.contains("\r\n\r\n150\r\n"), "frame_duration_ms value must be sent");
    }

    @Test
    public void unsetGifFramingFieldsStayOffTheEditWire() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().editImage(EditImageRequest.builder().prompt("x").build(),
                tempPng(), (java.io.File) null);

        String body = capturedBody.get();
        for (String absent : new String[]{"frame_count", "frame_duration_ms", "complexity"}) {
            assertFalse(body.contains("name=\"" + absent + "\""),
                    "unset field must not be sent: " + absent);
        }
    }

    /**
     * svg.reasoning.effort is a second level of nesting — the one place a
     * serialization mistake would silently drop the whole block.
     */
    @Test
    public void createImageSendsSvgReasoningEffort() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().createImage(CreateImageRequest.builder()
                .prompt("a flat icon of a bicycle")
                .model("gpt-image-2.6-flare")
                .outputFormat("svg")
                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.of("gpt-6.1-terra", "low"))
                .build());

        com.fasterxml.jackson.databind.JsonNode sent =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());
        assertEquals("gpt-6.1-terra", sent.get("svg").get("model").asText());
        assertTrue(sent.get("svg").get("reasoning").isObject(),
                "svg.reasoning must serialize as a nested object");
        assertEquals("low", sent.get("svg").get("reasoning").get("effort").asText());
    }

    @Test
    public void svgOptionsWithoutReasoningOmitsTheBlock() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().createImage(CreateImageRequest.builder()
                .prompt("x").outputFormat("svg")
                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model("gpt-6.1-terra"))
                .build());

        com.fasterxml.jackson.databind.JsonNode sent =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());
        assertNull(sent.get("svg").get("reasoning"),
                "an unset reasoning block must not be sent as null");
    }

    // ------------------------------------------------ 客户端前置校验 (2.2.0)

    /**
     * The SDK refuses only these shapes. Everything else — gif with streaming,
     * svg with n &gt; 1, a transparent jpeg — is left to the server, on purpose:
     * see ImageRequestValidator. The live suite pins those server rules.
     */
    @Test
    public void onlyTheTwoDeliberateRulesAreEnforcedLocally() {
        responseBody = "{\"created\":1,\"data\":[]}";
        OpenaiClient c = client();

        // Kept: a missing svg.model costs a round trip to discover.
        assertRefused(() -> c.createImage(CreateImageRequest.builder().prompt("x")
                .outputFormat("svg").build()));

        // Dropped on purpose — these must now reach the wire and let the
        // server answer, so the SDK cannot outlive a relaxed upstream rule.
        for (CreateImageRequest allowed : new CreateImageRequest[]{
                CreateImageRequest.builder().prompt("x").outputFormat("gif")
                        .outputCompression(80).build(),
                CreateImageRequest.builder().prompt("x").outputFormat("jpeg")
                        .background("transparent").build(),
                CreateImageRequest.builder().prompt("x").outputFormat("svg")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model("m"))
                        .n(2).build()}) {
            capturedBody.set(null);
            c.createImage(allowed);
            assertNotNull(capturedBody.get(),
                    "the SDK must not block this — the server decides");
        }
    }

    /**
     * A field that is set and silently never sent is the failure mode that hid
     * moderation from 1.9.17 to 1.11.0, so each transport refuses the fields
     * it physically cannot carry.
     */
    @Test
    public void fieldsTheTransportCannotCarryAreRefused() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";
        OpenaiClient c = client();
        java.io.File png = tempPng();
        me.vacuity.ai.sdk.openai.image.entity.ImageReference ref =
                me.vacuity.ai.sdk.openai.image.entity.ImageReference.fileId("file-abc");

        // multipart cannot send references
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x")
                .images(java.util.Collections.singletonList(ref)).build(),
                png, (java.io.File) null));
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x")
                .maskReference(ref).build(), png, (java.io.File) null));
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x")
                .markupReference(ref).build(), png, (java.io.File) null));

        // the JSON form cannot send files, and needs at least one input
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x").build()));
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x")
                .images(java.util.Collections.singletonList(ref))
                .markup(png).build()));

        // mask and markup stay mutually exclusive on both transports
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x")
                .markup(png).build(), png, png));
        assertRefused(() -> c.editImage(EditImageRequest.builder().prompt("x")
                .images(java.util.Collections.singletonList(ref))
                .markupReference(ref).maskReference(ref).build()));

        assertNull(capturedBody.get(), "no refused request may reach the wire");
    }

    // ------------------------------------------------- JSON 形式的 edits (2.2.0)

    @Test
    public void editImageByReferenceSendsTheJsonShape() throws Exception {
        responseBody = "{\"created\":1,\"output_format\":\"png\","
                + "\"data\":[{\"b64_json\":\"aGk=\",\"generation_id\":\"gen_1\"}]}";

        ImageResponse response = client().editImage(EditImageRequest.builder()
                .model("gpt-image-2.6-sunburst")
                .prompt("make the mug red")
                .images(java.util.Arrays.asList(
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference.fileId("file-abc"),
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference
                                .imageUrl("https://example.invalid/a.png")))
                .markupReference(me.vacuity.ai.sdk.openai.image.entity.ImageReference
                        .imageUrl("data:image/png;base64,aGk="))
                .complexity("low")
                .quality("low")
                .size("1024x1024")
                .build());

        com.fasterxml.jackson.databind.JsonNode sent =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());
        assertEquals(2, sent.get("images").size());
        assertEquals("file-abc", sent.get("images").get(0).get("file_id").asText());
        assertEquals("https://example.invalid/a.png",
                sent.get("images").get(1).get("image_url").asText());
        assertNull(sent.get("images").get(0).get("image_url"),
                "an unset half of a reference must not be sent as null");
        // markupReference must land on the wire as "markup", not its Java name
        assertNotNull(sent.get("markup"), "markupReference must serialize as \"markup\"");
        assertEquals("data:image/png;base64,aGk=", sent.get("markup").get("image_url").asText());
        assertNull(sent.get("markupReference"), "the Java field name must not reach the wire");
        assertEquals("low", sent.get("complexity").asText());
        assertEquals("gen_1", response.getData().get(0).getGenerationId());
    }

    @Test
    public void maskReferenceSerializesAsMask() throws Exception {
        responseBody = "{\"created\":1,\"data\":[]}";

        client().editImage(EditImageRequest.builder()
                .prompt("x")
                .images(java.util.Collections.singletonList(
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference.fileId("file-abc")))
                .maskReference(me.vacuity.ai.sdk.openai.image.entity.ImageReference
                        .fileId("file-mask"))
                .build());

        com.fasterxml.jackson.databind.JsonNode sent =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(capturedBody.get());
        assertEquals("file-mask", sent.get("mask").get("file_id").asText());
        assertNull(sent.get("maskReference"), "the Java field name must not reach the wire");
    }

    @Test
    public void streamEditImageByReferenceParsesEditEvents() {
        responseContentType = "text/event-stream";
        responseBody = "data: {\"type\":\"image_edit.partial_image\",\"b64_json\":\"cDA=\","
                + "\"partial_image_index\":0}\n\n"
                + "data: {\"type\":\"image_edit.completed\",\"b64_json\":\"ZG9uZQ==\","
                + "\"usage\":{\"total_tokens\":7}}\n\n";

        java.util.List<ImageStreamEvent> events = new java.util.ArrayList<>();
        client().streamEditImage(EditImageRequest.builder()
                .prompt("x")
                .images(java.util.Collections.singletonList(
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference.fileId("file-abc")))
                .partialImages(1)
                .build()).blockingSubscribe(events::add);

        assertEquals(2, events.size());
        assertEquals(ImageStreamEventConstant.EDIT_PARTIAL_IMAGE, events.get(0).getType());
        assertEquals(0, events.get(0).getPartialImageIndex());
        assertTrue(events.get(1).isCompleted(),
                "image_edit.completed must count as completed");
        assertEquals(7, events.get(1).getUsage().getTotalTokens());
    }

    private static void assertRefused(org.junit.jupiter.api.function.Executable call) {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, call);
    }
}
