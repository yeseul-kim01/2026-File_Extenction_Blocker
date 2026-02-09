package dev.project.domain.policy.excep;
import dev.project.domain.DomainException;

public class ConnectionCodeNotFoundException extends DomainException {

    public static final String CODE = "CONNECTION_CODE_NOT_FOUND";

    public ConnectionCodeNotFoundException() {
        super(CODE, "Connection code not found.");
    }
}
