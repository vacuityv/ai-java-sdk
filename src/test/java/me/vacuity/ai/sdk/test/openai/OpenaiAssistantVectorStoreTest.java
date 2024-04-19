package me.vacuity.ai.sdk.test.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStore;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStoreFile;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStoreFileBatch;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.ExpiresAfter;
import me.vacuity.ai.sdk.openai.assistant.request.ModifyVectorStoreRequest;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreFileBatchRequest;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreFileRequest;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreRequest;
import me.vacuity.ai.sdk.openai.entity.DeleteStatus;
import me.vacuity.ai.sdk.openai.entity.ListRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:52
 **/

public class OpenaiAssistantVectorStoreTest {

    OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY);

    ObjectMapper mapper = defaultObjectMapper();

    
    // VectorStore
    @Test
    public void createVectorStore() {

        ExpiresAfter expiresAfter = ExpiresAfter.builder()
                .anchor("last_active_at")
                .days(1)
                .build();
        String fileId = "file-cyXRLxSuPQI65qRoEDVy3tK7";
        VectorStoreRequest request = VectorStoreRequest.builder()
                .fileIds(Arrays.asList(fileId))
                .name("test1")
                .expiresAfter(expiresAfter)
                .build();
        VectorStore store = client.createVectorStore(request);
        assertNotNull(store);
        System.out.println(store);
        // vs_pLAIKZbmJ9IEGERKHsSOOrpZ
    }

    @Test
    public void listVectorStores() {
        ListRequest request = ListRequest.builder()
                .build();
        List<VectorStore> stores = client.listVectorStores(request);
        assertNotNull(stores);
        System.out.println(stores);
    }

    
    @Test
    public void retrieveVectorStore() {
        String id = "vs_pLAIKZbmJ9IEGERKHsSOOrpZ";
        VectorStore store = client.retrieveVectorStore(id);
        assertNotNull(store);
        System.out.println(store);
    }

    @Test
    public void modifyAssistant() {

        String id = "vs_pLAIKZbmJ9IEGERKHsSOOrpZ";
        ExpiresAfter expiresAfter = ExpiresAfter.builder()
                .anchor("last_active_at")
                .days(1)
                .build();
        ModifyVectorStoreRequest request = ModifyVectorStoreRequest.builder()
                .name("test2")
                .expiresAfter(expiresAfter)
                .build();
        VectorStore store = client.modifyAssistant(id, request);
        assertNotNull(store);
        System.out.println(store);
        // vs_pLAIKZbmJ9IEGERKHsSOOrpZ
    }

    @Test
    public void deleteVectorStore() {
        String id = "vs_pLAIKZbmJ9IEGERKHsSOOrpZ";
        DeleteStatus status = client.deleteVectorStore(id);
        assertNotNull(status);
        System.out.println(status);
    }
    
    // VectorStoreFile
    @Test
    public void createVectorStoreFile() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";

        String fileId = "file-cyXRLxSuPQI65qRoEDVy3tK7";
        VectorStoreFileRequest request = VectorStoreFileRequest.builder()
                .fileId(fileId)
                .build();
        VectorStoreFile entity = client.createVectorStoreFile(vectorStoreId, request);
        assertNotNull(entity);
        System.out.println(entity);
    }

    @Test
    public void listVectorStoreFiles() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";
        ListRequest request = ListRequest.builder()
                .build();
        List<VectorStoreFile> list = client.listVectorStoreFiles(vectorStoreId, request);
        assertNotNull(list);
        System.out.println(list);
    }

    @Test
    public void deleteVectorStoreFile() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";
        String fileId = "file-cyXRLxSuPQI65qRoEDVy3tK7";

        DeleteStatus entity = client.deleteVectorStoreFile(vectorStoreId, fileId);
        assertNotNull(entity);
        System.out.println(entity);
    }
    
    // VectorStoreFileBatch
    @Test
    public void createVectorStoreFileBatch() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";
        String fileId = "file-cyXRLxSuPQI65qRoEDVy3tK7";
        VectorStoreFileBatchRequest request = VectorStoreFileBatchRequest.builder()
                .fileIds(Arrays.asList(fileId))
                .build();
        VectorStoreFileBatch entity = client.createVectorStoreFileBatch(vectorStoreId, request);
        assertNotNull(entity);
        System.out.println(entity);
        // vsfb_b4f1f12c332541b1b2ccc25a855c9c0b
    }

    @Test
    public void retrieveVectorStoreFileBatch() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";
        String batchId = "vsfb_b4f1f12c332541b1b2ccc25a855c9c0b";
        VectorStoreFileBatch entity = client.retrieveVectorStoreFileBatch(vectorStoreId, batchId);
        assertNotNull(entity);
        System.out.println(entity);
        // vsfb_b4f1f12c332541b1b2ccc25a855c9c0b
    }

    @Test
    public void listVectorStoreFileInBatch() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";
        String batchId = "vsfb_b4f1f12c332541b1b2ccc25a855c9c0b";
        ListRequest request = ListRequest.builder()
                .build();
        List<VectorStoreFile> list = client.listVectorStoreFileInBatch(vectorStoreId, batchId, request);
        assertNotNull(list);
        System.out.println(list);
    }

    @Test
    public void cancelVectorStoreFileBatch() {
        String vectorStoreId = "vs_8IqXSzpSsRlRJe2ZFeTvU4TQ";
        String batchId = "vsfb_b4f1f12c332541b1b2ccc25a855c9c0b";
        VectorStoreFileBatch entity = client.cancelVectorStoreFileBatch(vectorStoreId, batchId);
        assertNotNull(entity);
        System.out.println(entity);
    }
}
