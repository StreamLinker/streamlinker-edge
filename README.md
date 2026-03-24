# StreamLinker Edge

Edge-side streaming application for StreamLinker.

`streamlinker-edge` runs on the edge node and is responsible for:
- managing pull stream definitions
- starting and stopping local pull pipelines
- managing RTMP push targets
- recovering runtime state after application restart or local ZLMediaKit restart
- exposing local admin APIs and a built-in lightweight web console

## Current status

The repository already contains a working enterprise-style skeleton based on:
- Spring Boot 3
- MyBatis-Plus
- MySQL
- ZLMediaKit HTTP API integration

Current capabilities:
- persistent stream definitions, push targets, runtime state, and process state
- pull modes: `FFMPEG` and `PROXY`
- push mode: `RTMP`
- `stream_process` driven orchestration for `PULL_UP`, `PULL_DOWN`, `PUSH_UP`, `PUSH_DOWN`
- startup recovery and reconcile tasks
- local admin APIs for stream and push-target CRUD
- built-in static admin pages

## Repository layout

```text
streamlinker-edge/
|- src/main/java/io/streamlinker/edge
|  |- domain
|  |- infra/db
|  |- service
|  |- service/process
|  |- service/task
|  `- web
|- src/main/resources
|  |- sql
|  `- static
`- src/test
```

## Built-in web pages

After the application starts, these pages are available:
- `/` : dashboard
- `/pulls.html` : pull stream management
- `/pushes.html` : push target management

## Main APIs

Admin APIs:
- `GET/POST/PUT/DELETE /api/admin/streams`
- `GET/POST/PUT/DELETE /api/admin/push-targets`

Local runtime APIs:
- `GET /api/local/streams`
- `POST /api/local/streams/{streamId}/start`
- `POST /api/local/streams/{streamId}/stop`
- `GET /api/local/push-targets`
- `POST /api/local/push-targets/{targetCode}/start`
- `POST /api/local/push-targets/{targetCode}/stop`

## Configuration

Main configuration file:
- `src/main/resources/application.yml`

Example local ZLM configuration:
- `src/main/resources/application-local-zlm.example.yml`

Database schema:
- `src/main/resources/sql/streamlinker-edge-schema.sql`

## Dependency

This project depends on the shared SDK repository:
- [streamlinker-zlm-sdk](https://github.com/StreamLinker/streamlinker-zlm-sdk)

## Local development

Prerequisites:
- JDK 17+
- MySQL 8+
- local or reachable ZLMediaKit

Run tests:

```bash
mvn test
```

Run application:

```bash
mvn spring-boot:run
```

## Roadmap

Planned next steps:
- improve README and operator documentation
- add stream process diagnostics to the web console
- build `streamlinker-cloud`
- publish `streamlinker-zlm-sdk` artifacts and versioning strategy