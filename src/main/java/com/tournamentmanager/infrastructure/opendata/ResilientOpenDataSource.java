package com.tournamentmanager.infrastructure.opendata;

import com.tournamentmanager.application.exception.OpenDataUnavailableException;
import com.tournamentmanager.application.model.DatasetDefinition;
import com.tournamentmanager.application.port.out.OpenDataSourcePort;
import com.tournamentmanager.infrastructure.resilience.CircuitBreaker;
import com.tournamentmanager.infrastructure.resilience.RetryPolicy;

/**
 * Decorador: envuelve una fuente con retry (por fuera) y circuit breaker (por dentro).
 * Cada intento pasa por el circuito, así que un circuito abierto corta los reintentos.
 */
public final class ResilientOpenDataSource implements OpenDataSourcePort {

    private final OpenDataSourcePort delegate;
    private final CircuitBreaker circuitBreaker;
    private final RetryPolicy retryPolicy;

    public ResilientOpenDataSource(OpenDataSourcePort delegate, CircuitBreaker circuitBreaker,
                                   RetryPolicy retryPolicy) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public DatasetDefinition definition() {
        return delegate.definition();
    }

    @Override
    public String fetchRecordsAsJson(int limit) {
        try {
            return retryPolicy.execute(
                    () -> circuitBreaker.execute(() -> delegate.fetchRecordsAsJson(limit)));
        } catch (RuntimeException ex) {
            throw new OpenDataUnavailableException(
                    "La fuente " + definition().displayName() + " no está disponible: " + ex.getMessage(), ex);
        }
    }
}
