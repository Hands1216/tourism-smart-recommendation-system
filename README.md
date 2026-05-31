# 旅游智慧推荐系统

基于大语言模型与混合推荐算法的一站式旅游服务平台。融合 AI 多智能体编排、协同过滤推荐、智能行程规划等能力，为用户提供个性化的旅游体验。

## 目录

- [系统简介](#系统简介)
- [快速开始](#快速开始)
  - [方式一：Docker 一键运行（推荐）](#方式一docker-一键运行推荐)
  - [方式二：本地手动运行（开发者）](#方式二本地手动运行开发者)
- [默认账号](#默认账号)
- [系统操作说明书](#系统操作说明书)
  - [一、账号与登录](#一账号与登录)
  - [二、管理员功能详解](#二管理员功能详解)
  - [三、普通用户功能详解](#三普通用户功能详解)
  - [四、常见问题](#四常见问题)
- [项目结构](#项目结构)
- [技术栈](#技术栈)

## 系统简介

- **AI 智能对话**：多智能体架构（景点/酒店/餐饮/路线规划 Agent），按用户意图自动分发
- **混合推荐引擎**：User-based CF 协同过滤 + 内容推荐，冷启动自动降级为热门推荐
- **行程规划**：LLM 生成结构化多日行程，注入实时天气、交通、预订链接
- **攻略社区**：用户发布/浏览/点赞/收藏/评论旅游攻略
- **后台管理**：景点管理、攻略审核、用户管理、数据分析仪表盘
- **三角色权限**：游客 / 内容管理员 / 系统管理员

## 快速开始

### 方式一：Docker 一键运行（推荐）

> 需要预先安装 [Docker](https://www.docker.com/) 和 Docker Compose

```bash
git clone https://github.com/your-username/tourism-smart-recommendation-system.git
cd tourism-smart-recommendation-system
docker-compose up -d
```

服务启动后：
- 前端：http://localhost
- 后端 API：http://localhost:8080/api
- MySQL：localhost:3306（数据库自动初始化）
- Redis：localhost:6379

### 方式二：本地手动运行（开发者）

**环境要求**：JDK 17+、Node.js 18+、MySQL 8.0、Redis 7+、Maven 3.8+

**1. 数据库**

创建数据库 `tourism_db`，导入 `database/init.sql`。

**2. 后端配置**

在 `tourism-backend/tourism-main/src/main/resources/` 下创建 `application.yml`，配置数据库、Redis、AI API Key 等信息（参考 `application-example.yml`）。

**3. 构建并启动后端**

```bash
# 先安装独立模块（必须按顺序）
cd tourism-ai && mvn clean install -DskipTests
cd ../tourism-algorithm && mvn clean install -DskipTests
cd ../tourism-backend && mvn clean install -DskipTests

# 启动
cd tourism-main
mvn spring-boot:run
```

后端运行在 http://localhost:8080/api

**4. 启动前端**

```bash
cd tourism-frontend
npm install
npm run dev
```

前端运行在 http://localhost:3000

## 默认账号

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 系统管理员 | 13800000001 | admin123 |
| 内容管理员 | 13800000002 | admin123 |
| 普通用户 | 13800000003 | user123 |

## 系统操作说明书

### 一、账号与登录

1. 打开系统首页，点击右上角「登录」按钮
2. 输入手机号和密码完成登录；新用户点击「注册」填写信息后注册
3. 登录后系统根据角色自动跳转：管理员进入后台管理页，普通用户进入首页
4. 支持「忘记密码」功能，通过手机号重置密码

### 二、管理员功能详解

**仪表盘**
- 登录后进入管理仪表盘，展示景点总数、用户数、攻略待审核数、热门景点排行等关键指标

**景点管理**
- 查看所有景点列表，支持搜索、分类筛选
- 添加景点：填写名称、描述、图片、地理位置、开放时间、门票价格等
- 编辑/删除已有景点信息

**攻略审核**
- 查看用户提交的攻略列表
- 审核通过或拒绝攻略发布
- 下架违规或低质量攻略

**用户管理**（仅系统管理员）
- 查看所有用户列表
- 禁用/启用用户账户
- 变更用户角色（游客/内容管理员/系统管理员）
- 删除用户账户

**操作日志**（仅系统管理员）
- 查看所有管理员和用户的操作记录，包含操作时间、操作人、操作内容等

### 三、普通用户功能详解

**景点浏览**
- 首页展示个性化推荐景点
- 景点列表支持分类、排序、关键词搜索
- 查看景点详情（介绍、图片、评分、开放时间、门票等）
- 对景点评分、评论、收藏、标记「去过」

**旅游攻略**
- 浏览其他用户发布的攻略，支持搜索和筛选
- 发布自己的图文攻略
- 对攻略点赞、收藏、评论、分享
- 管理自己的攻略（编辑/删除/查看草稿）

**AI 智能对话**
- 与 AI 助手多轮对话，咨询景点、美食、酒店、路线等问题
- AI 自动识别意图，分发至对应专业 Agent 回答

**行程规划**
- 输入目的地、天数、预算等条件
- AI 自动生成结构化多日行程，包含景点、餐饮、交通建议
- 支持跳转第三方平台预订门票/酒店

**个人中心**
- 查看/编辑个人信息和旅游偏好
- 查看收藏景点、旅游足迹
- 查看历史行程记录

### 四、常见问题

**Q: 后端启动报错找不到 tourism-ai 或 tourism-algorithm 依赖？**
A: 这两个是独立 Maven 项目，需要先分别执行 `mvn clean install -DskipTests` 安装到本地仓库。

**Q: 前端请求后端接口 404？**
A: 确认后端已启动且运行在 8080 端口，前端代理配置指向 `http://localhost:8080/api`。

**Q: AI 对话功能无响应？**
A: 检查 `application.yml` 中 AI 相关的 API Key 是否正确配置。

**Q: Docker 启动后数据库连接失败？**
A: MySQL 容器初始化需要约 30 秒，等待后重试。可通过 `docker-compose logs mysql` 查看状态。

## 项目结构

```
tourism-smart-recommendation-system/
├── tourism-frontend/          # Vue 3 + TypeScript 前端
│   └── src/
│       ├── api/               # 接口请求封装
│       ├── views/             # 页面组件
│       ├── stores/            # Pinia 状态管理
│       ├── router/            # 路由配置
│       └── components/        # 公共组件
├── tourism-backend/           # Spring Boot 多模块后端
│   ├── tourism-common/        # 公共工具、统一响应 Result<T>
│   ├── tourism-model/         # Entity / DTO / VO
│   ├── tourism-service/       # 业务逻辑、Agent 编排
│   ├── tourism-api/           # Controller 层
│   └── tourism-main/          # 启动入口
├── tourism-ai/                # AI 服务（LLM 抽象层）
├── tourism-algorithm/         # 推荐算法（协同过滤 + 内容推荐）
├── database/                  # SQL 初始化脚本
├── docs/                      # API 文档、数据库文档
└── docker-compose.yml         # Docker 编排配置
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 + TypeScript + Vite 5 |
| UI 组件库 | Element Plus |
| 状态管理 | Pinia |
| 富文本编辑 | WangEditor |
| 后端框架 | Spring Boot 3.2 + Spring Security |
| ORM | MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0 + Redis 7 |
| 连接池 | Druid |
| 认证 | JWT (JJWT 0.12) |
| AI 集成 | DeepSeek API（多智能体编排） |
| 推荐算法 | User-based CF + 内容推荐 + 混合推荐 |
| 外部 API | 高德地图、和风天气 |
| 容器化 | Docker + Docker Compose |
