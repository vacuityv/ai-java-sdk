package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 15:37
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Model {

    private String id;

    private Integer created;

    @JsonProperty("owned_by")
    private String ownedBy;

    /** "model". @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("object")
    private String object;
    /** When the model is scheduled to stop serving, if announced. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("shutdown_date")
    private String shutdownDate;
}
