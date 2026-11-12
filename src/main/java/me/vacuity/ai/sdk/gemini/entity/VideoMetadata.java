package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Clipping and sampling for a video part. Offsets are protobuf durations such
 * as "10s" or "1.5s".
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoMetadata {

    private String startOffset;

    private String endOffset;

    /** Frames sampled per second. */
    private Double fps;
}
