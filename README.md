# FieldOps API

FieldOps API is a Spring Boot backend for field service operations. It manages work orders, customers, locations, assets, engineers, parts, time entries, signatures, attachments, and offline sync features. It exposes a simple, consistent REST surface with UUID identifiers and ISO‑8601 timestamps.

This app is a field service management platform designed for field engineers who often work offline and dispatchers who manage jobs in real time. Engineers can do their entire job on-site — even without internet — and everything syncs reliably once they’re back online.

## Who It’s For

- Field Engineers
  - Receive assigned jobs
  - Work fully offline
  - Capture everything on-site
  - Sync automatically when connectivity returns
- Dispatchers / Office Staff
  - Create and assign work orders
  - Track job progress in near real time
  - Review completed jobs with full history
  - Resolve conflicts if needed

## Capabilities

- Work Orders
  - Create, assign, and manage work orders
  - Job lifecycle: NEW → ASSIGNED → EN_ROUTE → ON_SITE → COMPLETED / CANCELLED
  - Priority handling (LOW → URGENT)
  - Scheduling (planned vs actual time)
  - Soft deletion via `deletedAt` (audit-safe)
- Customers & Locations
  - Customers with one or more locations
  - Locations include address, GPS, and contact info
  - Every job links to a specific location
- Assets (Equipment)
  - Track equipment installed at customer locations
  - Store serial numbers, manufacturer/model
  - Link jobs to a specific asset (optional) for per-asset history
- Engineer Assignment
  - Assign one or more engineers to a job
  - Track who is assigned and when assignments change
  - Supports team or solo jobs
- Time Tracking
  - Log travel, on-site work, breaks
  - Actual duration can be computed from entries
  - Useful for productivity, reporting, and billing
- Notes & Job Timeline
  - Add notes during work
  - Timeline events for status changes, notes, parts, attachments
  - Full audit-friendly history per job
- Attachments
  - Photos (before/after), documents, signatures
  - Files stored locally first; uploaded when online
  - Attachments never block job completion
- Signatures
  - Capture customer or engineer signatures
  - Stored securely and linked to job completion
- Parts & Materials
  - Maintain parts catalog with pricing
  - Record parts used per job (qty and price)
- Location Tracking (Optional)
  - Record engineer GPS locations
  - Track last known location and travel history
  - Useful for ETA, dispatch visibility, safety
- Offline-First Sync (Core Feature)
  - Works 100% offline with local-first saves
  - Background sync when network is available
  - Sync is reliable, idempotent, and conflict-aware
- Conflict Handling
  - Detects concurrent edits; never silently overwrites
  - Keep local engineer work, allow dispatcher resolution, merge notes/attachments
- Audit Trail
  - Tracks who changed what and when for compliance and accountability
- AI-Ready (Assistive)
  - Auto-summaries of job notes, voice-to-text, smart status suggestions
  - Parts/duration prediction, image analysis (e.g., serial numbers)

## Features

- Work orders: lifecycle, scheduling, completion metadata
- Assignments: link engineers to work orders over time
- Events: audit-worthy actions on work orders
- Time entries: track engineer effort per work order
- Parts & catalog: usage per work order with pricing
- Signatures & attachments: files linked to work orders/events
- Customers, locations, assets: core master data
- Engineer telemetry: last location and location history, availability
- Offline sync: device sync state, queue, conflict tracking, changes log
- System audit logs for record changes

## Tech Stack

- Java 21, Gradle
- Spring Boot (Web, Validation, Data JPA)
- PostgreSQL
- Lombok (DTOs/entities)
- Error handling: `io.github.wimdeblauwe:error-handling-spring-boot-starter`

## Quick Start

Prerequisites:

- JDK 21+
- PostgreSQL 14+ running locally

1) Configure database

Update `src/main/resources/application.yml` or override via environment variables:

```
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/fieldops-api
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=<your_password>
SPRING_PROFILES_ACTIVE=local
```

Example Postgres setup:

```sql
CREATE DATABASE "fieldops-api";
-- Using default superuser 'postgres' (or create a dedicated user)
```

2) Run the app

```
./gradlew bootRun
```

The API listens on http://localhost:8080. A quick readiness check is `GET /` which responds with "Hello World!".

## Configuration

- Profiles: `local` (development), `production`
- DB settings: see `application.yml` for env var fallbacks
- JPA auditing is enabled; entities include created/updated timestamps

## API Overview

Base path: `/api`

All resources follow conventional CRUD:

- `GET /api/<resource>` → list (no pagination)
- `GET /api/<resource>/{id}` → fetch by UUID
- `POST /api/<resource>` → create (returns UUID)
- `PUT /api/<resource>/{id}` → update
- `DELETE /api/<resource>/{id}` → delete

Available resources (non-exhaustive):

- Users: `/api/users`
- Customers: `/api/customers`
- Locations: `/api/locations`
- Assets: `/api/assets`
- Parts catalog: `/api/partsCatalogs`
- Work orders: `/api/workOrders`
- Work order assignments: `/api/workOrderAssignments`
- Work order events: `/api/workOrderEvents`
- Work order time entries: `/api/workOrderTimeEntries`
- Work order parts: `/api/workOrderParts`
- Work order signatures: `/api/workOrderSignatures`
- Attachments: `/api/attachments`
- Engineer availability: `/api/engineerAvailabilities`
- Engineer locations (history): `/api/engineerLocations`
- Engineer last location: `/api/engineerLastLocations`
- Device sync state: `/api/deviceSyncStates`
- Sync queue: `/api/syncQueues`
- Sync conflicts: `/api/syncConflicts`
- Offline changes log: `/api/offlineChangesLogs`
- Audit logs: `/api/auditLogs`

Notes:

- All identifiers are UUIDs.
- Timestamps use ISO‑8601 (OffsetDateTime).
- Some deletes are guarded by referential checks; a 409 is returned if a record is referenced by others (see Error Handling).

## Example: Work Orders

Endpoint: `/api/workOrders`

Create

```bash
curl -X POST http://localhost:8080/api/workOrders \
  -H 'Content-Type: application/json' \
  -d '{
    "workOrderNo": "WO-1001",
    "title": "Replace pump gasket",
    "description": "Leak detected, replace gasket and test",
    "priority": "HIGH",
    "status": "SCHEDULED",
    "scheduledStart": "2025-01-01T09:00:00Z",
    "scheduledEnd": "2025-01-01T12:00:00Z",
    "estimatedDurationMinutes": 180,
    "version": 1,
    "changeVersion": 1,
    "createdAt": "2025-01-01T08:00:00Z",
    "updatedAt": "2025-01-01T08:00:00Z",
    "location": "<location-uuid>",
    "asset": "<asset-uuid>",
    "lastModifiedBy": "<user-uuid>"
  }'
```

Fetch

```bash
curl http://localhost:8080/api/workOrders/<work-order-uuid>
```

Delete (may return 409 if referenced)

```bash
curl -X DELETE http://localhost:8080/api/workOrders/<work-order-uuid>
```

## Error Handling

- 404 Not Found: when an entity does not exist
- 409 Conflict: when attempting to delete an entity that is still referenced
- Standardized JSON error responses provided by the error-handling starter

## Development Tips

- Lombok: ensure your IDE has Lombok plugin and annotation processing enabled (e.g., IntelliJ)
- Edit `application-local.yml` to override local config without changing defaults
- Entities and DTOs use UUIDs; prefer server-generated IDs where applicable

## Build

Build the application:

```
./gradlew clean build
```

Run the built JAR (production profile shown):

```
java -Dspring.profiles.active=production -jar ./build/libs/fieldops-api-0.0.1-SNAPSHOT.jar
```

## Docker

Build an OCI image with the Spring Boot plugin:

```
./gradlew bootBuildImage --imageName=com.fieldops/fieldops-api
```

Run the image (configure DB and profile):

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e JDBC_DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/fieldops-api \
  -e JDBC_DATABASE_USERNAME=postgres \
  -e JDBC_DATABASE_PASSWORD=<your_password> \
  com.fieldops/fieldops-api
```

## Testing

Run tests:

```
./gradlew test
```

## Credits

Initial scaffolding generated with Bootify.io. See their tips for working with the code [here](https://bootify.io/next-steps/).

## References

- Spring Boot reference: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/
- Spring Data JPA reference: https://docs.spring.io/spring-data/jpa/reference/jpa.html
- Gradle user manual: https://docs.gradle.org/
