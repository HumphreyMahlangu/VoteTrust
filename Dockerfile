# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy

LABEL org.opencontainers.image.title="VoteTrust API" \
      org.opencontainers.image.description="Secure REST API for a South African online voting simulation" \
      org.opencontainers.image.source="https://github.com/humphreymahlangu/voting-api" \
      org.opencontainers.image.licenses="MIT"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system votetrust \
    && useradd --system --gid votetrust --home-dir /app --shell /usr/sbin/nologin votetrust

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R votetrust:votetrust /app

USER votetrust
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health/readiness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:-} -jar app.jar"]
