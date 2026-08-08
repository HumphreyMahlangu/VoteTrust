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

## Azure ACR Tasks Deployment

VoteTrust can be deployed to Azure Container Apps through Azure Container Registry Tasks. This keeps GitHub Actions responsible for tests and uses ACR Tasks for image build, push, and runtime update after code reaches `main`.

The Azure deployment assets live in `infra/azure/`:

* `acr-task.yaml` builds the Dockerfile image, tags it with the Git commit SHA and `main`, pushes both tags to ACR, and updates Azure Container Apps.
* `provision-acr-tasks-deployment.ps1` creates the Azure resource group, ACR, private-network PostgreSQL Flexible Server, Key Vault, managed identities, Container Apps environment, Container App, and ACR Task.
* `README.md` contains the exact runbook for provisioning, validation, first-admin bootstrap, ACR task logs, manual task runs, and rollback.

Prerequisites:

```powershell
az login
az account set --subscription "<subscription-id-or-name>"
az extension add --name containerapp --upgrade
$env:GITHUB_PAT = "<github-pat-with-public_repo-and-repo:status>"
```

Deploy:

```powershell
pwsh .\infra\azure\provision-acr-tasks-deployment.ps1
```

The script targets `spaincentral` by default and stops during preflight if Azure Container Apps, ACR, Key Vault, or PostgreSQL Flexible Server is unavailable in that region for the active subscription.

After deployment, verify:

```powershell
$baseUrl = "https://<container-app-fqdn>"
curl "$baseUrl/actuator/health/readiness"
curl "$baseUrl/swagger-ui.html"
```

For Postman, set the environment `baseUrl` to the Container App URL.

## Production Hardening Notes

* Keep `VOTETRUST_ADMIN_BOOTSTRAP_ENABLED=false` except during a controlled first-admin bootstrap window.
* Rotate the bootstrap token immediately after first-admin creation.
* Use managed PostgreSQL with backups, point-in-time recovery, and encrypted storage.
* Restrict public exposure to the REST API and health endpoints required by the platform.
* Use an API gateway or distributed limiter for horizontally scaled deployments.
* Store logs centrally, but do not log raw credentials, South African ID numbers, anonymous voting credentials, or ballot receipts.
* Protect `main` in GitHub so only CI-passing pull requests can trigger the ACR production deployment task.
* Use a separate migration/database role before treating this as a real production deployment; the portfolio script uses the managed PostgreSQL admin account so Flyway can bootstrap the schema automatically.
