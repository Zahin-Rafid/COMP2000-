package com.ecosystem.exceptions;

/**
 * Thrown when an organism's genetic chromosome contains invalid, NaN, or out-of-range trait values.
 */
public class InvalidGenomeException extends EcosystemException {
    private final String traitName;
    private final double invalidValue;

    public InvalidGenomeException(String traitName, double invalidValue, String reason) {
        super(String.format("Invalid genome trait '%s' with value %.3f: %s", traitName, invalidValue, reason));
        this.traitName = traitName;
        this.invalidValue = invalidValue;
    }

    public String getTraitName() {
        return traitName;
    }

    public double getInvalidValue() {
        return invalidValue;
    }
}
