package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.claude.ClaudeClient;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards against the Lombok 1.16 -> 1.18 regression that shipped in 1.10.0.
 * <p>
 * Lombok 1.16 quietly emitted a private no-args constructor for {@code @Builder}
 * classes; 1.18 does not. Jackson needs that constructor, so every response type
 * built with a builder silently stopped deserializing with
 * "no Creators, like default constructor, exist".
 * <p>
 * A Lombok {@code @Builder} leaves a runtime-visible trace — the generated static
 * {@code builder()} method — so this walks the compiled output and asserts that
 * every such class still declares a no-args constructor.
 */
public class NoArgsConstructorContractTest {

    private static final String BASE_PACKAGE = "me.vacuity.ai.sdk";

    @Test
    public void everyBuilderClassKeepsANoArgsConstructor() throws Exception {
        List<Class<?>> classes = scanCompiledClasses();
        assertTrue(classes.size() > 100,
                "expected to scan the whole SDK, only found " + classes.size() + " classes");

        List<String> offenders = new ArrayList<>();
        int builderClasses = 0;

        for (Class<?> type : classes) {
            if (type.isInterface() || type.isEnum() || type.isAnonymousClass()
                    || Modifier.isAbstract(type.getModifiers())) {
                continue;
            }
            if (!hasBuilderMethod(type)) {
                continue;
            }
            builderClasses++;
            if (!hasNoArgsConstructor(type)) {
                offenders.add(type.getName());
            }
        }

        assertTrue(builderClasses > 50,
                "expected many @Builder classes, found " + builderClasses);

        if (!offenders.isEmpty()) {
            fail("These @Builder classes have no no-args constructor, so Jackson cannot "
                    + "deserialize them (check the Lombok version):\n  "
                    + String.join("\n  ", offenders));
        }
    }

    private boolean hasBuilderMethod(Class<?> type) {
        try {
            return Modifier.isStatic(type.getDeclaredMethod("builder").getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean hasNoArgsConstructor(Class<?> type) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Walks the main output directory rather than using a classpath-scanning
     * dependency. Resolved from a main class's code source — looking the package
     * up by resource name would find test-classes first and scan the wrong tree.
     */
    private List<Class<?>> scanCompiledClasses() throws Exception {
        URL location = ClaudeClient.class.getProtectionDomain().getCodeSource().getLocation();
        File root = new File(new File(location.toURI()), BASE_PACKAGE.replace('.', '/'));
        if (!root.isDirectory()) {
            fail("could not locate compiled main classes, looked in " + root);
        }
        List<Class<?>> found = new ArrayList<>();
        collect(root, BASE_PACKAGE, found);
        return found;
    }

    private void collect(File dir, String packageName, List<Class<?>> out) {
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.isDirectory()) {
                collect(entry, packageName + "." + entry.getName(), out);
            } else if (entry.getName().endsWith(".class")) {
                String simple = entry.getName().substring(0, entry.getName().length() - 6);
                try {
                    out.add(Class.forName(packageName + "." + simple, false,
                            getClass().getClassLoader()));
                } catch (Throwable ignored) {
                    // classes whose optional dependencies are absent — not our concern here
                }
            }
        }
    }
}
