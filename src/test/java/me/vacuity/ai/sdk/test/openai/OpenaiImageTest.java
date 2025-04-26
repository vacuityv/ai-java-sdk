package me.vacuity.ai.sdk.test.openai;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.image.entity.ImageData;
import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.request.EditImageRequest;
import me.vacuity.ai.sdk.openai.image.request.ImageVariationRequest;
import me.vacuity.ai.sdk.openai.image.response.ImageResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:27
 **/

public class OpenaiImageTest {


    public static final String MODEL = "gpt-image-1";
    
    OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY, Duration.ofSeconds(600));

    @Test
    public void createImage() {
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("a dog walk in the street, rainy day")
                .model(MODEL)
                .build();
        ImageResponse response = client.createImage(request);
        System.out.println(response);
    }

    @Test
    public void editImage() {
        String path1 = "/Users/vacuity/Downloads/1916008131035820034.webp";
        String path2 = "/Users/vacuity/Downloads/WechatIMG2938.jpg";
        List<String> paths = new ArrayList<>();
        paths.add(path1);
        paths.add(path2);
        EditImageRequest request = EditImageRequest.builder()
                .prompt("put the first image into the second")
                .model(MODEL)
                .build();
        ImageResponse images = client.editImage(request, paths, null);
        System.out.println(images);
    }

    @Test
    public void imageVariation() {
        String path = "/Users/vacuity/Downloads/IntelliJ_Plugin-FutureTest_java.png";
        ImageVariationRequest request = ImageVariationRequest.builder()
                .build();
        ImageResponse images = client.imageVariation(request, path);
        System.out.println(images);
    }
}
