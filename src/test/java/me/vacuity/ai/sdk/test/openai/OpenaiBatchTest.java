package me.vacuity.ai.sdk.test.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.batch.entity.Batch;
import me.vacuity.ai.sdk.openai.batch.entity.BatchRequestInputObject;
import me.vacuity.ai.sdk.openai.batch.request.CreateBatchRequest;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.file.entity.OpenaiFile;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.vacuity.ai.sdk.openai.OpenaiClient.defaultObjectMapper;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 18:12
 **/

public class OpenaiBatchTest {

    public static final String DIR = "/Users/vacuity/Work/cache/0429/";

    private static final ObjectMapper mapper = defaultObjectMapper();

    OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY);

    @Test
    public void testCreateBatch() throws JsonProcessingException {

        // 1. make a list of batch request input object
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "hello"));
        ChatRequest request = ChatRequest.builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .build();
        BatchRequestInputObject inputObject = BatchRequestInputObject.builder()
                .customId("id_test_2")
                .method("POST")
                .url("/v1/chat/completions")
                .body(request)
                .build();
        List<BatchRequestInputObject> inputObjects = new ArrayList<>();
        inputObjects.add(inputObject);
        // 2. write it to a file split by \n
        String filePath = DIR + "test.txt";
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < inputObjects.size(); i++) {
            content.append(mapper.writeValueAsString(inputObjects.get(i)));
            if (i < inputObjects.size() - 1) {
                content.append("\n");
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 3. upload file
        OpenaiFile file = client.uploadFile("batch", filePath);
        // 4. create batch
        CreateBatchRequest createBatchRequest = CreateBatchRequest.builder()
                .endpoint("/v1/chat/completions")
                .inputFileId(file.getId())
                .build();
        Batch batch = client.createBatch(createBatchRequest);
        System.out.println(batch);
        // batch_6811c54b5a8c81908b77fe1e4a06878c
    }

    @Test
    public void testRetrieveBatch() {
        String batchId = "batch_6811c97a91148190bfa25a32d49c285b";
        Batch batch = client.retrieveBatch(batchId);
        System.out.println(batch);
        if ("completed".equals(batch.getStatus())) {
            System.out.println(batch.getOutputFileId());
        }
    }

    @Test
    public void testCancelBatch() {
        String batchId = "batch_6811c54b5a8c81908b77fe1e4a06878c";
        Batch batch = client.cancelBatch(batchId);
        System.out.println(batch);
    }

    @Test
    public void testListBatch() {
        List<Batch> batchs = client.listBatch(null, null);
        System.out.println(batchs);
    }
}
