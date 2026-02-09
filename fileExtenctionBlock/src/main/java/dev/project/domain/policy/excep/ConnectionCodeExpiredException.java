package dev.project.domain.policy.excep;

import dev.project.domain.DomainException;

public class ConnectionCodeExpiredException extends DomainException {

    public static final String CODE = "CONNECTION_CODE_EXPIRED";

    public ConnectionCodeExpiredException() {
        super(CODE, "Connection code expired.");
    }
}
