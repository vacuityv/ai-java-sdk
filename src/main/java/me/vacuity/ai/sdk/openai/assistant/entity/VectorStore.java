package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.ExpiresAfter;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.FileCounts;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:41
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStore {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private String created_at;

    private String name;

    /**
     * Total bytes the store's files take up. The wire field is
     * {@code usage_bytes}.
     * <p>
     * Before 2.7.0 this was a field named {@code bytes}, which under the
     * client's snake_case naming read a {@code bytes} key the API never sends
     * — so it was null on every response. Verified against a live store: the
     * response carries {@code usage_bytes} and no {@code bytes} at all.
     */
    @JsonProperty("usage_bytes")
    private Long usageBytes;

    @JsonProperty("file_counts")
    private FileCounts fileCounts;

    private String status;

    @JsonProperty("expires_after")
    private ExpiresAfter expiresAfter;

    @JsonProperty("expires_at")
    private Integer expiresAt;

    @JsonProperty("last_active_at")
    private Integer lastActiveAt;

    private Map<String, Object> metadata;

    /**
     * @deprecated always returned null before 2.7.0, because it read a key the
     *             API does not send. Use {@link #getUsageBytes()}.
     */
    @Deprecated
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getBytes() {
        return usageBytes == null ? null : usageBytes.intValue();
    }

    /** The store's description — present on a live response, verified. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("description")
    private String description;
}
