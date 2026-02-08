package dev.project.domain.policy.excep;

import dev.project.domain.DomainException;

public class DuplicateExtensionException extends DomainException {

    public static final String CODE = "POLICY_EXTENSION_DUPLICATE";

    public DuplicateExtensionException(String ext) {
        super(CODE, "Extension already exists: '" + ext + "'.");
    }
}
