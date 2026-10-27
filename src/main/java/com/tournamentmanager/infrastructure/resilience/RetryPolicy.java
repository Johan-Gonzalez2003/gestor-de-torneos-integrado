package com.tournamentmanager.infrastructure.resilience;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Reintenta con backoff exponencial: espera initialBackoff, luego initialBackoff * multiplier, etc.,
 * con un máximo de maxBackoff. No reintenta si el circuito está abierto, porque reintentar sería inútil.
 */
public final class RetryPolicy {

    public interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    private final int maxAttempts;
    private final Duration initialBackoff;
    private final double multiplier;
    private final Duration maxBackoff;
    private final Sleeper sleeper;

    public RetryPolicy(int maxAttempts, Duration initialBackoff, double multiplier,
                       Duration maxBackoff, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts debe ser al menos 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.multiplier = multiplier;
        this.maxBackoff = maxBackoff;
        this.sleeper = sleeper;
    }

    public static RetryPolicy withThreadSleep(int maxAttempts, Duration initialBackoff,
                                              double multiplier, Duration maxBackoff) {
        return new RetryPolicy(maxAttempts, initialBackoff, multiplier, maxBackoff,
                duration -> Thread.sleep(duration.toMillis()));
    }

    public <T> T execute(Supplier<T> call) {
        Duration delay = initialBackoff;
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return call.get();
            } catch (CircuitOpenException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                last = ex;
                if (attempt == maxAttempts) {
                    break;
                }
                pause(delay);
                delay = nextDelay(delay);
            }
        }
        throw last;
    }

    Duration nextDelay(Duration current) {
        long next = (long) (current.toMillis() * multiplier);
        return Duration.ofMillis(Math.min(next, maxBackoff.toMillis()));
    }

    private void pause(Duration duration) {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Reintento interrumpido", ex);
        }
    }
}
