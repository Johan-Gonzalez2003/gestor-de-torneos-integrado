# Gestor de Torneos de Videojuegos — Guía 7 (Sesiones 13 y 14)

![CI](https://github.com/TU_USUARIO/TU_REPO/actions/workflows/ci.yml/badge.svg)
![CodeQL](https://github.com/TU_USUARIO/TU_REPO/actions/workflows/codeql.yml/badge.svg)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=TU_ORGANIZACION_gestor-torneos&metric=alert_status)](https://sonarcloud.io/dashboard?id=TU_ORGANIZACION_gestor-torneos)
![Mutation score](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/TU_USUARIO/TU_REPO/badges/mutation.json)

> Reemplace `TU_USUARIO`, `TU_REPO` y `TU_ORGANIZACION` por los valores reales.

API REST (Java 17, Spring Boot 3, Maven) con autenticación **JWT**, autorización por roles (**RBAC**: `ADMIN`, `USER`) y controles de seguridad alineados con **OWASP API Security Top 10 – 2023**.

## Arquitectura (simple y escalable)

```
com.torneos
├── auth/       login (AuthController, AuthService) y endpoints de prueba por rol
├── security/   JWT, filtro, reglas de acceso, usuarios, anti fuerza bruta
├── torneo/     CRUD: controller → service → repository (interfaz)
└── common/     errores y manejo global de excepciones
```

Cada capa depende de una **interfaz** (`TorneoRepository`, `UserRepository`): hoy son en memoria; para pasar a JPA/PostgreSQL solo se agrega otra implementación.

## Cómo ejecutar

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export SEED_ADMIN_PASSWORD="una-clave-admin"
export SEED_USER_PASSWORD="una-clave-user"
mvn spring-boot:run
```

## Usuarios de demostración

| Usuario | Rol | Contraseña |
|---|---|---|
| `admin` | `ADMIN` | `SEED_ADMIN_PASSWORD` |
| `user` | `USER` | `SEED_USER_PASSWORD` |
| `user2` | `USER` | `SEED_USER_PASSWORD` (sirve para probar que no puede editar torneos ajenos) |

## Endpoints

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/v1/auth/login` | Público |
| GET | `/api/v1/admin/ping` | Solo `ADMIN` |
| GET | `/api/v1/user/ping` | `USER` o `ADMIN` |
| GET | `/api/v1/torneos?page=0&size=20` | `USER` o `ADMIN` (tamaño máx. 50) |
| GET | `/api/v1/torneos/{id}` | `USER` o `ADMIN` |
| POST | `/api/v1/torneos` | `USER` o `ADMIN` |
| PUT / DELETE | `/api/v1/torneos/{id}` | Dueño del torneo o `ADMIN` |

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"una-clave-admin"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["token"])')

curl -i localhost:8080/api/v1/admin/ping -H "Authorization: Bearer $TOKEN"
```

## Pruebas

```bash
mvn verify   # 59 pruebas unitarias e integración, con reporte JaCoCo en target/site/jacoco
```

Las pruebas usan `application-test.properties` (valores ficticios, no son credenciales reales).

## Documentación para sustentación

Ver [`docs/SUSTENTACION.md`](docs/SUSTENTACION.md): decisiones de diseño, mapeo a OWASP, preguntas probables y limitaciones conocidas.

## Calidad

| Objetivo | Comando |
|---|---|
| Pruebas + cobertura JaCoCo | `mvn verify` |
| Mutation Testing (falla si < 50 %) | `mvn test-compile org.pitest:pitest-maven:mutationCoverage` → `target/pit-reports/index.html` |
| Resumen de mutantes | `python3 scripts/mutation_report.py target/pit-reports/mutations.xml` |
| SonarCloud | `SONAR_TOKEN=... mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar` |

## DevSecOps en GitHub

- `.github/workflows/ci.yml`: build, pruebas, SonarCloud (con *Quality Gate wait*) y PIT.
- `.github/workflows/codeql.yml`: análisis estático de seguridad (CodeQL).
- `.github/dependabot.yml`: actualizaciones semanales de Maven y GitHub Actions.

Secreto requerido en GitHub: `SONAR_TOKEN` (Settings → Secrets and variables → Actions).
