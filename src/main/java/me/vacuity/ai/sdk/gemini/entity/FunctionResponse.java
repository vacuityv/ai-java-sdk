package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 15:33
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponse {

    private String name;

    private JsonNode response;

    /** The {@code FunctionCall.id} this answers. Needed to pair responses with calls made in parallel. @since 2.7.0 */
    private String id;
    /** Media returned by the function ({@code inlineData}, {@code fileData}). @since 2.7.0 */
    private java.util.List<java.util.Map<String, Object>> parts;
    /** For non-blocking calls: "SILENT", "WHEN_IDLE" or "INTERRUPT". @since 2.7.0 */
    private String scheduling;
    /** For non-blocking calls: more responses for this call will follow. @since 2.7.0 */
    private Boolean willContinue;
}
