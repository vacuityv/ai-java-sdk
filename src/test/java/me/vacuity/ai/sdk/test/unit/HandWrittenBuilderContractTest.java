package me.vacuity.ai.sdk.test.unit;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A hand-written {@code Builder} must carry every field of its class.
 * <p>
 * Lombok's builders track the class automatically; a hand-written one does
 * not, and its {@code build()} copies only the fields someone remembered. In
 * 2.7.0 all three {@code ChatFunction} classes gained fields — OpenAI's
 * {@code strict}, Gemini's {@code parametersJsonSchema} and friends, Claude's
 * {@code eagerInputStreaming} and {@code inputExamples} — and none of their
 * builders could set them. The field-binding tests passed regardless, because
 * Jackson sets fields through setters, not through the builder; only a caller
 * using the builder would have found out.
 * <p>
 * Checked by actually building: each field is given a sentinel through its
 * builder method, and the built object must hand that same value back.
 *
 * @author vacuity
 */
public class HandWrittenBuilderContractTest {

    /** Set through a combined builder method rather than one of their own. */
    private static final Set<String> SET_INDIRECTLY = new HashSet<>(Arrays.asList(
            "parametersClass", "executor"));

    private static final List<Class<?>> CLASSES = Arrays.asList(
            me.vacuity.ai.sdk.claude.entity.ChatFunction.class,
            me.vacuity.ai.sdk.gemini.entity.ChatFunction.class,
            me.vacuity.ai.sdk.openai.entity.ChatFunction.class);

    @Test
    public void everyFieldCanBeSetThroughTheBuilderAndSurvivesBuild() throws Exception {
        List<String> problems = new ArrayList<>();
        for (Class<?> type : CLASSES) {
            Class<?> builderType = Class.forName(type.getName() + "$Builder");
            Method builderFactory = type.getDeclaredMethod("builder");
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || SET_INDIRECTLY.contains(field.getName())) {
                    continue;
                }
                Method setter;
                try {
                    setter = builderType.getMethod(field.getName(), field.getType());
                } catch (NoSuchMethodException e) {
                    problems.add(type.getName() + "." + field.getName()
                            + ": the builder has no method to set it");
                    continue;
                }
                Object sentinel = sentinelFor(field.getType());
                if (sentinel == null) {
                    continue;
                }
                Object builder = builderFactory.invoke(null);
                // name is required (@NonNull on Claude's), so give every build a
                // baseline one; the field under test is set after it and wins.
                builderType.getMethod("name", String.class).invoke(builder, "baseline");
                setter.invoke(builder, sentinel);
                Object built = builderType.getMethod("build").invoke(builder);
                field.setAccessible(true);
                if (field.get(built) != sentinel && !sentinel.equals(field.get(built))) {
                    problems.add(type.getName() + "." + field.getName()
                            + ": set on the builder but dropped by build()");
                }
            }
        }
        assertTrue(CLASSES.size() == 3);
        if (!problems.isEmpty()) {
            fail("Hand-written builders out of step with their classes:\n  "
                    + String.join("\n  ", problems));
        }
    }

    private static Object sentinelFor(Class<?> type) {
        if (type == String.class) {
            return "sentinel";
        }
        if (type == Boolean.class) {
            return Boolean.TRUE;
        }
        if (type == Integer.class) {
            return 42;
        }
        if (type == Object.class) {
            return new Object();
        }
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<Object>();
        }
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
