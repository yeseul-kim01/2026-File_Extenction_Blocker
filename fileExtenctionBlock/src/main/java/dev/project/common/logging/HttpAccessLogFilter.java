package dev.project.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HttpAccessLogFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("ACCESS_LOG");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // return request.getRequestURI().startsWith("/assets");
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startNs = System.nanoTime();
        int status = 500;

        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000;

            String method = request.getMethod();
            String path = request.getRequestURI();
            String query = request.getQueryString();
            String fullPath = (query == null || query.isBlank()) ? path : (path + "?" + query);

            String clientIp = getClientIp(request);
            String userAgent = safeHeader(request, "User-Agent");

            String traceId = MDC.get("traceId");

            ACCESS_LOG.info(
                    "event=http.access method={} path=\"{}\" status={} durationMs={} clientIp={} userAgent=\"{}\" traceId={}",
                    method,
                    fullPath,
                    status,
                    durationMs,
                    clientIp,
                    sanitizeForLog(userAgent),
                    (traceId == null ? "-" : traceId)
            );
        }
    }

    private static String safeHeader(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return v == null ? "-" : v;
    }

    /**
     * ALB/ECS 환경을 고려한 Client IP 추출
     */
    private static String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }
        String xrip = req.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) return xrip.trim();
        return req.getRemoteAddr();
    }

    private static String sanitizeForLog(String value) {
        if (value == null) return "-";
        return value.replace("\n", " ").replace("\r", " ").replace("\t", " ");
    }
}
