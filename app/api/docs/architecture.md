# Architecture Guide

This project follows a feature-based architecture with explicit boundaries between web, business, persistence, and infrastructure concerns.

Database policy:

- H2 for local development and tests
- PostgreSQL for production
- Flyway as the only schema migration tool

## Principles

- Keep controllers thin.
- Put business logic in services.
- Use DTOs for external contracts.
- Never expose JPA entities directly from controllers.
- Centralize mapping in dedicated mapper classes.
- Keep shared infrastructure in `config`, `security`, `exception`, and `common`.

## Package Responsibilities

### `com.scanit.config`

Application-wide configuration and bootstrapping:

- Swagger/OpenAPI configuration
- environment or profile-specific infrastructure helpers
- bean wiring that does not belong to a specific feature

### `com.scanit.security`

Security infrastructure:

- JWT properties and token handling
- authentication filter
- security filter chain
- `UserPrincipal` and principal loading
- CORS configuration
- access denied and authentication entry point handlers

### `com.scanit.common`

Shared utilities that are not feature-specific:

- base DTOs
- shared constants
- reusable helpers
- cross-feature abstractions

Keep this package small. If code belongs to one business capability, place it in that feature package instead.

### `com.scanit.exception`

Application-level errors and global exception handling:

- domain and application exceptions
- error response models
- centralized exception translation to HTTP responses

### Feature Packages

Each feature package should contain the same internal pattern:

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

Examples:

- `com.scanit.user`
- `com.scanit.auth`
- future modules such as `receipt`, `budget`, or `merchant`

## Request Flow

1. A request enters a controller.
2. Request DTO validation runs automatically with Jakarta Validation.
3. The controller delegates to a service.
4. The service enforces business rules, coordinates repositories, and invokes mappers.
5. The repository persists or queries entities.
6. The mapper converts entities into response DTOs.
7. The controller returns a response DTO and keeps HTTP concerns limited to status codes, headers, and request/response orchestration.

## Authentication Flow

### Registration

1. `POST /auth/register` receives a registration request DTO.
2. The auth service maps the request into a user creation DTO.
3. The user service validates uniqueness and hashes the password.
4. The user repository persists the user entity.
5. The auth service generates a JWT and returns an authentication response DTO.

### Login

1. `POST /auth/login` receives credentials.
2. Spring Security authenticates the request using the authentication manager and the user principal service.
3. On success, the auth service generates a JWT.
4. The client stores the token and sends it in the `Authorization: Bearer <token>` header.

### Authenticated Requests

1. The JWT authentication filter runs before controller execution.
2. The filter validates the token and loads the principal.
3. Protected endpoints receive an authenticated `UserPrincipal`.
4. Controllers can read the current user without duplicating token parsing logic.

## Naming Conventions

- Controllers end with `Controller`
- Services end with `Service`
- Service implementations end with `ServiceImpl` when an interface is useful
- Repositories end with `Repository`
- Entities use singular business nouns
- Request DTOs end with `RequestDto` or `Request`
- Response DTOs end with `ResponseDto` or `Response`
- Mappers end with `Mapper`
- Exceptions should describe the business or technical failure clearly

## How to Create a New Feature Module

Use this checklist when adding a new capability:

1. Create `com.scanit.<feature>` and the standard subpackages.
2. Add the entity or model under `model`.
3. Add repository interfaces under `repository`.
4. Add request and response DTOs under `dto/request` and `dto/response`.
5. Add a mapper to translate between entities and DTOs.
6. Add service classes for all business logic and validation that is not purely HTTP validation.
7. Add a thin controller that delegates to the service.
8. Add tests for service behavior, controller behavior, and repository queries where appropriate.
9. Update OpenAPI annotations and README documentation if the module exposes public endpoints.

## Folder Structure Recommendations

For a small-to-medium startup backend team, keep the following rules:

- Group code by business capability, not by technical layer alone.
- Do not create shared utilities too early. Only promote code to `common` when it is genuinely reused.
- Avoid placing business rules in controllers, entities, or mappers.
- Keep DTOs immutable where practical.
- Prefer constructor injection.
- Keep feature modules isolated enough that one feature can evolve without forcing unrelated changes.

## Current Codebase Mapping

The current repository already follows the intended direction:

- `auth` orchestrates registration, login, and current-user lookup
- `user` owns persistence and user-specific DTO conversion
- `security` owns token handling and authentication plumbing
- `config` owns OpenAPI and environment integration
- `exception` centralizes API error handling
- local profiles use H2, production uses PostgreSQL, and schema creation comes from Flyway migrations only

When new work is added, keep extending this structure instead of introducing new top-level technical buckets.

## User Module

The user module is the persistence and domain source of truth for user data.

Responsibilities:

- Persist users
- Enforce email uniqueness
- Hash passwords before save
- Map user entities to response DTOs
- Provide user data to the authentication flow and the current-user endpoint

Recommended internal package layout:

```text
com.scanit.user
|-- controller
|-- service
|-- repository
|-- model
|-- dto
|   |-- request
|   `-- response
`-- mapper
```

Rules for the user module:

- Do not place business rules in the controller.
- Do not expose the `User` entity directly from web endpoints.
- Keep the repository focused on persistence operations.
- Keep mapping logic in the mapper layer.
- Keep security concerns such as password verification or token creation outside the entity itself.

