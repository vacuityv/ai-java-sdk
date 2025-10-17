package me.vacuity.ai.sdk.test.gemini;

import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.video.constant.VeoVideoConstant;
import me.vacuity.ai.sdk.gemini.video.request.VeoVideoRequest;
import me.vacuity.ai.sdk.gemini.video.response.VeoVideoResponse;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @description: Test class for Gemini Veo video generation
 * @author: vacuity
 * @create: 2025-10-16
 **/
public class VeoVideoTest {

//    public static final String MODEL = "veo-3.1-fast-generate-preview";
    public static final String MODEL = "veo-3.1-generate-preview";

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private GeminiClient client;

    @BeforeEach
    public void setUp() {
        // Create client with longer timeout for video generation
        client = new GeminiClient(API_KEY, Duration.ofSeconds(120));
    }

    /**
     * Test text-to-video generation with basic prompt
     */
    @Test
    public void testGenerateVideoWithTextPrompt() {
        System.out.println("========== Test 1: Text-to-Video Generation ==========");

        // Build request
        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("一匹马在海边奔跑")
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(5)
                .sampleCount(1)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        // Execute request
        VeoVideoResponse response = client.generateVideo(MODEL, request);

        // Verify response
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getName(), "Operation name should not be null");

        System.out.println("Operation Name: " + response.getName());
    }

    /**
     * Test video generation with image input (image-to-video)
     */
    @Test
    public void testGenerateVideoWithImage() throws IOException {
        System.out.println("========== Test 2: Image-to-Video Generation ==========");

        // Load and encode image (replace with actual image path)
        String imagePath = "src/test/resources/test_image.jpg";
        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(Paths.get(imagePath));
        } catch (IOException e) {
            System.out.println("Test image not found, skipping test: " + e.getMessage());
            return;
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Build media object
        VeoVideoRequest.Media image = VeoVideoRequest.Media.builder()
                .bytesBase64Encoded(base64Image)
                .mimeType("image/jpeg")
                .build();

        // Build request
        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("Animate this image with gentle motion and natural lighting changes")
                .image(image)
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(5)
                .sampleCount(1)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        // Execute request
        VeoVideoResponse response = client.generateVideo(MODEL, request);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getName());
        System.out.println("Image-to-Video Operation Name: " + response.getName());
    }

    /**
     * Test video generation with advanced parameters (Veo 3)
     */
    @Test
    public void testGenerateVideoWithAdvancedParameters() {
        System.out.println("========== Test 3: Advanced Parameters (Veo 3) ==========");

        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("A futuristic city with flying cars and neon lights at night")
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_9_16)
                .resolution(VeoVideoConstant.Resolution.RES_1080P)
                .durationSeconds(6)
                .sampleCount(2)
                .enhancePrompt(true)
                .generateAudio(true)
                .seed(12345)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        // Execute with Veo 3 model
        VeoVideoResponse response = client.generateVideo(MODEL, request);

        // Verify response
        assertNotNull(response);
        assertNotNull(response.getName());
        System.out.println("Veo 3 Operation Name: " + response.getName());
        System.out.println("Using enhanced parameters: resolution=1080p, audio=enabled");
    }

    /**
     * Test fetching video operation status
     */
    @Test
    public void testGetVideoOperation() {
        System.out.println("========== Test 4: Get Video Operation Status ==========");

        // First, create a video generation request
        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("A peaceful garden with blooming flowers and butterflies")
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(5)
                .sampleCount(1)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        // Generate video
        VeoVideoResponse initialResponse = client.generateVideo(MODEL, request);
        assertNotNull(initialResponse);
        assertNotNull(initialResponse.getName());

        String operationName = initialResponse.getName();
        System.out.println("Initial Operation Name: " + operationName);
        System.out.println("Initial Status - Done: " + initialResponse.getDone());

        // Check operation status
        VeoVideoResponse statusResponse = client.fetchVideoOperation(operationName);

        // Verify status response
        assertNotNull(statusResponse);
        assertNotNull(statusResponse.getDone());
        assertEquals(operationName, statusResponse.getName());

        System.out.println("Current Status - Done: " + statusResponse.getDone());

        if (statusResponse.getDone()) {
            assertNotNull(statusResponse.getResponse());
            System.out.println("Video generation completed!");

            if (statusResponse.getResponse().getGenerateVideoResponse() != null &&
                statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples() != null &&
                !statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples().isEmpty()) {
                System.out.println("Number of videos: " + statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples().size());
                System.out.println("Video URI: " + statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples().get(0).getVideo().getUri());
            }
        } else {
            System.out.println("Video generation still in progress...");
        }
    }

    /**
     * Test polling for video completion
     */
    @Test
    public void testPollVideoCompletion() throws InterruptedException, IOException {
        System.out.println("========== Test 5: Poll for Video Completion ==========");

        String imagePath = "/Users/vacuity/Downloads/720.jpg";
        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(Paths.get(imagePath));
        } catch (IOException e) {
            System.out.println("Reference image not found, skipping test: " + e.getMessage());
            return;
        }


        String base64RefImage = Base64.getEncoder().encodeToString(imageBytes);
        VeoVideoRequest.Media image = VeoVideoRequest.Media.builder()
                .bytesBase64Encoded(base64RefImage)
                .mimeType("image/jpeg")
                .build();

        
        List<String> paths = Arrays.asList("/Users/vacuity/Downloads/1.jpg", "/Users/vacuity/Downloads/3.jpg", "/Users/vacuity/Downloads/3.jpg");
        List<VeoVideoRequest.ReferenceImage> referenceImages = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            byte[] imageBytes2;
            try {
                imageBytes2 = Files.readAllBytes(Paths.get(paths.get(i)));
            } catch (IOException e) {
                System.out.println("Reference image not found, skipping test: " + e.getMessage());
                return;
            }


            String base64RefImage2 = Base64.getEncoder().encodeToString(imageBytes2);
            VeoVideoRequest.Media image2 = VeoVideoRequest.Media.builder()
                    .bytesBase64Encoded(base64RefImage2)
                    .mimeType("image/jpeg")
                    .build();
            VeoVideoRequest.ReferenceImage referenceImage = VeoVideoRequest.ReferenceImage.builder()
                    .image(image2)
                    .referenceType("asset")
                    .build();
            referenceImages.add(referenceImage);
        }

        // Create video generation request
        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("根据图片生成一段影视片段，要求电影级的效果")
                .image(image)
                .referenceImages(referenceImages)
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(8)
                .sampleCount(1)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        // Generate video
        VeoVideoResponse initialResponse = client.generateVideo(MODEL, request);
        String operationName = initialResponse.getName();

        System.out.println("Started video generation: " + operationName);

        // Poll for completion (max 5 attempts with 10 second intervals)
        int maxAttempts = 20;
        int pollIntervalSeconds = 10;
        boolean completed = false;

        for (int i = 0; i < maxAttempts; i++) {
            System.out.println("Polling attempt " + (i + 1) + "/" + maxAttempts);

            VeoVideoResponse statusResponse = client.fetchVideoOperation(operationName);

            if (statusResponse.getDone() != null && statusResponse.getDone()) {
                completed = true;
                System.out.println("Video generation completed!");

                if (statusResponse.getResponse() != null &&
                    statusResponse.getResponse().getGenerateVideoResponse() != null &&
                    statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples() != null) {
                    int videoCount = statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples().size();
                    System.out.println("Successfully generated " + videoCount + " video(s)");

                    // Print video URIs and download
                    for (int j = 0; j < videoCount; j++) {
                        String videoUri = statusResponse.getResponse().getGenerateVideoResponse()
                            .getGeneratedSamples().get(j).getVideo().getUri();
                        System.out.println("Video " + (j + 1) + " URI: " + videoUri);

                        // Download the video
                        download(videoUri);
                    }
                }
                break;
            } else {
                System.out.println("Still processing... waiting " + pollIntervalSeconds + " seconds");
                if (i < maxAttempts - 1) {
                    Thread.sleep(pollIntervalSeconds * 1000L);
                }
            }
        }

        if (!completed) {
            System.out.println("Video generation did not complete within polling window");
            System.out.println("Note: This is normal for video generation which can take several minutes");
        }
    }

    /**
     * Test video generation with reference images
     */
    @Test
    public void testGenerateVideoWithReferenceImages() throws IOException {
        System.out.println("========== Test 6: Video with Reference Images ==========");

        // Load reference image
        String refImagePath = "src/test/resources/reference_style.jpg";
        byte[] refImageBytes;
        try {
            refImageBytes = Files.readAllBytes(Paths.get(refImagePath));
        } catch (IOException e) {
            System.out.println("Reference image not found, skipping test: " + e.getMessage());
            return;
        }

        String base64RefImage = Base64.getEncoder().encodeToString(refImageBytes);

        VeoVideoRequest.Media refMedia = VeoVideoRequest.Media.builder()
                .bytesBase64Encoded(base64RefImage)
                .mimeType("image/jpeg")
                .build();

        VeoVideoRequest.ReferenceImage referenceImage = VeoVideoRequest.ReferenceImage.builder()
                .image(refMedia)
                .referenceType(VeoVideoConstant.ReferenceImageType.STYLE)
                .build();

        // Build request with reference image
        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("A dancing figure in an artistic style")
                .referenceImages(Collections.singletonList(referenceImage))
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(5)
                .sampleCount(1)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        VeoVideoResponse response = client.generateVideo(MODEL, request);

        assertNotNull(response);
        System.out.println("Video with style reference - Operation: " + response.getName());
    }

    /**
     * Test video generation with GCS URI storage
     */
    @Test
    public void testGenerateVideoWithGcsStorage() {
        System.out.println("========== Test 7: Video with GCS Storage ==========");

        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("A waterfall in a tropical rainforest")
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(5)
                .sampleCount(1)
                .storageUri("gs://my-bucket/veo-videos/")
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        VeoVideoResponse response = client.generateVideo(MODEL, request);

        assertNotNull(response);
        System.out.println("Video with GCS storage - Operation: " + response.getName());
        System.out.println("Note: Videos will be stored in Cloud Storage instead of returned as base64");
    }

    /**
     * Test downloading video from URI
     */
    @Test
    public void testDownloadVideo() throws IOException, InterruptedException {
        System.out.println("========== Test 8: Download Video ==========");

        // Generate a video first
        VeoVideoRequest.Instance instance = VeoVideoRequest.Instance.builder()
                .prompt("A cat playing with a ball of yarn")
                .build();

        VeoVideoRequest.Parameters parameters = VeoVideoRequest.Parameters.builder()
                .aspectRatio(VeoVideoConstant.AspectRatio.RATIO_16_9)
                .durationSeconds(4)
                .sampleCount(1)
                .build();

        VeoVideoRequest request = VeoVideoRequest.builder()
                .instances(Collections.singletonList(instance))
                .parameters(parameters)
                .build();

        VeoVideoResponse initialResponse = client.generateVideo(MODEL, request);
        String operationName = initialResponse.getName();
        System.out.println("Video generation started: " + operationName);

        // Poll until complete
        VeoVideoResponse statusResponse = null;
        int maxAttempts = 15;
        for (int i = 0; i < maxAttempts; i++) {
            System.out.println("Checking status... attempt " + (i + 1));
            statusResponse = client.fetchVideoOperation(operationName);

            if (statusResponse.getDone() != null && statusResponse.getDone()) {
                System.out.println("Video generation completed!");
                break;
            }

            if (i < maxAttempts - 1) {
                Thread.sleep(10000); // Wait 10 seconds
            }
        }

        // Download the video
        if (statusResponse != null && statusResponse.getDone() != null && statusResponse.getDone()) {
            assertNotNull(statusResponse.getResponse());
            assertNotNull(statusResponse.getResponse().getGenerateVideoResponse());
            assertNotNull(statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples());
            assertTrue(statusResponse.getResponse().getGenerateVideoResponse().getGeneratedSamples().size() > 0);

            String videoUri = statusResponse.getResponse().getGenerateVideoResponse()
                    .getGeneratedSamples().get(0).getVideo().getUri();
            download(videoUri);
        } else {
            System.out.println("Video generation did not complete within the polling window");
        }
    }

    private void download(String videoUrl) throws IOException {
        String outputPath = "/Users/vacuity/Downloads/veo_" + System.currentTimeMillis() + ".mp4";

        ResponseBody videoContent = client.downloadVeoVideo(videoUrl);
        try (InputStream inputStream = videoContent.byteStream();
             FileOutputStream outputStream = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            System.out.println("Step 3: Video downloaded successfully!");
            System.out.println("File size: " + String.format("%.2f MB", totalBytesRead / (1024.0 * 1024.0)));
            System.out.println("Saved to: " + outputPath);
        }
    }
}
