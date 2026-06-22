# MiniPay - Payment & Notification Microservice

A production-grade microservice built with Java 17 + Spring Boot 3.x that handles mock payments (M-Pesa, Card) and async SMS notifications. Features JWT authentication, Resilience4j circuit breaker, Spring `@Async` processing with an in-memory queue, PostgreSQL persistence, Dockerized deployment, and a Vue.js frontend.

## Live Links

| Resource | URL |
|---|---|
| **Frontend (Vue app)** | https://minipay-frontend.onrender.com |
| **Backend API base** | https://minipay-backend.onrender.com/api |
| **Swagger / OpenAPI docs** | https://minipay-backend.onrender.com/swagger-ui/index.html |
| **Health check** | https://minipay-backend.onrender.com/actuator/health |
| **GitHub repo** | https://github.com/reggyshicky/minipay-service |

> **Note:** the backend is hosted on Render's free tier, which spins down after periods of inactivity. The **first** request after idle time may take 30 - 90 seconds to respond while the service wakes up; this is expected Render free-tier behavior, not an application bug.

> **Before testing/reviewing: please hit the health check link above first** (https://minipay-backend.onrender.com/actuator/health) and wait for it to return `{"status":"UP"}`. This "wakes up" the backend in advance. Once warm, all subsequent requests (login, payments, transaction history) respond quickly and normally - only the very first request after a period of inactivity is slow. If you go straight to the frontend or Swagger without doing this first, the initial action you try (e.g. logging in) may appear to hang for up to a minute; this is the container cold-starting, not the application failing or being broken.

---

## Tech Stack

| Layer | Technology                                                                                           |
|---|------------------------------------------------------------------------------------------------------|
| Language | Java 17                                                                                              |
| Framework | Spring Boot 3.5.x                                                                                    |
| Database | PostgreSQL                                                                                           |
| Async Processing | Spring `@Async` + custom in-memory `BlockingQueue` consumer                                          |
| SMS Provider | Twilio (with console-logging fallback support built in)                                              |
| Auth | Spring Security + JWT                                                                                |
| Resilience | Resilience4j (Circuit Breaker + Retry)                                                               |
| Mapping | MapStruct + Lombok                                                                                   |
| API Docs | Swagger / OpenAPI 3 (springdoc) - submitted as the API documentation deliverable for this assessment |
| Logging | SLF4J (via Lombok `@Slf4j`) throughout all service and controller classes                            |
| Testing | JUnit 5 + Mockito + Testcontainers (real Postgres in CI and locally)                                 |
| Containerization | Docker + Docker Compose                                                                              |
| CI/CD | GitHub Actions                                                                                       |
| Frontend | Vue 3 + Vuetify + Pinia + Vue Router                                                                 |
| Deployment | Render (backend web service, managed Postgres, static frontend site)                                 |

---

## Architecture

```
minipay-service/
├── backend/                  Spring Boot REST API
│   ├── src/main/java/com/minipay/minipay_service/
│   │   ├── config/            Security, Async, Swagger, SMS provider config
│   │   ├── controller/        Auth, Payment, Webhook controllers
│   │   ├── service/            Business logic (Payment, Auth, Notification, Gateway)
│   │   ├── repository/         Spring Data JPA repositories
│   │   ├── domain/              JPA entities + enums
│   │   ├── dto/                  Request/response DTOs
│   │   ├── mapper/                MapStruct entity <-> DTO mapping
│   │   ├── exception/              Custom exceptions + global handler
│   │   └── security/                JWT provider, filter, UserDetailsService
│   ├── src/test/                Unit tests (Mockito) + integration tests (Testcontainers)
│   └── Dockerfile
├── frontend/                  Vue 3 SPA
│   ├── src/
│   │   ├── views/               Login, Register, Dashboard, Payments
│   │   ├── components/          PaymentForm, TransactionTable
│   │   ├── stores/                Pinia auth store
│   │   └── services/               Axios API client with JWT interceptor
│   └── Dockerfile
├── docker-compose.yml         Full local stack: Postgres + backend + frontend
└── .github/workflows/ci.yml   Automated test + build pipeline
```

### Request Flow — Payment Initiation

```
Client → POST /api/payments (JWT required)
  → PaymentController
    → PaymentService
      1. Save payment as PENDING
      2. Call PaymentGatewayService (wrapped in Resilience4j Circuit Breaker + Retry)
           - Amount ≥ 100,000 → deterministic failure (business rule, not a transient error)
           - 10% random chance → simulated transient gateway failure (retried)
           - Otherwise → success, generates a mock reference
      3. Update payment to SUCCESS or FAILED
      4. Trigger NotificationService.sendPaymentNotification() — fires @Async, non-blocking
           → enqueues an SMS job onto an in-memory BlockingQueue
           → a dedicated background consumer thread picks it up and "delivers" it (or sends via Twilio)
           → logs the outcome to notification_logs
  ← Returns immediately to the client; SMS delivery happens on a separate thread
```

---

## Running Locally

### Prerequisites
- Java 17+
- Maven (or use the included `mvnw`)
- Node.js 20+
- Docker + Docker Compose

### Option A — Full stack via Docker Compose (recommended)

1. Create a `.env` file at the project root:
   ```env
   POSTGRES_DB=minipay_db
   POSTGRES_USER=minipay_user
   POSTGRES_PASSWORD=minipay_pass
   JWT_SECRET=replace-with-a-real-32-plus-character-secret
   TWILIO_ACCOUNT_SID=your-twilio-sid
   TWILIO_AUTH_TOKEN=your-twilio-token
   TWILIO_FROM_NUMBER=+1xxxxxxxxxx
   ```

2. Run:
   ```bash
   docker compose up --build
   ```

3. Access:
    - Frontend: `http://localhost:8080`
    - Backend API: `http://localhost:3271/api`
    - Swagger UI: `http://localhost:3271/swagger-ui/index.html`

### Option B — Run backend and frontend separately (for active development)

**Backend:**
```bash
cd backend
# Ensure a local Postgres instance is running on localhost:5432
# with database minipay_db, user minipay_user, password minipay_pass
./mvnw spring-boot:run
```
This uses the `dev` profile by default (`application-dev.yml`), which auto-creates the schema via Hibernate (`ddl-auto: update`).

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Visit `http://localhost:5173`.

---

## Running Tests

```bash
cd backend
./mvnw clean test
```

This runs **21 tests** across two layers:
- **Unit tests** (`AuthServiceTest`, `PaymentServiceTest`) - Mockito-based, no Spring context, no database.
- **Integration tests** (`AuthControllerIntegrationTest`, `PaymentControllerIntegrationTest`, `MinipayServiceApplicationTests`) - full Spring context, real HTTP calls via `TestRestTemplate`, and a **real PostgreSQL instance spun up via Testcontainers** for each test run - this exercises the actual JPA mappings, Hibernate schema generation, and Spring Security filter chain exactly as they behave in production.

Docker must be running locally for the integration tests to start their Testcontainers-managed Postgres instance.

Tests also run automatically on every push via GitHub Actions (see badge/Actions tab on the repo).

---

## API Reference

Full interactive documentation is available via Swagger UI at the link above. Summary of endpoints:

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register a new user, returns JWT |
| POST | `/api/auth/login` | No | Login, returns JWT |
| POST | `/api/payments` | Yes | Initiate a payment |
| GET | `/api/payments/{id}` | Yes | Get a single payment's details |
| GET | `/api/payments` | Yes | Paginated transaction history (`?page=&size=&sort=`) |
| POST | `/api/webhooks/payment` | No | Simulates an external payment gateway callback updating a payment's status by reference |

> **Note on pagination in Swagger UI:** springdoc renders the `Pageable` parameter as a single JSON object rather than separate query fields, which can be confusing. If testing pagination directly in Swagger, remove the `sort` field from that JSON box entirely. Testing via curl/Postman with explicit query params (`?page=0&size=10&sort=createdAt,desc`) avoids this challenge entirely.

---

## Mock Integrations

**Payment Gateway** - `PaymentGatewayService` simulates a real payment processor:
- Amounts ≥ `100,000` always fail deterministically (configurable via `payment.gateway.failure-threshold`) - this is a demonstrable, repeatable way to exercise the failure path.
- A further 10% random chance of failure on any other amount (`payment.gateway.random-failure-rate`) - simulates genuine transient gateway errors, and is wrapped in Resilience4j `@Retry` + `@CircuitBreaker`.

**SMS Notifications** — `SmsService` is an interface, allowing the active provider to be swapped via configuration without touching business logic:
- `ConsoleSmsService` (default, currently active) - logs the message and routes it through a genuine in-memory `BlockingQueue` with a dedicated consumer thread, satisfying the assignment's *"console logging + simple in-memory queue"* requirement as a literal architectural pattern, not just a log statement.
- `TwilioSmsService` - fully implemented and tested with real SMS delivery during development. **Disabled in the current deployment** because Twilio's trial number is domestic-US-only for SMS (confirmed via Twilio error 21612), and delivering to Kenyan numbers would require a different sender configuration outside this assessment's scope. The code path is complete and was verified working end-to-end against a US recipient number.

Switch providers via `sms.provider` in `application.yml` (`console` or `twilio`).

---

## Known Tradeoffs & Decisions

This section documents deliberate engineering decisions made under the assessment's time constraints, including where a more robust approach exists and why it wasn't taken here:

- **`ddl-auto: update` in production**, rather than versioned Flyway/Liquibase migrations. Hibernate auto-manages schema changes on the production database. This is appropriate for a fresh deployment with no production data at risk, but in a longer-lived system this would be replaced with explicit, reviewable migration files and `ddl-auto: validate` to prevent any silent schema drift.
- **Transaction history search (`PaymentsView`) is page-level, not full-history.** The search box filters only the currently loaded page of results client-side, not the entire dataset server-side. A complete solution would add a `search` query parameter to the backend's `GET /api/payments` endpoint.
- **No Sender-ID-verified SMS delivery to Kenyan numbers** - Twilio's trial sender is domestic-US-only for SMS, and a Kenya-capable sender requires configuration outside this assessment's scope. The `SmsService` interface and `TwilioSmsService` implementation are fully built and were verified working end-to-end against a real US number; only the final sender-region constraint blocks delivery to Kenyan numbers in this deployment. Console logging (with the in-memory queue) is the active provider and fully satisfies the assignment's explicit fallback option.
- **AWS deployment was not used** - no active AWS free-tier eligibility at submission time. Render was selected over Railway specifically because Railway's free tier was discontinued and its current low monthly credit allotment risked the deployment going offline mid-review.
- **Local dev database credentials (`application-dev.yml`) are committed in plaintext** (`minipay_user`/`minipay_pass`). This is intentional and low-risk: these credentials only ever resolve to `localhost`, which is not reachable outside the developer's own machine. Real, internet-reachable credentials (Twilio, the production database, JWT signing secret) are not committed — they're supplied via environment variables, `.env` (gitignored), Render's environment configuration, or GitHub Actions encrypted secrets.

---

## Security Notes

- JWT-based stateless authentication; tokens expire after 24 hours (configurable via `jwt.expiration`).
- Passwords hashed with BCrypt.
- CORS explicitly configured to allow only the known frontend origins (`localhost:5173`, `localhost:8080`, and the deployed Render frontend URL) rather than a wildcard.
- All third-party credentials (Twilio, database) are environment-variable-driven with no committed defaults in the production profile.