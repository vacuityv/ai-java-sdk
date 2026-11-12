package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Every model class serializes cleanly and emits only real wire fields.
 * <p>
 * Added in 2.7.0 after an audit round trip turned up six derived getters —
 * {@code getSingleContent()}, {@code isRefusal()}, {@code isCompleted()} and
 * others — that Jackson was serializing as if they were API fields. One of
 * them also threw a NullPointerException, so writing a ChatResponse back out
 * could crash. A helper method on a model class is easy to add and easy to
 * forget to exclude; this is what notices.
 * <p>
 * The judgement is Jackson's own, not a hand-written rule: it introspects each
 * class the way the client's mapper will, and every property it decides to
 * serialize must be backed by a field. A property with only a getter is a
 * derived value leaking out as a fake key.
 *
 * @author vacuity
 */
public class SerializationContractTest {

    private static final String BASE_PACKAGE = "me.vacuity.ai.sdk";

    private static ObjectMapper mapperFor(Class<?> type) {
        String name = type.getName();
        if (name.contains(".openai.")) {
            return OpenaiClient.defaultObjectMapper();
        }
        if (name.contains(".gemini.")) {
            return GeminiClient.defaultObjectMapper();
        }
        if (name.contains(".claude.")) {
            return ClaudeClient.defaultObjectMapper();
        }
        return null;
    }

    @Test
    public void noModelClassSerializesAPropertyWithoutABackingField() throws Exception {
        List<String> offenders = new ArrayList<>();
        int checked = 0;
        for (Class<?> type : modelClasses()) {
            ObjectMapper mapper = mapperFor(type);
            if (mapper == null) {
                continue;
            }
            checked++;
            BeanDescription description = mapper.getSerializationConfig()
                    .introspect(mapper.constructType(type));
            for (BeanPropertyDefinition property : description.findProperties()) {
                if (property.couldSerialize() && !property.hasField()) {
                    offenders.add(type.getName() + "." + property.getName()
                            + "  (from " + property.getAccessor() + ")");
                }
            }
        }
        assertTrue(checked > 150, "expected to check the whole SDK, only checked " + checked);
        if (!offenders.isEmpty()) {
            fail("These derived getters would be written to JSON as if they were API fields. "
                    + "Mark them @JsonIgnore:\n  " + String.join("\n  ", offenders));
        }
    }

    /**
     * Serializing an empty instance must not throw. A derived getter that
     * dereferences a null field turns a harmless write — logging a response,
     * caching it — into a crash.
     */
    @Test
    public void everyModelClassSerializesWhenEmpty() throws Exception {
        List<String> offenders = new ArrayList<>();
        for (Class<?> type : modelClasses()) {
            ObjectMapper mapper = mapperFor(type);
            if (mapper == null) {
                continue;
            }
            try {
                Constructor<?> constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                mapper.writeValueAsString(constructor.newInstance());
            } catch (Exception e) {
                Throwable root = e;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                offenders.add(type.getName() + ": " + root);
            }
        }
        if (!offenders.isEmpty()) {
            fail("These classes throw when an empty instance is serialized:\n  "
                    + String.join("\n  ", offenders));
        }
    }

    // ---- discovery: concrete classes with a static builder() and a no-args constructor

    private static List<Class<?>> modelClasses() throws Exception {
        URL location = ClaudeClient.class.getProtectionDomain().getCodeSource().getLocation();
        File root = new File(new File(location.toURI()), BASE_PACKAGE.replace('.', '/'));
        List<Class<?>> all = new ArrayList<>();
        collect(root, BASE_PACKAGE, all);
        List<Class<?>> models = new ArrayList<>();
        for (Class<?> type : all) {
            if (type.isInterface() || type.isEnum() || type.isAnonymousClass()
                    || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            try {
                if (!Modifier.isStatic(type.getDeclaredMethod("builder").getModifiers())) {
                    continue;
                }
                type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                continue;
            }
            models.add(type);
        }
        return models;
    }

    private static void collect(File dir, String pkg, List<Class<?>> out) {
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.isDirectory()) {
                collect(entry, pkg + "." + entry.getName(), out);
            } else if (entry.getName().endsWith(".class") && !entry.getName().contains("Builder")) {
                String name = pkg + "." + entry.getName().substring(0, entry.getName().length() - 6);
                try {
                    out.add(Class.forName(name, false, SerializationContractTest.class.getClassLoader()));
                } catch (Throwable ignored) {
                    // not loadable on its own; skip
                }
            }
        }
    }
}
