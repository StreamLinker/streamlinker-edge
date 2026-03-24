<div align="center">


# ⚡ StreamLinker Edge

**企业级流媒体管理平台 · 客户端**

运行于 Windows 工控机，实现摄像头 / 无人机视频流的拉取、转发与推送

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![ZLMediaKit](https://img.shields.io/badge/ZLMediaKit-latest-orange.svg)](https://github.com/ZLMediaKit/ZLMediaKit)
[![Platform](https://img.shields.io/badge/platform-Windows-0078D6.svg)](https://www.microsoft.com/windows)
[![Docker](https://img.shields.io/badge/Docker-supported-2496ED.svg)](https://www.docker.com/)

[English](README_EN.md) · 简体中文 · [在线 Demo](#) · [定制开发](#联系我们)

</div>

---

## ✨ 项目简介

StreamLinker Edge 是 [StreamLinker](https://github.com/StreamLinker) 的**客户端组件**，部署在 Windows 工控机或现场 PC 上，负责从摄像头、无人机等设备拉取视频流，并实时转发推送至 [StreamLinker Cloud](https://github.com/StreamLinker/streamlinker-cloud) 服务端。

```
摄像头 / 无人机 / 本地设备
         ↓  RTSP / RTMP / GB28181
  StreamLinker Edge（Windows 工控机）
    ┌─────────────────────────┐
    │  拉流 → 转码 → 推流     │
    │  本地预览 & 状态上报    │
    └─────────────────────────┘
         ↓  RTMP / SRT
  StreamLinker Cloud（服务器）
```

---

## 🎯 适用场景

| 场景         | 说明                               |
| ------------ | ---------------------------------- |
| 🔒 安防监控   | 现场摄像头接入，集中推送至管理平台 |
| 🚁 无人机图传 | 无人机视频实时回传与转发           |
| 🏗️ 工地巡检   | 多点位摄像头统一采集上云           |
| 📡 直播推流   | 本地视频源采集推流                 |

---

## 🖥️ 功能特性

### 开源版

- ✅ 多路流拉取（RTSP / RTMP / HLS）
- ✅ 流转发 & 推流至 Cloud 端
- ✅ 本地流状态监控
- ✅ 自动断线重连
- ✅ 轻量 Web 管理界面（本地）
- ✅ 流信息上报至 Cloud

### 商业版 *(即将推出)*

- 🚁 无人机私有协议对接
- 📷 GB28181 摄像头接入
- 🔄 多 Cloud 节点负载转发
- 📊 本地录像缓存（断网续传）
- 🛡️ 商业技术支持 SLA

---

## 🚀 快速开始

### 环境要求

- Windows 10 / Windows Server 2016+
- JDK 17+
- ZLMediaKit（已内置）

### 方式一：安装包运行（推荐）

```bash
# 下载最新 Release
# 解压后运行
streamlinker-edge.exe
# 访问本地管理界面
http://localhost:18080
```

### 方式二：源码启动

```bash
git clone https://github.com/StreamLinker/streamlinker-edge.git
cd streamlinker-edge

# 修改配置
cp src/main/resources/application.yml.example src/main/resources/application.yml
# 编辑 application.yml，填写 Cloud 服务器地址

mvn spring-boot:run
```

### 方式三：Docker

```bash
docker-compose up -d
```

---

## ⚙️ 配置说明

```yaml
streamlinker:
  cloud:
    server: http://your-cloud-server:8080   # Cloud 服务端地址
    token: your-access-token                # 认证 Token

  zlmedia:
    host: 127.0.0.1
    port: 9092

  streams:
    - name: 摄像头-01
      pull-url: rtsp://192.168.1.100:554/stream
      push-url: rtmp://cloud-server/live/cam01
      auto-reconnect: true
```

---

## 📁 项目结构

```
streamlinker-edge/
├── src/main/java/
│   └── com/streamlinker/edge/
│       ├── controller/       # 本地管理 API
│       ├── service/          # 拉流/推流业务
│       ├── zlmedia/          # ZLMediaKit 集成
│       └── config/           # 配置类
├── zlmediakit/               # ZLMediaKit 内置
├── docker-compose.yml
└── README.md
```

---

## 📡 与 Cloud 端配合

本项目需要配合 [StreamLinker Cloud](https://github.com/StreamLinker/streamlinker-cloud) 使用：

- **Edge 端**（本项目）：现场设备侧，拉流 + 推流
- **Cloud 端**：服务器侧，统一接收 + 管理 + 播放

---

## 🤝 定制开发 & 商业合作

如果您有以下需求，欢迎联系：

- 🔧 私有化部署定制
- 🔌 硬件协议对接（无人机 / 摄像头 / 工业设备）
- 🏗️ 行业解决方案定制（安防 / 工地 / 巡检）

📧 邮箱：`your@email.com`  
💬 微信：`your_wechat`

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源，商业使用请遵守协议。

---

<div align="center">


如果这个项目对你有帮助，请点一个 ⭐ Star，这是对我最大的支持！

</div>
