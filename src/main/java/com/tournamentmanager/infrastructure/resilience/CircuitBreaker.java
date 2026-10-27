package com.tournamentmanager.infrastructure.resilience;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Circuit breaker con tres estados:
 * CLOSED: las llamadas pasan; tras {@code failureThreshold} fallos consecutivos pasa a OPEN.
 * OPEN: las llamadas se rechazan de inmediato durante {@code openDuration}.
 * HALF_OPEN: se permite una llamada de prueba; si tiene éxito vuelve a CLOSED, si falla vuelve a OPEN.
 */
public final class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final String name;
    private final int failureThreshold;
    private final Duration openDuration;
    private final Clock clock;

    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private Instant openedAt;

    public CircuitBreaker(String name, int failureThreshold, Duration openDuration, Clock clock) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException("failureThreshold debe ser al menos 1");
        }
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
        this.clock = clock;
    }

    public synchronized State state() {
        if (state == State.OPEN && !clock.instant().isBefore(openedAt.plus(openDuration))) {
            state = State.HALF_OPEN;
        }
        return state;
    }

    public <T> T execute(Supplier<T> call) {
        if (state() == State.OPEN) {
            throw new CircuitOpenException(name);
        }
        try {
            T result = call.get();
            onSuccess();
            return result;
        } catch (RuntimeException ex) {
            onFailure();
            throw ex;
        }
    }

    private synchronized void onSuccess() {
        consecutiveFailures = 0;
        state = State.CLOSED;
    }

    private synchronized void onFailure() {
        consecutiveFailures++;
        if (state == State.HALF_OPEN || consecutiveFailures >= failureThreshold) {
            state = State.OPEN;
            openedAt = clock.instant();
        }
    }
}
