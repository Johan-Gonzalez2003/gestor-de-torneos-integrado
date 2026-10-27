<<<<<<< HEAD
# Gestor de Torneos de Videojuegos

API REST en Java 17 con Spring Boot 3, organizada con **arquitectura hexagonal** y **DDD**.
Integra dos fuentes de datos abiertos colombianos (datos.gov.co y DANE) con **circuit breaker**,
**retry con backoff exponencial** y análisis descriptivo con **DuckDB**.

Proyecto semestral del Taller de Programación 2026-262, Universidad Manuela Beltrán (guía 6, sesiones 11 y 12).

## Arquitectura

```text
src/main/java/com/tournamentmanager/
├── domain/          # Entidades y reglas puras (sin frameworks)
├── application/     # Casos de uso (services) y puertos (interfaces)
├── api/             # Controladores REST, DTOs y manejo de errores HTTP
└── infrastructure/  # Adaptadores: JPA, OpenAI, Socrata, DuckDB, RAG, resiliencia, configuración
```

Las decisiones de arquitectura están en `docs/adr/`:
- ADR-001: arquitectura hexagonal y bounded contexts.
- ADR-002: circuit breaker y retry implementados a mano.

## Requisitos

- JDK 17 (el proyecto compila con 17; también funciona con 21)
- Maven 3.8+
- MySQL 8+ (solo para ejecutar la aplicación; las pruebas usan H2)
- Clave de OpenAI (opcional; sin ella el asistente responde con un aviso)

## Configuración

Copia `.env.example` y define las variables en tu terminal o IDE. **No subas claves al repositorio.**

Los IDs de dataset de datos.gov.co y DANE vienen como `CAMBIAR_ID_DATASET`. Antes de usar
el análisis, reemplázalos por los IDs reales del portal (ver la sección "Fuentes de datos abiertos").

## Ejecutar

```bash
mvn clean verify       # compila y ejecuta todas las pruebas
mvn spring-boot:run    # levanta la API en http://localhost:8080
```

Documentación interactiva: http://localhost:8080/swagger-ui.html

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/tournaments` | Listar torneos |
| GET | `/api/tournaments/{id}` | Consultar torneo |
| POST | `/api/tournaments` | Crear torneo |
| PUT | `/api/tournaments/{id}` | Actualizar torneo |
| DELETE | `/api/tournaments/{id}` | Eliminar torneo |
| POST | `/api/assistant/ask` | Asistente con modos `ZERO_SHOT`, `FEW_SHOT` y `COT` |
| GET | `/api/opendata/summary?limit=100` | Análisis con DuckDB de cada fuente abierta |

### Ejemplo de creación

```json
{
  "name": "Copa Universitaria",
  "videoGame": "Valorant",
  "startDate": "2030-06-15",
  "maxTeams": 8,
  "status": "REGISTRATION_OPEN",
  "description": "Torneo de eliminación directa"
}
```

### Ejemplo de datos abiertos

La respuesta trae un bloque por fuente. Si una fuente cae, ese bloque llega con
`"status": "UNAVAILABLE"` y el resto sigue funcionando:

```json
[
  { "sourceKey": "datos-gov-co", "status": "OK", "rowCount": 100, "average": 42.5, "topCategories": [...] },
  { "sourceKey": "dane", "status": "UNAVAILABLE", "message": "La fuente DANE no está disponible: ..." }
]
```

## Resiliencia

| Parámetro | Valor |
|---|---|
| Circuit breaker: fallos consecutivos para abrir | 3 |
| Circuit breaker: tiempo en OPEN antes de HALF_OPEN | 30 s |
| Retry: intentos totales | 3 |
| Retry: backoff inicial, multiplicador, máximo | 500 ms, x2, 4 s |

## Fuentes de datos abiertos

Ambas fuentes usan la API Socrata (SODA) que publica datos.gov.co:
`{base-url}/resource/{dataset-id}.json?$limit=N`.

Para configurar una fuente:
1. Busca el dataset en https://www.datos.gov.co y copia su identificador de cuatro bloques (por ejemplo `abcd-1234`).
2. Revisa qué campo es numérico y cuál es categórico, y ponlos en `*_NUMERIC_FIELD` y `*_CATEGORY_FIELD`.
3. Prueba el endpoint en el navegador antes de ejecutar la aplicación.

## Pruebas

```bash
mvn clean verify
```

- `domain/`: reglas de validación del torneo.
- `application/`: servicios con repositorios simulados con Mockito.
- `infrastructure/resilience/`: transiciones del circuit breaker y backoff del retry, con reloj manual.
- `api/`: integración con Spring Boot y H2.

## Control de versiones

Commits con Conventional Commits, por ejemplo `feat: add DuckDB summary for open-data sources`.
=======
# Proyecto-gestor-de-torneos-integrado
>>>>>>> e94f88fe0ae923618c521c3e308d83c45e34aff7
