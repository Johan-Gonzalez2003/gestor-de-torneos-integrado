package com.torneos.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Bloquea temporalmente un usuario tras varios intentos fallidos de login
 * (defensa contra fuerza bruta y consumo abusivo de recursos).
 */
@Service
public class LoginAttemptService {

    private record Attempts(int failures, Instant lockedUntil) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxFailures;
    private final Duration lockDuration;
    private final Clock clock;

    public LoginAttemptService(@Value("${app.security.max-failed-attempts}") int maxFailures,
                               @Value("${app.security.lock-minutes}") long lockMinutes,
                               Clock clock) {
        this.maxFailures = maxFailures;
        this.lockDuration = Duration.ofMinutes(lockMinutes);
        this.clock = clock;
    }

    public boolean isLocked(String username) {
        Attempts current = attempts.get(username);
        return current != null
                && current.lockedUntil() != null
                && clock.instant().isBefore(current.lockedUntil());
    }

    public void registerFailure(String username) {
        attempts.compute(username, (key, old) -> {
            // Si el bloqueo anterior ya venció, empezamos a contar desde cero
            int previous = (old == null || hasExpired(old)) ? 0 : old.failures();
            int failures = previous + 1;
            Instant lockedUntil = failures >= maxFailures ? clock.instant().plus(lockDuration) : null;
            return new Attempts(failures, lockedUntil);
        });
    }

    private boolean hasExpired(Attempts attempts) {
        return attempts.lockedUntil() != null && !clock.instant().isBefore(attempts.lockedUntil());
    }

    public void registerSuccess(String username) {
        attempts.remove(username);
    }
}
