
# BuildFlow — Distributed CI/CD Build and Test Platform

BuildFlow is a Java Spring Boot backend infrastructure project that simulates how modern CI/CD systems schedule, distribute, execute, retry, and monitor build and test jobs across multiple worker nodes.

The project demonstrates core backend and distributed-systems concepts such as asynchronous job execution, Kafka-based task distribution, Redis-backed worker coordination, idempotent job triggering, heartbeat monitoring, retry handling, failure recovery, and observability.

---

## Problem Statement

Modern engineering teams rely on CI/CD systems to build, test, and validate code changes before deployment. However, a build platform must handle several complex backend problems:

- Multiple users triggering builds concurrently
- Duplicate build requests from retries or network failures
- Worker crashes during build execution
- Long-running or stuck build jobs
- Queue saturation during high traffic
- Job retry and failure tracking
- Build history, logs, and operational visibility

A simple API that starts a build is not enough.

BuildFlow solves this by creating a distributed build execution platform where jobs are submitted through APIs, dispatched through Kafka, coordinated using Redis, executed by worker services, and tracked through PostgreSQL-backed metadata and observability signals.

---

## Key Highlights

- Build and test pipeline creation
- Kafka-based asynchronous job dispatch
- Redis-backed worker heartbeat tracking
- Redis locks and idempotency keys for duplicate prevention
- PostgreSQL job metadata and execution history
- Retry handling with failed job tracking
- Worker timeout detection and orphaned-job recovery
- Build status lifecycle management
- Queue and worker observability signals
- Docker Compose-based local infrastructure
- Swagger/OpenAPI API documentation
- Production-style backend project structure

---

## Why BuildFlow Is Different

Most fresher projects are simple CRUD applications. BuildFlow focuses on real backend infrastructure problems similar to those handled by systems like GitHub Actions, GitLab CI, Jenkins, and internal developer productivity platforms.

| Normal Project | BuildFlow |
|---|---|
| Simple CRUD APIs | Distributed job execution system |
| Single request-response flow | Kafka-based asynchronous workflows |
| No worker failure handling | Heartbeat monitoring and orphaned-job recovery |
| No duplicate request protection | Redis idempotency keys and locks |
| Basic status updates | Full build lifecycle tracking |
| No infrastructure simulation | Queue, worker, retry, and timeout handling |
| No observability | Metrics, logs, and operational signals |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3 |
| Messaging | Apache Kafka |
| Database | PostgreSQL |
| Cache / Coordination | Redis |
| API Documentation | Swagger / OpenAPI |
| DevOps | Docker, Docker Compose |
| Observability | OpenTelemetry-style logs and metrics |
| Testing | JUnit, Mockito, Testcontainers-ready design |

---

## System Architecture

```text
Client / Developer
        |
        v
Spring Boot API Service
        |
        +-----------------------------+
        |                             |
        v                             v
PostgreSQL                     Kafka Topics
Build Metadata                 build.requested
Pipeline Runs                  build.started
Job History                    build.completed
Worker Status                  build.failed
Execution Logs                 worker.events
        |                             |
        v                             v
Redis                         Worker Service
Idempotency Keys              Build Executor
Distributed Locks             Test Executor
Worker Heartbeats             Retry Handler
Job Progress Cache            Timeout Handler
        |
        v
Observability Layer
Logs, Metrics, Traces
````

---

## Core Workflow

```text
User Triggers Build
        ↓
API Validates Request
        ↓
Idempotency Key Checked in Redis
        ↓
Build Metadata Stored in PostgreSQL
        ↓
Kafka Event Published
        ↓
Available Worker Consumes Build Job
        ↓
Worker Heartbeat Starts
        ↓
Build/Test Steps Execute
        ↓
Status Updated in PostgreSQL
        ↓
Logs and Metrics Emitted
        ↓
Job Marked SUCCESS / FAILED / RETRYING
```

---

## Main Modules

### 1. Build API Service

Handles incoming build and test requests.

Responsibilities:

* Trigger new build
* Trigger test job
* Fetch build status
* Cancel build
* Retry failed build
* Fetch build history
* Expose dashboard metrics

---

### 2. Job Scheduler

The scheduler decides how build jobs are queued and dispatched.

Responsibilities:

* Create build job metadata
* Assign job priority
* Publish job events to Kafka
* Track job state transitions
* Prevent duplicate job creation using idempotency keys

Possible scheduling factors:

* Job priority
* Repository/project ID
* Retry count
* Worker availability
* Queue depth
* Timeout limits

---

### 3. Kafka Job Dispatcher

Kafka decouples job submission from job execution.

Example topics:

```text
build.requested
build.started
build.completed
build.failed
build.retry
worker.heartbeat
audit.events
```

Why Kafka is used:

* Asynchronous build execution
* Worker scalability
* Event-driven job processing
* Retry-safe message handling
* Decoupling API service from workers
* Build event history

---

### 4. Worker Service

Workers consume build jobs from Kafka and execute simulated build/test steps.

Responsibilities:

* Consume build events
* Acquire Redis lock before execution
* Update worker heartbeat
* Execute build stages
* Push logs and progress updates
* Mark job success or failure
* Release lock after completion

Example worker lifecycle:

```text
IDLE → ASSIGNED → RUNNING → COMPLETED
                  ↓
                FAILED
                  ↓
               RETRYING
```

---

### 5. Redis Coordination Layer

Redis is used for fast coordination between API services and workers.

Redis key examples:

```text
idempotency:build:{requestId}
lock:build:{buildId}
worker:heartbeat:{workerId}
job_progress:{buildId}
retry_count:{buildId}
```

Use cases:

* Prevent duplicate build triggers
* Prevent two workers from executing the same job
* Track worker heartbeat
* Cache build progress
* Store retry counters

---

### 6. Worker Heartbeat Monitoring

Worker heartbeat helps detect failed or unresponsive workers.

```text
Worker sends heartbeat every N seconds
        ↓
Monitor checks last heartbeat timestamp
        ↓
If heartbeat is stale:
    worker = UNHEALTHY
    running job = ORPHANED
    job is marked for retry or reassignment
```

This allows BuildFlow to recover jobs that were interrupted because of worker crashes or network issues.

---

### 7. Retry and Failure Handling

BuildFlow supports retry-aware execution for failed jobs.

Failure cases handled:

| Failure Case            | Handling Strategy                        |
| ----------------------- | ---------------------------------------- |
| Duplicate API request   | Redis idempotency key                    |
| Duplicate Kafka event   | Redis execution lock                     |
| Worker crash            | Heartbeat timeout and job reassignment   |
| Build timeout           | Mark failed and retry if attempts remain |
| Queue saturation        | Track queue depth and worker utilization |
| Job exceeds retry limit | Move to failed/dead-letter state         |

---

## Build Job Lifecycle

```text
PENDING
   ↓
QUEUED
   ↓
RUNNING
   ↓
SUCCESS

Alternative flows:

RUNNING → FAILED → RETRYING → QUEUED
RUNNING → TIMEOUT → RETRYING
RUNNING → ORPHANED → REASSIGNED
FAILED  → DEAD_LETTERED
QUEUED  → CANCELLED
```

---

## Database Design

Main tables:

```text
users
projects
pipelines
build_jobs
build_steps
worker_nodes
job_logs
audit_logs
```

### build_jobs

```text
id
project_id
pipeline_id
status
priority
idempotency_key
attempt_count
max_attempts
worker_id
error_message
created_at
started_at
completed_at
updated_at
```

### worker_nodes

```text
id
worker_name
status
last_heartbeat_at
current_job_id
total_jobs_executed
created_at
updated_at
```

### build_steps

```text
id
build_job_id
step_name
status
started_at
completed_at
logs
exit_code
```

### job_logs

```text
id
build_job_id
worker_id
log_level
message
created_at
```

---

## Recommended Indexes

```sql
CREATE INDEX idx_build_jobs_status
ON build_jobs(status);

CREATE INDEX idx_build_jobs_project_created
ON build_jobs(project_id, created_at DESC);

CREATE INDEX idx_build_jobs_worker
ON build_jobs(worker_id);

CREATE INDEX idx_worker_nodes_status
ON worker_nodes(status);

CREATE INDEX idx_job_logs_build
ON job_logs(build_job_id, created_at);
```

---

## API Endpoints

### Build Jobs

| Method | Endpoint                              | Description                 |
| ------ | ------------------------------------- | --------------------------- |
| POST   | `/api/v1/builds`                      | Trigger a new build         |
| GET    | `/api/v1/builds/{buildId}`            | Get build status            |
| POST   | `/api/v1/builds/{buildId}/retry`      | Retry failed build          |
| POST   | `/api/v1/builds/{buildId}/cancel`     | Cancel queued/running build |
| GET    | `/api/v1/builds/{buildId}/logs`       | Get build logs              |
| GET    | `/api/v1/projects/{projectId}/builds` | Get project build history   |

### Workers

| Method | Endpoint                               | Description           |
| ------ | -------------------------------------- | --------------------- |
| POST   | `/api/v1/workers/register`             | Register worker       |
| POST   | `/api/v1/workers/{workerId}/heartbeat` | Send worker heartbeat |
| GET    | `/api/v1/workers`                      | List workers          |
| GET    | `/api/v1/workers/{workerId}`           | Get worker details    |

### Dashboard

| Method | Endpoint                     | Description                  |
| ------ | ---------------------------- | ---------------------------- |
| GET    | `/api/v1/dashboard/stats`    | Build system statistics      |
| GET    | `/api/v1/dashboard/queue`    | Queue depth and pending jobs |
| GET    | `/api/v1/dashboard/workers`  | Worker health summary        |
| GET    | `/api/v1/dashboard/failures` | Failure and retry summary    |

---

## Example Build Request

```bash
curl -X POST http://localhost:8080/api/v1/builds \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: build-req-001" \
  -d '{
    "projectId": 1,
    "branch": "main",
    "commitHash": "a1b2c3d4",
    "priority": "HIGH",
    "pipelineSteps": ["checkout", "compile", "test", "package"]
  }'
```

---

## Example Build Response

```json
{
  "buildId": 101,
  "projectId": 1,
  "status": "QUEUED",
  "priority": "HIGH",
  "message": "Build job created successfully",
  "createdAt": "2026-05-24T10:30:00"
}
```

---

## Example Job Status Response

```json
{
  "buildId": 101,
  "status": "RUNNING",
  "workerId": "worker-01",
  "currentStep": "test",
  "attemptCount": 1,
  "startedAt": "2026-05-24T10:31:00",
  "logsUrl": "/api/v1/builds/101/logs"
}
```

---

## Setup

### Prerequisites

* Java 17+
* Maven
* Docker
* Docker Compose

---

## Run Infrastructure

```bash
docker compose up -d
```

This starts:

| Service       | Port |
| ------------- | ---- |
| PostgreSQL    | 5432 |
| Redis         | 6379 |
| Kafka         | 9092 |
| Zookeeper     | 2181 |
| BuildFlow API | 8080 |

---

## Run Application Locally

```bash
mvn clean install
mvn spring-boot:run
```

Application:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

---

## Docker Run

```bash
mvn clean package -DskipTests
docker build -t buildflow .
docker run -p 8080:8080 buildflow
```

Or run everything:

```bash
docker compose up --build
```

---

## Environment Variables

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/buildflow
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## Observability

BuildFlow is designed to expose production-style operational signals.

Recommended metrics:

```text
build_jobs_total
build_jobs_success_total
build_jobs_failed_total
build_duration_seconds
queue_depth
worker_utilization
worker_heartbeat_lag
retry_count_total
orphaned_jobs_total
kafka_consumer_lag
```

Recommended log fields:

```text
correlation_id
project_id
build_id
worker_id
pipeline_id
status
attempt_count
duration_ms
```

Trace flow:

```text
API Request
  → Build Job Creation
  → Kafka Publish
  → Worker Consume
  → Build Step Execution
  → PostgreSQL Status Update
  → Log/Metrics Emission
```

---

## Testing Strategy

BuildFlow should include tests for:

* Build job creation
* Idempotent build trigger behavior
* Kafka event publishing
* Worker job consumption
* Worker heartbeat update
* Worker timeout detection
* Failed job retry flow
* Duplicate Kafka event handling
* Build cancellation
* Build log retrieval
* Dashboard statistics

Recommended tools:

* JUnit 5
* Mockito
* Spring Boot Test
* Testcontainers
* PostgreSQL container
* Redis container
* Kafka container

---

## Failure Handling

| Scenario                              | Expected Behavior                            |
| ------------------------------------- | -------------------------------------------- |
| Duplicate build request               | Return existing build using idempotency key  |
| Duplicate Kafka event                 | Ignore duplicate execution using Redis lock  |
| Worker crash                          | Detect stale heartbeat and reassign job      |
| Build timeout                         | Mark job failed and retry if attempts remain |
| Retry limit exceeded                  | Mark job as permanently failed               |
| Kafka temporarily unavailable         | Retry publishing or mark dispatch failure    |
| DB update fails after build execution | Retry status update and reconcile job state  |

---

## Design Decisions

### Why Kafka?

Kafka allows BuildFlow to decouple build submission from build execution. The API service only creates metadata and publishes an event, while workers process jobs asynchronously.

### Why Redis?

Redis is used for fast coordination tasks such as idempotency, distributed locks, worker heartbeat tracking, and job progress caching.

### Why PostgreSQL?

PostgreSQL stores durable build metadata, job history, logs, worker status, and pipeline execution records.

### Why Heartbeats?

Heartbeats help detect failed workers and recover jobs that were running on unavailable nodes.

### Why Idempotency Keys?

Idempotency keys prevent duplicate build creation when users retry requests or when network failures cause repeated API calls.

---

## Future Improvements

* Kubernetes deployment manifests
* Horizontal worker autoscaling
* Priority queue scheduling
* Dead-letter queue dashboard
* GitHub webhook integration
* Real shell command execution sandbox
* Prometheus and Grafana dashboards
* Role-based access control for projects
* Artifact storage using MinIO/S3
* Build cache support
* Multi-stage pipeline DAG execution
* Worker resource limits and isolation

---



## Author

**Kunal Kumar**

GitHub: [github.com/kunal7216](https://github.com/kunal7216)
LinkedIn: [linkedin.com/in/kunal7216](https://linkedin.com/in/kunal7216)

```
```
