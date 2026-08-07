# VoteTrust

> **A Secure Digital Voting Platform**

*Exploring how modern software engineering, cryptography, and secure system design can improve transparency, accessibility, and trust in digital voting.*

---

## Table of Contents

* [Overview](#overview)
* [Why VoteTrust?](#why-votetrust)
* [The Problem](#the-problem)
* [Project Vision](#project-vision)
* [Project Goals](#project-goals)
* [Core Principles](#core-principles)
* [Key Features](#key-features)
* [Technology Stack](#technology-stack)
* [Local Deployment](#local-deployment)
* [Project Status](#project-status)
* [Roadmap](#roadmap)
* [Disclaimer](#disclaimer)
* [License](#license)

---

# Overview

VoteTrust is a backend-focused software engineering project that explores how secure digital voting systems can be designed using modern technologies.

The project investigates the use of secure authentication, cryptography, audit logging, and distributed ledger concepts to build a voting platform that prioritizes integrity, transparency, privacy, and accessibility.

Rather than attempting to replace existing election systems, VoteTrust serves as a technical exploration of the challenges involved in designing a secure digital voting platform while following modern software engineering best practices.

The primary objective is to demonstrate robust backend architecture, secure application development, and thoughtful system design.

---

# Why VoteTrust?

Digital technology has transformed how people bank, communicate, study, shop, and access government services. However, voting remains a largely physical process in many parts of the world.

While in-person voting plays an important role in maintaining election integrity, practical barriers such as long queues, travel requirements, accessibility challenges, and time constraints may discourage participation for some eligible voters.

VoteTrust was created to explore an important engineering question:

> **How can modern software technologies improve accessibility to voting while preserving security, privacy, transparency, and public trust?**

This project does not claim to solve every challenge associated with online voting. Instead, it investigates how secure software engineering principles can contribute to future digital voting systems.

---

# The Problem

Designing a secure digital voting system is significantly more complex than building a standard web application.

A trustworthy voting platform must address challenges such as:

* Verifying voter identity.
* Preventing duplicate voting.
* Protecting ballot secrecy.
* Preventing unauthorized modification of votes.
* Providing transparent and auditable election records.
* Preserving voter privacy.
* Maintaining confidence in election results.

VoteTrust explores these challenges through software engineering, security, and modern backend architecture.

---

# Project Vision

The vision of VoteTrust is to build a secure, scalable, and maintainable backend platform that demonstrates how modern technologies can be combined to create a transparent and trustworthy digital voting system.

The project emphasizes clean architecture, security-first development, and real-world engineering practices over simply implementing features.

---

# Project Goals

* Build a secure RESTful API using Spring Boot.
* Implement robust authentication and authorization.
* Ensure every eligible voter can cast only one vote.
* Protect ballot confidentiality through cryptographic techniques.
* Maintain immutable and verifiable audit records.
* Apply secure software engineering principles throughout the system.
* Demonstrate scalable backend architecture suitable for enterprise applications.
* Produce a professional portfolio project that reflects real-world engineering practices.

---

# Core Principles

VoteTrust is built around five fundamental principles.

## Security

Protect voter identities, ballots, and election data against unauthorized access and tampering.

## Privacy

Ensure that individual votes remain confidential while maintaining election integrity.

## Integrity

Guarantee that votes cannot be modified, duplicated, or removed once accepted by the system.

## Transparency

Provide verifiable audit logs that improve accountability without exposing sensitive information.

## Accessibility

Explore how digital platforms may reduce practical barriers that discourage participation.

---

# Key Features

The current implementation demonstrates:

* Secure user registration and JWT authentication
* Role-based account model
* Election and voting district read APIs
* Voter registration with South African ID validation and registration-window enforcement
* Anonymous one-time voting credentials
* Digital ballot submission
* One vote per voter per contest enforcement
* Ballot ledger entries that do not store voter identity
* Tamper-evident SHA-256 hash chain for ballot ledger auditing
* Final result tallying after voting closes
* Public audit and ledger verification endpoints
* OpenAPI / Swagger documentation
* Docker Compose deployment with PostgreSQL
* GitHub Actions CI workflow
* Automated unit and integration tests

---

# Technology Stack

## Backend

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Maven

## Database

* PostgreSQL

## Security

* JWT Authentication
* Password Encryption
* Role-Based Access Control

## Runtime Configuration

Set these secrets through environment variables. Do not commit real values.

* `VOTETRUST_JWT_SECRET`: JWT signing secret with at least 32 characters.
* `VOTETRUST_ID_HASH_PEPPER`: HMAC pepper for hashing South African ID numbers with at least 32 characters.
* `VOTETRUST_VOTE_CREDENTIAL_PEPPER`: HMAC pepper for hashing anonymous voting credentials with at least 32 characters.
* `VOTETRUST_CORS_ALLOWED_ORIGINS`: Comma-separated browser origins allowed to call the API.

## API Surface

Current implemented endpoints include:

* `POST /api/v1/auth/register` and `POST /api/v1/auth/login`
* `GET /api/v1/elections` and `GET /api/v1/elections/{electionId}`
* `POST /api/v1/elections/{electionId}/registrations`
* `GET /api/v1/elections/{electionId}/contests`
* `POST /api/v1/elections/{electionId}/contests/{contestId}/credentials`
* `POST /api/v1/ballots`
* `GET /api/v1/elections/{electionId}/contests/{contestId}/results`
* `GET /api/v1/elections/{electionId}/contests/{contestId}/audit`
* `GET /api/v1/elections/{electionId}/contests/{contestId}/ledger`

Results, audit summaries, and public ledger entries are exposed only after the election status is `COMPLETED`, the contest status is `CLOSED`, and the voting window has ended.

## Documentation

* OpenAPI (Swagger)

## Testing

* JUnit 5
* Mockito

## DevOps

* Docker
* GitHub Actions

---

# Local Deployment

## Prerequisites

* Java 21
* Docker Desktop or a compatible Docker engine
* Maven wrapper included in this repository

## Run Tests

```powershell
.\mvnw.cmd test
```

## Run With Docker Compose

Create a local environment file from the committed template, then replace every `change-me-*` value before starting the stack.

```powershell
Copy-Item .env.example .env
docker compose up --build
```

The API will be available at:

* `http://localhost:8080`
* `http://localhost:8080/swagger-ui.html`
* `http://localhost:8080/actuator/health`

## Build Container Image Only

```powershell
docker build -t votetrust-api:local .
```

## Production Notes

* Do not reuse `.env.example` values outside local development.
* Set `VOTETRUST_JWT_SECRET`, `VOTETRUST_ID_HASH_PEPPER`, and `VOTETRUST_VOTE_CREDENTIAL_PEPPER` from a secret manager.
* Set `VOTETRUST_CORS_ALLOWED_ORIGINS` to the exact frontend domains that should call the API.
* Keep PostgreSQL storage on a managed volume or managed database service.
* Expose only `/actuator/health` publicly; other actuator endpoints require authentication.
* Flyway migrations run automatically on startup, and Hibernate validates the schema instead of creating it.

---

# Project Status

🚧 **Portfolio MVP Implemented**

The core backend workflow is implemented: authentication, voter registration, anonymous voting, hash-chain auditability, final tallying, OpenAPI documentation, automated tests, and local container deployment support.

The project remains an educational portfolio simulation and should not be presented as a certified online election platform.

---

# Roadmap

Potential next steps:

* Admin-only election, contest, and voting district management endpoints.
* Refresh-token flow and token revocation.
* Database-backed audit events for administrative actions.
* PostgreSQL Testcontainers integration in CI.
* API versioned seed data for demo elections.
* Cloud deployment pipeline and production observability.
* Independent cryptographic review of the anonymous credential and ledger design.

---

# Disclaimer

VoteTrust is an educational software engineering project created for learning, experimentation, and portfolio purposes.

The project explores secure digital voting concepts and demonstrates modern backend engineering techniques.

It is **not** intended to replace existing election infrastructure or represent a production-ready election system.

---

# License

This project is licensed under the MIT License.
