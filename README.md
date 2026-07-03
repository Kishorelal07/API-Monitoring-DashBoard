# API Monitoring Dashboard

Full-stack monitoring system for tracking and visualizing external API calls from a central Java hub (Fintech APIs, CBS APIs, etc.).

## Architecture

```
┌─────────────────────┐     async HTTP POST      ┌──────────────────────┐
│  Existing Java JAR  │ ───────────────────────► │  Spring Boot Service │
│  (WAR / Hub)        │      /api/log            │  (Port 8080)         │
│  + Monitoring Client│                          └──────────┬───────────┘
└─────────────────────┘                                     │
                                                              ▼
                                                   ┌──────────────────────┐
                                                   │  MySQL               │
                                                   └──────────┬───────────┘
                                                              │
                                                   ┌──────────▼───────────┐
                                                   │  React Dashboard     │
                                                   │  (Port 5173 / 3000)  │
                                                   └──────────────────────┘
```

## Project Structure

| Path | Description |
|------|-------------|
| `backend/` | Spring Boot monitoring service |
| `frontend/` | React dashboard (Vite) |
| `integration/` | Plain Java client JAR for existing application |
| `docker-compose.yml` | Full stack with MySQL |
| `scripts/` | Sample data seeding scripts |

## Prerequisites

- **Java 17+** (backend)
- **Maven 3.8+**
- **Node.js 18+** (frontend dev)
- **MySQL 8+** (or use Docker)
- **Docker & Docker Compose** (optional)

---

## Quick Start with Docker

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Dashboard | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| MySQL | localhost:3306 |

Seed sample data (PowerShell):

```powershell
.\scripts\seed-sample-logs.ps1
```

---

## Manual Setup

### 1. Database (MySQL)

```sql
CREATE DATABASE api_monitoring;
```

Schema is auto-created by Hibernate (`ddl-auto: update`). Manual DDL is in `backend/src/main/resources/schema.sql`.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Environment variables (optional):

| Variable | Default |
|----------|---------|
| `DB_HOST` | localhost |
| `DB_PORT` | 3306 |
| `DB_NAME` | api_monitoring |
| `DB_USER` | root |
| `DB_PASSWORD` | root |
| `CORS_ORIGINS` | http://localhost:3000,http://localhost:5173 |

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

Optional AI chatbot environment variables (`frontend/.env`):

```bash
VITE_COHERE_API_KEY=your_cohere_api_key
VITE_COHERE_MODEL=command-a-03-2025
```

### 4. Integration Client (Existing JAR)

Build the client JAR:

```bash
cd integration
mvn clean package
```

Copy `target/api-monitoring-client-1.0.0.jar` into your existing application's `WEB-INF/lib` or classpath.

**HttpURLConnection central hub (recommended for your app):**

```java
import com.apimonitor.client.HttpURLConnectionApiLogger;

private static final HttpURLConnectionApiLogger API_LOGGER =
    new HttpURLConnectionApiLogger("http://your-monitor-host:8080");

public String callExternalApi(String apiName, String workItemId, String url, String payload) {
    return API_LOGGER.callExternalApi(apiName, workItemId, url, "POST", payload);
}
```

See full examples in:
- `integration/.../examples/HttpURLConnectionHubExample.java` — HttpURLConnection hub
- `integration/.../examples/CentralApiHubExample.java` — generic wrapper pattern
- `integration/.../examples/ApiInterceptorExample.java` — interceptor pattern

Set monitoring URL via JVM property:

```bash
-Dapi.monitor.url=http://localhost:8080
```

---

## API Reference

### POST `/api/log`

Receive API execution log (used by integration client). `responseTime` is optional — server calculates it from timestamps.

See `docs/sample-api-log-request.json` and `docs/sample-api-log-response.json`.

### GET `/api/logs`

Returns **today's logs by default** (`DATE(request_timestamp) = CURRENT_DATE`).

| Param | Type | Description |
|-------|------|-------------|
| `date` | yyyy-MM-dd | Filter by specific date (omit for today) |
| `apiName` | string | Exact API name match |
| `status` | SUCCESS \| FAILURE | Filter by status |

Examples:
- `/api/logs` → today's logs
- `/api/logs?date=2026-06-08&status=FAILURE`

### GET `/api/stats`

Aggregated statistics for today (or the given `date`). Same filter params as `/api/logs`.

```json
{
  "totalCalls": 100,
  "successCount": 92,
  "failureCount": 8,
  "successRate": 92.0,
  "avgResponseTime": 245.5,
  "byApiName": [...]
}
```

---

## Database Schema

```sql
CREATE TABLE api_logs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_name            VARCHAR(255) NOT NULL,
    work_item_id        VARCHAR(100),
    request_timestamp   TIMESTAMP NOT NULL,
    response_timestamp  TIMESTAMP NOT NULL,
    response_code       INTEGER,
    status              VARCHAR(20) NOT NULL,  -- SUCCESS | FAILURE
    error_message       VARCHAR(2000),
    response_time       BIGINT NOT NULL          -- milliseconds
);
```

---

## Dashboard Features

- **Stats cards** — total calls, success rate, avg response time
- **Today's logs by default** — loads `/api/logs` with no params on page open
- **Filters** — date picker, API name, status
- **Charts** — success vs failure bar chart, response time line chart
- **Log table** — full call history
- **Auto-refresh** — polls every 10 seconds

---

## Design Notes

- **Async logging** — integration client uses a daemon thread pool; API calls are never blocked waiting for the monitor
- **Decoupled** — frontend talks to backend REST API only; no server-side rendering
- **Plain Java client** — no Spring dependency in the integration module; uses `java.net.http.HttpClient`
- **Failure-safe** — monitoring failures are logged locally and do not affect the main API flow
