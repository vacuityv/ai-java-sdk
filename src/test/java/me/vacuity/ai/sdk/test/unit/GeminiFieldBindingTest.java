package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every field added to the gemini side in 2.7.0, bound in both directions.
 * <p>
 * Generated from a diff of each class against the previous release, so the
 * set is exactly what changed — not a hand-picked sample. Each test feeds a
 * JSON object carrying the new wire names through the client's own mapper and
 * back out, and asserts every key survives with its value intact.
 * <p>
 * Matching the official spec's field names is necessary but not enough: the
 * {@code VideoJob} timestamps had names that looked right in the source and
 * still never bound, because an explicit {@code @JsonProperty} overrode the
 * client's naming strategy. A round trip through the real mapper is what
 * catches that.
 *
 * @author vacuity
 */
public class GeminiFieldBindingTest {

    private static final ObjectMapper MAPPER = me.vacuity.ai.sdk.gemini.GeminiClient.defaultObjectMapper();

    private static void assertRoundTrip(Class<?> type, String fixture, String... wireNames)
            throws Exception {
        JsonNode in = MAPPER.readTree(fixture);
        Object bound = MAPPER.treeToValue(in, type);
        JsonNode out = MAPPER.valueToTree(bound);
        for (String wire : wireNames) {
            assertTrue(out.has(wire), type.getSimpleName() + " dropped \"" + wire
                    + "\" on the way through — it does not bind. Out: " + out);
            JsonNode a = in.get(wire), b = out.get(wire);
            // A Long field writes back as a LongNode where the input parsed as an
            // IntNode; the value is what matters, not Jackson's node subclass.
            boolean same = a.isNumber() && b.isNumber()
                    ? a.decimalValue().compareTo(b.decimalValue()) == 0
                    : a.equals(b);
            assertTrue(same, type.getSimpleName() + "." + wire
                    + " changed in the round trip: " + a + " -> " + b);
        }
    }

    /** 4 field(s) added in 2.7.0 on entity.ChatFunction. */
    @Test
    public void roundTrip_ChatFunction() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.ChatFunction.class,
                "{\"behavior\":\"s_behavior\",\"parametersJsonSchema\":{\"k\":\"v\"},\"response\":{},\"responseJsonSchema\":{\"k\":\"v\"}}",
                "behavior", "parametersJsonSchema", "response", "responseJsonSchema");
    }
    /** 1 field(s) added in 2.7.0 on entity.ChatFunctionCall. */
    @Test
    public void roundTrip_ChatFunctionCall() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.ChatFunctionCall.class,
                "{\"id\":\"s_id\"}",
                "id");
    }
    /** 7 field(s) added in 2.7.0 on entity.ChatMessageContentPart. */
    @Test
    public void roundTrip_ChatMessageContentPart() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart.class,
                "{\"audioTranscription\":{\"k\":\"v\"},\"fileData\":{},\"mediaProcessing\":\"s_mediaProcessing\",\"mediaResolution\":{\"k\":\"v\"},\"partMetadata\":{\"k\":\"v\"},\"speechMetadata\":{\"k\":\"v\"},\"videoMetadata\":{}}",
                "audioTranscription", "fileData", "mediaProcessing", "mediaResolution", "partMetadata", "speechMetadata", "videoMetadata");
    }
    /** 1 field(s) added in 2.7.0 on entity.CitationMetadata. */
    @Test
    public void roundTrip_CitationMetadata() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.CitationMetadata.class,
                "{\"citationSources\":[{}]}",
                "citationSources");
    }
    /** 4 field(s) added in 2.7.0 on entity.CitationMetadata.CitationSource. */
    @Test
    public void roundTrip_CitationMetadata_CitationSource() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.CitationMetadata.CitationSource.class,
                "{\"endIndex\":7,\"license\":\"s_license\",\"startIndex\":7,\"uri\":\"s_uri\"}",
                "endIndex", "license", "startIndex", "uri");
    }
    /** 1 field(s) added in 2.7.0 on entity.CodeExecutionResult. */
    @Test
    public void roundTrip_CodeExecutionResult() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.CodeExecutionResult.class,
                "{\"id\":\"s_id\"}",
                "id");
    }
    /** 1 field(s) added in 2.7.0 on entity.ExecutableCode. */
    @Test
    public void roundTrip_ExecutableCode() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.ExecutableCode.class,
                "{\"id\":\"s_id\"}",
                "id");
    }
    /** 3 field(s) added in 2.7.0 on entity.FileData. */
    @Test
    public void roundTrip_FileData() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.FileData.class,
                "{\"displayName\":\"s_displayName\",\"fileUri\":\"s_fileUri\",\"mimeType\":\"s_mimeType\"}",
                "displayName", "fileUri", "mimeType");
    }
    /** 4 field(s) added in 2.7.0 on entity.FunctionResponse. */
    @Test
    public void roundTrip_FunctionResponse() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.FunctionResponse.class,
                "{\"id\":\"s_id\",\"parts\":[{\"k\":\"v\"}],\"scheduling\":\"s_scheduling\",\"willContinue\":true}",
                "id", "parts", "scheduling", "willContinue");
    }
    /** 3 field(s) added in 2.7.0 on entity.LogprobsResult. */
    @Test
    public void roundTrip_LogprobsResult() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.LogprobsResult.class,
                "{\"chosenCandidates\":[{}],\"logProbabilitySum\":0.5,\"topCandidates\":[{}]}",
                "chosenCandidates", "logProbabilitySum", "topCandidates");
    }
    /** 3 field(s) added in 2.7.0 on entity.LogprobsResult.TokenLogprob. */
    @Test
    public void roundTrip_LogprobsResult_TokenLogprob() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.LogprobsResult.TokenLogprob.class,
                "{\"logProbability\":0.5,\"token\":\"s_token\",\"tokenId\":7}",
                "logProbability", "token", "tokenId");
    }
    /** 1 field(s) added in 2.7.0 on entity.LogprobsResult.TopCandidates. */
    @Test
    public void roundTrip_LogprobsResult_TopCandidates() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.LogprobsResult.TopCandidates.class,
                "{\"candidates\":[{}]}",
                "candidates");
    }
    /** 3 field(s) added in 2.7.0 on entity.ModelStatus. */
    @Test
    public void roundTrip_ModelStatus() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.ModelStatus.class,
                "{\"message\":\"s_message\",\"modelStage\":\"s_modelStage\",\"retirementTime\":\"s_retirementTime\"}",
                "message", "modelStage", "retirementTime");
    }
    /** 2 field(s) added in 2.7.0 on entity.Schema. */
    @Test
    public void roundTrip_Schema() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.Schema.class,
                "{\"default\":{\"k\":\"v\"},\"example\":{\"k\":\"v\"}}",
                "default", "example");
    }
    /** 5 field(s) added in 2.7.0 on entity.Tool. */
    @Test
    public void roundTrip_Tool() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.Tool.class,
                "{\"computerUse\":{\"k\":\"v\"},\"fileSearch\":{\"k\":\"v\"},\"googleMaps\":{\"k\":\"v\"},\"mcpServers\":[{\"k\":\"v\"}],\"urlContext\":{\"k\":\"v\"}}",
                "computerUse", "fileSearch", "googleMaps", "mcpServers", "urlContext");
    }
    /** 1 field(s) added in 2.7.0 on entity.ToolConfig. */
    @Test
    public void roundTrip_ToolConfig() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.ToolConfig.class,
                "{\"retrievalConfig\":{\"k\":\"v\"}}",
                "retrievalConfig");
    }
    /** 1 field(s) added in 2.7.0 on entity.UrlContextMetadata. */
    @Test
    public void roundTrip_UrlContextMetadata() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.UrlContextMetadata.class,
                "{\"urlMetadata\":[{}]}",
                "urlMetadata");
    }
    /** 2 field(s) added in 2.7.0 on entity.UrlContextMetadata.UrlMetadata. */
    @Test
    public void roundTrip_UrlContextMetadata_UrlMetadata() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.UrlContextMetadata.UrlMetadata.class,
                "{\"retrievedUrl\":\"s_retrievedUrl\",\"urlRetrievalStatus\":\"s_urlRetrievalStatus\"}",
                "retrievedUrl", "urlRetrievalStatus");
    }
    /** 1 field(s) added in 2.7.0 on entity.Usage. */
    @Test
    public void roundTrip_Usage() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.Usage.class,
                "{\"serviceTier\":\"s_serviceTier\"}",
                "serviceTier");
    }
    /** 3 field(s) added in 2.7.0 on entity.VideoMetadata. */
    @Test
    public void roundTrip_VideoMetadata() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.entity.VideoMetadata.class,
                "{\"endOffset\":\"s_endOffset\",\"fps\":0.5,\"startOffset\":\"s_startOffset\"}",
                "endOffset", "fps", "startOffset");
    }
    /** 4 field(s) added in 2.7.0 on request.ChatRequest. */
    @Test
    public void roundTrip_ChatRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.request.ChatRequest.class,
                "{\"continuationToken\":\"s_continuationToken\",\"labels\":{\"k\":\"v\"},\"serviceTier\":\"s_serviceTier\",\"store\":true}",
                "continuationToken", "labels", "serviceTier", "store");
    }
    /** 3 field(s) added in 2.7.0 on response.ChatResponse. */
    @Test
    public void roundTrip_ChatResponse() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.response.ChatResponse.class,
                "{\"modelStatus\":{},\"modelVersion\":\"s_modelVersion\",\"responseId\":\"s_responseId\"}",
                "modelStatus", "modelVersion", "responseId");
    }
    /** 8 field(s) added in 2.7.0 on response.ChatResponseCandidate. */
    @Test
    public void roundTrip_ChatResponseCandidate() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.response.ChatResponseCandidate.class,
                "{\"avgLogprobs\":0.5,\"citationMetadata\":{},\"continuationToken\":\"s_continuationToken\",\"finishMessage\":\"s_finishMessage\",\"groundingAttributions\":[{\"k\":\"v\"}],\"logprobsResult\":{},\"tokenCount\":7,\"urlContextMetadata\":{}}",
                "avgLogprobs", "citationMetadata", "continuationToken", "finishMessage", "groundingAttributions", "logprobsResult", "tokenCount", "urlContextMetadata");
    }
    /** 1 field(s) added in 2.7.0 on response.ChatResponseSafetyRating. */
    @Test
    public void roundTrip_ChatResponseSafetyRating() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.response.ChatResponseSafetyRating.class,
                "{\"blocked\":true}",
                "blocked");
    }
    /** 3 field(s) added in 2.7.0 on response.GroundingMetadata. */
    @Test
    public void roundTrip_GroundingMetadata() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.response.GroundingMetadata.class,
                "{\"googleMapsWidgetContextToken\":\"s_googleMapsWidgetContextToken\",\"imageSearchQueries\":[\"a\"],\"retrievalMetadata\":{\"k\":\"v\"}}",
                "googleMapsWidgetContextToken", "imageSearchQueries", "retrievalMetadata");
    }
    /** 4 field(s) added in 2.7.0 on response.StreamChatResponse. */
    @Test
    public void roundTrip_StreamChatResponse() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.gemini.response.StreamChatResponse.class,
                "{\"modelStatus\":{},\"modelVersion\":\"s_modelVersion\",\"promptFeedback\":{},\"responseId\":\"s_responseId\"}",
                "modelStatus", "modelVersion", "promptFeedback", "responseId");
    }
}
