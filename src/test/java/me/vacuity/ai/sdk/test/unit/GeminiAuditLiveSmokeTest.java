package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatFunction;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.FileData;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Gemini fields the 2.7.0 audit added, through the SDK against the live
 * API.
 * <p>
 * Not covered live: {@code Candidate.logprobsResult}. Every model reachable
 * here answers "Logprobs is not enabled for this model", so the field is
 * covered by the offline binding test only.
 *
 * @author vacuity
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiAuditLiveSmokeTest {

    private static final String MODEL = "gemini-3-flash-preview";

    private GeminiClient client() {
        return new GeminiClient(System.getenv("GEMINI_API_KEY"), Duration.ofSeconds(180));
    }

    private static ChatMessage user(ChatMessageContentPart... parts) {
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setParts(Arrays.asList(parts));
        return message;
    }

    private static ChatMessageContentPart text(String t) {
        ChatMessageContentPart part = new ChatMessageContentPart();
        part.setText(t);
        return part;
    }

    /** Present on every live response; dropped by the SDK before 2.7.0. */
    @Test
    public void responseIdModelVersionAndServiceTierArrive() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .contents(Collections.singletonList(user(text("Say ok"))))
                .build());

        assertNotNull(response.getResponseId(), "responseId must bind");
        assertTrue(response.getModelVersion().startsWith("gemini"),
                "modelVersion must bind: " + response.getModelVersion());
        assertNotNull(response.getUsageMetadata().getServiceTier(),
                "usageMetadata.serviceTier must bind");
    }

    /** The id that pairs a call with its response when several run in parallel. */
    @Test
    public void functionCallsCarryAnId() {
        ChatFunction weather = new ChatFunction();
        weather.setName("get_weather");
        weather.setDescription("Weather for a city");
        weather.setParametersJsonSchema(new java.util.LinkedHashMap<String, Object>() {{
            put("type", "object");
            put("properties", Collections.singletonMap("city",
                    Collections.singletonMap("type", "string")));
            put("required", Collections.singletonList("city"));
        }});

        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .contents(Collections.singletonList(user(text("What's the weather in Paris? Use the tool."))))
                .tools(Collections.singletonList(Tool.builder()
                        .functionDeclarations(Collections.singletonList(weather)).build()))
                .build());

        String id = null;
        for (ChatMessageContentPart part : response.getCandidates().get(0).getContent().getParts()) {
            if (part.getFunctionCall() != null) {
                id = part.getFunctionCall().getId();
            }
        }
        assertNotNull(id, "a function call must carry its id — and parametersJsonSchema must "
                + "have been accepted for the call to happen at all");
    }

    @Test
    public void urlContextReportsWhatItFetched() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .contents(Collections.singletonList(user(text("Summarize https://example.com in one sentence."))))
                .tools(Collections.singletonList(Tool.urlContext()))
                .build());

        assertNotNull(response.getCandidates().get(0).getUrlContextMetadata(),
                "the url-context tool must report urlContextMetadata");
        assertTrue(!response.getCandidates().get(0).getUrlContextMetadata().getUrlMetadata().isEmpty());
    }

    /** fileData lets a part reference a video by URL instead of sending bytes. */
    @Test
    public void fileDataReferencesAVideoByUrl() {
        ChatMessageContentPart video = new ChatMessageContentPart();
        video.setFileData(FileData.of("https://www.youtube.com/watch?v=jNQXAC9IVRw", "video/mp4"));

        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .contents(Collections.singletonList(user(video, text("In five words, what is this video?"))))
                .build());

        assertNotNull(response.getCandidates().get(0).getContent(),
                "a fileData part must be accepted and answered");
    }
}
