package dev.project.domain.policy.excep;

import dev.project.domain.DomainException;

public class InvalidExtensionException extends DomainException {

    public static final String CODE = "POLICY_INVALID_EXTENSION";

    public InvalidExtensionException(String raw) {
        super(
                CODE,
                "Invalid extension: '" + raw + "'. Use 1-20 chars [a-z0-9] without dot."
        );
    }
}
