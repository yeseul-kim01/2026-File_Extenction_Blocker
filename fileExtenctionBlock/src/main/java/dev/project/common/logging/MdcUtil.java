package dev.project.common.logging;

import org.slf4j.MDC;

import java.util.Map;

public final class MdcUtil {
    private MdcUtil() {}

    public static void withMdc(Map<String, String> values, Runnable action) {
        if (values != null) values.forEach(MDC::put);
        try {
            action.run();
        } finally {
            if (values != null) values.keySet().forEach(MDC::remove);
        }
    }
}


/* 사용방법
import static dev.project.common.logging.MdcUtil.withMdc;

public void updatePolicy(Long policyId, String ext) {
    withMdc(Map.of(
            "event", "policy.update",
            "policyId", String.valueOf(policyId),
            "ext", ext
    ), () -> log.info("policy updated"));
}
 */
