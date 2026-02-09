package dev.project.domain.connection;

import java.time.Instant;
import java.util.Optional;

public interface ConnectionCodeRepository {
    ConnectionCode save(ConnectionCode code);
    Optional<ConnectionCode> findByCode(String code);
    void delete(String code);
    int deleteExpired(Instant now);
}
