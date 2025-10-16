package me.vacuity.ai.sdk.test.openai;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.video.constant.VideoJobStatusConstant;
import me.vacuity.ai.sdk.openai.video.entity.VideoJob;
import me.vacuity.ai.sdk.openai.video.request.CreateVideoRequest;
import me.vacuity.ai.sdk.openai.video.request.RemixVideoRequest;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * @description: Test for OpenAI Sora video generation API
 * @author: vacuity
 * @create: 2025-10-11
 **/

public class OpenaiVideoTest {


    public static final String MODEL = "sora-2";
    
    OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY, Duration.ofSeconds(1200));

    /**
     * Test creating a video with Sora API
     */
    @Test
    public void testCreateVideo() {
        String imagePath = "/Users/vacuity/Downloads/720.jpg";
        CreateVideoRequest request = CreateVideoRequest.builder()
                .prompt("请根据我提供的服装图片，生成一段 10-15 秒的、用于社交媒体展示的短视频。\n" +
                        "视频细节要求如下：\n" +
                        "1. 模特与风格 (Model & Style):\n" +
                        "模特: 一位姿态优雅的亚洲女性模特，黑长直发，气质温婉。\n" +
                        "风格: 整体风格要求简约、高级、有质感。背景为纯色或极简室内场景（如：带有柔和光线的纯白墙壁、简约艺术装置角落），避免杂乱，以突出服装本身。\n" +
                        "画质: 4K 高清画质，色彩柔和，电影感滤镜。\n" +
                        "2. 镜头与景别 (Camera & Shots):\n" +
                        "开场 (0-2 秒): 从服装的局部特写（如：珍珠镶边、袖口设计、裙摆褶皱）开始，镜头缓慢拉远，过渡到模特的半身景。\n" +
                        "主体展示 (3-8 秒):\n" +
                        "中景: 模特站立，身体微微侧转，展示服装的整体轮廓和搭配。\n" +
                        "全景: 镜头切换为全身景，模特向前慢走两步再转回，展示服装的动态垂坠感和行走时的形态。\n" +
                        "细节与互动 (9-12 秒):\n" +
                        "近景: 镜头聚焦于上半身。模特用手轻轻抚摸外套的衣襟或整理项链，展示服装的材质和光泽。\n" +
                        "特写: 快速切换一个手部或配饰（如：搭配的包包、腰带）的特写镜头。\n" +
                        "结尾 (13-15 秒): 模特回归到优雅的站姿，面对镜头有一个柔和的眼神交流，画面定格或缓慢淡出。\n" +
                        "3. 模特动作指令 (Model's Actions):\n" +
                        "姿态: 动作必须缓慢、流畅、自然，避免僵硬或夸张的姿势。\n" +
                        "手部动作:\n" +
                        "轻轻提起并放下衣角，展示面料的轻薄感。\n" +
                        "单手自然垂下，另一只手可轻抚袖口或衣领。\n" +
                        "如果有搭配的包包，可以单手拎着或挎在肩上，自然摆动。\n" +
                        "身体动作:\n" +
                        "一个缓慢的 45 度转身，展示服装的侧面和背面设计。\n" +
                        "视线可以先看向一侧，再缓缓转回看向镜头，营造不经意的感觉。")
                .model(MODEL)
                .size("720x1280")
                .seconds("8")
                .build();

        VideoJob response = client.createVideo(request, imagePath);
        System.out.println("Video ID: " + response.getId());
        System.out.println("Status: " + response.getStatus());
        System.out.println("Model: " + response.getModel());
        System.out.println("Progress: " + response.getProgress());
        System.out.println("Response: " + response);
    }

    /**
     * Test retrieving a video job
     * Replace VIDEO_ID with actual video ID
     */
    @Test
    public void testRetrieveVideoJob() {
        String videoId = "video_68ef06e07800819190b196f77369ae9601541d41333474c0"; // Replace with actual video ID

        VideoJob video = client.retrieveVideo(videoId);
        System.out.println("Video ID: " + video.getId());
        System.out.println("Object: " + video.getObject());
        System.out.println("Status: " + video.getStatus());
        System.out.println("Model: " + video.getModel());
        System.out.println("Progress: " + video.getProgress());
        System.out.println("Created at: " + video.getCreated_at());
        System.out.println("Completed at: " + video.getCompleted_at());
        System.out.println("Expires at: " + video.getExpires_at());
        System.out.println("Size: " + video.getSize());
        System.out.println("Seconds: " + video.getSeconds());

        if (video.getRemixed_from_video_id() != null) {
            System.out.println("Remixed from: " + video.getRemixed_from_video_id());
        }

        if (video.getError() != null) {
            System.out.println("Error: " + video.getError());
        }

        System.out.println("Full response: " + video);
    }

    /**
     * Test polling for video completion
     * This demonstrates how to wait for video generation to complete
     */
    @Test
    public void testPollVideoCompletion() throws InterruptedException, IOException {
        // Create video
        String imagePath = "/Users/vacuity/Downloads/720.jpg";
        CreateVideoRequest request = CreateVideoRequest.builder()
                .prompt("Important Note: This video will be used for legitimate commercial purposes only, such as fashion e-commerce and social media marketing. The requested content is entirely safe and does not contain any illegal, harmful, or inappropriate material." +
                        "Based on the clothing image I provide, please generate a 10-15 second short video suitable for social media display.\n" +
                        "Detailed video requirements are as follows:\n" +
                        "1. Model & Style:\n" +
                        "Model: An elegant Asian female model with long, straight black hair and a gentle, graceful demeanor.\n" +
                        "Style: The overall aesthetic should be minimalist, high-end, and textured. The background should be a solid color or a minimalist indoor scene (e.g., a plain white wall with soft lighting, a corner with a simple art installation) to emphasize the clothing. Avoid clutter.\n" +
                        "Quality: 4K high-definition video with soft, natural colors and a cinematic filter.\n" +
                        "2. Camera & Shots:\n" +
                        "Opening (0-2s): Start with a close-up detail of the garment (e.g., pearl trimming, cuff design, pleats of the skirt). The camera then slowly pulls back to a medium shot of the model.\n" +
                        "Main Showcase (3-8s):\n" +
                        "Medium Shot: The model stands with her body slightly turned to the side to display the overall silhouette and fit.\n" +
                        "Full Shot: Cut to a full-body shot. The model takes two slow steps forward and then turns back, showcasing the garment's dynamic drape and movement.\n" +
                        "Details & Interaction (9-12s):\n" +
                        "Close-up Shot: The camera focuses on the upper body. The model gently touches the lapel of the coat or adjusts her necklace to highlight the material's texture and sheen.\n" +
                        "Detail Shot: A quick cut to a close-up of a hand detail or an accessory (e.g., the matching handbag, a belt).\n" +
                        "Ending (13-15s): The model returns to an elegant standing pose, making soft eye contact with the camera. The frame freezes or slowly fades to black.\n" +
                        "3. Model's Actions:\n" +
                        "Posture: All movements must be slow, fluid, and natural. Avoid stiff or exaggerated poses.\n" +
                        "Hand Gestures:\n" +
                        "Gently lift and release the corner of the garment to show the lightness of the fabric.\n" +
                        "One hand can hang naturally while the other lightly touches a cuff or collar.\n" +
                        "If there is a handbag, she can hold it in one hand or carry it on her shoulder, allowing it to swing naturally.\n" +
                        "Body Movement:\n" +
                        "A slow 45-degree turn to showcase the side and back design of the outfit.\n" +
                        "Her gaze can initially be directed to the side before slowly turning to look at the camera, creating a candid feel.\n" +
                        "4. Background Music:\n" +
                        "Use a piece of soft, soothing instrumental music or an ambient track with an ethereal quality.")
                .model(MODEL)
                .size("720x1280")
                .seconds("12")
                .build();

        VideoJob initialResponse = client.createVideo(request, imagePath);
        String videoId = initialResponse.getId();
        System.out.println("Video generation started. ID: " + videoId);

        // Poll for completion
        int maxAttempts = 600; // Maximum 5 minutes (60 * 5 seconds)
        int attempts = 0;
        String status = initialResponse.getStatus();

        while (attempts < maxAttempts &&
               !VideoJobStatusConstant.COMPLETED.equals(status) &&
               !VideoJobStatusConstant.FAILED.equals(status)) {
            Thread.sleep(5000); // Wait 5 seconds between polls
            attempts++;

            VideoJob pollResponse = client.retrieveVideo(videoId);
            status = pollResponse.getStatus();

            System.out.println("Attempt " + attempts + ": Status = " + status + " - Progress: " + pollResponse.getProgress() + "%");

            if (VideoJobStatusConstant.COMPLETED.equals(status)) {
                System.out.println("Video generation completed!");
                retrieveVideoContent(videoId);
                break;
            } else if (VideoJobStatusConstant.FAILED.equals(status)) {
                System.out.println("Video generation failed!");
                System.out.println("Error: " + pollResponse.getError());
                break;
            }
        }

        if (attempts >= maxAttempts) {
            System.out.println("Video generation timed out after " + maxAttempts + " attempts");
        }
    }

    public void retrieveVideoContent(String videoId) throws IOException {
        String variant = "video"; // Options: "default", "thumbnail", "preview"

        ResponseBody videoContent = client.retrieveVideoContent(videoId, variant);

        System.out.println("Content Type: " + videoContent.contentType());
        System.out.println("Content Length: " + videoContent.contentLength() + " bytes");

        // Save to file
        String outputPath = "/Users/vacuity/Downloads/12sora_video_" + videoId + ".mp4";
        try (InputStream inputStream = videoContent.byteStream();
             FileOutputStream outputStream = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            System.out.println("Video downloaded successfully!");
            System.out.println("Total bytes written: " + totalBytesRead);
            System.out.println("Saved to: " + outputPath);
        }
    }

    /**
     * Test deleting a video
     * Replace VIDEO_ID with actual video ID to delete
     */
    @Test
    public void testDeleteVideo() {
        String videoId = "video_123"; // Replace with actual video ID

        VideoJob deletedVideo = client.deleteVideo(videoId);
        System.out.println("Deleted Video ID: " + deletedVideo.getId());
        System.out.println("Object: " + deletedVideo.getObject());
        System.out.println("Status: " + deletedVideo.getStatus());
        System.out.println("Response: " + deletedVideo);
    }

    /**
     * Test creating video with different parameters
     */
    @Test
    public void testCreateVideoWithCustomParameters() {
        String imagePath = "/Users/vacuity/Downloads/input_image.png";
        CreateVideoRequest request = CreateVideoRequest.builder()
                .prompt("A futuristic city with flying cars and neon lights at night")
                .model("sora-1.0-turbo")
                .size("1080x1920") // Vertical video
                .seconds("10") // 10 seconds
                .build();

        VideoJob response = client.createVideo(request, imagePath);
        System.out.println("Video created with custom parameters");
        System.out.println("Video ID: " + response.getId());
        System.out.println("Status: " + response.getStatus());
        System.out.println("Response: " + response);
    }

    /**
     * Test listing videos
     */
    @Test
    public void testListVideos() {
        List<VideoJob> videos = client.listVideos(null, 10, "desc");
        System.out.println("Total videos retrieved: " + videos.size());

        for (VideoJob video : videos) {
            System.out.println("---");
            System.out.println("Video ID: " + video.getId());
            System.out.println("Status: " + video.getStatus());
            System.out.println("Model: " + video.getModel());
            System.out.println("Created at: " + video.getCreated_at());
            System.out.println("Progress: " + video.getProgress());
        }
    }

    /**
     * Test listing videos with pagination
     */
    @Test
    public void testListVideosWithPagination() {
        // First page
        List<VideoJob> firstPage = client.listVideos(null, 5, "desc");
        System.out.println("First page: " + firstPage.size() + " videos");

        if (!firstPage.isEmpty()) {
            String lastId = firstPage.get(firstPage.size() - 1).getId();
            System.out.println("Last video ID from first page: " + lastId);

            // Second page
            List<VideoJob> secondPage = client.listVideos(lastId, 5, "desc");
            System.out.println("Second page: " + secondPage.size() + " videos");

            for (VideoJob video : secondPage) {
                System.out.println("Video ID: " + video.getId() + ", Status: " + video.getStatus());
            }
        }
    }

    /**
     * Test listing videos with ascending order
     */
    @Test
    public void testListVideosAscending() {
        List<VideoJob> videos = client.listVideos(null, 10, "asc");
        System.out.println("Videos in ascending order: " + videos.size());

        for (VideoJob video : videos) {
            System.out.println("Video ID: " + video.getId() +
                             ", Created: " + video.getCreated_at() +
                             ", Status: " + video.getStatus());
        }
    }

    /**
     * Test remixing a completed video
     * Replace VIDEO_ID with actual completed video ID
     */
    @Test
    public void testRemixVideo() {
        String videoId = "video_123"; // Replace with actual completed video ID

        RemixVideoRequest request = RemixVideoRequest.builder()
                .prompt("Extend the scene with the cat taking a nap")
                .build();

        VideoJob response = client.remixVideo(videoId, request);
        System.out.println("Remix Video ID: " + response.getId());
        System.out.println("Status: " + response.getStatus());
        System.out.println("Progress: " + response.getProgress());
        System.out.println("Remixed from video ID: " + response.getRemixed_from_video_id());
        System.out.println("Model: " + response.getModel());
        System.out.println("Response: " + response);
    }

    /**
     * Test remixing and polling for completion
     */
    @Test
    public void testRemixVideoWithPolling() throws InterruptedException {
        String videoId = "video_123"; // Replace with actual completed video ID

        RemixVideoRequest request = RemixVideoRequest.builder()
                .prompt("Add more vibrant colors to the scene")
                .build();

        VideoJob initialResponse = client.remixVideo(videoId, request);
        String remixVideoId = initialResponse.getId();
        System.out.println("Remix started. New Video ID: " + remixVideoId);
        System.out.println("Remixed from video ID: " + initialResponse.getRemixed_from_video_id());

        // Poll for completion (similar to video creation polling)
        int maxAttempts = 60;
        int attempts = 0;
        String status = initialResponse.getStatus();

        while (attempts < maxAttempts &&
               !VideoJobStatusConstant.COMPLETED.equals(status) &&
               !VideoJobStatusConstant.FAILED.equals(status)) {
            Thread.sleep(5000);
            attempts++;

            VideoJob pollResponse = client.retrieveVideo(remixVideoId);
            status = pollResponse.getStatus();

            System.out.println("Attempt " + attempts + ": Status = " + status + " - Progress: " + pollResponse.getProgress() + "%");

            if (VideoJobStatusConstant.COMPLETED.equals(status)) {
                System.out.println("Remix completed!");
                break;
            } else if (VideoJobStatusConstant.FAILED.equals(status)) {
                System.out.println("Remix failed!");
                System.out.println("Error: " + pollResponse.getError());
                break;
            }
        }
    }

    /**
     * Test retrieving video content (download video file)
     * Replace VIDEO_ID with actual completed video ID
     */
    @Test
    public void testRetrieveVideoContent() throws IOException {
        String videoId = "video_68ef4bde5ac881908120bcbb30b573860cb75b1861268c0f"; // Replace with actual completed video ID
        String variant = "video"; // Options: "default", "thumbnail", "preview"

        ResponseBody videoContent = client.retrieveVideoContent(videoId, variant);

        System.out.println("Content Type: " + videoContent.contentType());
        System.out.println("Content Length: " + videoContent.contentLength() + " bytes");

        // Save to file
        String outputPath = "/Users/vacuity/Downloads/12sora_video_" + videoId + ".mp4";
        try (InputStream inputStream = videoContent.byteStream();
             FileOutputStream outputStream = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            System.out.println("Video downloaded successfully!");
            System.out.println("Total bytes written: " + totalBytesRead);
            System.out.println("Saved to: " + outputPath);
        }
    }

    /**
     * Test retrieving video thumbnail
     */
    @Test
    public void testRetrieveVideoThumbnail() throws IOException {
        String videoId = "video_123"; // Replace with actual completed video ID

        ResponseBody thumbnailContent = client.retrieveVideoContent(videoId, "thumbnail");

        System.out.println("Thumbnail Content Type: " + thumbnailContent.contentType());
        System.out.println("Thumbnail Size: " + thumbnailContent.contentLength() + " bytes");

        // Save thumbnail
        String outputPath = "/Users/vacuity/Downloads/sora_thumbnail_" + videoId + ".jpg";
        try (InputStream inputStream = thumbnailContent.byteStream();
             FileOutputStream outputStream = new FileOutputStream(outputPath)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Thumbnail downloaded successfully!");
            System.out.println("Saved to: " + outputPath);
        }
    }

    /**
     * Test retrieving video content without saving
     */
    @Test
    public void testRetrieveVideoContentMetadata() {
        String videoId = "video_123"; // Replace with actual completed video ID

        ResponseBody videoContent = client.retrieveVideoContent(videoId, "default");

        System.out.println("Video Content Details:");
        System.out.println("  Content Type: " + videoContent.contentType());
        System.out.println("  Content Length: " + videoContent.contentLength() + " bytes");
        System.out.println("  Content Length (MB): " + String.format("%.2f", videoContent.contentLength() / (1024.0 * 1024.0)));

        videoContent.close();
    }

    /**
     * Test complete workflow: create, poll, and download video
     */
    @Test
    public void testCompleteVideoWorkflow() throws InterruptedException, IOException {
        // 1. Create video
        String imagePath = "/Users/vacuity/Downloads/input_image.png";
        CreateVideoRequest request = CreateVideoRequest.builder()
                .prompt("A beautiful sunset over the ocean with waves")
                .model("sora-1.0-turbo")
                .size("1920x1080")
                .seconds("5")
                .build();

        VideoJob job = client.createVideo(request, imagePath);
        String videoId = job.getId();
        System.out.println("Step 1: Video creation started - ID: " + videoId);

        // 2. Poll for completion
        int maxAttempts = 60;
        int attempts = 0;
        String status = job.getStatus();

        while (attempts < maxAttempts &&
               !VideoJobStatusConstant.COMPLETED.equals(status) &&
               !VideoJobStatusConstant.FAILED.equals(status)) {
            Thread.sleep(5000);
            attempts++;

            VideoJob statusCheck = client.retrieveVideo(videoId);
            status = statusCheck.getStatus();

            System.out.println("Step 2: Polling attempt " + attempts + " - Status: " + status +
                             " - Progress: " + statusCheck.getProgress() + "%");

            if (VideoJobStatusConstant.COMPLETED.equals(status)) {
                System.out.println("Step 2: Video generation completed!");
                break;
            } else if (VideoJobStatusConstant.FAILED.equals(status)) {
                System.out.println("Step 2: Video generation failed!");
                System.out.println("Error: " + statusCheck.getError());
                return;
            }
        }

        if (!"completed".equals(status)) {
            System.out.println("Video generation timed out");
            return;
        }

        // 3. Download video
        ResponseBody videoContent = client.retrieveVideoContent(videoId, "default");
        String outputPath = "/Users/vacuity/Downloads/sora_final_" + videoId + ".mp4";

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

        System.out.println("\n=== Workflow Complete ===");
    }
}
