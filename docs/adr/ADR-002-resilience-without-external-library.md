# ADR-002: Implement circuit breaker and retry in-house for open-data sources

## Status
Accepted

## Context
Calls to public Colombian data portals can be slow or unavailable. The course requires a circuit
breaker with three states, retries with exponential backoff, and graceful degradation. Resilience4j
is a mature option, but adopting it would hide the state machine that the course evaluates.

## Decision
Implement two small classes in `infrastructure/resilience`:

- `CircuitBreaker`: states CLOSED, OPEN, and HALF_OPEN. It opens after a configurable number of
  consecutive failures, rejects calls while open, allows a trial call after the wait time, and
  closes again on success or reopens on failure. The clock is injected so tests are deterministic.
- `RetryPolicy`: retries with exponential backoff, capped at a maximum delay. It never retries
  when the circuit is open.

`ResilientOpenDataSource` is a decorator. Retry wraps the circuit breaker, so every attempt passes
through the circuit. When all attempts fail, the decorator raises `OpenDataUnavailableException`,
and `OpenDataService` reports that source as `UNAVAILABLE` without failing the whole response.

Default settings (in `AppConfig`): circuit opens after 3 consecutive failures and waits 30 seconds;
retry makes 3 attempts with 500 ms initial backoff, multiplier 2, and a 4 s cap.

## Consequences
Positive:
- The behavior of each state is visible and covered by unit tests with a manual clock.
- No extra runtime dependency.

Negative:
- The implementation is simpler than Resilience4j. It is not thread-safe under heavy concurrency
  beyond synchronized state transitions, and it has no metrics. For production, Resilience4j is
  the better choice.
