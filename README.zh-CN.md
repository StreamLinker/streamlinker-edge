# StreamLinker Edge

[English](README.md) | 简体中文

`streamlinker-edge` 是 StreamLinker 的边缘侧应用，运行在边缘节点上，负责：
- 管理拉流定义
- 启动和停止本地拉流链路
- 管理 RTMP 推流目标
- 在应用重启或本地 ZLMediaKit 重启后恢复运行状态
- 提供本地管理 API 和内置轻量 Web 控制台

## 当前状态

当前仓库已经具备一个企业级骨架，技术栈包括：
- Spring Boot 3
- MyBatis-Plus
- MySQL
- ZLMediaKit HTTP API 集成

当前能力包括：
- 持久化保存流定义、推流目标、运行态和流程态
- 拉流模式：`FFMPEG` 和 `PROXY`
- 推流模式：`RTMP`
- 基于 `stream_process` 的 `PULL_UP`、`PULL_DOWN`、`PUSH_UP`、`PUSH_DOWN` 编排
- 启动恢复和巡检补偿任务
- 流和推流目标的本地管理 API
- 内置静态管理页面

## 仓库结构

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

## 内置页面

应用启动后，可直接访问：
- `/` : 总览面板
- `/pulls.html` : 拉流管理页
- `/pushes.html` : 推流管理页

## 主要接口

管理接口：
- `GET/POST/PUT/DELETE /api/admin/streams`
- `GET/POST/PUT/DELETE /api/admin/push-targets`

本地运行控制接口：
- `GET /api/local/streams`
- `POST /api/local/streams/{streamId}/start`
- `POST /api/local/streams/{streamId}/stop`
- `GET /api/local/push-targets`
- `POST /api/local/push-targets/{targetCode}/start`
- `POST /api/local/push-targets/{targetCode}/stop`

## 配置

主配置文件：
- `src/main/resources/application.yml`

本地 ZLM 示例配置：
- `src/main/resources/application-local-zlm.example.yml`

数据库初始化脚本：
- `src/main/resources/sql/streamlinker-edge-schema.sql`

## 依赖仓库

当前项目依赖共享 SDK：
- [streamlinker-zlm-sdk](https://github.com/StreamLinker/streamlinker-zlm-sdk)

## 本地开发

前置要求：
- JDK 17+
- MySQL 8+
- 本地或可访问的 ZLMediaKit

运行测试：

```bash
mvn test
```

启动应用：

```bash
mvn spring-boot:run
```

## 后续计划

下一步建议包括：
- 持续完善 README 和运维文档
- 在控制台中增加流程诊断信息
- 开始构建 `streamlinker-cloud`
- 明确 `streamlinker-zlm-sdk` 的发布与版本策略