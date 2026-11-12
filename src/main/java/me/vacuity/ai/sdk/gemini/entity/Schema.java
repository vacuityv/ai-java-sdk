package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * An OpenAPI-subset schema, used for {@code responseSchema}.
 * <p>
 * Field names and types follow the v1beta discovery document. Note that the
 * size bounds are {@code int64} there and so arrive as strings on the wire,
 * which is why they are typed {@link Long} rather than {@code Integer}.
 * <p>
 * For a plain JSON Schema, {@code responseJsonSchema} takes one directly and
 * avoids restating it in this shape.
 *
 * @author vacuity
 * @since 2.6.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schema {

    /**
     * "STRING", "NUMBER", "INTEGER", "BOOLEAN", "ARRAY", "OBJECT" or "NULL" —
     * upper case, unlike plain JSON Schema's lower case. Not validated here;
     * the server names the accepted values when it rejects one.
     */
    private String type;

    private String format;

    private String title;

    private String description;

    private Boolean nullable;

    /** Allowed values, for a STRING type. */
    private List<String> enumValues;

    /** ARRAY only — the element schema. */
    private Schema items;

    private Long minItems;

    private Long maxItems;

    /** OBJECT only — property name to its schema. */
    private Map<String, Schema> properties;

    private List<String> required;

    /**
     * OBJECT only — the order the model should emit properties in. Verified
     * to be honoured: a schema ordering "ok" before "why" came back in that
     * order.
     */
    private List<String> propertyOrdering;

    private Long minProperties;

    private Long maxProperties;

    private Double minimum;

    private Double maximum;

    private Long minLength;

    private Long maxLength;

    private String pattern;

    private List<Schema> anyOf;

    /** Jackson handles the name clash: the wire field is "enum". */
    @com.fasterxml.jackson.annotation.JsonProperty("enum")
    public List<String> getEnumValues() {
        return enumValues;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("enum")
    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    /** Default value. {@code default} is a Java keyword, hence the field name; the wire name is still {@code default}. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("default")
    private Object defaultValue;
    /** An example value. @since 2.7.0 */
    private Object example;
}
