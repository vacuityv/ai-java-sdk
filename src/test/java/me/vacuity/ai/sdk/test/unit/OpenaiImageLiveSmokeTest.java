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
        ImageResponse response = client().createImage(CreateImageRequest.builder()
                .model("gpt-image-2.6-flare")
                .prompt("a flat icon of a bicycle")
                .outputFormat("svg")
                .svg(me.vacuity.ai.sdk.openai.image.entity.SvgOptions.model(SVG_MODEL))
                .quality("low")
                .n(1)
                .build());

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
     * Omitting svg.model is the one failure mode worth pinning: the request is
     * otherwise valid, so a silently dropped nested object would look like a
     * server problem rather than an SDK one.
     */
    @Test
    public void svgWithoutAnAuthoringModelIsRejectedByTheServer() {
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().createImage(CreateImageRequest.builder()
                                .model("gpt-image-2.6-flare")
                                .prompt("a flat icon of a bicycle")
                                .outputFormat("svg")
                                .quality("low")
                                .n(1)
                                .build()));

        assertEquals("missing_required_parameter", e.getCode());
        assertTrue(e.getMessage().contains("svg.model"), "server must name svg.model: "
                + e.getMessage());
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
}
