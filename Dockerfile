# ============ ETAPA 1: compilar el proyecto ============
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Se copia primero el pom.xml para que Docker guarde en caché las dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Luego se copia el código y se genera el .jar (las pruebas ya corren en el pipeline)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ============ ETAPA 2: imagen final liviana ============
# Solo incluye el JRE y el .jar, sin Maven ni código fuente.
# Se usa la variante Ubuntu (no Alpine) porque DuckDB trae una librería nativa que necesita glibc.
FROM eclipse-temurin:17-jre
WORKDIR /app

# curl para el HEALTHCHECK y usuario sin privilegios (no root) por seguridad
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 1001 --no-create-home app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Comprueba que la aplicación responde (Render/Railway envían el puerto en PORT)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:${PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
