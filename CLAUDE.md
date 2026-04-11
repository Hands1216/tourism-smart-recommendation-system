# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目根目录说明

⚠️ 仓库根目录下的实际项目位于 `tourism-smart-recommendation-system` 子目录。执行构建、测试、启动命令前，先 `cd tourism-smart-recommendation-system`。

## 项目概述

基于大模型的旅游智慧推荐系统（毕业设计），提供景点推荐、路线规划、旅游攻略、智能问答和后台内容管理。

技术栈：
- 后端：Spring Boot 3.2、MyBatis-Plus、MySQL 8.0、Redis 7.x、JWT、Druid
- 前端：Vue 3、Vite 5、TypeScript、Pinia、Element Plus、Sass
- AI / 推荐：DeepSeek（主）、多智能体编排、协同过滤 + 内容推荐混合推荐
- 外部服务：高德地图 API、和风天气 API（JWT 认证）、阿里云短信

系统角色：`user`（游客）、`content_admin`（内容管理员）、`admin`（系统管理员）

## 环境要求

Java 17、Maven 3.8+、Node.js >= 18、MySQL 8.0、Redis 7.x

## 常用命令

### 关键构建顺序

`tourism-ai` 和 `tourism-algorithm` 是**独立 Maven 项目**（非 `tourism-backend` 子模块），`tourism-backend/tourism-service` 依赖它们。首次构建或这两个模块修改后，必须先安装到本地仓库：

```bash
cd tourism-smart-recommendation-system
cd tourism-ai && mvn clean install -DskipTests && cd ../tourism-algorithm && mvn clean install -DskipTests && cd ../tourism-backend && mvn clean install -DskipTests
```

如果只改了 `tourism-common` 或 `tourism-model`，也需要在 `tourism-backend` 下重新 `mvn install`，否则上层模块仍引用本地仓库旧包。

### 后端

```bash
cd tourism-smart-recommendation-system/tourism-backend

mvn clean install                        # 全量构建
mvn clean install -DskipTests            # 跳过测试
mvn clean install -pl tourism-common     # 单模块构建
mvn test -pl tourism-service             # 单模块测试
mvn test -Dtest=UserServiceTest#testLogin # 单方法测试

cd tourism-main && mvn spring-boot:run   # 启动（:8080/api）
```

注意：仓库里当前几乎没有现成测试文件。

### 前端

```bash
cd tourism-smart-recommendation-system/tourism-frontend

npm install
npm run dev       # 开发服务器（:3000）
npm run build     # 生产构建
npm run lint      # ESLint
npx vue-tsc --noEmit  # TypeScript 类型检查
```

前端没有 `test` 脚本。Prettier 配置：无分号、单引号、100 字符宽、无尾逗号。

### 数据库与 Docker

```bash
cd tourism-smart-recommendation-system
docker-compose up -d mysql redis              # 仅基础服务
docker-compose up --build                     # 整套系统（注意：backend/frontend Dockerfile 尚未创建）
mysql -u root -p < database/init.sql          # 手动初始化（后续迁移脚本见 database/ 目录）
```

数据库名 `tourism_db`，开发环境默认账密 `root/123456`。

## 后端架构

### 模块依赖链

```
tourism-main → tourism-api → tourism-service → tourism-model + tourism-common + tourism-ai + tourism-algorithm
```

### 各模块职责

| 模块 | 职责 | 关键文件 |
|------|------|----------|
| `tourism-main` | 启动类、Security 配置、Mapper XML | `TourismApplication.java`、`MainSecurityConfig`、`resources/mapper/` |
| `tourism-api` | Controller、JWT Filter、异常处理 | `JwtAuthenticationFilter`、`GlobalExceptionHandler`、8 个 Controller |
| `tourism-service` | 业务逻辑、Mapper、多智能体、外部 API | `ChatServiceImpl`、`RecommendServiceImpl`、`agent/` 目录 |
| `tourism-model` | Entity / DTO / VO 三层模型 | DTO（请求）、VO（响应）、Entity（数据库），**严禁跨层混用** |
| `tourism-common` | `Result<T>`、JWT 工具、`UserContext` | `Result.java`、`JwtUtil`、`UserContext`、`UserRole` 枚举 |
| `tourism-ai` | LLM 封装（独立 Maven 项目） | `LLMService` 接口、`LLMServiceImpl`（WebClient，DeepSeek） |
| `tourism-algorithm` | 推荐算法（独立 Maven 项目） | `UserBasedCF`、`ContentBasedRecommender`、`HybridRecommender`（CF 0.6 + 内容 0.4） |

### 启动与扫描

`TourismApplication` 关键注解：
- `@SpringBootApplication(scanBasePackages = "com.tourism")`
- `@MapperScan("com.tourism.service.mapper")`
- `@EnableScheduling`

### 认证链路（最易误判的部分）

- `server.servlet.context-path: /api` — 所有接口 URL 以 `/api` 开头
- `MainSecurityConfig` 基本全部 `permitAll()`，Security 本身不拦截
- **真正的认证靠 `JwtAuthenticationFilter`**：解析 `Authorization: Bearer <token>`，写入 `UserContext`（ThreadLocal）
- 业务代码通过 `UserContext.getUserId()` / `UserContext.getUserRole()` 获取当前用户，Filter 请求结束后自动清理
- 接口是否允许匿名访问，要看**业务层是否显式判断当前用户为空**，不能只看路由或 Security 配置

### 重要 Service

- **`RecommendServiceImpl`**：后端最重的服务。调用 LLM 生成行程 JSON → 清洗解析不稳定输出 → 内置城市数据兜底 → 注入天气/交通/预订链接 → 支持动态调整行程。涉及路线规划优先读这里。
- **`ChatServiceImpl`**：聊天主入口。本地意图分析 → `AgentOrchestrator` 选择智能体 → 外部服务补充实时数据 → LLM 生成 → 失败降级为普通对话。
- **`RecommendAlgorithmService`**：推荐算法业务接入点。加载行为/特征/偏好 → 初始化 CF + 内容推荐 → **每小时定时刷新** → 无结果时降级为热门推荐。

### 多智能体系统

```
ChatServiceImpl → AgentOrchestrator → TravelAgent (接口) → BaseTravelAgent (基类)
                                        ├── AttractionAgent
                                        ├── HotelAgent
                                        ├── RestaurantAgent
                                        └── RoutePlanAgent
```

新增智能体：继承 `BaseTravelAgent`、实现 `canHandle()` / `process()`、加 `@Component`，自动注入到 `AgentOrchestrator`。通过 `chat.multi-agent.enabled` 开关控制。

### AI 模块特性

- 使用 `WebClient`（WebFlux），默认供应商 DeepSeek，可切换通义千问/ChatGLM
- 超时和失败返回兜底响应，不抛异常给上层
- AI 相关能力优先从 `tourism-ai` 和 `tourism-service` 扩展，不在业务层散落供应商细节

### 数据访问模式

- `@RequiredArgsConstructor` 依赖注入
- 简单 CRUD：`BaseMapper` + `LambdaQueryWrapper`
- 复杂查询：Mapper XML（`tourism-main/src/main/resources/mapper/`），`AttractionMapper` 是最重的
- 定时任务：推荐模型刷新（每小时）、敏感词刷新

### 配置文件

| 文件 | 用途 |
|------|------|
| `application.yml` | 主配置（含所有设置项） |
| `application-dev.yml` | 开发环境覆盖（DB/Redis） |
| `application-prod.yml` | 生产环境覆盖（环境变量），已在 `.gitignore` 中 |

⚠️ 文件上传路径硬编码为 `D:/tourism-uploads/`（`upload.path`），跨平台部署需修改。

## 前端架构

### 构建与代理配置

- Vite 别名：`@ → src/`
- `/api` 代理到 `http://localhost:8080`
- `/uploads` 代理到 `http://localhost:8080/api/uploads`
- 环境变量：`.env.development`、`.env.production`

### 关键目录

| 目录 | 职责 |
|------|------|
| `src/api/` | 按领域拆分的 API 封装（`auth.ts`、`attraction.ts`、`chat.ts`、`recommend.ts`、`strategy.ts` 等），新接口放对应文件 |
| `src/router/` | 路由定义 + 守卫 |
| `src/stores/` | Pinia：`auth.ts`（手动持久化到 localStorage）、`chat.ts`、`theme.ts` |
| `src/layouts/` | `DefaultLayout`（前台）、`AdminLayout`（后台） |
| `src/views/` | 按业务域组织：`auth/`、`home/`、`attraction/`、`chat/`、`plan/`、`strategy/`、`user/`、`admin/` |
| `src/assets/styles/` | 三层样式体系 |

### API 层约定

`src/api/index.ts`：axios 实例 + 拦截器 + `get`/`post`/`put`/`del` 封装。
- 自动附加 Bearer Token
- 统一解包 `Result<T>` 响应，统一处理 401/403/404/500
- `timeout: 180000ms`（为 AI 生成类请求留足时间）
- 文件上传因 `multipart/form-data` 需要，单独使用 axios 实例

### 路由守卫非直觉行为

- 未登录访问 `/chat`、`/plan`、部分攻略入口时，**停留当前页并弹提示**，不跳转登录页
- 后台路由依赖 `meta.roles` 限制，`admin` 和 `content_admin` 权限不完全相同

### 样式体系（三层）

1. `design-tokens.scss`：颜色、间距、圆角、阴影、断点
2. `_theme.scss`：CSS 变量、亮/暗主题映射、Element Plus 主题变量覆盖
3. `global.scss`：全局 reset 与工具类

修改主题或品牌色时，从这三层入手，不要只改单页样式。

### 其他前端要点

- 富文本编辑器：`@wangeditor/editor-for-vue`（v5），上传图片走 `FileUploadController`，路径需补 `/api` 前缀
- `auth` Store：初始化时从 localStorage 恢复登录态，处理头像相对路径补 `/api` 前缀

## 数据库

数据库名 `tourism_db`，通用约定：主键自增、`deleted` 逻辑删除、`create_time`/`update_time` 自动填充、驼峰转下划线自动映射。

重要业务表：`user`、`attraction`、`attraction_category`、`attraction_rating`、`favorite`、`user_footprint`、`user_behavior`、`route_plan`、`strategy`、`strategy_comment`、`attraction_strategy`、`role`、`permission`、`role_permission`、`operate_log`、`sensitive_word`。

详细结构参见 `docs/DATABASE.md`。

## 开发约定

- **DTO/VO/Entity 分层**：DTO 承载请求参数，VO 承载响应数据，Entity 映射数据库，严禁跨层混用
- **统一响应**：所有 API 返回 `Result<T>`（`{ code, message, data, timestamp }`）
- **AI 扩展**：优先从 `tourism-ai`（LLMService）和 `tourism-service` 现有封装扩展
- **推荐扩展**：优先复用 `tourism-algorithm` 与 `RecommendAlgorithmService`，不在 Controller 拼推荐逻辑
- **新增接口**：前端放 `src/api/` 对应领域文件；后端异常由 `GlobalExceptionHandler` 统一处理

## 仓库现状

- 前端有 ESLint + Prettier，无测试脚本
- 后端几乎没有现成测试文件
- 无 CI/CD 工作流
- Docker Compose 中 backend/frontend 的 Dockerfile 尚未创建

## 参考文档

- `tourism-smart-recommendation-system/docs/API.md` — 接口文档
- `tourism-smart-recommendation-system/docs/DATABASE.md` — 数据库文档
- `tourism-smart-recommendation-system/docs/use-case-diagram.puml` — 用例图
- `tourism-smart-recommendation-system/.github/copilot-instructions.md` — Copilot 指令
