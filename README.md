

# Gestor de Torneos de Videojuegos

API REST en Java 17 con Spring Boot 3, organizada con **arquitectura hexagonal** y **DDD**.
Integra dos fuentes de datos abiertos colombianos (datos.gov.co y DANE) con **circuit breaker**,
**retry con backoff exponencial** y análisis descriptivo con **DuckDB**.

Proyecto semestral del Taller de Programación 2026-262, Universidad Manuela Beltrán (guía 6, sesiones 11 y 12).
# Proyecto-gestor-de-torneos-integrado
# Gestor de Torneos de Videojuegos

API REST desarrollada en Java 17 con Spring Boot 3, organizada mediante arquitectura hexagonal y Domain-Driven Design (DDD).

El proyecto integra fuentes de datos abiertos colombianos, como datos.gov.co y DANE, mecanismos de resiliencia (circuit breaker y retry con backoff exponencial) y análisis descriptivo con DuckDB.

Proyecto semestral del Taller de Programación 2026-262 de la Universidad Manuela Beltrán, correspondiente a la guía 6, sesiones 11 y 12.
>>>>>>> 41d22a3fc7826f06e4bc6354fefada450b179c12

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
├── domain/           # Entidades y reglas de negocio
├── application/      # Casos de uso y puertos
├── api/               # Controladores REST, DTOs y errores HTTP
└── infrastructure/   # Adaptadores, persistencia e integraciones
```

Las decisiones de arquitectura se documentan en `docs/adr/`:

- ADR-001: arquitectura hexagonal y bounded contexts.
- ADR-002: implementación de circuit breaker y retry.

## Requisitos

- JDK 17 o superior.
- Maven 3.8 o superior.
- MySQL 8 o superior para ejecutar la aplicación, según la configuración del proyecto.
- Clave de OpenAI opcional para las funciones del asistente.

Las pruebas utilizan H2.

## Configuración

Revisa el archivo `.env.example` y configura las variables de entorno necesarias.

**No publiques claves de API, contraseñas ni credenciales en GitHub.**

Los identificadores de datasets de datos.gov.co y DANE aparecen como `CAMBIAR_ID_DATASET`. Debes sustituirlos por identificadores reales antes de utilizar el análisis de datos abiertos.

## Ejecutar

Desde la raíz del proyecto, ejecuta las pruebas:

```bash
mvn clean verify
```

Para iniciar la aplicación:

```bash
mvn spring-boot:run
```

Si el inicio termina correctamente y la aplicación utiliza el puerto 8080, estará disponible en:

http://localhost:8080

Documentación interactiva de la API, si Swagger UI está habilitado:

http://localhost:8080/swagger-ui.html

## Endpoints principales
>>>>>>> 41d22a3fc7826f06e4bc6354fefada450b179c12

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
| GET | `/api/tournaments/{id}` | Consultar un torneo |
| POST | `/api/tournaments` | Crear un torneo |
| PUT | `/api/tournaments/{id}` | Actualizar un torneo |
| DELETE | `/api/tournaments/{id}` | Eliminar un torneo |
| POST | `/api/assistant/ask` | Consultar el asistente |
| GET | `/api/opendata/summary?limit=100` | Obtener análisis de datos abiertos |

Los endpoints y sus parámetros deben coincidir con los controladores implementados en el código.

## Ejemplo de creación de un torneo
>>>>>>> 41d22a3fc7826f06e4bc6354fefada450b179c12

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
# Proyecto-gestor-de-torneos-integrado
>>>>>>> e94f88fe0ae923618c521c3e308d83c45e34aff7

>>>>>>> 41d22a3fc7826f06e4bc6354fefada450b179c12
