# AGENTS.md - sosialhjelp-mock-alt-api

> Mock server for DIGISOS (digital social services) ecosystem - provides mock implementations of Norwegian government APIs for testing.

## Project Overview

**Purpose**: Comprehensive mock server simulating ~18 external NAV services for the DIGISOS social assistance application ecosystem. Used for local development and integration testing.

**Team**: teamdigisos@nav.no / #team_digisos (Slack)

**Tech Stack**:
- Kotlin 2.3 / JDK 21
- Spring Boot 4.x
- Gradle with Kotlin DSL + version catalog
- Jackson 3.x for JSON
- mock-oauth2-server for authentication
- In-memory data storage (no database)

## Quick Reference

```bash
# Build
./gradlew build

# Run locally
./gradlew bootRun

# Run tests
./gradlew test

# Format code (ktlint)
./gradlew spotlessApply

# Check for dependency updates
./gradlew dependencyUpdates
```

**Local URLs**:
- Application: http://localhost:8989/sosialhjelp/mock-alt-api
- Swagger UI: http://localhost:8989/sosialhjelp/mock-alt-api/swagger-ui.html
- OAuth2 mock: http://localhost:4321

## Project Structure

```
src/main/kotlin/no/nav/sbl/sosialhjelp/mock/alt/
├── MockAltApplication.kt      # Entry point, ObjectMapper config
├── ExceptionHandler.kt        # Global error handling
├── ApplicationExitJob.kt      # Daily restart (01:00) to reset data
├── config/                    # Spring configuration
├── datastore/                 # In-memory data stores and services
│   ├── aareg/                 # Arbeidsgiverregisteret (employment)
│   ├── bostotte/              # Housing support
│   ├── ereg/                  # Enhetsregisteret (organizations)
│   ├── feil/                  # Error simulation service
│   ├── fiks/                  # FIKS Digisos (applications, documents)
│   ├── kontonummer/           # Bank accounts
│   ├── krr/                   # Contact preferences
│   ├── norg/                  # NAV offices
│   ├── pdl/                   # Person data (Folkeregisteret)
│   ├── roller/                # Admin roles
│   ├── skatteetaten/          # Tax data
│   ├── skjermedepersoner/     # Protected persons
│   └── utbetaling/            # NAV payments
├── integrations/              # Mock endpoints for external services
│   ├── aareg/                 # /aareg/*
│   ├── azure/                 # /azuread/*
│   ├── ereg/                  # /ereg/*
│   ├── fiks/                  # /fiks/digisos/api/v1/*, v2/*
│   ├── husbanken/             # /husbanken/*
│   ├── innsyn_api/            # /innsyn/*
│   ├── is_alive/              # /internal/*
│   ├── klage/                 # /fiks/klage/*
│   ├── kodeverk/              # /kodeverk/*
│   ├── kontonummer/           # /kontonummer/*
│   ├── krr/                   # /krr/*
│   ├── login/                 # /login/cookie/*
│   ├── norg/                  # /norg/*
│   ├── pdl/                   # /pdl_endpoint_url (GraphQL)
│   ├── skatteetaten/          # /skatteetaten/*
│   ├── skjermede_personer/    # /skjermede-personer/*
│   ├── utbetaling/            # /utbetaldata/*
│   └── wellknown/             # /well-known/*, /jwks/*
├── otherEndpoints/            # Admin/frontend APIs
│   ├── feil/                  # /feil/* - Error configuration
│   ├── frontend/              # /mock-alt/* - Admin UI API
│   ├── logg_ukjente_requester/
│   ├── prometheus/
│   └── unleash/
└── utils/                     # Shared utilities
    ├── FnrUtil.kt             # FNR (national ID) generation
    ├── MockAltException.kt
    └── Utils.kt               # Token parsing, logging helpers
```

## Architecture Patterns

### Data Storage Pattern
All mock data is stored **in-memory** using HashMap structures in Spring `@Service` beans:

```kotlin
@Service
class SomeService {
    private val dataStore: HashMap<String, DataClass> = HashMap()

    fun get(id: String): DataClass? = dataStore[id]
    fun put(id: String, data: DataClass) { dataStore[id] = data }
    fun getAll(): List<DataClass> = dataStore.values.toList()
}
```

Data resets daily at 01:00 via scheduled application restart (`ApplicationExitJob`).

### Controller Pattern
Controllers follow REST conventions with constructor injection:

```kotlin
@RestController
class SomeController(
    private val someService: SomeService,
    private val feilService: FeilService,  // For error simulation
) {
    companion object {
        private val log by logger()
    }

    @GetMapping("/some-path/{id}")
    fun getSomething(@PathVariable id: String): ResponseEntity<SomeDto> {
        feilService.eventuellFeil(fnr, this)  // Check for configured errors
        return ResponseEntity.ok(someService.get(id))
    }
}
```

### Error Simulation
The `FeilService` allows configuring timeouts and HTTP errors per user (fnr):

```kotlin
// In controller methods:
feilService.eventuellFeil(fnr, this)  // May throw KonfigurertFeil or delay
```

Configure via `/feil/*` endpoints or frontend API.

### Token/Auth Pattern
Extract user identity (fnr) from JWT tokens or headers:

```kotlin
// From Authorization header (JWT):
val fnr = hentFnrFraToken(headers)

// From nav-personident header:
val fnr = hentFnrFraHeaders(headers)

// Default fallback: FAST_FNR = "26504547549"
```

## Key Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Build config, dependencies |
| `gradle/libs.versions.toml` | Centralized version catalog |
| `src/main/resources/application.yml` | App config (ports, paths) |
| `nais/dev/mock.yaml` | NAIS deployment config |
| `MockAltApplication.kt` | Main entry, ObjectMapper bean |
| `ExceptionHandler.kt` | Global error handling |

## Pre-seeded Test Users

Created in `PdlService.init{}`:

| Name | FNR | Notes |
|------|-----|-------|
| Standard Standardsen | `26504547549` (FAST_FNR) | Full test user, all admin roles |
| Bergen Bergenhusen | Random | Bergen municipality (4601) |
| Tyske Tyskersen | Random | German citizenship |
| Admin Adminsen | Random | MODIA_VEILEDER role |
| NAV Kontaktsentersen | Random | Has multiple children |
| Hemmelig Adressesen | Random | STRENGT_FORTROLIG (protected address) |

## Important Constants

```kotlin
const val FAST_FNR = "26504547549"  // Default/fallback test user

// Context path
"/sosialhjelp/mock-alt-api"

// OAuth2 mock server port
4321
```

## Dependencies (Key Libraries)

From `gradle/libs.versions.toml`:

| Library | Purpose |
|---------|---------|
| `sosialhjelp-common-api` | NAV shared types/DTOs |
| `soknadsosialhjelp-filformat` | DIGISOS file format definitions |
| `token-validation-spring` | NAV JWT token validation |
| `mock-oauth2-server` | OAuth2/OIDC mock for testing |
| `springdoc-openapi` | Swagger/OpenAPI documentation |

## Testing

Tests use JUnit 5 with Spring Boot Test:

```kotlin
@SpringBootTest
class SomeTest {
    @Test
    fun `some test case`() { ... }
}
```

**Note**: Mockito is explicitly excluded - tests use real Spring context.

Run tests: `./gradlew test`

## Code Style

- **Formatter**: ktlint (via Spotless plugin)
- **Format on save**: `./gradlew spotlessApply`
- **Pre-push hook**: Installed automatically on build

## CI/CD

- **Build**: `.github/workflows/build_code.yml` - Runs on all branches
- **Deploy**: `.github/workflows/deploy_dev.yml` - Deploys to dev-gcp
- **Security**: `.github/workflows/codeql.yml` - CodeQL analysis

## Common Tasks

### Adding a new mock endpoint

1. Create data model in `datastore/<service>/model/`
2. Create service class in `datastore/<service>/` with HashMap storage
3. Create controller in `integrations/<service>/` with REST endpoints
4. Wire up via constructor injection

### Adding a new test user

Edit `PdlService.init{}` block - use `opprettBrukerMedAlt()` helper:

```kotlin
opprettBrukerMedAlt(
    brukerFnr = genererTilfeldigPersonnummer(),
    fornavn = "Fornavn",
    etternavn = "Etternavn",
    statsborgerskap = "NOR",
    position = nextPosition,
)
```

### Configuring error scenarios

Via API: POST to `/feil/feilsituasjon` with:
```json
{
    "fnr": "26504547549",
    "timeout": 5,
    "feilkode": 500,
    "feilmelding": "Test error",
    "className": "*",
    "functionName": "*"
}
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `HOST_ADDRESS` | `http://localhost:8989/` | Base URL |
| `COOKIE_DOMAIN` | `localhost` | Cookie domain |
| `LOGINURL` | `http://localhost:3008/sosialhjelp/mock-alt/login` | Login redirect |
| `FILTER_SOKNADER_ON_FNR` | `true` | Filter applications by user |

## Related Services

This mock serves as backend for:
- `sosialhjelp-soknad-api` - Application API
- `sosialhjelp-innsyn-api` - Citizen view API
- `sosialhjelp-modia-api` - Caseworker API
- `sosialhjelp-proxy` - API gateway
