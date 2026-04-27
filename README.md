# Flood CRM Service

Spring Boot microservice backend for the CRM Admin platform. Serves both the `flood-website-crm` admin dashboard and the `flood-mobile-crm` admin mobile app.

## Responsibility

Handles everything administrators need: sensor node management, IoT data ingestion, analytics, dashboard data, emergency broadcasts, incident reports, flood zones, user management, and admin authentication.

## Tech Stack

- **Java 21** + **Spring Boot 3.2**
- **PostgreSQL 16** — primary database (`flood_crm` schema)
- **Redis 7** — token cache, rate limiting, real-time alert queue
- **Spring Security** + **JWT** — admin authentication and authorisation
- **Maven** — build tool

## Port

Runs on **http://localhost:4002** by default.

## Prerequisites

- Java 21+
- Maven (or use `mvnw` wrapper)
- Docker + Docker Compose (for local PostgreSQL + Redis)

## Quick Start (with Docker)

```bash
git clone <repo-url>
cd flood-service-crm

cp .env.example .env
# Edit .env — set JWT_SECRET and JWT_REFRESH_SECRET

docker-compose up --build
```

API is available at **http://localhost:4002**

## Quick Start (without Docker)

```bash
cp .env.example .env
# Set DATABASE_URL to your local Postgres, REDIS_URL to your local Redis

# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/login` | Admin login |
| POST | `/auth/refresh` | Refresh access token |
| GET | `/sensors` | List all sensor nodes |
| GET | `/dashboard/nodes` | Dashboard node table |
| GET | `/dashboard/time-series` | Dashboard chart data |
| GET | `/analytics` | Analytics stats and charts |
| GET/POST | `/broadcasts` | Emergency broadcasts (admin only) |
| POST | `/ingest` | IoT sensor data ingestion |
| GET | `/feed` | Alert feed |
| GET/PATCH | `/reports/{id}/status` | Incident report management |
| GET | `/zones` | Flood risk zones |
| GET | `/admin/users` | User management (admin only) |

## Docker Compose Services

| Service | Container | Host Port |
|---------|-----------|-----------|
| Spring Boot API | `flood-crm-api` | 4002 |
| PostgreSQL 16 | `flood-crm-postgres` | 5434 |
| Redis 7 | `flood-crm-redis` | 6381 |

## Environment Variables

See `.env.example` for all required variables.
