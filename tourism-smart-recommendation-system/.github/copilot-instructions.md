# Copilot Instructions for Tourism Smart Recommendation System

## 项目架构概览
- **整体结构**：本项目为多模块单体仓库，包含前端（Vue 3 + Vite）、后端（Spring Boot 多模块）、AI服务、推荐算法、数据库脚本与文档。
- **主要目录**：
  - `tourism-frontend/` 前端 SPA，TypeScript + Pinia + Vue Router + Element Plus。
  - `tourism-backend/` 后端主目录，含：
    - `tourism-common/` 公共常量、工具、统一响应封装
    - `tourism-model/` 实体、DTO、VO
    - `tourism-service/` 业务逻辑与数据访问
    - `tourism-api/` 控制器与接口
    - `tourism-main/` 启动入口（TourismApplication.java）
  - `tourism-ai/` AI大模型服务（如通义千问/ChatGLM），核心接口为 `LLMService`
  - `tourism-algorithm/` 推荐算法实现（协同过滤、内容推荐、混合推荐）
  - `database/` MySQL初始化脚本
  - `docs/` API、数据库等文档

## 关键开发流程
- **数据库初始化**：
  - 推荐用 `mysql -u root -p < database/init.sql` 初始化数据库
  - 也可用 `docker-compose up -d` 启动 MySQL/Redis
- **后端启动**：
  - 需先配置 `tourism-main/src/main/resources/application-dev.yml` 数据库连接
  - 启动命令：`mvn spring-boot:run`（根目录或 `tourism-backend/`）
- **前端启动**：
  - 进入 `tourism-frontend/`，依次执行 `npm install`、`npm run dev`
- **一键启动**：
  - `docker-compose up -d` 启动全部服务（需配置好环境变量）

## 项目约定与模式
- **接口风格**：所有API统一前缀 `/api`，响应格式为 `{ code, message, data, timestamp }`，认证采用 JWT Bearer Token
- **DTO/VO分层**：后端严格区分 DTO（请求）、VO（响应）、Entity（数据库）
- **统一响应**：后端返回均封装为 `Result<T>`，详见 `tourism-common/result/`
- **推荐算法**：算法实现位于 `tourism-algorithm/`，如 `UserBasedCF`、`ContentBasedRecommender`、`HybridRecommender`
- **AI服务**：AI相关接口集中在 `tourism-ai/LLMService`，支持多大模型供应商
- **配置管理**：环境配置集中于 `application-*.yml`，敏感信息不应提交

## 重要依赖与集成
- **前端依赖**：Vue 3, Vite, Pinia, Element Plus, Axios
- **后端依赖**：Spring Boot 3.2+, MyBatis-Plus, Redis, JWT, Maven 3.8+, Java 17
- **AI/算法依赖**：大模型API（如通义千问/ChatGLM）、自定义推荐算法
- **数据库**：MySQL 8.0，表结构详见 `docs/DATABASE.md`

## 参考文件
- 项目结构说明：`PROJECT_STRUCTURE.md`
- API接口文档：`docs/API.md`
- 数据库文档：`docs/DATABASE.md`
- 主要后端入口：`tourism-backend/tourism-main/src/main/java/com/tourism/TourismApplication.java`
- 典型算法实现：`tourism-algorithm/src/main/java/`
- AI服务接口：`tourism-ai/src/main/java/`

## 其他注意事项
- 遵循现有分层与目录结构，勿混用DTO/VO/Entity
- 统一异常与响应处理，避免裸露异常栈
- 代码示例与接口细节请参考现有实现与文档

---
如遇不明确的约定或流程，请优先查阅 `README.md`、`PROJECT_STRUCTURE.md`、`docs/` 下文档，或参考现有模块实现。