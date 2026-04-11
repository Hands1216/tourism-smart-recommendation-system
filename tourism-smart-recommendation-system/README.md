# 基于大模型的旅游智慧推荐系统

## 项目信息

- **项目名称**: 基于大模型的旅游智慧推荐系统

## 项目简介

本系统是一个融合大语言模型与传统推荐算法的智慧旅游服务平台，旨在为用户提供个性化的旅游规划、景点推荐、攻略生成等服务。

### 核心功能

1. **智能推荐**: 结合协同过滤算法与大模型理解，提供个性化推荐
2. **路线规划**: AI自动生成详细的多日行程规划
3. **攻略生成**: 基于用户需求自动生成旅游攻略
4. **智能问答**: 24小时AI助手，解答旅游相关问题
5. **景点管理**: 完整的景点信息CRUD与审核流程
6. **内容社区**: 用户游记分享与互动

## 技术栈

### 前端
- Vue 3 + Vite
- TypeScript
- Pinia（状态管理）
- Vue Router（路由）
- Element Plus（UI组件）
- Axios（HTTP请求）

### 后端
- Spring Boot 3.2
- Spring Security（安全认证）
- MyBatis-Plus（持久层）
- Redis（缓存）
- MySQL 8.0（数据库）
- JWT（令牌认证）

### AI服务
- 大语言模型API集成（通义千问/ChatGLM）
- 意图识别与实体提取
- 自然语言生成

### 算法模块
- 协同过滤算法（User-based CF）
- 基于内容的推荐
- 混合推荐策略

## 项目结构

```
tourism-smart-recommendation-system/
├── tourism-frontend/          # 前端项目 (Vue 3)
├── tourism-backend/           # 后端项目 (Spring Boot)
│   ├── tourism-common/        # 公共模块
│   ├── tourism-model/         # 实体模块
│   ├── tourism-service/       # 服务层
│   ├── tourism-api/           # API接口
│   └── tourism-main/          # 启动模块
├── tourism-ai/                # AI服务模块
├── tourism-algorithm/         # 算法模块
├── database/                  # 数据库脚本
└── docs/                      # 项目文档
```

## 快速开始

### 环境要求

- Node.js >= 18
- Java 17
- Maven 3.8+
- MySQL 8.0
- Redis 7.x

### 数据库初始化

```bash
# 创建数据库并导入脚本
mysql -u root -p < database/init.sql
```

如果是已有数据库，在更新攻略审核/重新发布相关功能后，还需要执行：

```bash
mysql -u root -p tourism_db < database/add_strategy_root_id.sql
```

### 后端启动

```bash
# 进入后端目录
cd tourism-backend

# 修改配置文件
# 编辑 tourism-main/src/main/resources/application-dev.yml
# 配置数据库连接信息

# 编译打包
mvn clean install

# 启动服务
cd tourism-main
mvn spring-boot:run
```

### 前端启动

```bash
# 进入前端目录
cd tourism-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```


### 短信服务配置

配置短信服务提供商的API密钥。

## 系统角色

1. **普通游客 (user)**: 浏览景点、获取推荐、创建攻略
2. **内容管理员 (content_admin)**: 审核UGC内容、管理景点信息
3. **系统管理员 (admin)**: 用户管理、角色权限、系统配置

## 许可证

本项目仅供学习交流使用。

---

**毕设项目 - 金陵科技学院软件工程学院**
