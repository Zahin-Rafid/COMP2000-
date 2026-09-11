package com.ecosystem.exceptions;

/**
 * Thrown when an organism attempts to consume a resource, prey, or cell fertility
 * that has been exhausted.
 */
public class ResourceDepletedException extends EcosystemException {
    private final String resourceName;
    private final double requestedAmount;

    public ResourceDepletedException(String resourceName, double requestedAmount) {
        super(String.format("Resource '%s' depleted. Requested amount %.2f is unavailable.", resourceName, requestedAmount));
        this.resourceName = resourceName;
        this.requestedAmount = requestedAmount;
    }

    public String getResourceName() {
        return resourceName;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }
}
