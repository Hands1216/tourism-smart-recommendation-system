# 数据库设计文档

## 项目信息
- **项目名称**: 基于大模型的旅游智慧推荐系统

## 数据库概述
- **数据库名称**: tourism_db
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

## 表结构说明

### 1. 用户相关表

#### user (用户表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 用户ID（主键，自增） |
| phone | VARCHAR(20) | 手机号（唯一） |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| wechat_openid | VARCHAR(100) | 微信OpenID（唯一） |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| role | VARCHAR(20) | 角色：user/content_admin/admin（默认user） |
| status | TINYINT | 状态：0-禁用，1-正常（默认1） |
| preferences | TEXT | 用户偏好标签（JSON格式） |
| deleted | TINYINT | 逻辑删除：0-未删除，1-已删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 2. 景点相关表

#### attraction_category (景点分类表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 分类ID（主键） |
| name | VARCHAR(50) | 分类名称 |
| icon | VARCHAR(100) | 分类图标 |
| sort_order | INT | 排序序号 |
| deleted | TINYINT | 逻辑删除 |

#### attraction (景点表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 景点ID（主键） |
| name | VARCHAR(100) | 景点名称 |
| category_id | BIGINT | 分类ID |
| description | TEXT | 景点描述 |
| images | TEXT | 图片URL列表（JSON格式） |
| address | VARCHAR(255) | 详细地址 |
| province | VARCHAR(50) | 省份 |
| city | VARCHAR(50) | 城市 |
| district | VARCHAR(50) | 区县 |
| longitude | DECIMAL(10,7) | 经度 |
| latitude | DECIMAL(10,7) | 纬度 |
| open_time | VARCHAR(100) | 开放时间 |
| ticket_price | DECIMAL(10,2) | 门票价格 |
| charge_type | TINYINT | 收费类型：0-免费，1-收费 |
| rating | DECIMAL(3,2) | 评分 |
| view_count | INT | 浏览量 |
| favorite_count | INT | 收藏量 |
| review_count | INT | 评论数 |
| tags | TEXT | 标签列表（JSON格式） |
| features | TEXT | 特色标签（JSON格式，用于推荐算法） |
| scenic_level | VARCHAR(50) | 景区等级：5A/4A/3A/世界遗产等 |
| suggested_duration | VARCHAR(50) | 建议游玩时长 |
| best_months | VARCHAR(100) | 最佳游玩月份 |
| scene_type | VARCHAR(100) | 场景分类：独自/情侣/朋友/家庭出行 |
| contact_phone | VARCHAR(50) | 官方咨询电话 |
| official_website | VARCHAR(255) | 官方网站链接 |
| tips | TEXT | 避坑提示/注意事项（JSON数组） |
| audit_status | TINYINT | 审核状态：0-待审核，1-已通过，2-已驳回 |
| seasonal_status | TINYINT | 季节性状态：0-暂停开放，1-正常开放 |
| seasonal_note | VARCHAR(255) | 季节性说明 |
| status | TINYINT | 状态：0-下架，1-上架 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### attraction_rating (景点评分表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 评分ID（主键） |
| attraction_id | BIGINT | 景点ID |
| user_id | BIGINT | 用户ID |
| scenery_score | DECIMAL(2,1) | 景色评分（1.0-5.0） |
| fun_score | DECIMAL(2,1) | 趣味性评分（1.0-5.0） |
| value_score | DECIMAL(2,1) | 性价比评分（1.0-5.0） |
| overall_score | DECIMAL(2,1) | 综合评分（自动计算） |
| comment | VARCHAR(500) | 评价内容 |
| create_time | DATETIME | 评分时间 |

#### user_footprint (用户足迹表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 足迹ID（主键） |
| user_id | BIGINT | 用户ID |
| attraction_id | BIGINT | 景点ID |
| visit_date | DATE | 游览日期 |
| note | VARCHAR(255) | 备注 |
| create_time | DATETIME | 创建时间 |

#### attraction_strategy (景点攻略关联表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | ID（主键） |
| attraction_id | BIGINT | 景点ID |
| strategy_id | BIGINT | 攻略ID |
| create_time | DATETIME | 创建时间 |

### 3. 行程规划相关表

#### route_plan (路线规划表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 路线ID（主键） |
| user_id | BIGINT | 用户ID |
| title | VARCHAR(200) | 路线标题 |
| destination | VARCHAR(100) | 目的地 |
| days | INT | 天数 |
| budget | DECIMAL(10,2) | 预算 |
| companions | VARCHAR(50) | 同行人员类型 |
| preferences | JSON | 偏好设置 |
| plan_data | TEXT | 详细行程数据（JSON格式） |
| is_ai_generated | TINYINT | 是否AI生成 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 4. 攻略相关表

#### strategy (攻略表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 攻略ID（主键） |
| user_id | BIGINT | 作者ID |
| title | VARCHAR(200) | 攻略标题 |
| cover_image | VARCHAR(500) | 封面图 |
| destination | VARCHAR(100) | 目的地 |
| days | INT | 天数 |
| budget | DECIMAL(10,2) | 人均预算 |
| season | VARCHAR(20) | 适合季节 |
| content | TEXT | 攻略内容（富文本） |
| summary | VARCHAR(500) | 攻略摘要 |
| images | JSON | 图片列表 |
| tags | JSON | 标签 |
| view_count | INT | 浏览数 |
| like_count | INT | 点赞数 |
| favorite_count | INT | 收藏数 |
| comment_count | INT | 评论数 |
| is_ai_generated | TINYINT | 是否AI生成 |
| audit_status | TINYINT | 审核状态：0-待审核，1-已通过，2-已驳回 |
| audit_reason | VARCHAR(255) | 审核意见 |
| status | TINYINT | 状态：0-下架，1-上架 |
| visibility | TINYINT | 可见性：0-私密，1-公开 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### strategy_comment (攻略评论表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 评论ID（主键） |
| strategy_id | BIGINT | 攻略ID |
| user_id | BIGINT | 评论用户ID |
| parent_id | BIGINT | 父评论ID（0为一级评论） |
| content | VARCHAR(1000) | 评论内容 |
| like_count | INT | 点赞数 |
| status | TINYINT | 状态：0-隐藏，1-显示 |
| create_time | DATETIME | 创建时间 |

### 5. 用户行为表

#### favorite (收藏表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 收藏ID（主键） |
| user_id | BIGINT | 用户ID |
| item_type | VARCHAR(20) | 收藏类型：attraction/strategy |
| item_id | BIGINT | 收藏项目ID |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 收藏时间 |

#### user_behavior (用户行为记录表)
此表用于协同过滤推荐算法，记录用户的各种行为。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 记录ID（主键） |
| user_id | BIGINT | 用户ID |
| item_type | VARCHAR(20) | 项目类型：attraction/strategy |
| item_id | BIGINT | 项目ID |
| behavior_type | VARCHAR(20) | 行为类型：view/click/favorite/share/rating |
| rating | DECIMAL(3,2) | 评分（如有） |
| weight | DOUBLE | 行为权重（用于算法计算） |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 行为时间 |

### 6. 系统管理表

#### role (角色表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 角色ID（主键） |
| role_code | VARCHAR(50) | 角色编码（唯一） |
| role_name | VARCHAR(50) | 角色名称 |
| description | VARCHAR(200) | 角色描述 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### permission (权限表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 权限ID（主键） |
| parent_id | BIGINT | 父权限ID |
| permission_code | VARCHAR(50) | 权限编码（唯一） |
| permission_name | VARCHAR(50) | 权限名称 |
| permission_type | VARCHAR(20) | 权限类型：menu/button |
| path | VARCHAR(200) | 路由路径 |
| icon | VARCHAR(50) | 图标 |
| sort_order | INT | 排序序号 |
| deleted | TINYINT | 逻辑删除 |
| create_time | DATETIME | 创建时间 |

#### role_permission (角色权限关联表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | ID（主键） |
| role_id | BIGINT | 角色ID |
| permission_id | BIGINT | 权限ID |
| create_time | DATETIME | 创建时间 |

#### operate_log (操作日志表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 日志ID（主键） |
| user_id | BIGINT | 操作用户ID |
| username | VARCHAR(50) | 操作用户名 |
| user_role | VARCHAR(50) | 操作用户角色 |
| operation_type | VARCHAR(20) | 操作类型 |
| module | VARCHAR(50) | 操作模块 |
| description | VARCHAR(500) | 操作描述 |
| request_method | VARCHAR(10) | 请求方法 |
| request_params | TEXT | 请求参数 |
| ip_address | VARCHAR(50) | IP地址 |
| status | TINYINT | 执行状态：0-失败，1-成功 |
| error_msg | TEXT | 错误信息 |
| execute_time | BIGINT | 执行时长（毫秒） |
| create_time | DATETIME | 创建时间 |

#### sensitive_word (敏感词表)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 敏感词ID（主键） |
| word | VARCHAR(100) | 敏感词 |
| category | VARCHAR(50) | 分类：politics/porn/ad/abuse |
| status | TINYINT | 状态：0-禁用，1-启用 |
| create_time | DATETIME | 创建时间 |

## ER关系说明

1. **用户-景点**: 多对多关系（通过收藏表 favorite 关联）
2. **用户-景点评分**: 一对多关系（通过 attraction_rating 表）
3. **用户-足迹**: 一对多关系（通过 user_footprint 表）
4. **用户-攻略**: 一对多关系（一个用户可创建多篇攻略）
5. **用户-路线规划**: 一对多关系
6. **景点-分类**: 多对一关系
7. **景点-攻略**: 多对多关系（通过 attraction_strategy 关联）
8. **攻略-评论**: 一对多关系
9. **角色-权限**: 多对多关系（通过 role_permission 关联）

## 数据库初始化脚本执行顺序

```bash
mysql -u root -p < database/init.sql                           # 1. 基础表结构
mysql -u root -p tourism_db < database/attraction_upgrade.sql  # 2. 景点表升级
mysql -u root -p tourism_db < database/add_attractions_final.sql # 3. 景点数据
mysql -u root -p tourism_db < database/attraction_data_migration.sql # 4. 数据迁移
mysql -u root -p tourism_db < database/attraction_filter_fix.sql # 5. 筛选数据修复
mysql -u root -p tourism_db < database/init_roles.sql          # 6. 角色初始化
mysql -u root -p tourism_db < database/add_user_role_to_operate_log.sql # 7. 日志表升级
```
