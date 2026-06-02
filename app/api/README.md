# ScanIt Platform

ScanIt Platform is a Spring Boot backend foundation for receipt scanning and budget management.

## Overview

- Local development uses an H2 in-memory database.
- Production uses PostgreSQL.
- Flyway is the only database migration mechanism.
- Hibernate uses `ddl-auto=validate`.
- `schema.sql` and automatic schema creation are not used.

## Configuration Files

- `src/main/resources/application.yml` - common settings such as profile defaults, Flyway, JPA validation, server port, and actuator exposure
- `src/main/resources/application-local.yml` - H2 in-memory datasource, H2 console, and local security defaults
- `src/main/resources/application-prod.yml` - PostgreSQL datasource from environment variables and production security defaults

## Local Development With H2

Local development does not require Docker or PostgreSQL.

### Start the app

```bash
mvn spring-boot:run
```

The default profile is `local`, so H2 is used automatically.

### H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:scanit-local`
- Username: `sa`
- Password: empty

### Local environment variables

These are the main local values:

```properties
SPRING_PROFILES_ACTIVE=local
PORT=8080
APP_SECURITY_JWT_SECRET=replace-with-a-random-32-char-min-secret
APP_SECURITY_JWT_EXPIRATION=24h
APP_SECURITY_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
APP_SECURITY_CORS_ALLOWED_METHODS=GET,POST,PUT,PATCH,DELETE,OPTIONS
APP_SECURITY_CORS_ALLOWED_HEADERS=*
APP_SECURITY_CORS_EXPOSED_HEADERS=Authorization,Location
```

## PostgreSQL For Integration Testing And Production-Like Runs

Use Docker only when you need PostgreSQL locally.

### Start PostgreSQL

```bash
docker compose up -d postgres
```

The container uses these defaults:

- `DB_PORT=5432`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`
- `DB_NAME=scanit`

### Run with the `prod` profile

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "scanit"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:APP_SECURITY_JWT_SECRET = "replace-with-a-random-32-char-min-secret"
$env:APP_SECURITY_JWT_EXPIRATION = "24h"
mvn spring-boot:run
```

For a packaged run:

```powershell
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Required PostgreSQL environment variables

Set these for the `prod` profile:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `APP_SECURITY_JWT_SECRET`
- `APP_SECURITY_JWT_EXPIRATION`

Optional override:

- `SPRING_DATASOURCE_URL`

## Flyway Migration Workflow

All schema changes live in `src/main/resources/db/migration`.

Workflow:

1. Create a new versioned migration, for example `V2__add_receipt_table.sql`.
2. Keep migrations additive and ordered.
3. Do not edit migrations that have already been shared.
4. Start the app or run tests and Flyway will apply pending migrations automatically.

`schema.sql` is not used. Do not add DDL outside Flyway migrations.

## Tests

Run the test suite with:

```bash
mvn test
```

Tests also use H2 and Flyway, so they do not require PostgreSQL.

## Project Architecture And Folder Structure

The project is organized by feature plus a small set of shared infrastructure packages:

- `com.scanit.config` - application configuration and bootstrapping
- `com.scanit.security` - JWT, filters, principals, CORS, and authentication plumbing
- `com.scanit.exception` - API error handling
- `com.scanit.user` - user domain, persistence, DTOs, mapper, and service logic
- `com.scanit.auth` - authentication endpoints and orchestration

Feature modules should follow this structure:

```text
com.scanit.<feature>
|-- controller
|-- service
|-- repository
|-- model
|-- dto
|   |-- request
|   `-- response
`-- mapper
```

Rules:

- Keep business logic in services.
- Keep controllers thin.
- Use DTOs for request and response payloads.
- Keep entity-to-DTO mapping in mappers.
- Add new database tables and columns only through Flyway migrations.

## Example Branch Strategy

- `main` - production-ready code
- `feature/<ticket>-<short-name>` - new work
- `bugfix/<ticket>-<short-name>` - defect fixes
- `hotfix/<ticket>-<short-name>` - urgent production fixes
