package dev.project.infra;

import dev.project.domain.connection.ConnectionCode;
import dev.project.domain.connection.ConnectionCodeRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConnectionCodeRepository implements ConnectionCodeRepository {

    private final Map<String, ConnectionCode> store = new ConcurrentHashMap<>();

    @Override
    public ConnectionCode save(ConnectionCode code) {
        store.put(code.code(), code);
        return code;
    }

    @Override
    public Optional<ConnectionCode> findByCode(String code) {
        return Optional.ofNullable(store.get(code));
    }

    @Override
    public void delete(String code) {
        store.remove(code);
    }

    @Override
    public int deleteExpired(Instant now) {
        int before = store.size();
        store.entrySet().removeIf(e -> e.getValue().isExpired(now));
        return before - store.size();
    }
}
