# VoteTrust Deployment Checklist

VoteTrust is deployable as a Spring Boot container backed by PostgreSQL. This checklist is for portfolio/demo deployments and does not certify the system for binding public elections.

## Required Runtime Inputs

Set these values through a secret manager, platform environment variables, or a local `.env` file for Docker Compose:

* `SPRING_DATASOURCE_URL`
* `SPRING_DATASOURCE_USERNAME`
* `SPRING_DATASOURCE_PASSWORD`
* `VOTETRUST_JWT_SECRET`
* `VOTETRUST_ID_HASH_PEPPER`
* `VOTETRUST_VOTE_CREDENTIAL_PEPPER`

Optional but recommended:

* `VOTETRUST_CORS_ALLOWED_ORIGINS`
* `VOTETRUST_JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`
* `VOTETRUST_RATE_LIMIT_*`
* `VOTETRUST_ADMIN_BOOTSTRAP_ENABLED`
* `VOTETRUST_ADMIN_BOOTSTRAP_TOKEN`

## Local Compose Smoke Test

```powershell
Copy-Item .env.example .env
docker compose --env-file .env config
docker compose up --build
```

Then verify:

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/swagger-ui.html
```

For manual API testing, import the Postman collection and local environment from `postman/`.

## CI Deployment Gates

The GitHub Actions workflow must pass:

* Maven tests, including PostgreSQL Testcontainers on Docker-capable runners.
* Maven package build.
* Docker Compose config validation with `.env.example`.
* Container image build.

## Production Hardening Notes

* Keep `VOTETRUST_ADMIN_BOOTSTRAP_ENABLED=false` except during a controlled first-admin bootstrap window.
* Rotate the bootstrap token immediately after first-admin creation.
* Use managed PostgreSQL with backups, point-in-time recovery, and encrypted storage.
* Restrict public exposure to the REST API and health endpoints required by the platform.
* Use an API gateway or distributed limiter for horizontally scaled deployments.
* Store logs centrally, but do not log raw credentials, South African ID numbers, anonymous voting credentials, or ballot receipts.
