package dev.project.domain.connection;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.connection")
public record ConnectionProperties(
        int codeTtlSeconds,
        String codePrefix,
        int cleanupIntervalSeconds
) {}
