package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * The result of a server-side tool invocation, surfaced only when
 * {@link ToolConfig#getIncludeServerSideToolInvocations()} is on.
 *
 * @author: vacuity
 * @create: 2026-10-15
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerToolResponse {

    /**
     * e.g. "GOOGLE_SEARCH_WEB".
     */
    private String toolType;

    /**
     * The tool's raw result. For GOOGLE_SEARCH_WEB this carries
     * search_suggestions, an HTML fragment.
     */
    private Map<String, Object> response;

    private String id;

    /** 兜底捕获服务端返回但 SDK 未声明的字段，保证多轮回传不丢内容。 */
    @lombok.Builder.Default
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
}
