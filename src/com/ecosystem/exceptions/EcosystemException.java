package com.ecosystem.exceptions;

/**
 * Base checked exception for domain-specific simulation failures and violations.
 */
public class EcosystemException extends Exception {
    public EcosystemException(String message) {
        super(message);
    }

    public EcosystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
