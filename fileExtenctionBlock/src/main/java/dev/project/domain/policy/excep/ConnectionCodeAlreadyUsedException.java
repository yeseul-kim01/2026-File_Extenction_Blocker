package dev.project.domain.policy.excep;

import dev.project.domain.DomainException;

public class ConnectionCodeAlreadyUsedException extends DomainException {

    public static final String CODE = "CONNECTION_CODE_ALREADY_USED";

    public ConnectionCodeAlreadyUsedException() {
        super(CODE, "Connection code already used.");
    }
}
