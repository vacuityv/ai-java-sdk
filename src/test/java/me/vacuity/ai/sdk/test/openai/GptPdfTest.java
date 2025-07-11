package me.vacuity.ai.sdk.test.openai;

import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatMessageContent;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-07-11 23:14
 **/

public class GptPdfTest {


    public static final String API_KEY = "sk-proj-*******";


    OpenaiClient client = new OpenaiClient(API_KEY);


    @Test
    public void chat() throws IOException {

        String pdfPath = "/Users/vacuity/Downloads/test.pdf";
        String base64 = convertPdfToBase64(pdfPath);

        List<ChatMessageContent> contentList = new ArrayList<>();

        ChatMessageContent content = new ChatMessageContent();
        content.setType("text");
        content.setText("explain this file");
        contentList.add(content);
        ChatMessageContent fileContent = new ChatMessageContent();
        fileContent.setType("file");
        ChatMessageContent.ChatMessageContentFile file = ChatMessageContent.ChatMessageContentFile.builder()
                .filename("test.pdf")
                .fileData("data:application/pdf;base64," + base64)
                .build();
        fileContent.setFile(file);
        contentList.add(fileContent);
        ChatMessage message = new ChatMessage("user", contentList);


        List<ChatMessage> messages = new ArrayList<>();
        messages.add(message);


        messages.add(new ChatMessage("user", "explain this file"));
        ChatRequest request = ChatRequest.builder()
                .model("gpt-4o")
                .messages(messages)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response.getSingleContent());
            System.out.println(response.getUsage());
        } catch (VacSdkException e) {
            System.out.println(e);
        }
    }
    

    public String convertPdfToBase64(String filePath) throws IOException {
        // 验证文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在：" + filePath);
        }

        // 验证是否为PDF文件
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            throw new IOException("文件不是PDF格式：" + filePath);
        }

        // 读取文件并转换为Base64
        Path path = Paths.get(filePath);
        byte[] fileBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(fileBytes);
    }
}
