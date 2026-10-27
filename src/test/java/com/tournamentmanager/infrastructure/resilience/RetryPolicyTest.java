package com.tournamentmanager.infrastructure.resilience;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {

    @Test
    void retriesWithExponentialBackoffUntilSuccess() {
        List<Long> sleeps = new ArrayList<>();
        RetryPolicy policy = new RetryPolicy(4, Duration.ofMillis(500), 2.0, Duration.ofSeconds(10),
                d -> sleeps.add(d.toMillis()));
        AtomicInteger calls = new AtomicInteger();

        String result = policy.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("temporal");
            }
            return "listo";
        });

        assertThat(result).isEqualTo("listo");
        assertThat(sleeps).containsExactly(500L, 1000L);
    }

    @Test
    void capsBackoffAtMaximum() {
        List<Long> sleeps = new ArrayList<>();
        RetryPolicy policy = new RetryPolicy(4, Duration.ofMillis(500), 2.0, Duration.ofMillis(1200),
                d -> sleeps.add(d.toMillis()));

        assertThatThrownBy(() -> policy.execute(() -> {
            throw new IllegalStateException("siempre");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(sleeps).containsExactly(500L, 1000L, 1200L);
    }

    @Test
    void doesNotRetryWhenCircuitIsOpen() {
        RetryPolicy policy = new RetryPolicy(3, Duration.ofMillis(1), 2.0, Duration.ofMillis(5), d -> { });
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> policy.execute(() -> {
            calls.incrementAndGet();
            throw new CircuitOpenException("x");
        })).isInstanceOf(CircuitOpenException.class);

        assertThat(calls.get()).isEqualTo(1);
    }
}
