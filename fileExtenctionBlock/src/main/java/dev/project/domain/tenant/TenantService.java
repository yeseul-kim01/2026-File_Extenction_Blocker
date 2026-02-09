package dev.project.domain.tenant;

import dev.project.common.logging.MdcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.SplittableRandom;

@Slf4j
@Service
public class TenantService {

    private final SplittableRandom random = new SplittableRandom();

    public IssueTenantResult issueTenant() {
        String tenantId = generateTenantId();
        MdcUtil.withMdc(Map.of(
                "event", "tenant.make.tenantId",
                "tenantId", tenantId
        ), () -> log.info("made tenant Id "));
        return new IssueTenantResult(tenantId, Instant.now());
    }

    private String generateTenantId() {
        // 예: t_7F92QX1K2M8P
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // I/O/1/0 제거
        StringBuilder sb = new StringBuilder("t_");
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public record IssueTenantResult(
            String tenantId,
            Instant createdAt
    ) {}
}
