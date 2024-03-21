package me.vacuity.ai.sdk.test.openai;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.image.entity.Image;
import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.request.EditImageRequest;
import me.vacuity.ai.sdk.openai.image.request.ImageVariationRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:27
 **/

public class OpenaiImageTest {

    OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY, Duration.ofSeconds(120));

    @Test
    public void createImage() {
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("a dog walk in the street, rainy day")
                .responseFormat("url")
                .build();
        List<Image> images = client.createImage(request);
        assertNotNull(images);
        System.out.println(images);
    }

    @Test
    public void editImage() {
        String path = "/Users/vacuity/Downloads/IntelliJ_Plugin-FutureTest_java.png";
        EditImageRequest request = EditImageRequest.builder()
                .prompt("change the main color to red")
                .build();
        List<Image> images = client.editImage(request, path, null);
        assertNotNull(images);
        System.out.println(images);
    }

    @Test
    public void imageVariation() {
        String path = "/Users/vacuity/Downloads/IntelliJ_Plugin-FutureTest_java.png";
        ImageVariationRequest request = ImageVariationRequest.builder()
                .build();
        List<Image> images = client.imageVariation(request, path);
        assertNotNull(images);
        System.out.println(images);
    }
}
