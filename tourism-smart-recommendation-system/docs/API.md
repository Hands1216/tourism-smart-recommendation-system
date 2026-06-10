# API接口文档

## 项目信息
- **项目名称**: 基于大模型的旅游智慧推荐系统

## 接口说明
- **Base URL**: `/api`
- **响应格式**: JSON
- **认证方式**: JWT Bearer Token（请求头 `Authorization: Bearer <token>`）

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

## 1. 认证模块 (Auth)

### 1.1 手机号登录
- **URL**: `/auth/login/phone`
- **Method**: POST
- **请求参数**:
```json
{
  "phone": "13800138000",
  "code": "123456"
}
```
- **响应数据**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userInfo": {
    "id": 1,
    "nickname": "游客",
    "avatar": "",
    "role": "user"
  }
}
```

### 1.2 微信扫码登录
- **URL**: `/auth/login/wechat`
- **Method**: POST
- **请求参数**:
```json
{
  "code": "微信授权码"
}
```
- **响应数据**: 同手机号登录

### 1.3 获取微信授权URL
- **URL**: `/auth/wechat/authorize-url`
- **Method**: GET
- **请求参数**:
  - redirectUri: 回调地址（必填）
  - state: 状态参数（可选）
- **响应数据**: 微信授权URL字符串

### 1.4 发送短信验证码
- **URL**: `/auth/sms/send`
- **Method**: POST
- **请求参数**:
```json
{
  "phone": "13800138000"
}
```

### 1.5 用户注册
- **URL**: `/auth/register`
- **Method**: POST
- **请求参数**:
```json
{
  "phone": "13800138000",
  "code": "123456",
  "password": "123456",
  "nickname": "游客"
}
```

## 2. 景点模块 (Attraction)

### 2.1 获取景点列表
- **URL**: `/attraction/list`
- **Method**: GET
- **请求参数**:
  - keyword: 搜索关键词
  - categoryId: 分类ID
  - province: 省份
  - page: 页码（默认1）
  - size: 每页数量（默认10）

### 2.2 获取景点详情
- **URL**: `/attraction/{id}`
- **Method**: GET

### 2.3 收藏景点
- **URL**: `/attraction/{id}/favorite`
- **Method**: POST
- **需要认证**: 是

### 2.4 取消收藏
- **URL**: `/attraction/{id}/favorite`
- **Method**: DELETE
- **需要认证**: 是

### 2.5 提交景点评分
- **URL**: `/attraction/{id}/rating`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "sceneryScore": 4.5,
  "funScore": 4.0,
  "valueScore": 4.5,
  "comment": "景色很美"
}
```

### 2.6 标记足迹（去过）
- **URL**: `/attraction/{id}/footprint`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "visitDate": "2026-02-15",
  "note": "和家人一起去的"
}
```

### 2.7 取消足迹标记
- **URL**: `/attraction/{id}/footprint`
- **Method**: DELETE
- **需要认证**: 是

## 3. 智能推荐模块 (Recommend)

### 3.1 获取个性化推荐
- **URL**: `/recommend/personalized`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "city": "北京",
  "days": 3,
  "budget": 3000,
  "interests": ["历史文化", "美食"],
  "companions": "情侣"
}
```

### 3.2 生成路线规划
- **URL**: `/recommend/route-plan`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "destination": "北京",
  "days": 3,
  "preferences": "喜欢历史文化，想要深度游"
}
```

### 3.3 保存路线规划
- **URL**: `/recommend/route-plan/save`
- **Method**: POST
- **需要认证**: 是

### 3.4 获取行程记录列表
- **URL**: `/recommend/route-plans`
- **Method**: GET
- **需要认证**: 是

### 3.5 删除行程记录
- **URL**: `/recommend/route-plan/{id}`
- **Method**: DELETE
- **需要认证**: 是

## 4. AI对话模块 (Chat)

### 4.1 发送聊天消息
- **URL**: `/chat/send`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "sessionId": "会话ID",
  "message": "北京有哪些好玩的地方？",
  "history": []
}
```

### 4.2 分析用户意图
- **URL**: `/chat/analyze`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "message": "我想去北京玩三天"
}
```

## 5. 旅游攻略模块 (Strategy)

### 5.1 获取攻略列表
- **URL**: `/strategy/list`
- **Method**: GET
- **请求参数**:
  - destination: 目的地
  - page: 页码
  - size: 每页数量

### 5.2 获取攻略详情
- **URL**: `/strategy/{id}`
- **Method**: GET

### 5.3 创建攻略
- **URL**: `/strategy/create`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "title": "北京三日游攻略",
  "destination": "北京",
  "content": "攻略内容...",
  "coverImage": "封面图URL",
  "tags": ["历史文化", "美食"]
}
```

### 5.4 更新攻略
- **URL**: `/strategy/{id}`
- **Method**: PUT
- **需要认证**: 是

### 5.5 删除攻略
- **URL**: `/strategy/{id}`
- **Method**: DELETE
- **需要认证**: 是

### 5.6 AI生成攻略
- **URL**: `/strategy/ai-generate`
- **Method**: POST
- **需要认证**: 是
- **请求参数**:
```json
{
  "destination": "北京",
  "days": 3,
  "budget": 3000,
  "interests": "历史文化"
}
```

### 5.7 获取我的攻略
- **URL**: `/strategy/my`
- **Method**: GET
- **需要认证**: 是

### 5.8 获取草稿箱
- **URL**: `/strategy/drafts`
- **Method**: GET
- **需要认证**: 是

## 6. 用户中心模块 (User)

### 6.1 获取个人信息
- **URL**: `/user/profile`
- **Method**: GET
- **需要认证**: 是

### 6.2 更新个人信息
- **URL**: `/user/profile`
- **Method**: PUT
- **需要认证**: 是

### 6.3 获取我的收藏
- **URL**: `/user/favorites`
- **Method**: GET
- **需要认证**: 是

### 6.4 获取行程记录
- **URL**: `/user/history`
- **Method**: GET
- **需要认证**: 是

## 7. 文件上传模块 (Upload)

### 7.1 上传图片
- **URL**: `/upload/image`
- **Method**: POST
- **Content-Type**: multipart/form-data
- **需要认证**: 是
- **请求参数**: file（图片文件）

## 8. 管理后台模块 (Admin)

> 需要 `admin` 或 `content_admin` 角色

### 8.1 仪表盘统计
- **URL**: `/admin/dashboard/stats`
- **Method**: GET
- **响应数据**:
```json
{
  "userCount": 100,
  "attractionCount": 200,
  "strategyCount": 50,
  "pendingAuditCount": 5
}
```

### 8.2 热门景点列表
- **URL**: `/admin/dashboard/hot-attractions`
- **Method**: GET
- **请求参数**: limit（默认15）

### 8.3 热门攻略列表
- **URL**: `/admin/dashboard/hot-strategies`
- **Method**: GET
- **请求参数**: limit（默认15）

### 8.4 景点管理
- **URL**: `/admin/attractions` - GET 获取列表
- **URL**: `/admin/attraction` - POST 创建景点
- **URL**: `/admin/attraction/{id}` - PUT 更新景点
- **URL**: `/admin/attraction/{id}` - DELETE 删除景点
- **URL**: `/admin/attraction/{id}/seasonal-status` - PUT 设置季节性状态

### 8.5 景点数据分析
- **URL**: `/admin/attraction/analytics`
- **Method**: GET

### 8.6 用户管理（仅admin）
- **URL**: `/admin/users` - GET 获取用户列表
- **URL**: `/admin/user/{id}/status` - PUT 修改用户状态（封禁/解封）
- **URL**: `/admin/user/{id}/role` - PUT 修改用户角色

### 8.7 内容审核
- **URL**: `/admin/content/pending` - GET 获取待审核内容
- **URL**: `/admin/content/audit` - POST 审核内容
- **请求参数**:
```json
{
  "type": "strategy",
  "id": 1,
  "status": 1,
  "reason": "审核通过"
}
```

### 8.8 操作日志查询（仅admin）
- **URL**: `/admin/logs`
- **Method**: GET
- **请求参数**:
  - username: 操作人
  - operationType: 操作类型
  - startTime: 开始时间
  - endTime: 结束时间
  - page: 页码
  - size: 每页数量
