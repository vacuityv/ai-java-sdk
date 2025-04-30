package me.vacuity.ai.sdk.openai.batch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 17:43
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Batch {

    @JsonProperty("cancelled_at")
    private Integer cancelledAt;

    @JsonProperty("cancelling_at")
    private Integer cancellingAt;

    @JsonProperty("completed_at")
    private Integer completedAt;

    @JsonProperty("completion_window")
    private String completionWindow;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("error_file_id")
    private String errorFileId;


    @JsonProperty("errors")
    private BatchError errors;

    @JsonProperty("expired_at")
    private Integer expiredAt;

    @JsonProperty("expires_at")
    private Integer expiresAt;

    @JsonProperty("failed_at")
    private Integer failedAt;

    @JsonProperty("finalizing_at")
    private Integer finalizingAt;

    @JsonProperty("id")
    private String id;

    @JsonProperty("in_progress_at")
    private Integer inProgressAt;

    @JsonProperty("input_file_id")
    private String inputFileId;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("object")
    private String object;

    @JsonProperty("output_file_id")
    private String outputFileId;

    @JsonProperty("request_counts")
    private BatchRequestCount requestCounts;

    private String status;
}
