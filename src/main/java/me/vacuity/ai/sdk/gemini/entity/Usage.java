package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mirrors GenerateContentResponse.usageMetadata.
 * <p>
 * On a streaming call this arrives on every chunk and the counters are
 * cumulative, not deltas — read the last chunk rather than summing them.
 *
 * @description:
 * @author: vacuity
 * @create: 2024-06-03 15:47
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usage {

    private Integer promptTokenCount;

    /**
     * Visible answer tokens. Does <b>not</b> include {@link #thoughtsTokenCount},
     * so billing output on this field alone undercounts thinking models.
     */
    private Integer candidatesTokenCount;

    private Integer totalTokenCount;

    /**
     * Tokens the model spent thinking, on 2.5 / 3.x / 4 models. Google bills
     * these at the output rate even though they are excluded from
     * {@link #candidatesTokenCount}. Null when the model did not think.
     */
    private Integer thoughtsTokenCount;

    /**
     * Tokens served from cached content.
     */
    private Integer cachedContentTokenCount;

    /**
     * Prompt tokens consumed by built-in tool use.
     */
    private Integer toolUsePromptTokenCount;

    private List<ModalityTokenCount> promptTokensDetails;

    private List<ModalityTokenCount> candidatesTokensDetails;

    private List<ModalityTokenCount> cacheTokensDetails;

    private List<ModalityTokenCount> toolUsePromptTokensDetails;

    private String trafficType;

    /** 兜底捕获服务端返回但 SDK 未声明的字段，避免 Google 新增字段被静默丢弃。 */
    @Builder.Default
    private Map<String, Object> extra = new HashMap<>();

    @JsonAnySetter
    public void putExtra(String key, Object value) {
        if (extra == null) extra = new HashMap<>();
        extra.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * Retained so existing {@code new Usage(prompt, candidates, total)} call
     * sites keep compiling after the usageMetadata fields were filled in.
     */
    @Deprecated
    public Usage(Integer promptTokenCount, Integer candidatesTokenCount, Integer totalTokenCount) {
        this.promptTokenCount = promptTokenCount;
        this.candidatesTokenCount = candidatesTokenCount;
        this.totalTokenCount = totalTokenCount;
        this.extra = new HashMap<>();
    }

    /**
     * Total tokens billed at the output rate: the visible answer plus the
     * thinking the model was charged for. Returns null only when both counters
     * are absent.
     */
    public Integer getBillableOutputTokenCount() {
        if (candidatesTokenCount == null && thoughtsTokenCount == null) {
            return null;
        }
        return (candidatesTokenCount == null ? 0 : candidatesTokenCount)
                + (thoughtsTokenCount == null ? 0 : thoughtsTokenCount);
    }
}
