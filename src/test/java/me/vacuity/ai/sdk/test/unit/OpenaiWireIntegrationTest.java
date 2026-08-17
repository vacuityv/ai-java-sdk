package me.vacuity.ai.sdk.test.unit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.file.entity.OpenaiFile;
import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.response.ImageResponse;
import me.vacuity.ai.sdk.openai.video.entity.VideoJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
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
}
