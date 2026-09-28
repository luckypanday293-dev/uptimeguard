# UptimeGuard

UptimeGuard is a full-stack website and API monitoring platform built with Java, Spring Boot, PostgreSQL, React, and Docker. It schedules health checks, tracks latency, retries failed requests, records incidents, detects recovery, and surfaces monitoring history on a dashboard.

## Features

- Scheduled HTTP health checks with per-monitor intervals
- Configurable timeout, expected HTTP status, and retry count
- Response-time tracking and uptime percentage calculation
- Incident creation when a monitor becomes unavailable
- Automatic incident resolution when the service recovers
- In-app incident and recovery notifications
- Dashboard showing monitor status, latency, uptime, incidents, and notifications
- Manual "check now" endpoint/action
- PostgreSQL persistence
- Automated unit tests for retry and recovery behavior
- Dockerfiles plus Docker Compose configuration
- GitHub Actions workflow for backend tests, frontend build, and container image publishing

## Architecture

```text
React dashboard (5174)
        |
        | REST/JSON
        v
Spring Boot API (8081)
        |
        +--> Scheduled monitor checks --> external websites/APIs
        |
        v
PostgreSQL (host port 5433)
```

## Main data model

- `Monitor` — URL, expected status, timeout, retries, interval, current state
- `CheckResult` — every health check, latency, status code, attempts, error
- `Incident` — outage start, recovery time, duration, failure reason
- `Notification` — incident-opened and recovery events

## Local development

### Requirements

- Java 21
- Maven 3.9+
- Node.js 20+ or 22+
- Docker Desktop

### 1. Start PostgreSQL

From the project root:

```powershell
docker compose up -d
```

Verify it:

```powershell
docker compose ps
```

### 2. Start the backend

```powershell
cd backend
mvn spring-boot:run
```

Backend: `http://localhost:8081`

Local PostgreSQL is exposed on host port `5433` so it can coexist with other projects that may already use `5432`.

Health endpoint: `http://localhost:8081/api/health`

A default monitor named **UptimeGuard API** is seeded and checks the backend's own health endpoint every 20 seconds.

### 3. Start the frontend

Open another terminal:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Frontend: `http://localhost:5174`

## Useful API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Backend health |
| GET | `/api/dashboard` | Dashboard summary |
| GET | `/api/monitors` | List monitors |
| POST | `/api/monitors` | Create monitor |
| PUT | `/api/monitors/{id}` | Update monitor |
| POST | `/api/monitors/{id}/check` | Run a manual check |
| GET | `/api/checks` | Check history |
| GET | `/api/incidents` | Incident history |
| GET | `/api/notifications` | Notifications |
| PATCH | `/api/notifications/{id}/read` | Mark notification read |

## Retry and incident behavior

For a monitor configured with `retryCount = 2`, UptimeGuard can make up to three attempts: the initial request plus two retries. If all attempts fail, the monitor is marked `DOWN` and an incident is opened if one is not already active. When a later check succeeds, the incident is resolved and a recovery notification is created.

## Automated tests

```powershell
cd backend
mvn test
```

The service tests verify retry behavior, incident creation, recovery handling, and default monitor settings.

## Frontend production build

```powershell
cd frontend
npm.cmd run build
```

## Run the full stack with Docker

The normal local workflow uses Docker only for PostgreSQL. To build and run the backend and frontend as containers too:

```powershell
docker compose -f compose.full.yml up --build
```

Then open `http://localhost:5174`.

## CI/CD

`.github/workflows/ci-cd.yml` runs backend tests and builds the React frontend for pushes and pull requests. On pushes to `main`, it also builds and publishes backend/frontend container images to GitHub Container Registry after the test/build jobs pass.
