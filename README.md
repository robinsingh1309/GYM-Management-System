# FitManager Backend

Spring Boot backend for the FitManager gym administration application. It manages members, memberships, pricing plans, and payments, and also provides exercise recommendations and a PDF-grounded personal assistant.

For frontend setup and architecture, see the [Frontend README](Frontend/README.md). Detailed domain behavior is recorded in [Business Rules](docs/business-rules.md).

## Features

- Email/password authentication with BCrypt password hashing
- Google OAuth 2.0 / OpenID Connect authentication
- Stateless JWT authorization for `ADMIN` and `STAFF` roles
- Member, membership, pricing, and cash-payment management
- Historical membership pricing with controlled activation
- Exercise catalog and Qdrant-backed recommendations
- PDF knowledge-base ingestion and grounded assistant responses

### Current limitations

- Cash payments only
- No refunds, reversals, or payment editing/deletion
- No membership cancellation, replacement, or freeze/pause
- No personal-training scheduling or formal discount model

## Technology stack

- Java 17
- Spring Boot 4.1.1
- Spring Security and OAuth 2.0 Resource Server
- Spring Data JPA and MySQL
- Qdrant Java client
- Hugging Face embeddings
- Groq language model
- Spring AI PDF document reader
- Gradle

## Architecture

```text
HTTP request
    ↓
Controller
    ↓
Service interface
    ↓
ServiceImpl
    ↓
Repository / integration client
    ↓
MySQL / Qdrant / external AI provider
```

- Controllers own the HTTP boundary and request validation.
- DTOs define request and response contracts.
- Service interfaces define application capabilities.
- The corresponding `ServiceImpl` classes contain business logic.
- Repositories handle database access.
- Security classes handle JWT, roles, and OAuth handoff behavior.
- Global exception handling converts failures into API error responses.

The `Service` and corresponding `ServiceImpl` separation is an intentional project convention.

## Project structure

```text
src/main/java/com/example/fitmanager/
├── component/      Startup data loaders and in-memory stores
├── config/         Security and external-service configuration
├── constant/       Shared constants
├── controller/     REST controllers
├── dto/            Request and response contracts
├── entity/         JPA entities and domain enums
├── exception/      Domain exceptions and API error handling
├── repository/     Database access
├── security/       JWT and Google OAuth support
├── service/        Service interfaces
├── serviceImpl/    Service implementations
└── util/           Shared utilities

src/main/resources/
├── application.properties
├── data/           Exercise catalog JSON
└── knowledgebase/  PDF documents used by the assistant
```

## Prerequisites

- JDK 17
- MySQL running on port `3306`
- Qdrant with its gRPC endpoint available on port `6334`
- Hugging Face API token
- Groq API key
- Google OAuth client credentials

Use the Gradle wrapper included in this repository; a separate Gradle installation is not required.

## Environment variables

Configure these variables before starting the backend:

```bash
export JWT_SECRET_KEY="base64-encoded-signing-key"
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export OAUTH2_COOKIE_SECRET="your-oauth-cookie-secret"
export HUGGING_FACE_TOKEN="your-hugging-face-token"
export GROQ_API_KEY="your-groq-api-key"
```

| Variable | Purpose |
|---|---|
| `JWT_SECRET_KEY` | Base64-encoded HMAC key used to sign JWTs |
| `GOOGLE_CLIENT_ID` | Google OAuth client identifier |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `OAUTH2_COOKIE_SECRET` | Protects temporary OAuth authorization data |
| `HUGGING_FACE_TOKEN` | Authenticates embedding requests |
| `GROQ_API_KEY` | Authenticates grounded-answer generation requests |

Never commit real credentials. The JWT secret must be valid Base64 because the backend decodes it before constructing the signing key.

## MySQL setup

Create the local database:

```sql
CREATE DATABASE gym_management;
```

The development configuration currently connects to:

```text
jdbc:mysql://localhost:3306/gym_management
username: root
password: root
```

Hibernate uses `spring.jpa.hibernate.ddl-auto=update` during local development. Replace the committed development credentials and adopt controlled migrations before deploying to production.

## Qdrant setup

The backend expects these collections to already exist:

| Collection | Purpose | Vector dimension |
|---|---|---:|
| `fitmanager_exercises` | Exercise recommendation vectors | 384 |
| `fitmanager_knowledgebase` | PDF page vectors used by the assistant | 384 |

Qdrant is configured at `localhost:6334` using its gRPC interface. The current application checks and writes collections but does not create them, so create both collections with cosine distance before indexing data.

The exercise catalog is loaded from `src/main/resources/data/exercise.json` when the application starts. Exercise vectors can then be synchronized through the exercise Qdrant endpoints.

PDF documents live in `src/main/resources/knowledgebase`. An ADMIN can replace the knowledge collection contents with the current PDFs by calling:

```http
POST /api/v1/knowledge-base/reindex
Authorization: Bearer <admin-jwt>
```

Each nonblank PDF page is embedded as one Qdrant point with its source filename, page number, content, and content hash.

## Google OAuth setup

For local development, configure this authorized Google redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

The local authentication flow is:

```text
Frontend
  → http://localhost:8080/oauth2/authorization/google
  → Google
  → backend OAuth callback
  → http://localhost:5173/oauth2/callback
  → one-time code exchange
  → JWT
```

New email/password and Google registrations receive the `STAFF` role. Creating users with another role is an ADMIN-controlled operation.

## Run locally

Start services in this order:

1. MySQL
2. Qdrant
3. Required environment variables
4. FitManager backend
5. FitManager frontend

Start the backend from the repository root:

```bash
./gradlew bootRun
```

Local addresses:

| Service | URL |
|---|---|
| Backend | `http://localhost:8080` |
| Health check | `http://localhost:8080/api/v1/health` |
| Frontend | `http://localhost:5173` |

## Authentication and authorization

- JWTs expire after one hour by default.
- Authorities are stored as `ROLE_ADMIN` and `ROLE_STAFF`.
- Public endpoints are limited to registration, login, OAuth exchange, OAuth callbacks, and health checks.
- Other endpoints require a valid Bearer token.
- Unauthenticated requests return `401 Unauthorized`.
- Insufficient permissions return `403 Forbidden`.
- The backend remains the authoritative authorization boundary even when the frontend hides restricted actions.

The detailed role-permission matrix is available in [Business Rules](docs/business-rules.md#2-users--authorization).

## API overview

| Area | Base path | Access |
|---|---|---|
| Authentication | `/api/v1/auth` | Public |
| Health | `/api/v1/health` | Public |
| Users | `/api/v1/users` | Authenticated; creation requires ADMIN |
| Members | `/api/v1/members` | Authenticated; status changes require ADMIN |
| Memberships | `/api/v1/memberships` | Authenticated; status changes require ADMIN |
| Membership pricing | `/api/v1/membership-pricing` | Authenticated; mutations require ADMIN |
| Payments | `/api/v1/payments` | Authenticated |
| Exercises | `/api/v1/exercises` | Authenticated |
| Personal assistant | `/api/v1/chat` | Authenticated |
| Knowledge ingestion | `/api/v1/knowledge-base` | Reindexing requires ADMIN |

Request DTOs use Jakarta Bean Validation. Domain-rule violations generally return `400 Bad Request`; missing resources return `404 Not Found`.

## AI functionality

### Exercise recommendations

The application loads its exercise catalog from JSON and uses `sentence-transformers/all-MiniLM-L6-v2` through Hugging Face to generate 384-dimensional embeddings. Recommendation queries combine natural-language similarity with optional structured filters and return Qdrant scores in the range `0–1`.

The frontend uses:

```http
POST /api/v1/exercises/qdrant/recommend
```

### Personal assistant

The knowledge pipeline reads PDF documents from `src/main/resources/knowledgebase`, embeds each nonblank page, and stores it in `fitmanager_knowledgebase`. At query time, the backend retrieves relevant pages using Qdrant and passes the grounded context to the configured Groq model.

The frontend uses:

```http
POST /api/v1/chat
```

The response includes the generated answer and source citations defined by the chat DTO contract.

## Build and verification

```bash
./gradlew test
./gradlew build
```

Backend tests belong in `src/test/java`. The repository currently has no substantive automated backend test suite, so business-rule and security coverage should be added as behavior evolves.

Frontend verification commands are documented in the [Frontend README](Frontend/README.md).

## Business rules

Important rules include:

- Member status and membership status are independent.
- Membership status is derived from dates and the membership active flag.
- Membership periods cannot overlap.
- Outstanding balances block new memberships.
- Pricing records preserve history and are not edited in place.
- Membership amounts and payment records are immutable.
- Remaining balances on expired memberships must be settled in one payment.

See [Business Rules](docs/business-rules.md) for the complete specification and examples.

## Production considerations

- Move database credentials and environment-specific URLs out of committed configuration.
- Replace `ddl-auto=update` with controlled database migrations.
- Disable SQL logging unless it is deliberately required.
- Restrict CORS to the deployed frontend origin.
- Enable secure OAuth cookies and HTTPS.
- Persist and back up MySQL and Qdrant data.
- Rotate JWT, OAuth, Hugging Face, and Groq credentials.
- Add monitoring for database, Qdrant, and external AI-provider failures.
