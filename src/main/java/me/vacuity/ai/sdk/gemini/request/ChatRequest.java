package me.vacuity.ai.sdk.gemini.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ImageConfig;
import me.vacuity.ai.sdk.gemini.entity.Schema;
import me.vacuity.ai.sdk.gemini.entity.SpeechConfig;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.entity.ToolConfig;
import me.vacuity.ai.sdk.gemini.enums.MediaResolution;
import me.vacuity.ai.sdk.gemini.enums.Modality;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:12
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    // default model: gemini-pro
    @Builder.Default
    @JsonIgnore
    private String model = "gemini-pro";

    private List<ChatMessage> contents;

    private List<SafetySetting> safetySettings;

    private GenerationConfig generationConfig;

    private ChatMessage systemInstruction;

    private List<Tool> tools;

    /**
     * Required when combining function declarations with a built-in tool such
     * as googleSearch on gemini-4-argon — see
     * {@link ToolConfig#includeServerSideToolInvocations()}. Omitted from the
     * request body when null.
     */
    private ToolConfig toolConfig;

    /**
     * A cached-content resource name, as returned when the cache was created
     * — the full {@code cachedContents/...} form, not just the id.
     * <p>
     * Caches are created and deleted through the separate
     * {@code v1beta/cachedContents} endpoint, which this SDK does not wrap;
     * this field is for using one that already exists. The hit shows up as
     * {@code usageMetadata.cachedContentTokenCount}, which
     * {@link me.vacuity.ai.sdk.gemini.entity.Usage} already carries — a
     * measured call reported 3302 cached of 3307 prompt tokens.
     * <p>
     * Works on both the v1alpha path this SDK uses for generateContent and on
     * v1beta.
     *
     * @since 2.6.0
     */
    private String cachedContent;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetySetting {

        private String category;

        private String threshold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {

        private List<String> stopSequences;

        private String responseMimeType;

        private List<Modality> responseModalities;

        private Integer candidateCount;

        private Float temperature;

        private Integer maxOutputTokens;

        private Float topP;

        /**
         * Top-k sampling. int32 per the v1beta discovery document, not a
         * float: this was declared {@code Float} before 2.6.0, which let
         * callers write {@code .topK(5.5f)} and get
         * {@code Invalid value at 'generation_config.top_k' (TYPE_INT32), 5.5}
         * from the server. Whole values like {@code 5.0f} were always
         * accepted, so nothing on the wire was wrong — the type just allowed
         * a request that could not succeed. Must be non-negative.
         */
        private Integer topK;

        private Integer seed;

        private Float presencePenalty;

        private Float frequencyPenalty;

        private Boolean responseLogprobs;

        private Integer logprobs;

        private Boolean enableEnhancedCivicAnswers;

        private MediaResolution mediaResolution;

        private ThinkingConfig thinkingConfig;

        /**
         * Constrains the response to this schema. Pair it with
         * {@code responseMimeType = "application/json"}.
         * <p>
         * Verified to constrain the output, {@code propertyOrdering} included.
         * For a plain JSON Schema use {@link #responseJsonSchema} instead —
         * that one also honours {@code additionalProperties: false}, which
         * this shape has no field for.
         *
         * @since 2.6.0
         */
        private Schema responseSchema;

        /**
         * A plain JSON Schema, as an alternative to {@link #responseSchema}.
         * Typed loosely on purpose: it is {@code any} in the discovery
         * document, so a Map or a Jackson tree both work and the SDK does not
         * have to track JSON Schema's own evolution.
         *
         * @since 2.6.0
         */
        private Object responseJsonSchema;

        /**
         * Image generation settings, for models returning an IMAGE modality.
         *
         * @since 2.6.0
         */
        private ImageConfig imageConfig;

        /**
         * Speech settings, for TTS models asked for an AUDIO modality.
         *
         * @since 2.6.0
         */
        private SpeechConfig speechConfig;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThinkingConfig {

        private Boolean includeThoughts;

        private Integer thinkingBudget;
        
        private String thinkingLevel;
    }
}
