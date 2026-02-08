package dev.project.domain.policy.excep;

import dev.project.domain.DomainException;

public class ExtensionLimitExceededException extends DomainException {

    public static final String CODE = "POLICY_EXTENSION_LIMIT_EXCEEDED";

    public ExtensionLimitExceededException(int limit) {
        super(CODE, "Extension limit exceeded. Max allowed = " + limit + ".");
    }
}
