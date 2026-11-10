package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.image.constant.ImageStreamEventConstant;
import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.request.EditImageRequest;
import me.vacuity.ai.sdk.openai.image.response.ImageResponse;
import me.vacuity.ai.sdk.openai.image.response.ImageStreamEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests that call the real Images API. Skipped unless VAC_OPENAI_KEY is
 * set, so the default {@code mvn test} stays offline and free.
 * <p>
 * These verify what the local-HttpServer tests structurally cannot: that the
 * server actually accepts the fields this SDK sends, and that the streaming
 * event sequence matches what the SDK models. Image generation is billed, so
 * each case uses one small, low-quality image.
 */
@EnabledIfEnvironmentVariable(named = "VAC_OPENAI_KEY", matches = ".+")
public class OpenaiImageLiveSmokeTest {

    /**
     * gpt-image-1 was retired upstream — it now answers "The model
     * 'gpt-image-1' does not exist." These tests pin a current model instead.
     */
    private static final String MODEL = "gpt-image-2.6-sunburst";

    /** The SVG authoring model, a separate model billed at its own rates. */
    private static final String SVG_MODEL = "gpt-6.1-terra";

    private OpenaiClient client() {
        String baseUrl = System.getenv("VAC_OPENAI_BASE_URL");
        String key = System.getenv("VAC_OPENAI_KEY");
        return baseUrl == null || baseUrl.isEmpty()
                ? new OpenaiClient(key, Duration.ofSeconds(300))
                : new OpenaiClient(key, Duration.ofSeconds(300), baseUrl);
    }

    /**
     * A small solid-colour PNG to edit, so the test needs no fixture on disk.
     */
    private File smallPng() throws Exception {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(60, 120, 200));
        g.fillRect(0, 0, 256, 256);
        g.dispose();
        File f = File.createTempFile("edit-src-", ".png");
        f.deleteOnExit();
        ImageIO.write(img, "png", f);
        return f;
    }

    /**
     * Baseline: the blocking path still works end to end.
     */
    @Test
    public void createImageWorks() {
        ImageResponse response = client().createImage(CreateImageRequest.builder()
                .model(MODEL)
                .prompt("a plain blue circle on a white background")
                .n(1)
                .size("1024x1024")
                .quality("low")
                .build());

        assertNotNull(response, "image response must deserialize");
        assertNotNull(response.getData(), "data must be present");
        assertFalse(response.getData().isEmpty(), "at least one image expected");
        assertNotNull(response.getData().get(0).getB64Json(),
                "gpt-image-1 returns base64 image data");
    }

    /**
     * The feature added in this round: does the server actually stream, and does
     * the event sequence match what ImageStreamEvent models?
     */
    @Test
    public void streamCreateImageEmitsPartialsThenCompleted() {
        List<ImageStreamEvent> events = new ArrayList<>();

        client().streamCreateImage(CreateImageRequest.builder()
                        .model(MODEL)
                        .prompt("a plain red square on a white background")
                        .n(1)
                        .size("1024x1024")
                        .quality("low")
                        .partialImages(2)
                        .build())
                .blockingSubscribe(events::add);

        assertFalse(events.isEmpty(), "streaming produced no events at all");

        ImageStreamEvent last = events.get(events.size() - 1);
        assertTrue(last.isCompleted(),
                "the final event should be a completed event; sequence was "
                        + eventTypes(events));
        assertEquals(ImageStreamEventConstant.GENERATION_COMPLETED, last.getType());
        assertNotNull(last.getB64Json(), "the completed event must carry the image");
        assertNotNull(last.getUsage(), "the completed event must carry usage");

        // partial_images is an upper bound, not a promise: the same request
        // repeated returned 1, then 2, then 1 partial, and a generation that
        // finishes quickly can emit none at all. Asserting partials > 0 here
        // makes the test depend on server timing — the deterministic coverage
        // of partial-event parsing lives in OpenaiWireIntegrationTest instead.
        // What is checked live is that the server accepts partial_images and
        // that any partials it does send are well formed.

        // partial indexes must be 0-based and ascending when present
        int expected = 0;
        for (ImageStreamEvent e : events) {
            if (ImageStreamEventConstant.GENERATION_PARTIAL_IMAGE.equals(e.getType())) {
                assertEquals(expected, e.getPartialImageIndex(),
                        "partial_image_index should ascend from 0");
                assertNotNull(e.getB64Json(), "each partial must carry image data");
                expected++;
            }
        }
    }

    /**
     * The three fields wired into the edit multipart this round. Before the fix
     * they existed on the request object and were never sent.
     */
    @Test
    public void editImageAcceptsBackgroundAndOutputFormat() throws Exception {
        ImageResponse response = client().editImage(EditImageRequest.builder()
                        .model(MODEL)
                        .prompt("cut out the shape and make the background transparent")
                        .n(1)
                        .size("1024x1024")
                        .quality("low")
                        .background("transparent")
                        .outputFormat("png")
                        .build(),
                smallPng(), (File) null);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
        assertNotNull(response.getData().get(0).getB64Json());
    }

    private static String eventTypes(List<ImageStreamEvent> events) {
        StringBuilder sb = new StringBuilder();
        for (ImageStreamEvent e : events) {
            sb.append(e.getType()).append(' ');
        }
        return sb.toString().trim();
    }

    // ------------------------------------------------- GPT-Image-2.6 (2.1.0)

    /**
     * An animated GIF really comes back as a GIF, and the new framing fields
     * are accepted together.
     * <p>
     * 512x512 rather than 1024x1024 is deliberate: frames are rendered as one
     * sprite sheet whose longest edge must stay within 3840px, so 4 frames at
     * 1024x1024 is rejected upstream.
     */
    @Test
    public void gifGenerationReturnsAGifAndEchoesTheSettings() {
        ImageResponse response = client().createImage(CreateImageRequest.builder()
                .model(MODEL)
                .prompt("a small waving cartoon cat")
                .outputFormat("gif")
                .complexity("low")
                .frameCount(4)
                .frameDurationMs(150)
                .quality("low")
                .size("512x512")
                .n(1)
                .build());

        assertEquals("gif", response.getOutputFormat(), "server must echo output_format");
        assertEquals("512x512", response.getSize());
        assertNotNull(response.getData().get(0).getGenerationId(), "generation_id must be set");

        byte[] bytes = java.util.Base64.getDecoder()
                .decode(response.getData().get(0).getB64Json());
        assertEquals("GIF89a", new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII),
                "payload must really be a GIF, not a PNG labelled as one");

        assertNotNull(response.getUsage().getOutputTokensDetails());
        assertTrue(response.getUsage().getOutputTokensDetails().getImageTokens() > 0);
    }

    /**
     * SVG comes back as base64-wrapped SVG text, and svg_usage is reported
     * separately from the image usage because the authoring model bills at its
     * own rates.
     */
    @Test
    public void svgGenerationReturnsSvgTextAndSeparateUsage() {
        ImageResponse response = throughGatewayHiccups(
                () -> client().createImage(CreateImageRequest.builder()
                .model("gpt-image-2.6-flare")
                .prompt("a flat icon of a bicycle")
                .outputFormat("svg")
                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model(SVG_MODEL))
                .quality("low")
                .n(1)
                .build()));

        assertEquals("svg", response.getOutputFormat());

        String svg = new String(java.util.Base64.getDecoder()
                .decode(response.getData().get(0).getB64Json()),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(svg.startsWith("<svg"), "payload must be SVG source, was: "
                + svg.substring(0, Math.min(40, svg.length())));

        assertNotNull(response.getSvgUsage(), "svg_usage must come back on an SVG generation");
        assertEquals(SVG_MODEL, response.getSvgUsage().getModel());
        assertTrue(response.getSvgUsage().getTotalTokens() > 0);
        assertTrue(response.getSvgUsage().getTotalTokens() != response.getUsage().getTotalTokens(),
                "svg_usage and usage are separate bills, not the same number");
    }

    /**
     * Omitting svg.model is now refused by the SDK before the request leaves,
     * so this no longer reaches the server.
     * <p>
     * That is the cost of the pre-flight checks added in 2.2.0: this test used
     * to be a live canary that would have told us if the server ever stopped
     * requiring svg.model, and it cannot be that any more. The server rule was
     * confirmed by hand on 2026-11-05 (`missing_required_parameter` on
     * `svg.model`); anyone dropping the guard in ImageRequestValidator has to
     * re-check it against the live API rather than trusting this test.
     */
    @Test
    public void svgWithoutAnAuthoringModelIsRefusedBeforeTheRequest() {
        IllegalArgumentException e = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> client().createImage(CreateImageRequest.builder()
                        .model("gpt-image-2.6-flare")
                        .prompt("a flat icon of a bicycle")
                        .outputFormat("svg")
                        .quality("low")
                        .n(1)
                        .build()));

        assertTrue(e.getMessage().contains("svg.model"),
                "the refusal must name svg.model: " + e.getMessage());
    }

    /**
     * complexity and markup both travel in the hand-built multipart body, so
     * only a live edit proves they arrive.
     */
    @Test
    public void editAcceptsComplexityAndMarkup() throws Exception {
        File source = smallPng();

        ImageResponse response = client().editImage(EditImageRequest.builder()
                .model(MODEL)
                .prompt("paint the circled area bright yellow")
                .complexity("low")
                .markup(annotationPng(256, 256))
                .quality("low")
                .size("1024x1024")
                .safetyIdentifier("sdk-live-test")
                .build(), source, (File) null);

        assertNotNull(response.getData().get(0).getB64Json(), "edit must return image data");
        assertEquals("sdk-live-test", response.getSafetyIdentifier(),
                "safety_identifier must reach the server and be echoed back");
    }

    /**
     * The server treats mask and markup as mutually exclusive; the SDK refuses
     * the combination before spending a request. If upstream ever relaxes this,
     * this test is what will say so.
     */
    @Test
    public void markupWithMaskIsRefusedBeforeTheRequest() throws Exception {
        EditImageRequest request = EditImageRequest.builder()
                .model(MODEL)
                .prompt("x")
                .markup(annotationPng(256, 256))
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> client().editImage(request, smallPng(), smallPng()));
    }

    /**
     * A transparent PNG with a drawn annotation, matching the input image's
     * dimensions as markup requires.
     */
    private File annotationPng(int w, int h) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.setStroke(new java.awt.BasicStroke(6f));
        g.drawOval(w / 4, h / 4, w / 2, h / 2);
        g.dispose();
        File f = File.createTempFile("markup-", ".png");
        f.deleteOnExit();
        ImageIO.write(img, "png", f);
        return f;
    }

    // ------------------------------------------- GIF 编辑 / svg.reasoning (2.2.0)

    /**
     * A GIF edit really returns a GIF. The two framing fields go out in the
     * hand-built multipart body, so only a live call proves they arrive.
     * <p>
     * 512x512 is deliberate and is NOT interchangeable with a PNG edit: a
     * plain edit at that size is refused as "below the current minimum pixel
     * budget", because for GIF the budget is measured on the sprite sheet
     * rather than one frame.
     */
    @Test
    public void gifEditReturnsAGif() throws Exception {
        ImageResponse response = client().editImage(EditImageRequest.builder()
                .model(MODEL)
                .prompt("make the shape pulse")
                .outputFormat("gif")
                .frameCount(4)
                .frameDurationMs(150)
                .quality("low")
                .size("512x512")
                .build(), smallPng(), (File) null);

        assertEquals("gif", response.getOutputFormat(), "server must echo output_format");
        byte[] bytes = java.util.Base64.getDecoder()
                .decode(response.getData().get(0).getB64Json());
        assertEquals("GIF89a", new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII),
                "an edit asking for gif must return a real GIF");
    }

    /**
     * frame_count's range differs between the two endpoints' documentation
     * only in prose, so pin what the server actually enforces on edits.
     */
    @Test
    public void gifEditRejectsAFrameCountAboveSixteen() throws Exception {
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().editImage(EditImageRequest.builder()
                                .model(MODEL).prompt("x")
                                .outputFormat("gif").frameCount(20)
                                .quality("low").size("512x512")
                                .build(), smallPng(), (File) null));

        assertTrue(e.getMessage().contains("2 to 16"),
                "server must state the 2-16 range: " + e.getMessage());
    }

    /**
     * svg.reasoning.effort is not decorative — it changes what the authoring
     * model charges. "low" measured 57 reasoning tokens against 501-1226 with
     * the effort left to the model's default.
     */
    @Test
    public void svgReasoningEffortReachesTheAuthoringModel() {
        ImageResponse response = throughGatewayHiccups(
                () -> client().createImage(CreateImageRequest.builder()
                .model("gpt-image-2.6-flare")
                .prompt("a flat icon of a bicycle")
                .outputFormat("svg")
                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.of(SVG_MODEL, "low"))
                .quality("low")
                .n(1)
                .build()));

        assertEquals(SVG_MODEL, response.getSvgUsage().getModel());
        assertTrue(response.getSvgUsage().getReasoningTokens() > 0,
                "the author model still reports reasoning tokens at effort=low");
        assertTrue(response.getSvgUsage().getReasoningTokens() < 400,
                "effort=low should be far below the default's 500+ reasoning tokens, got "
                        + response.getSvgUsage().getReasoningTokens());
    }

    /**
     * An invalid effort must be the server's complaint, not a silently
     * dropped field.
     */
    @Test
    public void anUnknownSvgReasoningEffortIsRejectedByTheServer() {
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().createImage(CreateImageRequest.builder()
                                .model("gpt-image-2.6-flare").prompt("x")
                                .outputFormat("svg")
                                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions
                                        .of(SVG_MODEL, "bogus"))
                                .n(1).build()));

        assertTrue(e.getMessage().contains("minimal") || e.getMessage().contains("effort"),
                "server must name the effort enum: " + e.getMessage());
    }

    /**
     * SVG generations take 30s+ and this proxy intermittently fails them at
     * the gateway — 502 and 520 have both been seen. Retrying a gateway error
     * is not the same as retrying a failed assertion: the call never reached a
     * verdict, so there is nothing to paper over.
     * <p>
     * The condition is any 5xx rather than a list of codes, because the list
     * was the bug: a 520 from Cloudflare failed the run for a reason that has
     * nothing to do with the SDK. A 4xx is still rethrown at once — that is
     * the server judging the request, which is exactly what these tests are
     * for.
     */
    private <T> T throughGatewayHiccups(java.util.function.Supplier<T> call) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return call.get();
            } catch (retrofit2.adapter.rxjava2.HttpException e) {
                if (e.code() < 500) {
                    throw e;
                }
                last = e;
            }
        }
        throw new AssertionError("gateway kept failing after 3 attempts", last);
    }

    // ---------------------------- 服务端格式规则的活体哨兵 (2.2.0)

    /**
     * The format restrictions the SDK deliberately does NOT mirror.
     * <p>
     * 2.2.0 had client-side copies of all of these and dropped them: a
     * mirrored rule keeps being enforced after upstream relaxes it, and it
     * costs exactly this test. So they live here instead. Every case below is
     * a validation error, rejected before any image is produced, so the whole
     * method is cheap despite being eight live calls.
     * <p>
     * A failure here does not mean the SDK broke. It means the API changed,
     * and that the docs and javadoc need rereading.
     */
    @Test
    public void theServerStillEnforcesTheFormatRestrictions() {
        assertServerRejects("gif + output_compression",
                CreateImageRequest.builder().model(MODEL).prompt("x")
                        .outputFormat("gif").size("512x512").outputCompression(80).build());
        assertServerRejects("gif + partial_images",
                CreateImageRequest.builder().model(MODEL).prompt("x")
                        .outputFormat("gif").size("512x512").partialImages(2).build());
        assertServerRejects("svg + n=2",
                CreateImageRequest.builder().model("gpt-image-2.6-flare").prompt("x")
                        .outputFormat("svg")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model(SVG_MODEL))
                        .n(2).build());
        assertServerRejects("svg + output_compression",
                CreateImageRequest.builder().model("gpt-image-2.6-flare").prompt("x")
                        .outputFormat("svg")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model(SVG_MODEL))
                        .n(1).outputCompression(80).build());
        assertServerRejects("svg 对象配 png",
                CreateImageRequest.builder().model("gpt-image-2.6-flare").prompt("x")
                        .outputFormat("png")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model(SVG_MODEL))
                        .build());
        assertServerRejects("transparent + jpeg",
                CreateImageRequest.builder().model(MODEL).prompt("x")
                        .outputFormat("jpeg").background("transparent").build());
    }

    @Test
    public void theServerStillRefusesGifWithAMask() throws Exception {
        File png = smallPng();

        // "'mask' is not supported for GIF output."
        org.junit.jupiter.api.Assertions.assertThrows(
                me.vacuity.ai.sdk.common.VacSdkException.class,
                () -> client().editImage(EditImageRequest.builder().model(MODEL).prompt("x")
                        .outputFormat("gif").size("512x512").build(), png, png));
    }

    /**
     * SVG edits used to be refused outright — "Invalid value: 'svg'. Supported
     * values are: 'png', 'webp', and 'jpeg'." Between 2026-11-05 and
     * 2026-11-10 that changed, and this suite is what noticed: the old canary
     * started failing because the server had moved on to complaining about a
     * missing svg.model instead.
     * <p>
     * This is the case for keeping such rules on the server rather than
     * mirroring them in the SDK. A mirrored "svg edits are unsupported" check
     * would still be refusing this call today.
     */
    @Test
    public void svgEditsAreSupportedNowAndStillNeedAnAuthoringModel() throws Exception {
        File png = smallPng();

        ImageResponse response = throughGatewayHiccups(
                () -> client().editImage(EditImageRequest.builder()
                        .model("gpt-image-2.6-flare")
                        .prompt("redraw this as a flat vector icon")
                        .outputFormat("svg")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions
                                .of(SVG_MODEL, "low"))
                        .quality("low")
                        .n(1)
                        .build(), png, (File) null));

        assertEquals("svg", response.getOutputFormat());
        String svg = new String(java.util.Base64.getDecoder()
                .decode(response.getData().get(0).getB64Json()),
                java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(svg.startsWith("<svg"), "an svg edit must return SVG source, was: "
                + svg.substring(0, Math.min(40, svg.length())));
        assertNotNull(response.getSvgUsage(), "svg_usage must come back on an svg edit");
        assertEquals(SVG_MODEL, response.getSvgUsage().getModel());

        // the SDK refuses this one locally rather than spending the request
        IllegalArgumentException e = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> client().editImage(EditImageRequest.builder().model(MODEL)
                        .prompt("x").outputFormat("svg").build(), png, (File) null));
        assertTrue(e.getMessage().contains("svg.model"));
    }

    /** n > 1 and streaming are still refused for svg, on edits too. */
    @Test
    public void svgEditsStillRefuseMultipleImagesAndStreaming() throws Exception {
        File png = smallPng();

        org.junit.jupiter.api.Assertions.assertThrows(
                me.vacuity.ai.sdk.common.VacSdkException.class,
                () -> client().editImage(EditImageRequest.builder()
                        .model("gpt-image-2.6-flare").prompt("x").outputFormat("svg")
                        .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model(SVG_MODEL))
                        .n(2).quality("low").build(), png, (File) null),
                "svg still requires n=1");
    }

    private void assertServerRejects(String label, CreateImageRequest request) {
        org.junit.jupiter.api.Assertions.assertThrows(
                me.vacuity.ai.sdk.common.VacSdkException.class,
                () -> client().createImage(request),
                "the server should still reject " + label
                        + " — if it now accepts it, drop the matching javadoc warning");
    }

    // ------------------------------------------- JSON 形式的 edits (2.2.0)

    /**
     * The JSON form is the only way to edit an image the API already holds.
     * Uploading once and editing by id is the point of it, so the test does
     * exactly that.
     */
    @Test
    public void editImageByFileIdWorks() throws Exception {
        String fileId = uploadForVision(smallPng());

        ImageResponse response = client().editImage(EditImageRequest.builder()
                .model(MODEL)
                .prompt("make the square bright red")
                .images(java.util.Collections.singletonList(
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference.fileId(fileId)))
                .quality("low")
                .size("1024x1024")
                .build());

        assertNotNull(response.getData().get(0).getB64Json(),
                "an edit by file_id must return image data");
        assertNotNull(response.getData().get(0).getGenerationId());
    }

    /**
     * A data URL rather than an upload, plus a markup layer sent the same way.
     */
    @Test
    public void editImageByDataUrlWithMarkupReferenceWorks() throws Exception {
        String source = dataUrl(smallPng());
        String markup = dataUrl(annotationPng(256, 256));

        ImageResponse response = client().editImage(EditImageRequest.builder()
                .model(MODEL)
                .prompt("paint the circled area bright yellow")
                .images(java.util.Collections.singletonList(
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference.imageUrl(source)))
                .markupReference(
                        me.vacuity.ai.sdk.openai.image.entity.ImageReference.imageUrl(markup))
                .quality("low")
                .size("1024x1024")
                .build());

        assertNotNull(response.getData().get(0).getB64Json());
    }

    /**
     * Streaming the JSON form, which emits image_edit.* rather than
     * image_generation.* events.
     */
    @Test
    public void streamEditImageByReferenceEmitsEditEvents() throws Exception {
        String fileId = uploadForVision(smallPng());
        List<ImageStreamEvent> events = new ArrayList<>();

        client().streamEditImage(EditImageRequest.builder()
                        .model(MODEL)
                        .prompt("make it green")
                        .images(java.util.Collections.singletonList(
                                me.vacuity.ai.sdk.openai.image.entity.ImageReference
                                        .fileId(fileId)))
                        .quality("low")
                        .size("1024x1024")
                        .partialImages(1)
                        .build())
                .blockingSubscribe(events::add);

        assertFalse(events.isEmpty(), "streaming produced no events");
        ImageStreamEvent last = events.get(events.size() - 1);
        assertTrue(last.isCompleted(),
                "the final event should be completed; sequence was " + eventTypes(events));
        assertEquals(ImageStreamEventConstant.EDIT_COMPLETED, last.getType(),
                "edits emit image_edit.*, not image_generation.*");
        assertNotNull(last.getB64Json());
    }

    private String uploadForVision(File file) {
        me.vacuity.ai.sdk.openai.file.entity.OpenaiFile uploaded =
                client().uploadFile("vision", file.getAbsolutePath());
        assertNotNull(uploaded.getId(), "the file upload must return an id");
        return uploaded.getId();
    }

    private static String dataUrl(File png) throws Exception {
        byte[] bytes = java.nio.file.Files.readAllBytes(png.toPath());
        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(bytes);
    }
}
