package dev.project.domain.policy;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FixedExtension {
    BAT("bat"),
    CMD("cmd"),
    COM("com"),
    CPL("cpl"),
    EXE("exe"),
    SCR("scr"),
    JS("js");

    private final String ext;

    FixedExtension(String ext) {
        this.ext = ext;
    }

    public String ext() {
        return ext;
    }

    public static Set<String> asSet() {
        return Arrays.stream(values())
                .map(FixedExtension::ext)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isFixed(String normalizedExt) {
        if (normalizedExt == null) return false;
        for (FixedExtension v : values()) {
            if (v.ext.equals(normalizedExt)) return true;
        }
        return false;
    }
}
