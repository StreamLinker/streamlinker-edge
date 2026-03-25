# StreamLinker Edge

[English](README.md) | 简体中文

StreamLinker Edge 是 StreamLinker 平台的边缘侧组件，面向现场 PC、工业 Windows 主机和边缘节点部署。
它的目标是把本地视频源和云端服务连接起来，承担拉流、转推、恢复与管理能力。

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![ZLMediaKit](https://img.shields.io/badge/ZLMediaKit-integrated-orange.svg)](https://github.com/ZLMediaKit/ZLMediaKit)
[![Platform](https://img.shields.io/badge/platform-Windows-0078D6.svg)](https://www.microsoft.com/windows)

## 项目定位

StreamLinker Edge 用于部署在现场侧，负责把摄像头、无人机、编码器等本地视频源接入到 StreamLinker 体系中。

典型链路如下：

```text
摄像头 / 无人机 / 本地编码器
        -> RTSP / RTMP / 自定义接入
StreamLinker Edge
        -> 本地拉流 / 转推 / 恢复 / 管理
StreamLinker Cloud
```

## 适用场景

| 场景 | 说明 |
| --- | --- |
| 安防监控 | 现场摄像头统一接入并转发到云端平台 |
| 无人机图传 | 无人机视频链路的接入与转发 |
| 巡检和工地 | 多点位现场视频统一采集上云 |
| 流媒体网关 | 作为本地视频接入和转推节点 |

## 开源版范围

当前开源方向包括：
- 多路拉流和转推能力
- 本地流程编排与恢复
- RTMP 推流目标管理
- 本地管理 API 和轻量控制台
- 与 StreamLinker 云端服务的协同

商业版或后续阶段能力可能包括：
- 无人机私有协议集成
- GB28181 支持
- 更复杂的多节点调度
- 本地录像和缓冲补偿
- 企业级支持与 SLA

## 当前实现状态

这个仓库当前已经具备企业级骨架，基于以下技术栈：
- Spring Boot 3
- MyBatis-Plus
- MySQL
- ZLMediaKit HTTP API 集成

当前已经实现的能力：
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
- 完善运维和部署文档
- 在控制台中增加流程诊断信息
- 开始构建 `streamlinker-cloud`
- 明确 `streamlinker-zlm-sdk` 的发布与版本策略