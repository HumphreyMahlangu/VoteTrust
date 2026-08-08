# VoteTrust Postman Assets

Import these two files into Postman:

* `VoteTrust.postman_collection.json`
* `VoteTrust.local.postman_environment.json`

Select the `VoteTrust Local` environment before running requests.

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

1. Run `00 - Health and Demo Setup / Initialize Demo Variables`.
2. Run `01 - Admin Auth / Bootstrap First Admin` once. If an admin already exists, run `Login Admin`.
3. Run the admin setup requests through `Transition Election to REGISTRATION_OPEN`.
4. Register and log in the voter, then run `Register Voter For Election` before `registrationEndAt`.
5. Wait until `votingStartAt`, then run the voting status transitions, issue a credential, and cast one ballot.
6. Wait until `votingEndAt`, then close the contest, complete the election, and query results/audit/ledger.

The collection generates a short demo election by default: registration stays open for 2 minutes, then voting stays open for 5 minutes. You can change `registrationWindowMinutes` and `votingWindowMinutes` in the Postman environment.

Blank and spoilt ballot requests are included as optional alternatives. A credential can only be used once, so run only one ballot-casting request per issued credential.
