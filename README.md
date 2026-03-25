# StreamLinker Edge

English | [简体中文](README.zh-CN.md)

Edge-side streaming application for StreamLinker.

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![ZLMediaKit](https://img.shields.io/badge/ZLMediaKit-integrated-orange.svg)](https://github.com/ZLMediaKit/ZLMediaKit)
[![Platform](https://img.shields.io/badge/platform-Windows-0078D6.svg)](https://www.microsoft.com/windows)

## Product positioning

StreamLinker Edge is the edge-side component of the StreamLinker platform.
It is designed to run on field PCs or industrial Windows hosts and connect local video sources with the cloud side.

Typical pipeline:

```text
Camera / Drone / Local encoder
        -> RTSP / RTMP / custom access
StreamLinker Edge
        -> local pull / process / push / recover
StreamLinker Cloud
```

## Typical scenarios

| Scenario | Description |
| --- | --- |
| Security monitoring | Collect and forward camera feeds from field sites |
| Drone video backhaul | Forward live video from drone-side devices |
| Inspection and construction | Unify multiple on-site video sources to the cloud |
| Streaming gateway | Use Edge as a local intake and relay node |

## Open-source scope

Current open-source direction includes:
- multi-stream pull and relay
- local orchestration and recovery
- RTMP push target management
- local admin APIs and lightweight console
- edge-to-cloud integration through StreamLinker services

Commercial or later-stage capabilities may include:
- private drone protocol integration
- GB28181 support
- advanced multi-node scheduling
- local recording and buffered retry
- enterprise support and SLA

## Current implementation status

The repository already contains a working enterprise-style skeleton based on:
- Spring Boot 3
- MyBatis-Plus
- MySQL
- ZLMediaKit HTTP API integration

Already implemented in this repository:
- persistent stream definitions, push targets, runtime state, and process state
- pull modes: `FFMPEG` and `PROXY`
- push mode: `RTMP`
- `stream_process` driven orchestration for `PULL_UP`, `PULL_DOWN`, `PUSH_UP`, `PUSH_DOWN`
- startup recovery and reconcile tasks
- local admin APIs for stream and push-target CRUD
- built-in static admin pages

## Quick start

### Environment requirements

- JDK 17+
- MySQL 8+
- local or reachable ZLMediaKit
- Windows edge host is the primary target for deployment

### Source run

```bash
git clone https://github.com/StreamLinker/streamlinker-edge.git
cd streamlinker-edge
mvn test
mvn spring-boot:run
```

Main configuration file:
- `src/main/resources/application.yml`

Example local ZLM configuration:
- `src/main/resources/application-local-zlm.example.yml`

Database schema:
- `src/main/resources/sql/streamlinker-edge-schema.sql`

### Planned packaging modes

The following delivery modes are part of the target product shape, but are not fully packaged in the current open-source snapshot yet:
- release package for direct deployment
- containerized deployment assets
- richer operator installation guides

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

## Working with Cloud

This repository is designed to work together with the StreamLinker cloud side.

Role split:
- `streamlinker-edge`: on-site intake, orchestration, and push
- `streamlinker-cloud`: centralized receiving, management, and playback

## Dependency

This project depends on the shared SDK repository:
- [streamlinker-zlm-sdk](https://github.com/StreamLinker/streamlinker-zlm-sdk)

## Roadmap

Planned next steps:
- improve operator and deployment documentation
- add stream process diagnostics to the web console
- build `streamlinker-cloud`
- publish `streamlinker-zlm-sdk` artifacts and versioning strategy