# ADR-001: Adopt hexagonal architecture with DDD bounded contexts

## Status
Accepted

## Context
The tournament manager started as a layered Spring Boot API (controller, service, repository).
The domain entity `Tournament` carried JPA annotations, and the service depended on Spring Data
types. This made business rules hard to test in isolation and made it difficult to add external
data sources (datos.gov.co, DANE) without touching core logic.

## Decision
Organize the code into four layers:

- `domain`: pure Java entities and rules. No framework imports.
- `application`: use cases (services) and ports (interfaces). Depends only on `domain`.
- `api`: REST controllers, DTOs, and HTTP error mapping. Depends on `application`.
- `infrastructure`: adapters for JPA, OpenAI, Socrata/SODA HTTP clients, DuckDB, the in-memory
  knowledge base, resilience patterns, and Spring configuration.

Dependencies point inward. `AppConfig` is the only place where ports are bound to adapters, and
every dependency is passed through a constructor.

Bounded contexts:

- **Tournament**: tournament lifecycle and validation rules.
- **Assistant**: question answering with prompt modes and basic retrieval (RAG).
- **OpenData**: integration with datos.gov.co and DANE, plus descriptive analysis with DuckDB.

## Consequences
Positive:
- Business rules are tested with plain JUnit and Mockito, without starting Spring or a database.
- Each external provider sits behind a port and can be replaced or faked in tests.

Negative:
- The domain model and the JPA entity are mapped in both directions (`TournamentEntity` <-> `Tournament`),
  which adds code.
- The dependency rule must be checked in code review, because the compiler does not enforce it.
