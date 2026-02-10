# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./gradlew build

# Run application (requires PostgreSQL on localhost:5432, database: pickleball_db)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.pickleball.PickleballBackendApplicationTests"

# Clean build
./gradlew clean build
```

## Tech Stack

- **Java 21** / **Spring Boot 4.0.2** with Spring Security, Spring Data JPA, Validation
- **PostgreSQL** with **Flyway** migrations (schema: `pickleball`, migrations in `src/main/resources/db/migration/`)
- **JWT authentication** via JJWT (HS256, 1h access / 24h refresh tokens)
- **Lombok** for boilerplate reduction (@Data, @Builder, @Slf4j, etc.)
- **Springdoc OpenAPI** for Swagger UI at `/swagger-ui.html`

## Architecture

Standard layered Spring Boot REST API: Controller → Service → Repository → Entity.

**Package structure** (`src/main/java/com/pickleball/`):
- `config/` — SecurityConfig (CORS, JWT filter chain, public/protected routes), OpenApiConfig
- `controller/` — REST endpoints under `/api/v1/`
- `service/` — Business logic with `@Transactional`
- `repository/` — Spring Data JPA repositories with custom JPQL queries
- `entity/` — JPA entities mapped to PostgreSQL tables
- `dto/` — Request/response DTOs as inner static classes (e.g., `AuthDto.LoginRequest`)
- `security/` — JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
- `exception/` — BusinessException with ErrorCode enum, GlobalExceptionHandler

## Domain Model

Core entities and relationships:
- **Partner** (facility) → has many **Court**s
- **Member** (user) → has many **MemberRole**s, **Reservation**s, **MemberSuspension**s
- **Court** → has many **Reservation**s
- **RejectVote** — rejection voting mechanism linking Court, target Member, and voter Member

## Key Business Logic

**Reservation creation** (`ReservationService.createReservation`) performs a 6-step validation chain:
1. Court exists and is not closed
2. Member is not suspended at the partner facility
3. No duplicate reservation for same user/court/date/slot
4. Capacity check with **pessimistic locking** (`PESSIMISTIC_WRITE`) to prevent race conditions
5. Rejection vote majority check
6. Persist reservation

**Reservation cancellation** enforces a 4-hour deadline before game start time and uses soft delete (`cancelYn='Y'`).

**Time slots** are hard-coded as 8 two-hour blocks from 06:00 to 22:00.

## Security Configuration

- **Public endpoints:** `/api/v1/auth/**`, Swagger paths, `GET /api/v1/partners/**`, `GET /api/v1/courts/*/slots/**`
- **Role-restricted:** `/api/v1/admin/**` (ROLE_ADMIN), `/api/v1/partner-manage/**` (ROLE_PARTNER)
- **All other endpoints** require authentication (Bearer token)
- CORS allows `localhost:3000` and `localhost:5173`
- Stateless sessions, CSRF disabled

## Conventions

- API versioning: URI-based (`/api/v1/`)
- DTOs use inner static classes grouped per domain (e.g., `AuthDto.LoginRequest`, `AuthDto.TokenResponse`)
- Exceptions use `BusinessException(ErrorCode.XXX)` — ErrorCode defines HTTP status, code string, and message
- Repositories use JPQL with named parameters (`@Param`), FETCH joins for lazy-loaded relations
- Audit fields: `createDate` / `updateDate` (LocalDateTime) on all entities
- JPA DDL mode is `validate` — schema changes must go through Flyway migrations
- Korean language is used in some business values (e.g., game levels, suspension types)
