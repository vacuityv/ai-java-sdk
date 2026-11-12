package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The serving model's lifecycle stage.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelStatus {

    /** For example "PREVIEW", "STABLE", "DEPRECATED" or "RETIRED". */
    private String modelStage;

    private String message;

    /** RFC 3339, when the model is scheduled to retire. */
    private String retirementTime;
}
