package com.ecosystem.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe EventBus implementing the Observer pattern.
 * Manages event subscription and broadcasts ecosystem notifications without tight coupling.
 */
public class EcosystemEventBus {
    private static final EcosystemEventBus INSTANCE = new EcosystemEventBus();

    private final List<EcosystemListener> listeners = new CopyOnWriteArrayList<>();
    private volatile long currentTick = 0;

    private EcosystemEventBus() {}

    public static EcosystemEventBus getInstance() {
        return INSTANCE;
    }

    public void setCurrentTick(long tick) {
        this.currentTick = tick;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public void subscribe(EcosystemListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(EcosystemListener listener) {
        listeners.remove(listener);
    }

    public void publish(EcosystemEvent event) {
        if (event == null) return;
        EcosystemEvent stampedEvent = (event.tick() <= 0 && currentTick > 0)
                ? new EcosystemEvent(event.type(), event.message(), event.location(), currentTick, event.sourceEntity())
                : event;

        for (EcosystemListener listener : listeners) {
            try {
                listener.onEcosystemEvent(stampedEvent);
            } catch (Exception e) {
                System.err.println("Error notifying ecosystem observer: " + e.getMessage());
            }
        }
    }

    public void clearListeners() {
        listeners.clear();
    }
}
