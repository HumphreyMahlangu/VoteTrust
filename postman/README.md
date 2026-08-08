# VoteTrust Postman Assets

Import these two verified files into Postman:

* `VoteTrust.verified.postman_collection.json`
* `VoteTrust.verified-local.postman_environment.json`

Select the `VoteTrust Verified Local` environment before running requests.

## Before You Start

Start the API locally:

```powershell
docker compose up --build
```

The local environment assumes:

* `baseUrl`: `http://localhost:8080`
* `adminBootstrapToken`: same value as `VOTETRUST_ADMIN_BOOTSTRAP_TOKEN` in `.env`
* `adminPassword` and `voterPassword`: `VeryStrongPassword1`

## Recommended Flow

1. Run `00 - Start Here / Reset Verified Demo Data`.
2. Run `01 - Admin Auth / Bootstrap First Admin - Optional` once. If an admin already exists, continue with `Login Admin`.
3. Run `01 - Admin Auth / Login Admin`.
4. Run every request in `02 - Election Setup`.
5. Run every request in `03 - Public Reads`.
6. Run every request in `04 - Voter Registration` before `registrationEndAt`.
7. Run `05 - Voting` in order. If `Timing Check - Voting Window Open` tells you to wait, wait the stated number of seconds and rerun that request.
8. Run `06 - Results and Audit` in order. If `Timing Check - Voting Window Closed` tells you to wait, wait the stated number of seconds and rerun that request.
9. Run `07 - Negative Checks` only after a successful ballot has been cast.

The verified collection generates unique demo data on each reset and creates short test windows by default: registration stays open for 180 seconds, then voting stays open for 90 seconds. You can change `registrationGraceSeconds` and `votingDurationSeconds` in the Postman environment before running `Create Election`.

The older `VoteTrust.postman_collection.json` files are kept for compatibility, but use the verified pair above for manual testing.
