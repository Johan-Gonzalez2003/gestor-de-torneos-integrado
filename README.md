# Proyecto-gestor-de-torneos-integrado
# Gestor de Torneos de Videojuegos

API REST desarrollada en Java 17 con Spring Boot 3, organizada mediante arquitectura hexagonal y Domain-Driven Design (DDD).

El proyecto integra fuentes de datos abiertos colombianos, como datos.gov.co y DANE, mecanismos de resiliencia (circuit breaker y retry con backoff exponencial) y análisis descriptivo con DuckDB.

Proyecto semestral del Taller de Programación 2026-262 de la Universidad Manuela Beltrán, correspondiente a la guía 6, sesiones 11 y 12.

## Arquitectura

```text
src/main/java/com/tournamentmanager/
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

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/tournaments` | Listar torneos |
| GET | `/api/tournaments/{id}` | Consultar un torneo |
| POST | `/api/tournaments` | Crear un torneo |
| PUT | `/api/tournaments/{id}` | Actualizar un torneo |
| DELETE | `/api/tournaments/{id}` | Eliminar un torneo |
| POST | `/api/assistant/ask` | Consultar el asistente |
| GET | `/api/opendata/summary?limit=100` | Obtener análisis de datos abiertos |

Los endpoints y sus parámetros deben coincidir con los controladores implementados en el código.

## Ejemplo de creación de un torneo

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


