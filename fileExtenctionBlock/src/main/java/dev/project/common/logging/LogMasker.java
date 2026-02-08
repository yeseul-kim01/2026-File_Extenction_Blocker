package dev.project.common.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogMasker {

    private LogMasker() {}

    private static final Pattern AUTH_HEADER = Pattern.compile("(?i)(authorization\\s*[:=]\\s*)(bearer|basic)\\s+([^\\s\"']+)");
    private static final Pattern KV_SECRETS = Pattern.compile("(?i)\\b(api[_-]?key|token|secret|password|passwd|access[_-]?key|private[_-]?key)\\b\\s*[:=]\\s*([^\\s,\"'}]+)");
    private static final Pattern AWS_ACCESS_KEY = Pattern.compile("\\b(AKIA|ASIA)[0-9A-Z]{16}\\b");
    private static final Pattern LONG_TOKEN = Pattern.compile("\\b([A-Za-z0-9+/]{24,}={0,2}|[a-f0-9]{32,})\\b", Pattern.CASE_INSENSITIVE);

    public static String mask(String input) {
        if (input == null || input.isBlank()) return input;

        String s = input;

        // Authorization 헤더 마스킹
        s = replaceGroup(s, AUTH_HEADER, 3, "***");

        // key=value 류 마스킹
        s = replaceGroup(s, KV_SECRETS, 2, "***");

        // AWS Access Key 마스킹
        s = AWS_ACCESS_KEY.matcher(s).replaceAll("***AWS_ACCESS_KEY***");

        s = LONG_TOKEN.matcher(s).replaceAll("***");

        return s;
    }

    private static String replaceGroup(String input, Pattern pattern, int group, String replacement) {
        Matcher m = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String g = m.group(group);
            String safe = g == null ? "" : replacement;
            String whole = m.group(0);
            String replaced = whole.replace(g, safe);
            m.appendReplacement(sb, Matcher.quoteReplacement(replaced));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
