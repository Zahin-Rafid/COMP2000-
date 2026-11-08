package com.ecosystem.events;

/**
 * Functional Observer interface responding to ecosystem lifecycle and environmental events.
 */
@FunctionalInterface
public interface EcosystemListener {

    /**
     * Invoked when an ecosystem event occurs.
     *
     * @param event The immutable event record.
     */
    void onEcosystemEvent(EcosystemEvent event);
}
