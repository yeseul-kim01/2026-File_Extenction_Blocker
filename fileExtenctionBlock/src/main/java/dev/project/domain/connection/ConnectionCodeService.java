package dev.project.domain.connection;

import dev.project.domain.policy.excep.ConnectionCodeAlreadyUsedException;
import dev.project.domain.policy.excep.ConnectionCodeExpiredException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import dev.project.domain.policy.excep.ConnectionCodeNotFoundException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ConnectionCodeService {

    private final ConnectionProperties props;
    private final ConnectionCodeRepository repo;
    private final SecureRandom random = new SecureRandom();

    public ConnectionCodeService(ConnectionProperties props, ConnectionCodeRepository repo) {
        this.props = props;
        this.repo = repo;
    }

    public IssueResult issue(String tenantId) {
        String code = generateCode(props.codePrefix());
        Instant now = Instant.now();
        Instant expiresAt = now.plus(props.codeTtlSeconds(), ChronoUnit.SECONDS);

        ConnectionCode cc = new ConnectionCode(code, tenantId, expiresAt, false, now);
        repo.save(cc);

        return new IssueResult(code, expiresAt);
    }

    public VerifyResult verifyAndConsume(String rawCode) {
        String code = normalizeCode(rawCode);

        ConnectionCode cc = repo.findByCode(code)
                .orElseThrow(ConnectionCodeNotFoundException::new);

        Instant now = Instant.now();

        if (cc.isExpired(now)) {
            repo.delete(code);
            throw new ConnectionCodeExpiredException();
        }

        if (cc.used()) {
            throw new ConnectionCodeAlreadyUsedException();
        }

        repo.save(cc.markUsed());

        return new VerifyResult(cc.tenantId());
    }

    private String generateCode(String prefix) {
        // 예: KSB-7F92QX (6 chars)
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 헷갈리는 I/O/1/0 제거
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return prefix + "-" + sb;
    }

    private String normalizeCode(String raw) {
        if (raw == null) return "";
        return raw.trim().toUpperCase();
    }

    // 만료 코드 정리 (in-memory 운영 안정화)
    @Scheduled(fixedDelayString = "#{@connectionProperties.cleanupIntervalSeconds() * 1000}")
    public void cleanupExpired() {
        repo.deleteExpired(Instant.now());
    }

    public record IssueResult(String code, Instant expiresAt) {}
    public record VerifyResult(String tenantId) {}
}
