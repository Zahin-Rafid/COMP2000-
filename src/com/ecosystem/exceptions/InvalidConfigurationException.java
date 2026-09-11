package com.ecosystem.exceptions;

/**
 * Thrown when configuration parameters or world initialization values are malformed.
 */
public class InvalidConfigurationException extends EcosystemException {
    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
