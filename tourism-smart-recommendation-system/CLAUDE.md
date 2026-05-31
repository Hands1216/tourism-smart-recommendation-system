# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

旅游智慧推荐系统 —— 基于 DeepSeek 大语言模型与混合推荐算法（User-based CF + 内容推荐）的一站式旅游服务平台。前后端分离架构，三角色权限体系（游客 / 内容管理员 / 系统管理员）。

## 构建与运行

### 环境要求

JDK 17+、Node.js 18+、MySQL 8.0、Redis 7+、Maven 3.8+

### 后端构建（必须按顺序）

```bash
# 1. 先安装两个独立 Maven 模块到本地仓库
cd tourism-ai && mvn clean install -DskipTests
cd ../tourism-algorithm && mvn clean install -DskipTests

# 2. 构建后端多模块项目
cd ../tourism-backend && mvn clean install -DskipTests

# 3. 启动 Spring Boot
cd tourism-main && mvn spring-boot:run
```

后端运行在 http://localhost:8080/api

### 前端构建

```bash
cd tourism-frontend
npm install
npm run dev      # 开发服务器 http://localhost:3000
npm run build    # 生产构建
npm run lint     # ESLint 检查
```

### 数据库初始化

创建数据库 `tourism_db`，导入 `database/database.sql`。

### Docker 一键运行

```bash
docker-compose up -d
```

## 架构概览

### 三个独立 Maven 项目

`tourism-ai` 和 `tourism-algorithm` 是**独立的 Maven 项目**（不是 tourism-backend 的子模块），必须先 `mvn install` 到本地仓库后，tourism-backend 才能引用它们。

- **tourism-ai** — LLM 抽象层，封装 DeepSeek API 调用（OpenAI 兼容格式），通过 WebClient 异步请求。提供 `LLMService` 接口：chat、意图分析、攻略生成、路线规划。
- **tourism-algorithm** — 推荐算法模块。`UserBasedCF`（协同过滤）+ `ContentBasedRecommender`（内容推荐）→ `HybridRecommender`（混合推荐，CF 权重 0.6 / 内容权重 0.4）。冷启动时自动降级为热门推荐。
- **tourism-backend** — Spring Boot 3.2 多模块后端（见下文）。

### tourism-backend 模块依赖链

```
tourism-common  →  tourism-model  →  tourism-service  →  tourism-api  →  tourism-main
```

| 模块 | 职责 |
|------|------|
| tourism-common | 统一响应 `Result<T>`、常量、枚举、工具类 |
| tourism-model | Entity / DTO / VO（实体类包：`com.tourism.model.entity`） |
| tourism-service | 业务逻辑、Agent 编排、外部 API 集成、MyBatis Mapper 接口 |
| tourism-api | Controller 层（REST 接口） |
| tourism-main | Spring Boot 启动入口 `TourismApplication`，Security 配置 |

### 多智能体（Multi-Agent）架构

`AgentOrchestrator` 根据用户意图的置信度评分，自动分发到专业 Agent：

- `AttractionAgent` — 景点推荐
- `RestaurantAgent` — 餐饮推荐
- `HotelAgent` — 酒店推荐
- `RoutePlanAgent` — 路线规划

支持三种编排模式：自动选择（置信度最高）、并行处理（多 Agent 合并）、链式处理（前一个输出作为后一个输入）。可通过 `chat.multi-agent.enabled` 配置开关。

### 外部 API 集成

| 服务 | 类 | 用途 |
|------|------|------|
| 高德地图 | `MapApiService` | 路线规划、POI 搜索、地理编码 |
| 和风天气 | `WeatherApiService` | 实时天气、天气预报（JWT 认证，Ed25519 签名） |
| 携程 | `BookingUrlService` | 生成景点/酒店/火车票/餐厅预订链接 |
| 阿里云短信 | `sms/` | 手机验证码发送 |

### 前端架构

Vue 3 + TypeScript + Vite 5 + Element Plus + Pinia

- `src/api/index.ts` — Axios 封装，baseURL 为 `/api`，自动附加 JWT Token
- `src/api/*.ts` — 按业务模块拆分的 API 请求（auth、attraction、chat、recommend、strategy、admin、user、upload）
- `src/stores/` — Pinia 状态管理（auth、chat、theme）
- `src/layouts/` — `DefaultLayout`（用户端）、`AdminLayout`（管理后台）
- `src/views/` — 按功能分目录：home、attraction、chat、plan、strategy、user、admin、auth
- `src/router/index.ts` — 路由守卫处理认证和角色权限

Vite 开发代理：`/api` → `http://localhost:8080`，`/uploads` → `http://localhost:8080/api/uploads`

## 测试

项目当前没有单元测试。构建时使用 `-DskipTests` 跳过测试阶段。

## 默认开发账号

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 系统管理员 | 13800000001 | admin123 |
| 内容管理员 | 13800000002 | admin123 |
| 普通用户 | 13800000003 | user123 |

## 关键技术细节

- **认证**：JWT（JJWT 0.12），Token 有效期 7 天，请求头 `Authorization: Bearer <token>`
- **ORM**：MyBatis-Plus 3.5，Mapper XML 在 `tourism-main/src/main/resources/mapper/*.xml`，逻辑删除字段 `deleted`
- **数据库连接池**：Druid
- **序列化**：后端 Jackson（GMT+8），部分场景使用 FastJSON2
- **文件上传**：本地存储 `D:/tourism-uploads/`，通过 WebConfig 映射 `/uploads/` 前缀
- **日志**：`com.tourism` 包 debug 级别，SQL 输出到控制台
- **主键策略**：数据库自增
- **扫描路径**：`@SpringBootApplication(scanBasePackages = "com.tourism")`，`@MapperScan("com.tourism.service.mapper")`

## 配置文件

主配置：`tourism-backend/tourism-main/src/main/resources/application.yml`

关键配置项：
- `llm.api-key` / `llm.model` / `llm.base-url` — DeepSeek API
- `external.amap.key` — 高德地图 API Key
- `external.weather.*` — 和风天气（JWT 认证）
- `aliyun.sms.*` — 阿里云短信
- `upload.path` — 文件上传路径
- `spring.profiles.active` — 环境切换（dev/test/prod），默认 dev

## 注意事项

- 所有表使用逻辑删除（`deleted` 字段，0=正常，1=已删除），查询时 MyBatis-Plus 自动过滤
- Entity 类使用 `@TableLogic` 注解标记逻辑删除字段
- 前端 Axios 拦截器自动处理 401 跳转登录页，无需手动处理 Token 过期
- 数据库密码、API Key 等敏感信息在 `application.yml` 中明文配置，仅限开发环境使用
