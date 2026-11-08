package com.ecosystem.events;

import com.ecosystem.core.Vector2D;
import com.ecosystem.model.Entity;

/**
 * Immutable event record dispatched through the Observer event bus.
 */
public record EcosystemEvent(
        EcosystemEventType type,
        String message,
        Vector2D location,
        long tick,
        Entity sourceEntity
) {
    public static EcosystemEvent of(EcosystemEventType type, String message, Vector2D location, long tick, Entity source) {
        return new EcosystemEvent(type, message, location, tick, source);
    }
}

