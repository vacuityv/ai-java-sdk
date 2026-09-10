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

    private static final String MODEL = "gpt-image-1";

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
                "the final event should be a completed event, got: " + last.getType());
        assertEquals(ImageStreamEventConstant.GENERATION_COMPLETED, last.getType());
        assertNotNull(last.getB64Json(), "the completed event must carry the image");
        assertNotNull(last.getUsage(), "the completed event must carry usage");

        long partials = events.stream()
                .filter(e -> ImageStreamEventConstant.GENERATION_PARTIAL_IMAGE.equals(e.getType()))
                .count();
        assertTrue(partials > 0,
                "asked for partial_images=2 but received none; event types were "
                        + eventTypes(events));

        // partial indexes must be 0-based and ascending
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
}
