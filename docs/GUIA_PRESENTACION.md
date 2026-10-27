# Guía para presentar: Gestor de Torneos (Guía 6, Sesiones 11 y 12)

Taller de Programación 2026-262 · UMB · Ingeniería de Software

Esta guía organiza la sustentación oral, el video en inglés y las preguntas orientadoras.
Las partes marcadas con **[COMPLETAR]** dependen de tu experiencia real: escríbelas tú, con tus
capturas, tus errores y tus decisiones. No las copies de una plantilla.

---

## 1. Checklist de entrega frente a la rúbrica

| Criterio (peso) | Evidencia que debes mostrar | Listo |
|---|---|---|
| Arquitectura hexagonal (25 %) | Árbol de paquetes `domain/application/api/infrastructure`, `mvn clean verify` en verde | ☐ |
| Inyección de dependencias (15 %) | `AppConfig.java`, constructores de `TournamentService` y `AssistantService`, prueba con Mockito en `TournamentServiceTest` | ☐ |
| ADR en inglés (10 %) | `docs/adr/ADR-001` y `ADR-002` con contexto, decisión y consecuencias | ☐ |
| 2 APIs colombianas integradas (30 %) | Captura de `/api/opendata/summary` con las dos fuentes, circuit breaker en los tres estados (pruebas `CircuitBreakerTest`) | ☐ |
| Análisis con DuckDB (10 %) | Dos consultas SQL en `DuckDbAnalyticsAdapter` y su salida en la respuesta JSON | ☐ |
| Preguntas orientadoras e informe (5 %) | Informe Word con APA V7 (ver sección 4) | ☐ |
| Video en inglés (5 %) | Video de 1 a 3 minutos publicado en YouTube (ver sección 3) | ☐ |

Antes de entregar, confirma que:
- Reemplazaste `CAMBIAR_ID_DATASET` por los IDs reales de datos.gov.co y DANE, y que el endpoint responde en el navegador.
- El pipeline de GitHub Actions está en verde en la rama principal.
- El enlace del repositorio funciona sin iniciar sesión.

---

## 2. Guion de la sustentación oral (aprox. 10 minutos)

1. **Problema y alcance (1 min).** Qué hace el gestor y qué parte de la guía cumple.
2. **Arquitectura (2 min).** Muestra el árbol de paquetes. Explica la regla de dependencias: el dominio no conoce Spring ni JPA.
   Señala `AppConfig` como el único lugar donde se conectan puertos y adaptadores.
3. **Demostración de testabilidad (1 min).** Ejecuta `mvn clean verify`. Abre `TournamentServiceTest` y explica cómo el repositorio simulado con Mockito evita la base de datos.
4. **Integración de APIs y resiliencia (3 min).** Explica el decorador `ResilientOpenDataSource`: retry por fuera, circuit breaker por dentro.
   Muestra el diagrama de los tres estados y una prueba que los recorra. Haz una demo: detén la conexión y muestra cómo la fuente pasa a `UNAVAILABLE` sin tumbar la respuesta.
5. **DuckDB (1 min).** Muestra las dos consultas SQL y explica por qué validas los nombres de campo antes de construir el SQL.
6. **Uso de GitHub Copilot (1 min).** Presenta una sugerencia que corregiste (ver sección 5).
7. **Cierre y aprendizajes (1 min).** Ver sección 4, pregunta 1.

**Demo sin conexión.** Si no hay internet durante la sustentación, ten lista una captura de `/api/opendata/summary` con datos reales y muestra la sección de pruebas en verde.

---

## 3. Guion del video en inglés (1 a 3 minutos)

The video must be in English, 1 to 3 minutes. Read it aloud, time it, and adjust the wording to your own voice.

**[0:00–0:20] Introduction**
"Hi, I'm [NAME]. This is my semester project for the Programming Workshop at Universidad Manuela Beltrán: a tournament manager for video games, built with Java 17 and Spring Boot."

**[0:20–0:50] Key idea: hexagonal architecture**
"The main idea of this lab is hexagonal architecture. The domain layer has no framework code. The application layer defines ports, and the infrastructure layer provides adapters, such as the JPA repository, the open-data clients, and the DuckDB analysis. Every dependency is injected through the constructor."

**[0:50–1:30] Open data and resilience**
"The project consumes two Colombian open-data sources, datos.gov.co and DANE, through the Socrata API. Each call goes through a circuit breaker with three states: closed, open, and half-open. Retries use exponential backoff. If a source fails, the response still arrives, and that source is marked as unavailable."

**[1:30–1:50] DuckDB and Copilot**
"DuckDB loads the JSON records and runs descriptive queries, such as counts and averages. I also used GitHub Copilot as an assistant. One of its suggestions was wrong: [DESCRIBE THE SUGGESTION], and I corrected it by [DESCRIBE THE CORRECTION]."

**[1:50–2:20] Demo and closing**
"Here is the test suite passing in the CI pipeline. What I learned is that [ONE CONCRETE LESSON]. Thank you."

Tips for recording:
- Show the terminal running `mvn clean verify` and the green CI badge.
- Keep the video under 3 minutes. Practice the timing once before recording.

---

## 4. Preguntas orientadoras

Cada respuesta debe tener evidencia técnica y argumentación. Usa la estructura sugerida y escribe tus propias ideas.

**1. ¿Cuáles fueron los aprendizajes obtenidos? (mínimo 3, con relación a tu futuro como ingeniero de software)**
- Estructura: aprendizaje → evidencia en el código → cómo lo usarías en un trabajo real.
- Ejemplo de aprendizaje a desarrollar (reescríbelo con tus palabras): separar el dominio de la infraestructura permite cambiar la base de datos sin reescribir las reglas de negocio.
- **[COMPLETAR]** tus tres aprendizajes con su evidencia.

**2. ¿Dónde presentaron mayor dificultad y cómo la resolvieron?**
- Estructura: dificultad concreta → qué intentaron primero → qué funcionó → qué harían distinto.
- Pista: las dificultades más comunes en esta práctica son mapear la entidad JPA hacia el dominio, y las transiciones del circuit breaker. **[COMPLETAR]** con lo que realmente les pasó.

**3. Evidencia técnica de los entregables.**
- Incluye capturas de: el árbol de paquetes, `mvn clean verify` en verde, la respuesta de `/api/opendata/summary`, la prueba de los estados del circuit breaker, y el pipeline de GitHub Actions.
- Cada captura debe tener un pie de figura en APA (Figura 1, Figura 2, ...).

**4. Ventajas y momentos de error de GitHub Copilot.**
- Estructura: ventaja concreta → sugerencia incorrecta → cómo la detectaron (prueba que falló, revisión, documentación) → corrección aplicada.
- **[COMPLETAR]** con sugerencias reales de su sesión. No inventes ejemplos: el video y el informe deben coincidir con lo que pasó.

---

## 5. Evaluación crítica de GitHub Copilot (para el informe y el video)

Registra al menos una sugerencia. Usa esta tabla:

| Sugerencia de Copilot | ¿Era correcta? | Qué detectaron | Corrección aplicada |
|---|---|---|---|
| **[COMPLETAR]** | No / Parcialmente | **[COMPLETAR]** | **[COMPLETAR]** |

Documenta qué generó la IA y qué cambios hicieron, tal como pide la guía.

---

## 6. Referencias sugeridas (APA 7)

Verifica cada referencia y usa las ediciones que consultaste.

- Cockburn, A. (2005). *Hexagonal architecture* [Entrada de blog]. https://alistair.cockburn.us/hexagonal-architecture/
- Evans, E. (2003). *Domain-driven design: Tackling complexity in the heart of software*. Addison-Wesley.
- Nygard, M. T. (2018). *Release it! Design and deploy production-ready software* (2.ª ed.). Pragmatic Bookshelf.
- Portal de datos abiertos de Colombia. (s. f.). *datos.gov.co*. https://www.datos.gov.co
- Socrata. (s. f.). *Socrata Open Data API (SODA) developer documentation*. https://dev.socrata.com/
- DuckDB Foundation. (s. f.). *DuckDB documentation*. https://duckdb.org/docs/

---

## 7. Cómo ejecutar la demo

```bash
mvn clean verify                 # pruebas en verde
mvn spring-boot:run              # levanta la API
curl "http://localhost:8080/api/opendata/summary?limit=50"
```
