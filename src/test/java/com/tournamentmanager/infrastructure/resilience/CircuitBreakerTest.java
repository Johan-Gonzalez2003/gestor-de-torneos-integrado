package com.tournamentmanager.infrastructure.resilience;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircuitBreakerTest {

    /** Reloj manual para controlar el paso del tiempo sin esperar de verdad. */
    static final class ManualClock extends Clock {
        private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-10-27T10:00:00Z"));
        void advance(Duration d) { now.updateAndGet(i -> i.plus(d)); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now.get(); }
    }

    private static void fail(CircuitBreaker cb) {
        try {
            cb.execute(() -> {
                throw new IllegalStateException("fallo");
            });
        } catch (IllegalStateException ignored) {
            // esperado
        }
    }

    @Test
    void opensAfterThresholdFailures() {
        ManualClock clock = new ManualClock();
        CircuitBreaker cb = new CircuitBreaker("test", 3, Duration.ofSeconds(30), clock);

        fail(cb);
        fail(cb);
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.CLOSED);

        fail(cb);
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void rejectsCallsWhileOpen() {
        ManualClock clock = new ManualClock();
        CircuitBreaker cb = new CircuitBreaker("test", 1, Duration.ofSeconds(30), clock);
        fail(cb);

        assertThatThrownBy(() -> cb.execute(() -> "nunca")).isInstanceOf(CircuitOpenException.class);
    }

    @Test
    void movesToHalfOpenAfterWaitAndClosesOnSuccess() {
        ManualClock clock = new ManualClock();
        CircuitBreaker cb = new CircuitBreaker("test", 1, Duration.ofSeconds(30), clock);
        fail(cb);

        clock.advance(Duration.ofSeconds(30));
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        assertThat(cb.execute(() -> "ok")).isEqualTo("ok");
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void reopensWhenHalfOpenTrialFails() {
        ManualClock clock = new ManualClock();
        CircuitBreaker cb = new CircuitBreaker("test", 1, Duration.ofSeconds(30), clock);
        fail(cb);
        clock.advance(Duration.ofSeconds(30));

        fail(cb);

        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
