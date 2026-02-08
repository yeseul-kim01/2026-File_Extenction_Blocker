package dev.project.domain.policy;

import dev.project.domain.policy.excep.InvalidExtensionException;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ExtensionNormalizer {

    private ExtensionNormalizer() {}

    private static final Pattern EXT_PATTERN = Pattern.compile("^[a-z0-9]{1,20}$");

    /**
     * 사용자 입력 확장자를 DB 저장용으로 정규화
     * - trim
     * - 앞의 '.' 제거
     * - 소문자화
     */
    public static String normalize(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        if (s.startsWith(".")) s = s.substring(1);
        s = s.toLowerCase(Locale.ROOT);

        return s;
    }

    /**
     * 정규화된 확장자 유효성 검사
     */
    public static boolean isValidNormalized(String normalized) {
        if (normalized == null || normalized.isBlank()) return false;
        return EXT_PATTERN.matcher(normalized).matches();
    }

    /**
     * 유효하지 않으면 InvalidExtensionException
     */
    public static String normalizeOrThrow(String raw) {
        String n = normalize(raw);
        if (!isValidNormalized(n)) {
            throw new InvalidExtensionException(raw);
        }
        return n;
    }

}
