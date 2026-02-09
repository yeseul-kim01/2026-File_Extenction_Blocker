package dev.project.domain.connection;

import java.time.Instant;

public record ConnectionCode(
        String code,
        String tenantId,
        Instant expiresAt,
        boolean used,
        Instant createdAt
) {
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public ConnectionCode markUsed() {
        return new ConnectionCode(code, tenantId, expiresAt, true, createdAt);
    }
}
