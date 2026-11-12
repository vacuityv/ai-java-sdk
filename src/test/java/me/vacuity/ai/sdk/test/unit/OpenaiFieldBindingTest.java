package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every field added to the openai side in 2.7.0, bound in both directions.
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
public class OpenaiFieldBindingTest {

    private static final ObjectMapper MAPPER = me.vacuity.ai.sdk.openai.OpenaiClient.defaultObjectMapper();

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

    /** 2 field(s) added in 2.7.0 on assistant.entity.VectorStore. */
    @Test
    public void roundTrip_VectorStore() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.assistant.entity.VectorStore.class,
                "{\"description\":\"s_description\",\"usage_bytes\":7}",
                "description", "usage_bytes");
    }
    /** 3 field(s) added in 2.7.0 on assistant.entity.VectorStoreFile. */
    @Test
    public void roundTrip_VectorStoreFile() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.assistant.entity.VectorStoreFile.class,
                "{\"attributes\":{\"k\":\"v\"},\"chunking_strategy\":{},\"usage_bytes\":7}",
                "attributes", "chunking_strategy", "usage_bytes");
    }
    /** 3 field(s) added in 2.7.0 on assistant.request.VectorStoreFileBatchRequest. */
    @Test
    public void roundTrip_VectorStoreFileBatchRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.assistant.request.VectorStoreFileBatchRequest.class,
                "{\"attributes\":{\"k\":\"v\"},\"chunking_strategy\":{},\"files\":[{\"k\":\"v\"}]}",
                "attributes", "chunking_strategy", "files");
    }
    /** 2 field(s) added in 2.7.0 on assistant.request.VectorStoreFileRequest. */
    @Test
    public void roundTrip_VectorStoreFileRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.assistant.request.VectorStoreFileRequest.class,
                "{\"attributes\":{\"k\":\"v\"},\"chunking_strategy\":{}}",
                "attributes", "chunking_strategy");
    }
    /** 2 field(s) added in 2.7.0 on assistant.request.VectorStoreRequest. */
    @Test
    public void roundTrip_VectorStoreRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.assistant.request.VectorStoreRequest.class,
                "{\"chunking_strategy\":{},\"description\":\"s_description\"}",
                "chunking_strategy", "description");
    }
    /** 2 field(s) added in 2.7.0 on batch.entity.Batch. */
    @Test
    public void roundTrip_Batch() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.batch.entity.Batch.class,
                "{\"model\":\"s_model\",\"usage\":{}}",
                "model", "usage");
    }
    /** 1 field(s) added in 2.7.0 on batch.request.CreateBatchRequest. */
    @Test
    public void roundTrip_CreateBatchRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.batch.request.CreateBatchRequest.class,
                "{\"output_expires_after\":{}}",
                "output_expires_after");
    }
    /** 2 field(s) added in 2.7.0 on decision.request.DecisionRequest. */
    @Test
    public void roundTrip_DecisionRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.decision.request.DecisionRequest.class,
                "{\"prompt_cache_key\":\"s_prompt_cache_key\",\"prompt_cache_options\":{}}",
                "prompt_cache_key", "prompt_cache_options");
    }
    /** 2 field(s) added in 2.7.0 on entity.ChatAnnotation. */
    @Test
    public void roundTrip_ChatAnnotation() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChatAnnotation.class,
                "{\"type\":\"s_type\",\"url_citation\":{}}",
                "type", "url_citation");
    }
    /** 4 field(s) added in 2.7.0 on entity.ChatAnnotation.UrlCitation. */
    @Test
    public void roundTrip_ChatAnnotation_UrlCitation() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChatAnnotation.UrlCitation.class,
                "{\"end_index\":7,\"start_index\":7,\"title\":\"s_title\",\"url\":\"s_url\"}",
                "end_index", "start_index", "title", "url");
    }
    /** 4 field(s) added in 2.7.0 on entity.ChatAudioOutput. */
    @Test
    public void roundTrip_ChatAudioOutput() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChatAudioOutput.class,
                "{\"data\":\"s_data\",\"expires_at\":7,\"id\":\"s_id\",\"transcript\":\"s_transcript\"}",
                "data", "expires_at", "id", "transcript");
    }
    /** 1 field(s) added in 2.7.0 on entity.ChatFunction. */
    @Test
    public void roundTrip_ChatFunction() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChatFunction.class,
                "{\"strict\":true}",
                "strict");
    }
    /** 2 field(s) added in 2.7.0 on entity.ChoiceLogprobs. */
    @Test
    public void roundTrip_ChoiceLogprobs() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChoiceLogprobs.class,
                "{\"content\":[{}],\"refusal\":[{}]}",
                "content", "refusal");
    }
    /** 2 field(s) added in 2.7.0 on entity.ChunkingStrategy. */
    @Test
    public void roundTrip_ChunkingStrategy() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChunkingStrategy.class,
                "{\"static\":{},\"type\":\"s_type\"}",
                "static", "type");
    }
    /** 2 field(s) added in 2.7.0 on entity.ChunkingStrategy.Static. */
    @Test
    public void roundTrip_ChunkingStrategy_Static() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ChunkingStrategy.Static.class,
                "{\"chunk_overlap_tokens\":7,\"max_chunk_size_tokens\":7}",
                "chunk_overlap_tokens", "max_chunk_size_tokens");
    }
    /** 1 field(s) added in 2.7.0 on entity.CompletionTokensDetails. */
    @Test
    public void roundTrip_CompletionTokensDetails() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.CompletionTokensDetails.class,
                "{\"text_tokens\":7}",
                "text_tokens");
    }
    /** 2 field(s) added in 2.7.0 on entity.ExpiresAfter. */
    @Test
    public void roundTrip_ExpiresAfter() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.ExpiresAfter.class,
                "{\"anchor\":\"s_anchor\",\"seconds\":7}",
                "anchor", "seconds");
    }
    /** 2 field(s) added in 2.7.0 on entity.Model. */
    @Test
    public void roundTrip_Model() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.Model.class,
                "{\"object\":\"s_object\",\"shutdown_date\":\"s_shutdown_date\"}",
                "object", "shutdown_date");
    }
    /** 2 field(s) added in 2.7.0 on entity.PromptTokensDetails. */
    @Test
    public void roundTrip_PromptTokensDetails() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.PromptTokensDetails.class,
                "{\"image_tokens\":7,\"text_tokens\":7}",
                "image_tokens", "text_tokens");
    }
    /** 1 field(s) added in 2.7.0 on entity.StreamOptions. */
    @Test
    public void roundTrip_StreamOptions() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.StreamOptions.class,
                "{\"include_obfuscation\":true}",
                "include_obfuscation");
    }
    /** 4 field(s) added in 2.7.0 on entity.TokenLogprob. */
    @Test
    public void roundTrip_TokenLogprob() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.entity.TokenLogprob.class,
                "{\"bytes\":[1],\"logprob\":0.5,\"token\":\"s_token\",\"top_logprobs\":[{}]}",
                "bytes", "logprob", "token", "top_logprobs");
    }
    /** 1 field(s) added in 2.7.0 on file.entity.OpenaiFile. */
    @Test
    public void roundTrip_OpenaiFile() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.file.entity.OpenaiFile.class,
                "{\"expires_at\":7}",
                "expires_at");
    }
    /** 3 field(s) added in 2.7.0 on realtime.entity.RealtimeClientSecret. */
    @Test
    public void roundTrip_RealtimeClientSecret() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeClientSecret.class,
                "{\"expires_at\":7,\"session\":{},\"value\":\"s_value\"}",
                "expires_at", "session", "value");
    }
    /** 17 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig. */
    @Test
    public void roundTrip_RealtimeSessionConfig() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.class,
                "{\"audio\":{},\"expires_at\":7,\"id\":\"s_id\",\"include\":[\"a\"],\"instructions\":\"s_instructions\",\"max_output_tokens\":{\"k\":\"v\"},\"model\":\"s_model\",\"object\":\"s_object\",\"output_modalities\":[\"a\"],\"parallel_tool_calls\":true,\"prompt\":{\"k\":\"v\"},\"reasoning\":{},\"tool_choice\":{\"k\":\"v\"},\"tools\":[{\"k\":\"v\"}],\"tracing\":{\"k\":\"v\"},\"truncation\":{\"k\":\"v\"},\"type\":\"s_type\"}",
                "audio", "expires_at", "id", "include", "instructions", "max_output_tokens", "model", "object", "output_modalities", "parallel_tool_calls", "prompt", "reasoning", "tool_choice", "tools", "tracing", "truncation", "type");
    }
    /** 2 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.Audio. */
    @Test
    public void roundTrip_RealtimeSessionConfig_Audio() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.Audio.class,
                "{\"input\":{},\"output\":{}}",
                "input", "output");
    }
    /** 2 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.AudioFormat. */
    @Test
    public void roundTrip_RealtimeSessionConfig_AudioFormat() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.AudioFormat.class,
                "{\"rate\":7,\"type\":\"s_type\"}",
                "rate", "type");
    }
    /** 4 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.AudioInput. */
    @Test
    public void roundTrip_RealtimeSessionConfig_AudioInput() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.AudioInput.class,
                "{\"format\":{},\"noise_reduction\":{\"k\":\"v\"},\"transcription\":{},\"turn_detection\":{}}",
                "format", "noise_reduction", "transcription", "turn_detection");
    }
    /** 3 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.AudioOutput. */
    @Test
    public void roundTrip_RealtimeSessionConfig_AudioOutput() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.AudioOutput.class,
                "{\"format\":{},\"speed\":0.5,\"voice\":{\"k\":\"v\"}}",
                "format", "speed", "voice");
    }
    /** 1 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.Reasoning. */
    @Test
    public void roundTrip_RealtimeSessionConfig_Reasoning() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.Reasoning.class,
                "{\"effort\":\"s_effort\"}",
                "effort");
    }
    /** 6 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.Transcription. */
    @Test
    public void roundTrip_RealtimeSessionConfig_Transcription() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.Transcription.class,
                "{\"delay\":\"s_delay\",\"keywords\":[\"a\"],\"language\":\"s_language\",\"languages\":[\"a\"],\"model\":\"s_model\",\"prompt\":\"s_prompt\"}",
                "delay", "keywords", "language", "languages", "model", "prompt");
    }
    /** 8 field(s) added in 2.7.0 on realtime.entity.RealtimeSessionConfig.TurnDetection. */
    @Test
    public void roundTrip_RealtimeSessionConfig_TurnDetection() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig.TurnDetection.class,
                "{\"create_response\":true,\"eagerness\":\"s_eagerness\",\"idle_timeout_ms\":7,\"interrupt_response\":true,\"prefix_padding_ms\":7,\"silence_duration_ms\":7,\"threshold\":0.5,\"type\":\"s_type\"}",
                "create_response", "eagerness", "idle_timeout_ms", "interrupt_response", "prefix_padding_ms", "silence_duration_ms", "threshold", "type");
    }
    /** 2 field(s) added in 2.7.0 on realtime.request.CreateRealtimeClientSecretRequest. */
    @Test
    public void roundTrip_CreateRealtimeClientSecretRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.request.CreateRealtimeClientSecretRequest.class,
                "{\"expires_after\":{},\"session\":{}}",
                "expires_after", "session");
    }
    /** 2 field(s) added in 2.7.0 on realtime.request.CreateRealtimeClientSecretRequest.ExpiresAfter. */
    @Test
    public void roundTrip_CreateRealtimeClientSecretRequest_ExpiresAfter() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.realtime.request.CreateRealtimeClientSecretRequest.ExpiresAfter.class,
                "{\"anchor\":\"s_anchor\",\"seconds\":7}",
                "anchor", "seconds");
    }
    /** 10 field(s) added in 2.7.0 on request.ChatRequest. */
    @Test
    public void roundTrip_ChatRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.request.ChatRequest.class,
                "{\"audio\":{\"k\":\"v\"},\"metadata\":{\"k\":\"v\"},\"modalities\":[\"a\"],\"moderation\":{\"k\":\"v\"},\"parallel_tool_calls\":true,\"prediction\":{\"k\":\"v\"},\"safety_identifier\":\"s_safety_identifier\",\"service_tier\":\"s_service_tier\",\"store\":true,\"web_search_options\":{\"k\":\"v\"}}",
                "audio", "metadata", "modalities", "moderation", "parallel_tool_calls", "prediction", "safety_identifier", "service_tier", "store", "web_search_options");
    }
    /** 3 field(s) added in 2.7.0 on response.ChatResponse. */
    @Test
    public void roundTrip_ChatResponse() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.response.ChatResponse.class,
                "{\"metadata\":{\"k\":\"v\"},\"moderation\":{\"k\":\"v\"},\"service_tier\":\"s_service_tier\"}",
                "metadata", "moderation", "service_tier");
    }
    /** 1 field(s) added in 2.7.0 on response.ChatResponseChoice. */
    @Test
    public void roundTrip_ChatResponseChoice() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.response.ChatResponseChoice.class,
                "{\"logprobs\":{}}",
                "logprobs");
    }
    /** 4 field(s) added in 2.7.0 on response.ChatResponseMessage. */
    @Test
    public void roundTrip_ChatResponseMessage() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.response.ChatResponseMessage.class,
                "{\"annotations\":[{}],\"audio\":{},\"refusal\":\"s_refusal\",\"role\":\"s_role\"}",
                "annotations", "audio", "refusal", "role");
    }
    /** 3 field(s) added in 2.7.0 on response.StreamChatResponse. */
    @Test
    public void roundTrip_StreamChatResponse() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.response.StreamChatResponse.class,
                "{\"moderation\":{\"k\":\"v\"},\"obfuscation\":\"s_obfuscation\",\"service_tier\":\"s_service_tier\"}",
                "moderation", "obfuscation", "service_tier");
    }
    /** 1 field(s) added in 2.7.0 on response.StreamChatResponseChoice. */
    @Test
    public void roundTrip_StreamChatResponseChoice() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.response.StreamChatResponseChoice.class,
                "{\"logprobs\":{}}",
                "logprobs");
    }
    /** 9 field(s) added in 2.7.0 on responses.entity.Response. */
    @Test
    public void roundTrip_Response() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.responses.entity.Response.class,
                "{\"access_programs\":{\"k\":\"v\"},\"image\":{\"k\":\"v\"},\"input_transformations\":[{\"k\":\"v\"}],\"moderation\":{\"k\":\"v\"},\"output_text\":\"s_output_text\",\"prompt\":{\"k\":\"v\"},\"prompt_cache_diagnostics\":{\"k\":\"v\"},\"prompt_cache_options\":{\"k\":\"v\"},\"tool_usage\":{\"k\":\"v\"}}",
                "access_programs", "image", "input_transformations", "moderation", "output_text", "prompt", "prompt_cache_diagnostics", "prompt_cache_options", "tool_usage");
    }
    /** 2 field(s) added in 2.7.0 on responses.entity.Response.TokenDetails. */
    @Test
    public void roundTrip_Response_TokenDetails() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.responses.entity.Response.TokenDetails.class,
                "{\"cache_write_tokens\":7,\"cache_write_tokens_details\":{\"k\":\"v\"}}",
                "cache_write_tokens", "cache_write_tokens_details");
    }
    /** 2 field(s) added in 2.7.0 on responses.request.ResponseRequest. */
    @Test
    public void roundTrip_ResponseRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.responses.request.ResponseRequest.class,
                "{\"access_programs\":{\"k\":\"v\"},\"image\":{\"k\":\"v\"}}",
                "access_programs", "image");
    }
    /** 4 field(s) added in 2.7.0 on video.entity.VideoJob. */
    @Test
    public void roundTrip_VideoJob() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.openai.video.entity.VideoJob.class,
                "{\"completed_at\":7,\"created_at\":7,\"expires_at\":7,\"remixed_from_video_id\":\"s_remixed_from_video_id\"}",
                "completed_at", "created_at", "expires_at", "remixed_from_video_id");
    }
}
