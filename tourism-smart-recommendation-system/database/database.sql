-- ============================================================

-- 旅游智慧推荐系统 - 完整数据库初始化脚本

-- 执行本文件即可完成：建库、建表、基础初始化数据、种子数据导入

-- ============================================================



-- ============================================================
-- 旅游智慧推荐系统 - 数据库初始化脚本
-- 数据库：tourism_db
-- 字符集：utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `tourism_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `tourism_db`;

-- ============================================================
-- 1. 用户表 (user)
-- 存储系统所有注册用户的账号信息、角色及个人偏好
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `phone`           VARCHAR(20)            DEFAULT NULL    COMMENT '手机号',
    `password`        VARCHAR(255)           DEFAULT NULL    COMMENT '密码（加密）',
    `wechat_openid`   VARCHAR(100)           DEFAULT NULL    COMMENT '微信OpenID',
    `nickname`        VARCHAR(50)            DEFAULT NULL    COMMENT '昵称',
    `avatar`          VARCHAR(255)           DEFAULT NULL    COMMENT '头像URL',
    `role`            VARCHAR(20)   NOT NULL DEFAULT 'user'  COMMENT '角色：user/content_admin/admin',
    `status`          TINYINT       NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-正常',
    `preferences`     JSON                   DEFAULT NULL    COMMENT '用户偏好标签',
    `deleted`         TINYINT       NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time`     DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_wechat_openid` (`wechat_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. 景点分类表 (attraction_category)
-- 存储景点的分类体系，如自然风光、历史文化、主题乐园等
-- ============================================================
DROP TABLE IF EXISTS `attraction_category`;
CREATE TABLE `attraction_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    `name`        VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
    `icon`        VARCHAR(255)          DEFAULT NULL    COMMENT '图标URL',
    `sort_order`  INT                   DEFAULT 0       COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='景点分类表';

-- ============================================================
-- 3. 景点表 (attraction)
-- 存储景点的基本信息、地理位置、门票、评分及审核状态等核心数据
-- ============================================================
DROP TABLE IF EXISTS `attraction`;
CREATE TABLE `attraction` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '景点ID',
    `name`                VARCHAR(100)  NOT NULL                 COMMENT '景点名称',
    `category_id`         BIGINT                 DEFAULT NULL    COMMENT '分类ID',
    `description`         TEXT                   DEFAULT NULL    COMMENT '景点描述',
    `images`              JSON                   DEFAULT NULL    COMMENT '图片URL列表',
    `videos`              JSON                   DEFAULT NULL    COMMENT '视频URL列表',
    `address`             VARCHAR(255)           DEFAULT NULL    COMMENT '详细地址',
    `city`                VARCHAR(50)            DEFAULT NULL    COMMENT '城市',
    `province`            VARCHAR(50)            DEFAULT NULL    COMMENT '省份',
    `district`            VARCHAR(50)            DEFAULT NULL    COMMENT '区县',
    `scenic_level`        VARCHAR(50)            DEFAULT NULL    COMMENT '景区等级：5A/4A/3A/世界文化遗产等',
    `longitude`           DECIMAL(10,7)          DEFAULT NULL    COMMENT '经度',
    `latitude`            DECIMAL(10,7)          DEFAULT NULL    COMMENT '纬度',
    `open_time`           VARCHAR(100)           DEFAULT NULL    COMMENT '开放时间',
    `ticket_price`        DECIMAL(10,2)          DEFAULT 0.00   COMMENT '门票价格',
    `charge_type`         TINYINT                DEFAULT 1       COMMENT '收费类型：0-免费，1-收费',
    `suggested_duration`  VARCHAR(50)            DEFAULT NULL    COMMENT '建议游玩时长',
    `best_months`         VARCHAR(100)           DEFAULT NULL    COMMENT '最佳游玩月份',
    `scene_type`          VARCHAR(100)           DEFAULT NULL    COMMENT '场景分类：独自/情侣/朋友/家庭出行',
    `rating`              DECIMAL(3,2)           DEFAULT 0.00   COMMENT '评分',
    `view_count`          INT                    DEFAULT 0       COMMENT '浏览量',
    `favorite_count`      INT                    DEFAULT 0       COMMENT '收藏量',
    `review_count`        INT                    DEFAULT 0       COMMENT '评论数',
    `tags`                JSON                   DEFAULT NULL    COMMENT '标签列表',
    `features`            JSON                   DEFAULT NULL    COMMENT '特色标签（用于推荐）',
    `contact_phone`       VARCHAR(50)            DEFAULT NULL    COMMENT '官方咨询电话',
    `official_website`    VARCHAR(255)           DEFAULT NULL    COMMENT '官方网站链接',
    `tips`                TEXT                   DEFAULT NULL    COMMENT '避坑提示/注意事项（JSON数组格式）',
    `audit_status`        TINYINT                DEFAULT 1       COMMENT '审核状态：0-待审核，1-已通过，2-已驳回',
    `seasonal_status`     TINYINT                DEFAULT 0       COMMENT '季节性状态：0-正常开放，1-季节性关闭，2-临时关闭',
    `seasonal_note`       VARCHAR(255)           DEFAULT NULL    COMMENT '季节性说明',
    `status`              TINYINT       NOT NULL DEFAULT 1       COMMENT '状态：0-下架，1-上架',
    `deleted`             TINYINT       NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `create_time`         DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_city` (`city`),
    KEY `idx_province` (`province`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='景点表';

-- ============================================================
-- 4. 景点评分表 (attraction_rating)
-- 存储用户对景点的多维度评分（景色、趣味、性价比）及评价内容
-- ============================================================
DROP TABLE IF EXISTS `attraction_rating`;
CREATE TABLE `attraction_rating` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '评分ID',
    `attraction_id`  BIGINT       NOT NULL                 COMMENT '景点ID',
    `user_id`        BIGINT       NOT NULL                 COMMENT '用户ID',
    `scenery_score`  DECIMAL(2,1) NOT NULL                 COMMENT '景色评分（1.0-5.0）',
    `fun_score`      DECIMAL(2,1) NOT NULL                 COMMENT '趣味性评分（1.0-5.0）',
    `value_score`    DECIMAL(2,1) NOT NULL                 COMMENT '性价比评分（1.0-5.0）',
    `overall_score`  DECIMAL(2,1) NOT NULL                 COMMENT '综合评分（自动计算平均值）',
    `comment`        VARCHAR(500)          DEFAULT NULL    COMMENT '评价内容',
    `create_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
    `update_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_attraction_id` (`attraction_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='景点评分表';

-- ============================================================
-- 5. 用户足迹表 (user_footprint)
-- 记录用户标记"去过"的景点及游览日期
-- ============================================================
DROP TABLE IF EXISTS `user_footprint`;
CREATE TABLE `user_footprint` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '足迹ID',
    `user_id`        BIGINT       NOT NULL                 COMMENT '用户ID',
    `attraction_id`  BIGINT       NOT NULL                 COMMENT '景点ID',
    `visit_date`     DATE                  DEFAULT NULL    COMMENT '游览日期',
    `note`           VARCHAR(255)          DEFAULT NULL    COMMENT '备注',
    `create_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_attraction_id` (`attraction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户足迹表';

-- ============================================================
-- 6. 景点攻略关联表 (attraction_strategy)
-- 存储景点与攻略之间的多对多关联关系
-- ============================================================
DROP TABLE IF EXISTS `attraction_strategy`;
CREATE TABLE `attraction_strategy` (
    `id`             BIGINT  NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `attraction_id`  BIGINT  NOT NULL                 COMMENT '景点ID',
    `strategy_id`    BIGINT  NOT NULL                 COMMENT '攻略ID',
    `create_time`    DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_attraction_id` (`attraction_id`),
    KEY `idx_strategy_id` (`strategy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='景点攻略关联表';

-- ============================================================
-- 7. 路线规划表 (route_plan)
-- 存储用户创建或AI生成的多日行程规划及其调整历史
-- ============================================================
DROP TABLE IF EXISTS `route_plan`;
CREATE TABLE `route_plan` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '路线ID',
    `user_id`             BIGINT        NOT NULL                 COMMENT '用户ID',
    `title`               VARCHAR(100)           DEFAULT NULL    COMMENT '路线标题',
    `destination`         VARCHAR(100)           DEFAULT NULL    COMMENT '目的地',
    `days`                INT                    DEFAULT 1       COMMENT '天数',
    `budget`              DECIMAL(10,2)          DEFAULT NULL    COMMENT '预算',
    `companions`          VARCHAR(50)            DEFAULT NULL    COMMENT '同行人员类型',
    `preferences`         JSON                   DEFAULT NULL    COMMENT '偏好设置',
    `plan_data`           JSON                   DEFAULT NULL    COMMENT '详细行程数据',
    `is_ai_generated`     TINYINT                DEFAULT 0       COMMENT '是否AI生成',
    `version`             INT                    DEFAULT 1       COMMENT '版本号，每次调整后递增',
    `adjustment_history`  TEXT                   DEFAULT NULL    COMMENT '调整历史（JSON格式）',
    `status`              TINYINT                DEFAULT 0       COMMENT '行程状态：0-草稿，1-已确认，2-进行中，3-已完成',
    `deleted`             TINYINT       NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `create_time`         DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='路线规划表';

-- ============================================================
-- 8. 攻略表 (strategy)
-- 存储用户发布或AI生成的旅游攻略内容及互动统计数据
-- ============================================================
DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '攻略ID',
    `user_id`          BIGINT        NOT NULL                 COMMENT '作者ID',
    `title`            VARCHAR(100)  NOT NULL                 COMMENT '攻略标题',
    `cover_image`      VARCHAR(255)           DEFAULT NULL    COMMENT '封面图',
    `destination`      VARCHAR(100)           DEFAULT NULL    COMMENT '目的地',
    `days`             INT                    DEFAULT NULL    COMMENT '天数',
    `budget`           DECIMAL(10,2)          DEFAULT NULL    COMMENT '人均预算',
    `season`           VARCHAR(50)            DEFAULT NULL    COMMENT '适合季节：spring/summer/autumn/winter/all',
    `content`          MEDIUMTEXT             DEFAULT NULL    COMMENT '攻略内容（富文本）',
    `images`           JSON                   DEFAULT NULL    COMMENT '图片列表',
    `tags`             JSON                   DEFAULT NULL    COMMENT '标签',
    `view_count`       INT                    DEFAULT 0       COMMENT '浏览数',
    `like_count`       INT                    DEFAULT 0       COMMENT '点赞数',
    `favorite_count`   INT                    DEFAULT 0       COMMENT '收藏数',
    `comment_count`    INT                    DEFAULT 0       COMMENT '评论数',
    `is_ai_generated`  TINYINT                DEFAULT 0       COMMENT '是否AI生成',
    `featured`         TINYINT(1)             DEFAULT 0       COMMENT '是否加精：0-否，1-是',
    `pinned`           TINYINT(1)             DEFAULT 0       COMMENT '是否置顶：0-否，1-是',
    `pinned_time`      DATETIME               DEFAULT NULL    COMMENT '置顶时间',
    `audit_status`     TINYINT                DEFAULT 0       COMMENT '审核状态：0-待审核，1-已通过，2-已驳回',
    `audit_reason`     VARCHAR(255)           DEFAULT NULL    COMMENT '审核意见',
    `summary`          VARCHAR(500)           DEFAULT NULL    COMMENT '攻略摘要',
    `root_strategy_id` BIGINT                 DEFAULT NULL    COMMENT '攻略链路ID（同一攻略不同版本共用）',
    `status`           TINYINT       NOT NULL DEFAULT 1       COMMENT '状态：0-下架，1-上架',
    `visibility`       TINYINT       NOT NULL DEFAULT 1       COMMENT '可见性：0-私密，1-公开',
    `deleted`          TINYINT       NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `create_time`      DATETIME               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='攻略表';

-- ============================================================
-- 9. 攻略评论表 (strategy_comment)
-- 存储用户对攻略的评论内容，支持多级嵌套回复
-- ============================================================
DROP TABLE IF EXISTS `strategy_comment`;
CREATE TABLE `strategy_comment` (
    `id`           BIGINT         NOT NULL AUTO_INCREMENT  COMMENT '评论ID',
    `strategy_id`  BIGINT         NOT NULL                 COMMENT '攻略ID',
    `user_id`      BIGINT         NOT NULL                 COMMENT '评论用户ID',
    `parent_id`    BIGINT                  DEFAULT 0       COMMENT '父评论ID（0为一级评论）',
    `content`      VARCHAR(1000)  NOT NULL                 COMMENT '评论内容',
    `like_count`   INT                     DEFAULT 0       COMMENT '点赞数',
    `status`       TINYINT                 DEFAULT 1       COMMENT '状态：0-隐藏，1-显示',
    `create_time`  DATETIME                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_strategy_id` (`strategy_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='攻略评论表';

-- ============================================================
-- 10. 收藏表 (favorite)
-- 存储用户对景点或攻略的收藏记录
-- ============================================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '收藏ID',
    `user_id`     BIGINT      NOT NULL                 COMMENT '用户ID',
    `item_type`   VARCHAR(20) NOT NULL                 COMMENT '收藏类型：attraction/strategy',
    `item_id`     BIGINT      NOT NULL                 COMMENT '项目ID',
    `create_time` DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0       COMMENT '逻辑删除：0-正常，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item` (`item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏表';

-- ============================================================
-- 11. 用户行为记录表 (user_behavior)
-- 记录用户的浏览、点击、收藏、分享等行为数据，作为推荐算法的数据源
-- ============================================================
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`        BIGINT      NOT NULL                 COMMENT '用户ID',
    `item_type`      VARCHAR(20)          DEFAULT NULL    COMMENT '项目类型',
    `item_id`        BIGINT               DEFAULT NULL    COMMENT '项目ID',
    `behavior_type`  VARCHAR(20)          DEFAULT NULL    COMMENT '行为类型：view/click/favorite/share',
    `rating`         DECIMAL(3,2)         DEFAULT NULL    COMMENT '评分（如有）',
    `weight`         DOUBLE               DEFAULT NULL    COMMENT '行为权重（用于推荐算法）',
    `duration`       INT                  DEFAULT NULL    COMMENT '停留时长（秒）',
    `create_time`    DATETIME             DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item` (`item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户行为记录表';

-- ============================================================
-- 12. 角色表 (role)
-- 存储系统角色定义，如普通游客、内容管理员、系统管理员
-- ============================================================
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '角色ID',
    `role_code`   VARCHAR(50)  NOT NULL                 COMMENT '角色编码',
    `role_name`   VARCHAR(50)  NOT NULL                 COMMENT '角色名称',
    `description` VARCHAR(255)          DEFAULT NULL    COMMENT '角色描述',
    `status`      TINYINT               DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ============================================================
-- 13. 权限表 (permission)
-- 存储系统菜单和按钮级权限定义，支持树形层级结构
-- ============================================================
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '权限ID',
    `parent_id`        BIGINT                DEFAULT 0       COMMENT '父权限ID',
    `permission_code`  VARCHAR(100) NOT NULL                 COMMENT '权限编码',
    `permission_name`  VARCHAR(50)  NOT NULL                 COMMENT '权限名称',
    `permission_type`  VARCHAR(20)           DEFAULT NULL    COMMENT '权限类型：menu/button',
    `path`             VARCHAR(255)          DEFAULT NULL    COMMENT '路由路径',
    `icon`             VARCHAR(50)           DEFAULT NULL    COMMENT '图标',
    `sort_order`       INT                   DEFAULT 0       COMMENT '排序',
    `status`           TINYINT               DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    `create_time`      DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- ============================================================
-- 14. 角色权限关联表 (role_permission)
-- 存储角色与权限之间的多对多授权关系
-- ============================================================
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
    `id`            BIGINT NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `role_id`       BIGINT NOT NULL                 COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL                 COMMENT '权限ID',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- ============================================================
-- 15. 操作日志表 (operate_log)
-- 记录后台管理操作的完整审计日志，包括请求参数和响应结果
-- ============================================================
DROP TABLE IF EXISTS `operate_log`;
CREATE TABLE `operate_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `user_id`         BIGINT                DEFAULT NULL    COMMENT '操作用户ID',
    `username`        VARCHAR(50)           DEFAULT NULL    COMMENT '操作用户名',
    `user_role`       VARCHAR(50)           DEFAULT NULL    COMMENT '操作用户角色',
    `operation_type`  VARCHAR(50)           DEFAULT NULL    COMMENT '操作类型',
    `module`          VARCHAR(50)           DEFAULT NULL    COMMENT '操作模块',
    `description`     VARCHAR(500)          DEFAULT NULL    COMMENT '操作描述',
    `request_method`  VARCHAR(10)           DEFAULT NULL    COMMENT '请求方式',
    `request_url`     VARCHAR(255)          DEFAULT NULL    COMMENT '请求URL',
    `request_params`  TEXT                  DEFAULT NULL    COMMENT '请求参数',
    `status`          TINYINT               DEFAULT 1       COMMENT '执行状态：0-失败，1-成功',
    `error_msg`       VARCHAR(500)          DEFAULT NULL    COMMENT '错误信息',
    `response_result` TEXT                  DEFAULT NULL    COMMENT '响应结果',
    `ip_address`      VARCHAR(50)           DEFAULT NULL    COMMENT 'IP地址',
    `execute_time`    BIGINT                DEFAULT NULL    COMMENT '执行时长（毫秒）',
    `create_time`     DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- ============================================================
-- 16. 敏感词表 (sensitive_word)
-- 存储用于内容审核的敏感词库，按政治、色情、暴力、广告分类
-- ============================================================
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '敏感词ID',
    `word`        VARCHAR(100) NOT NULL                 COMMENT '敏感词',
    `type`        VARCHAR(20)           DEFAULT NULL    COMMENT '类型：politics/porn/violence/ad',
    `status`      TINYINT               DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='敏感词表';



-- ============================================================

-- 基础初始化数据

-- ============================================================

-- ============================================================
-- 初始数据：角色
-- ============================================================
INSERT INTO `role` (`role_code`, `role_name`, `description`) VALUES
('user',          '普通用户',   '普通游客，可浏览景点、发布攻略、AI对话等'),
('content_admin', '内容管理员', '负责景点管理、攻略审核等内容运营工作'),
('admin',         '系统管理员', '系统最高权限，可管理用户、角色、权限及系统配置');

-- ============================================================
-- 初始数据：默认账号
-- 密码使用 BCrypt 加密
-- 系统管理员: 13800000001 / 123456
-- 内容管理员: 13800000002 / 123456
-- 普通用户:   13800000003 / 123456
-- ============================================================
INSERT INTO `user` (`phone`, `password`, `nickname`, `avatar`, `role`, `status`, `preferences`) VALUES
('13800000001', '$2a$10$0XRrYhVqTddYvd4QCsMRQOazUvhLgURpTX4eXqX9jOkrquaaD.4AO', '系统管理员', '/uploads/20260513/e31164e28fd1496b8433e2f9a9fb75d1.jpg', 'admin', 1, '["自然风光", "历史文化", "温泉养生", "户外探险"]'),
('13800000002', '$2a$10$0XRrYhVqTddYvd4QCsMRQOazUvhLgURpTX4eXqX9jOkrquaaD.4AO', '内容管理员', '/uploads/20260513/17f09ef69f5b4b10aef7f384b7884bf3.jpg', 'content_admin', 1, '["自然风光", "户外探险", "温泉养生"]'),
('13800000003', '$2a$10$0XRrYhVqTddYvd4QCsMRQOazUvhLgURpTX4eXqX9jOkrquaaD.4AO', '测试用户', '/uploads/20260513/99c6c7353b7b4513b62fe85053a3e72f.jpg', 'user', 1, '["自然风光", "历史文化", "温泉养生"]');

-- ============================================================
-- 初始数据：景点分类
-- ============================================================
INSERT INTO `attraction_category` (`name`, `icon`, `sort_order`, `status`) VALUES
('自然风光', NULL, 1, 1),
('历史文化', NULL, 2, 1),
('主题乐园', NULL, 3, 1),
('城市观光', NULL, 4, 1),
('宗教寺庙', NULL, 5, 1),
('海滨海岛', NULL, 6, 1),
('乡村田园', NULL, 7, 1),
('红色旅游', NULL, 8, 1);

-- ============================================================
-- 景点及关联表数据请导入 seed_data.sql
-- 执行顺序：先导入 init.sql，再导入 seed_data.sql
-- ============================================================



-- ============================================================

-- 种子数据

-- ============================================================

-- ============================================================
-- 景点数据（共 200 条）
-- ============================================================
INSERT INTO `attraction` (`name`, `category_id`, `description`, `images`, `address`, `city`, `province`, `district`, `scenic_level`, `longitude`, `latitude`, `open_time`, `ticket_price`, `charge_type`, `suggested_duration`, `best_months`, `scene_type`, `rating`, `view_count`, `favorite_count`, `review_count`, `tags`, `features`, `contact_phone`, `tips`, `status`) VALUES
('故宫博物院', 2, '北京故宫是中国明清两代的皇家宫殿，旧称紫禁城，占地面积72万平方米。是世界上现存规模最大、保存最为完整的木质结构古建筑之一，被誉为世界五大宫之首。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211391346547.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211501387899.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211419647904.jpg"]', '北京市东城区景山前街4号', '北京', '北京', '东城区', '5A', 116.397, 39.917, '08:30-17:00（旺季）；08:30-16:30（淡季），周一闭馆', 60.00, 1, '3-4小时', '3月-5月,9月-11月', '独自,情侣,家庭', 4.55, 8278, 10012, 2206, '["世界文化遗产","皇家宫殿","明清建筑","博物馆"]', '["历史文化","古建筑","博物馆","皇家园林"]', '010-85007421', '["建议提前网上预约门票","周一闭馆（法定节假日除外）","旺季人流量大建议早到"]', 1),
('八达岭长城', 2, '八达岭长城位于北京市延庆区，是万里长城的重要组成部分。地势险峻，城关坚固，是中国古代伟大的防御工程。1987年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211429257940.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211375935776.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211376325031.jpg"]', '北京市延庆区G6京藏高速58号出口', '北京', '北京', '延庆区', '5A', 116.02, 40.359, '06:30-19:00（旺季）；07:30-18:00（淡季）', 40.00, 1, '3-4小时', '4月-10月', '独自,情侣,朋友,家庭', 3.85, 23865, 16156, 3230, '["世界文化遗产","万里长城","军事防御"]', '["历史文化","长城","世界遗产"]', '010-69121383', '["建议穿舒适运动鞋","夏季注意防晒补水","缆车可节省体力"]', 1),
('天坛公园', 2, '天坛位于北京市南部，是明清两代皇帝祭祀皇天、祈五谷丰登之场所。占地约273万平方米，是中国现存最大的古代祭祀性建筑群。1998年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211511916071.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211403421744.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211454067335.jpg"]', '北京市东城区天坛内东里7号', '北京', '北京', '东城区', '5A', 116.411, 39.882, '06:00-21:00（旺季）；06:30-21:00（淡季）', 15.00, 1, '2-3小时', '3月-5月,9月-11月', '独自,情侣,家庭', 4.28, 76037, 4822, 1647, '["世界文化遗产","祭祀建筑","明清皇家"]', '["历史文化","古建筑","世界遗产"]', '010-67028866', '["联票包含祈年殿、回音壁、圜丘","建议上午游览光线好"]', 1),
('颐和园', 2, '颐和园位于北京西郊，是中国清朝时期的皇家园林。以昆明湖、万寿山为基址，汲取江南园林的设计手法而建成，被誉为皇家园林博物馆。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211572617326.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211484527213.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211471094650.jpg"]', '北京市海淀区新建宫门路19号', '北京', '北京', '海淀区', '5A', 116.276, 40.0, '06:30-18:00（旺季）；07:00-17:00（淡季）', 30.00, 1, '3-4小时', '4月-10月', '独自,情侣,家庭', 4.12, 68966, 9443, 870, '["世界文化遗产","皇家园林","昆明湖","万寿山"]', '["历史文化","皇家园林","世界遗产"]', '010-62881144', '["建议从东宫门入园","昆明湖游船需另购票","长廊彩画值得细看"]', 1),
('天安门广场', 4, '天安门广场位于北京市中心，面积达44万平方米，可容纳100万人举行盛大集会，是世界上最大的城市广场之一。广场中央矗立着人民英雄纪念碑。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211521552315.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211377360533.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211389513800.jpg"]', '北京市东城区东长安街', '北京', '北京', '东城区', NULL, 116.391, 39.905, '05:00-22:00', 0.00, 0, '1-2小时', '1月-12月', '独自,情侣,朋友,家庭', 4.84, 57428, 2309, 2072, '["地标建筑","升旗仪式","国家象征"]', '["城市观光","地标","红色旅游"]', '010-63095745', '["观看升旗仪式需提前到达","需携带身份证安检入场"]', 1),
('圆明园遗址公园', 2, '圆明园坐落在北京西北郊，由圆明园、长春园和绮春园组成。始建于1709年，曾被誉为万园之园，是中国近代史上的重要历史遗址。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211416057486.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211388629050.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211392232596.jpg"]', '北京市海淀区清华西路28号', '北京', '北京', '海淀区', '4A', 116.298, 40.009, '07:00-19:30（旺季）；07:00-17:30（淡季）', 10.00, 1, '2-3小时', '4月-10月', '独自,情侣,朋友,家庭', 4.21, 61385, 15865, 4875, '["皇家园林遗址","万园之园","历史教育"]', '["历史文化","遗址","爱国教育"]', '010-62628501', '["西洋楼遗址区需另购票","荷花季7-8月景色最佳"]', 1),
('天津之眼摩天轮', 4, '天津之眼是一座跨河建设、桥轮合一的摩天轮，是世界上唯一建在桥上的摩天轮。夜晚灯光璀璨，是天津的标志性建筑之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211382523640.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211388623850.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211402899650.jpg"]', '天津市红桥区三岔河口永乐桥上', '天津', '天津', '红桥区', '4A', 117.17, 39.145, '09:30-21:30', 70.00, 1, '1小时', '4月-10月', '情侣,朋友,家庭', 4.44, 22287, 10904, 4339, '["摩天轮","地标","夜景"]', '["城市观光","地标","浪漫"]', '022-26288830', '["夜间乘坐景色更佳","建议提前购票避免排队"]', 1),
('五大道风景区', 2, '五大道位于天津市和平区，是天津最具特色的万国建筑博览苑。区域内拥有上世纪二三十年代建造的各式建筑2000多所，汇聚英、法、意、德等国建筑风格。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211410646663.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211430858632.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211453959781.jpg"]', '天津市和平区重庆道83号', '天津', '天津', '和平区', '4A', 117.209, 39.112, '全天开放', 0.00, 0, '2-3小时', '3月-5月,9月-11月', '独自,情侣,朋友', 3.88, 59164, 4621, 2850, '["万国建筑","名人故居","租界风情"]', '["历史文化","建筑","名人故居"]', '022-23307222', '["建议乘坐马车游览","免费参观但部分故居收费"]', 1),
('古文化街', 2, '天津古文化街以天后宫为中心，集中了天津民间工艺和特色小吃。泥人张、杨柳青年画等非遗文化在此传承，是体验天津民俗文化的最佳去处。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211344335531.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211344340547.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346963301.jpg"]', '天津市南开区古文化街', '天津', '天津', '南开区', '5A', 117.177, 39.143, '09:00-21:00', 0.00, 0, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 4.38, 39887, 12930, 2186, '["民俗文化","老字号","手工艺"]', '["历史文化","民俗","购物"]', '022-27356128', '["泥人张和杨柳青年画是必看特色","春节期间活动丰富"]', 1),
('盘山风景区', 1, '盘山位于天津市蓟州区，是国家重点风景名胜区。盘山始记于汉，兴于唐，极盛于清，乾隆皇帝曾32次游历盘山，留下了大量诗篇。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211329188628.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211320295885.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211406916501.jpg"]', '天津市蓟州区官庄镇莲花岭村', '天津', '天津', '蓟州区', '5A', 117.256, 40.054, '08:00-17:00', 78.00, 1, '4-5小时', '4月-10月', '独自,朋友,家庭', 4.08, 70825, 6306, 2215, '["名山","乾隆","自然风光"]', '["自然风光","山岳","历史"]', '022-29821235', '["建议穿登山鞋","山上温度较低注意保暖"]', 1),
('瓷房子', 4, '瓷房子位于天津市和平区赤峰道72号，是一座用数万件古瓷器、水晶和玛瑙装饰的法式建筑。被誉为中国古瓷博物馆，是天津独特的艺术地标。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211374930425.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211439376496.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211538922863.jpg"]', '天津市和平区赤峰道72号', '天津', '天津', '和平区', NULL, 117.211, 39.126, '09:00-17:30', 50.00, 1, '1-2小时', '1月-12月', '独自,情侣,朋友', 4.94, 12146, 17796, 4163, '["瓷器","法式建筑","博物馆"]', '["城市观光","艺术","建筑"]', '022-27123366', '["外观拍照免费，入内参观需购票","建议请导游讲解"]', 1),
('承德避暑山庄', 2, '承德避暑山庄是中国现存最大的古典皇家园林，始建于1703年，历经清康熙、雍正、乾隆三朝。1994年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211407055483.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211391910576.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211402883498.jpg"]', '河北省承德市双桥区丽正门大街22号', '承德', '河北', '双桥区', '5A', 117.939, 40.987, '08:00-17:30', 130.00, 1, '4-6小时', '5月-10月', '独自,情侣,家庭', 4.52, 33318, 15958, 4312, '["世界文化遗产","皇家园林","避暑胜地"]', '["历史文化","皇家园林","避暑"]', '0314-2029771', '["建议租电瓶车游览","夏季是最佳游览季节"]', 1),
('秦皇岛北戴河', 6, '北戴河位于河北省秦皇岛市，是中国著名的海滨度假胜地。海岸线绵长，沙滩细软，气候宜人，自清末起就是避暑胜地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211416054169.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211340182782.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211327829034.jpg"]', '河北省秦皇岛市北戴河区', '秦皇岛', '河北', '北戴河区', '4A', 119.484, 39.834, '全天开放', 0.00, 0, '1-2天', '6月-9月', '情侣,朋友,家庭', 4.87, 23019, 15383, 4755, '["海滨","避暑","日出","沙滩"]', '["海滨海岛","度假","避暑"]', '0335-4041591', '["鸽子窝公园看日出","夏季海鲜丰富"]', 1),
('白洋淀', 1, '白洋淀位于河北省保定市安新县，是华北平原最大的淡水湖泊。以荷花闻名，夏季荷花盛开时景色壮观，也是抗日战争时期雁翎队的故乡。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211402141059.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392192929.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211401559184.jpg"]', '河北省保定市安新县旅游东路', '保定', '河北', '安新县', '5A', 115.945, 38.895, '08:00-18:00', 40.00, 1, '4-6小时', '6月-9月', '朋友,家庭', 3.91, 30722, 5914, 2679, '["湿地","荷花","红色旅游","雁翎队"]', '["自然风光","湿地","红色旅游"]', '0312-5116352', '["7-8月荷花盛开最美","建议乘船游览"]', 1),
('野三坡', 1, '野三坡位于河北省涞水县，以雄、险、奇、幽的自然景观著称。百里峡是其核心景区，峡谷幽深，奇石林立，被誉为天下第一峡。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211386985894.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211386985890.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211359986814.jpg"]', '河北省保定市涞水县野三坡镇', '保定', '河北', '涞水县', '5A', 115.38, 39.6, '07:30-17:30', 100.00, 1, '1天', '4月-10月', '朋友,家庭', 4.93, 78368, 1376, 4080, '["峡谷","自然风光","漂流"]', '["自然风光","峡谷","户外"]', '0312-4568838', '["百里峡是必游景点","夏季可体验漂流"]', 1),
('山海关', 2, '山海关位于河北省秦皇岛市，是明长城的东北关隘之一，素有天下第一关之称。关城建筑雄伟，历史文化底蕴深厚。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375625328.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211389696410.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211364001468.jpg"]', '河北省秦皇岛市山海关区东大街1号', '秦皇岛', '河北', '山海关区', '5A', 119.776, 39.978, '07:00-18:00（旺季）', 40.00, 1, '3-4小时', '4月-10月', '独自,朋友,家庭', 3.85, 7531, 8368, 4411, '["天下第一关","长城","历史"]', '["历史文化","长城","关隘"]', '0335-5051106', '["老龙头景区可看长城入海","建议与北戴河联游"]', 1),
('正定古城', 2, '正定古城位于河北省石家庄市，有1600多年历史。城内有隆兴寺、荣国府等众多古迹，素有九楼四塔八大寺之称。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211552276531.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211443246833.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211390983192.jpg"]', '河北省石家庄市正定县', '石家庄', '河北', '正定县', '4A', 114.573, 38.146, '全天开放', 0.00, 0, '1天', '3月-11月', '独自,情侣,朋友', 4.48, 31964, 7591, 537, '["古城","寺庙","历史","古建筑"]', '["历史文化","古城","古建筑"]', '0311-88789987', '["隆兴寺是必看景点","夜景灯光秀值得一看"]', 1),
('平遥古城', 2, '平遥古城位于山西省晋中市，是中国保存最为完好的四大古城之一。1997年被列为世界文化遗产，城内保留了明清时期的县城基本风貌。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211393127839.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211375544496.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211423269919.jpg"]', '山西省晋中市平遥县照壁南街58号', '晋中', '山西', '平遥县', '5A', 112.176, 37.189, '08:00-18:00', 125.00, 1, '1-2天', '3月-11月', '独自,情侣,朋友', 4.06, 65229, 1745, 1315, '["世界文化遗产","古城","明清建筑","票号"]', '["历史文化","古城","世界遗产"]', '0354-5690000', '["建议购买通票游览","日升昌票号是必看景点"]', 1),
('云冈石窟', 2, '云冈石窟位于山西省大同市，始建于北魏时期，现存主要洞窟45个，石雕造像51000余尊。2001年被列为世界文化遗产，是中国规模最大的古代石窟群之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211394639202.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211446494527.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211425981423.jpg"]', '山西省大同市云冈区云冈镇', '大同', '山西', '云冈区', '5A', 113.134, 40.112, '08:30-17:30', 120.00, 1, '3-4小时', '5月-10月', '独自,情侣,朋友', 4.16, 25203, 9703, 2081, '["世界文化遗产","石窟","北魏","佛教艺术"]', '["历史文化","石窟","世界遗产"]', '0352-7992622', '["第5、6窟是精华","建议请导游讲解"]', 1),
('五台山', 5, '五台山位于山西省忻州市，是中国佛教四大名山之首，世界文化遗产。因五座山峰如五根擎天大柱拔地而起，峰顶平坦如台而得名。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211551023790.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211377433823.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211406787422.jpg"]', '山西省忻州市五台县台怀镇', '忻州', '山西', '五台县', '5A', 113.593, 39.08, '全天开放', 135.00, 1, '1-2天', '5月-10月', '独自,家庭', 4.38, 77966, 8103, 2826, '["佛教名山","世界文化遗产","朝圣"]', '["宗教寺庙","名山","朝圣"]', '0350-6548690', '["夏季避暑胜地","建议至少安排两天"]', 1),
('壶口瀑布', 1, '壶口瀑布位于山西省吉县与陕西省宜川县之间的黄河峡谷中，是中国第二大瀑布、世界上最大的黄色瀑布。黄河水在此收束为一股，跌入深潭，气势磅礴。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211437002607.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211416461313.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211416177262.jpg"]', '山西省临汾市吉县壶口镇', '临汾', '山西', '吉县', '4A', 110.448, 36.147, '08:00-18:00', 100.00, 1, '2-3小时', '4月-5月,9月-11月', '独自,朋友,家庭', 4.89, 27678, 3134, 2150, '["黄河","瀑布","壮观","气势磅礴"]', '["自然风光","瀑布","黄河"]', '0357-7942036', '["春秋两季水量最大最壮观","注意防滑"]', 1),
('乔家大院', 2, '乔家大院位于山西省祁县，是清代著名商业金融资本家乔致庸的宅第。整体布局工整，建筑考究，被誉为北方民居建筑的明珠。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211433904162.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211380825908.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211412622445.jpg"]', '山西省晋中市祁县东观镇乔家堡村', '晋中', '山西', '祁县', '5A', 112.44, 37.34, '08:00-18:00', 115.00, 1, '2-3小时', '3月-11月', '独自,情侣,朋友', 4.32, 43546, 9288, 4167, '["晋商大院","清代建筑","乔致庸"]', '["历史文化","民居","晋商"]', '0354-5321045', '["电影《大红灯笼高高挂》取景地","建议请导游讲解"]', 1),
('悬空寺', 5, '悬空寺位于山西省大同市浑源县恒山金龙峡西侧翠屏峰峭壁间，始建于北魏后期。整座寺院悬挂在悬崖之上，以险奇著称，是中国仅存的佛、道、儒三教合一的独特寺庙。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211428615881.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211434190402.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211521116949.jpg"]', '山西省大同市浑源县恒山南路', '大同', '山西', '浑源县', '4A', 113.705, 39.665, '08:00-18:00', 125.00, 1, '1-2小时', '4月-10月', '独自,朋友', 4.36, 57947, 1552, 1867, '["悬崖寺庙","北魏","三教合一","奇观"]', '["宗教寺庙","奇观","古建筑"]', '0352-8322142', '["恐高者慎入","建议早上去人少"]', 1),
('呼伦贝尔大草原', 1, '呼伦贝尔大草原位于内蒙古自治区东北部，是世界四大草原之一。草原辽阔，水草丰美，有天堂草原之称，是中国最美的草原之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211405380267.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211391142948.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211400038911.jpg"]', '内蒙古自治区呼伦贝尔市新巴尔虎左旗', '呼伦贝尔', '内蒙古', '新巴尔虎左旗', '5A', 118.25, 49.22, '全天开放', 0.00, 0, '2-3天', '6月-9月', '独自,情侣,朋友,家庭', 4.29, 42709, 17850, 2001, '["草原","蒙古族","骑马","自驾"]', '["自然风光","草原","民族"]', '0470-8217684', '["6-8月草原最美","建议自驾或包车"]', 1),
('响沙湾', 1, '响沙湾位于内蒙古鄂尔多斯市达拉特旗，是中国三大响沙之一。沙丘高大，沙坡陡立，从沙丘顶部滑下时会发出嗡嗡的响声，十分奇特。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211406352985.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211436672675.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211493794845.jpg"]', '内蒙古自治区鄂尔多斯市达拉特旗', '鄂尔多斯', '内蒙古', '达拉特旗', '5A', 109.99, 40.01, '08:00-18:00', 80.00, 1, '1天', '5月-10月', '朋友,家庭', 3.88, 61962, 9069, 3851, '["沙漠","响沙","滑沙","骆驼"]', '["自然风光","沙漠","体验"]', '0477-5180888', '["建议体验滑沙和骑骆驼","注意防晒"]', 1),
('额济纳胡杨林', 1, '额济纳胡杨林位于内蒙古阿拉善盟额济纳旗，是中国境内最大的天然胡杨林。每年10月胡杨叶变金黄，景色绝美，被誉为中国最美秋色之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211364957461.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211416917862.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211365000572.jpg"]', '内蒙古自治区阿拉善盟额济纳旗达来呼布镇', '阿拉善', '内蒙古', '额济纳旗', '4A', 101.07, 41.96, '08:00-18:00', 150.00, 1, '1天', '9月下旬-10月中旬', '独自,情侣,朋友', 3.89, 58527, 16323, 3602, '["胡杨林","金秋","摄影","沙漠"]', '["自然风光","胡杨","摄影"]', '0483-6521018', '["10月初是最佳观赏期","住宿紧张需提前预订"]', 1),
('成吉思汗陵', 2, '成吉思汗陵位于内蒙古鄂尔多斯市伊金霍洛旗，是蒙古帝国创建者成吉思汗的衣冠冢。陵园气势恢宏，是蒙古族人民心中的圣地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211428524042.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211408538731.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211341017516.jpg"]', '内蒙古自治区鄂尔多斯市伊金霍洛旗', '鄂尔多斯', '内蒙古', '伊金霍洛旗', '5A', 109.79, 39.58, '08:00-18:00', 180.00, 1, '2-3小时', '5月-10月', '独自,朋友,家庭', 4.67, 70638, 4636, 4949, '["成吉思汗","蒙古族","历史","圣地"]', '["历史文化","陵墓","民族"]', '0477-8961162', '["每年农历三月廿一有大型祭祀活动","建议请导游讲解"]', 1),
('阿尔山国家森林公园', 1, '阿尔山国家森林公园位于内蒙古兴安盟阿尔山市，拥有火山地貌、温泉群、高山湿地等多种自然景观。秋季层林尽染，是摄影爱好者的天堂。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211426536095.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211310196281.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211351369469.jpg"]', '内蒙古自治区兴安盟阿尔山市', '兴安盟', '内蒙古', '阿尔山市', '5A', 119.94, 47.18, '07:00-17:00', 180.00, 1, '1-2天', '6月-10月', '独自,朋友', 4.6, 17561, 6469, 746, '["森林","火山","温泉","秋色"]', '["自然风光","森林","火山"]', '0482-7155555', '["秋季9-10月景色最美","建议自驾游览"]', 1),
('沈阳故宫', 2, '沈阳故宫始建于1625年，是中国仅存的两大宫殿建筑群之一。为清朝初期的皇宫，2004年被列为世界文化遗产。建筑风格融合了满、汉、蒙三族特色。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211458809820.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211459076525.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211458156730.jpg"]', '辽宁省沈阳市沈河区沈阳路171号', '沈阳', '辽宁', '沈河区', '4A', 123.457, 41.796, '08:30-17:00', 50.00, 1, '2-3小时', '4月-10月', '独自,情侣,朋友', 4.86, 43545, 16054, 3886, '["世界文化遗产","清朝皇宫","满族文化"]', '["历史文化","宫殿","世界遗产"]', '024-24843819', '["建议请导游讲解","与北京故宫风格不同值得对比"]', 1),
('大连老虎滩海洋公园', 3, '大连老虎滩海洋公园位于大连市南部海滨，是中国最大的现代化海洋主题公园之一。园内有极地馆、珊瑚馆、鸟语林等多个场馆。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211333178613.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211326364735.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211625282442.jpg"]', '辽宁省大连市中山区滨海中路9号', '大连', '辽宁', '中山区', '5A', 121.68, 38.87, '08:00-17:00', 210.00, 1, '1天', '5月-10月', '朋友,家庭', 4.18, 6111, 9681, 1468, '["海洋公园","极地动物","海豚表演"]', '["主题乐园","海洋","亲子"]', '0411-82689356', '["建议购买套票","海豚表演时间需提前确认"]', 1),
('本溪水洞', 1, '本溪水洞位于辽宁省本溪市，是世界上已发现的最长的充水溶洞。洞内钟乳石千姿百态，地下暗河蜿蜒曲折，乘船游览别有洞天。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211307996294.jpg", "https://vcg05.cfp.cn/creative/vcg/nowarter800/new/VCG211223816373.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211299894092.jpg"]', '辽宁省本溪市本溪满族自治县谢家崴子村', '本溪', '辽宁', '本溪满族自治县', '5A', 124.08, 41.3, '08:30-17:00', 150.00, 1, '2-3小时', '5月-10月', '朋友,家庭', 3.89, 29414, 12271, 4656, '["溶洞","地下河","钟乳石"]', '["自然风光","溶洞","地质"]', '024-46240298', '["洞内温度较低建议带外套","旺季排队时间较长"]', 1),
('金石滩', 1, '金石滩位于大连市金州区，拥有绵延30公里的海岸线。这里有奇特的海蚀地貌和金色沙滩，被誉为凝固的动物世界和天然地质博物馆。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211397295202.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211408378449.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211416423121.jpg"]', '辽宁省大连市金州区金石滩国家旅游度假区', '大连', '辽宁', '金州区', '5A', 122.06, 39.05, '全天开放', 0.00, 0, '1天', '5月-10月', '情侣,朋友,家庭', 4.13, 70816, 16735, 4821, '["海滨","地质公园","沙滩","海蚀"]', '["海滨海岛","地质","度假"]', '0411-87900241', '["黄金海岸沙滩质量好","地质公园值得一看"]', 1),
('长白山', 1, '长白山位于吉林省东南部，是中国十大名山之一。天池是长白山的标志性景观，是中国最大的火山口湖。长白山也是松花江、图们江、鸭绿江的发源地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211534830017.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211516975089.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211536857512.jpg"]', '吉林省延边朝鲜族自治州安图县', '延边', '吉林', '安图县', '5A', 128.06, 42.0, '06:30-16:00', 169.00, 1, '1-2天', '6月-9月', '独自,朋友,家庭', 4.81, 72538, 4722, 2815, '["天池","火山","雪景","温泉"]', '["自然风光","火山","名山"]', '0433-5410000', '["天池能否看到取决于天气","建议多预留一天"]', 1),
('雾凇岛', 1, '雾凇岛位于吉林省吉林市龙潭区乌拉街满族镇，是中国著名的雾凇观赏胜地。每年冬季松花江上雾气蒸腾，在树枝上凝结成洁白的冰晶，形成壮观的雾凇景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211328737934.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211351433386.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211374891122.jpg"]', '吉林省吉林市龙潭区乌拉街满族镇韩屯村', '吉林', '吉林', '龙潭区', NULL, 126.68, 43.92, '全天开放', 0.00, 0, '3-4小时', '12月-次年2月', '独自,情侣,朋友', 4.33, 62228, 15888, 541, '["雾凇","冬景","摄影","松花江"]', '["自然风光","冬景","摄影"]', '0432-63588008', '["最佳观赏时间为清晨","注意防寒保暖"]', 1),
('伪满皇宫博物院', 2, '伪满皇宫博物院位于长春市，是清朝末代皇帝溥仪充当伪满洲国傀儡皇帝时的宫殿。现为全国重点文物保护单位，是了解那段历史的重要场所。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211392172666.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211373764394.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211370350197.jpg"]', '吉林省长春市宽城区光复北路5号', '长春', '吉林', '宽城区', '5A', 125.34, 43.88, '08:30-17:20', 70.00, 1, '2-3小时', '4月-10月', '独自,朋友', 4.23, 56071, 7555, 2632, '["伪满洲国","溥仪","历史","博物馆"]', '["历史文化","博物馆","近代史"]', '0431-82866611', '["建议请导游讲解历史背景","周一闭馆"]', 1),
('净月潭', 1, '净月潭位于长春市东南部，因形似弯月而得名。森林覆盖率达96%，是亚洲最大的人工森林公园，被誉为长春的绿色明珠。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211447187971.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211381858588.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211424972071.jpg"]', '吉林省长春市南关区净月大街5840号', '长春', '吉林', '南关区', '5A', 125.44, 43.79, '06:00-18:00', 30.00, 1, '3-4小时', '5月-10月', '独自,朋友,家庭', 4.73, 47717, 7465, 2170, '["森林","湖泊","滑雪","骑行"]', '["自然风光","森林","运动"]', '0431-84518000', '["冬季可滑雪","夏季适合骑行和徒步"]', 1),
('哈尔滨冰雪大世界', 3, '哈尔滨冰雪大世界是世界上最大的冰雪主题乐园，每年冬季开放。园内冰雕雪塑规模宏大，夜晚灯光璀璨，是冰城哈尔滨的标志性景点。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211440406414.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211431035344.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211544806823.jpg"]', '黑龙江省哈尔滨市松北区太阳岛西侧', '哈尔滨', '黑龙江', '松北区', '4A', 126.6, 45.79, '11:00-21:00', 300.00, 1, '4-6小时', '12月-次年2月', '情侣,朋友,家庭', 4.75, 17585, 10837, 2308, '["冰雕","雪塑","冰灯","冬季"]', '["主题乐园","冰雪","冬季"]', '0451-88190230', '["注意防寒保暖","建议下午入园看日景和夜景"]', 1),
('镜泊湖', 1, '镜泊湖位于黑龙江省牡丹江市，是中国最大的高山堰塞湖。湖面如镜，群山环抱，吊水楼瀑布是其标志性景观，夏季水量充沛时气势壮观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211503073391.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211309628640.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211287210753.jpg"]', '黑龙江省牡丹江市宁安市镜泊湖镇', '牡丹江', '黑龙江', '宁安市', '5A', 128.75, 43.88, '07:00-17:00', 100.00, 1, '1天', '6月-9月', '朋友,家庭', 3.97, 50811, 6339, 2173, '["堰塞湖","瀑布","火山","森林"]', '["自然风光","湖泊","瀑布"]', '0453-6270668', '["吊水楼瀑布是必看景点","夏季水量最大"]', 1),
('漠河北极村', 4, '北极村位于黑龙江省漠河市，是中国最北端的村庄。这里可以观赏到北极光和极昼现象，冬季白雪皑皑，夏季凉爽宜人，是中国找北的最佳目的地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211484248689.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211432737198.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211484248684.jpg"]', '黑龙江省大兴安岭地区漠河市北极镇', '大兴安岭', '黑龙江', '漠河市', '5A', 122.37, 53.49, '全天开放', 60.00, 1, '1-2天', '6月-8月,12月-次年2月', '独自,朋友', 4.12, 76028, 14788, 3223, '["最北","北极光","极昼","雪乡"]', '["城市观光","极地","体验"]', '0457-2885241', '["夏至前后有机会看到北极光","冬季极寒注意保暖"]', 1),
('五大连池', 1, '五大连池位于黑龙江省黑河市，是世界地质公园。由14座火山和5个堰塞湖组成，拥有世界上保存最完整的火山地貌。矿泉水资源丰富，有天然火山博物馆之称。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211438269282.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211443711885.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211438269281.jpg"]', '黑龙江省黑河市五大连池市', '黑河', '黑龙江', '五大连池市', '5A', 126.17, 48.75, '07:30-17:30', 195.00, 1, '1-2天', '5月-9月', '独自,朋友', 4.39, 64412, 12078, 4770, '["火山","矿泉","地质公园","堰塞湖"]', '["自然风光","火山","地质"]', '0456-7296999', '["老黑山和火烧山是核心景点","矿泉水可直接饮用"]', 1),
('外滩', 4, '外滩位于上海市黄浦区黄浦江畔，全长1.5公里。西侧是52幢风格各异的万国建筑博览群，东侧隔江是陆家嘴金融中心的摩天大楼群，是上海最具标志性的景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211436707794.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211561552430.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375482483.jpg"]', '上海市黄浦区中山东一路', '上海', '上海', '黄浦区', NULL, 121.491, 31.24, '全天开放', 0.00, 0, '1-2小时', '1月-12月', '独自,情侣,朋友,家庭', 3.97, 69211, 17551, 2593, '["万国建筑","黄浦江","夜景","地标"]', '["城市观光","地标","夜景"]', '021-33761234', '["夜景最佳观赏时间为19:00-22:00","可乘坐浦江游船"]', 1),
('东方明珠广播电视塔', 4, '东方明珠广播电视塔位于上海浦东陆家嘴，塔高468米，是上海的标志性建筑。塔内设有旋转餐厅、观光层和上海城市历史发展陈列馆。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211584983908.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG41N2177632448.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211402487525.jpg"]', '上海市浦东新区世纪大道1号', '上海', '上海', '浦东新区', '5A', 121.506, 31.24, '08:00-21:30', 199.00, 1, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 4.38, 65282, 3906, 1249, '["地标","观光塔","全景","浦东"]', '["城市观光","地标","观景"]', '021-58791888', '["建议购买含透明观光层的套票","傍晚上塔可看日落和夜景"]', 1),
('上海迪士尼乐园', 3, '上海迪士尼乐园是中国内地首座迪士尼主题乐园，拥有六大主题园区。园内有全球最高最大的迪士尼城堡——奇幻童话城堡。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211422194254.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211422194248.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211405510567.jpg"]', '上海市浦东新区川沙镇黄赵路310号', '上海', '上海', '浦东新区', '5A', 121.67, 31.15, '08:00-20:00', 475.00, 1, '1天', '3月-5月,9月-11月', '情侣,朋友,家庭', 4.66, 9892, 17027, 235, '["迪士尼","主题乐园","亲子","城堡"]', '["主题乐园","亲子","娱乐"]', '021-31580000', '["建议工作日前往人少","下载官方APP查看排队时间"]', 1),
('豫园', 2, '豫园位于上海市黄浦区，始建于明嘉靖年间，是江南古典园林的代表。园内亭台楼阁、假山池塘布局精巧，周边的城隍庙商圈是品尝上海小吃的好去处。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211488611493.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392601898.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211423969425.jpg"]', '上海市黄浦区安仁街137号', '上海', '上海', '黄浦区', '4A', 121.492, 31.227, '08:30-17:00', 40.00, 1, '1-2小时', '3月-5月,9月-11月', '独自,情侣,朋友', 4.03, 19039, 11514, 1773, '["古典园林","明代","城隍庙","小吃"]', '["历史文化","园林","美食"]', '021-63260830', '["建议与城隍庙一起游览","南翔小笼包是必吃美食"]', 1),
('苏州园林拙政园', 2, '拙政园位于苏州市姑苏区，始建于明正德年间，是苏州最大的古典园林。以水为中心，山水萦绕，厅榭精美，被誉为中国园林之母。1997年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211544722508.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211376376595.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211437269969.jpg"]', '江苏省苏州市姑苏区东北街178号', '苏州', '江苏', '姑苏区', '5A', 120.631, 31.325, '07:30-17:30', 70.00, 1, '2-3小时', '3月-5月,9月-11月', '独自,情侣,朋友', 4.73, 9067, 11403, 298, '["世界文化遗产","古典园林","江南","水景"]', '["历史文化","园林","世界遗产"]', '0512-67546631', '["建议避开节假日","春季赏花秋季赏桂"]', 1),
('中山陵', 2, '中山陵位于南京市玄武区紫金山南麓，是中国近代伟大的民主革命先行者孙中山先生的陵寝。整个建筑群依山势而建，气势宏伟，融汇了中西建筑精华。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211436305345.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211391223003.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211391223004.jpg"]', '江苏省南京市玄武区石象路7号', '南京', '江苏', '玄武区', '5A', 118.856, 32.058, '08:30-17:00', 0.00, 0, '2-3小时', '3月-5月,9月-11月', '独自,朋友,家庭', 3.97, 29795, 4348, 2500, '["孙中山","陵寝","紫金山","近代史"]', '["历史文化","陵墓","红色旅游"]', '025-84437786', '["392级台阶建议穿舒适鞋","周一闭馆"]', 1),
('周庄古镇', 2, '周庄位于苏州市昆山市，是中国第一水乡。始建于北宋，保存了14座各具特色的古桥和60多个院落。双桥、沈厅、张厅是最具代表性的景点。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211343657444.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211346991834.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211328288943.jpg"]', '江苏省苏州市昆山市周庄镇全福路', '苏州', '江苏', '昆山市', '5A', 120.845, 31.114, '07:30-18:00', 100.00, 1, '1天', '3月-5月,9月-11月', '独自,情侣,朋友', 4.27, 29839, 11905, 1715, '["水乡","古镇","双桥","江南"]', '["历史文化","古镇","水乡"]', '0512-57211699', '["建议住一晚体验夜景","万三蹄是特色美食"]', 1),
('夫子庙秦淮河', 4, '夫子庙秦淮河风光带位于南京市秦淮区，以夫子庙为核心，集古迹、园林、画舫、市街为一体。秦淮河畔灯火辉煌，是南京最繁华的旅游商业区。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211393211389.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211324562756.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380414335.jpg"]', '江苏省南京市秦淮区秦淮河畔', '南京', '江苏', '秦淮区', '5A', 118.787, 32.022, '全天开放', 0.00, 0, '3-4小时', '1月-12月', '独自,情侣,朋友,家庭', 3.89, 38921, 5606, 3020, '["秦淮河","夫子庙","夜景","小吃"]', '["城市观光","夜景","美食"]', '025-52209788', '["夜游秦淮河画舫是经典体验","鸭血粉丝汤是必吃"]', 1),
('鼋头渚', 1, '鼋头渚位于无锡市太湖之滨，是太湖风景名胜区的主景点之一。因巨石突入湖中形状酷似神鼋昂首而得名，有太湖第一名胜之称。春季樱花盛开时尤为壮观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211455667400.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211348312687.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211330461058.jpg"]', '江苏省无锡市滨湖区鼋渚路1号', '无锡', '江苏', '滨湖区', '5A', 120.22, 31.52, '08:00-17:30', 90.00, 1, '4-6小时', '3月-4月,9月-11月', '独自,情侣,朋友,家庭', 4.04, 17149, 16762, 4907, '["太湖","樱花","日落","湖景"]', '["自然风光","湖泊","赏花"]', '0510-96889688', '["3月底-4月初樱花最盛","建议乘船游太湖"]', 1),
('瘦西湖', 1, '瘦西湖位于扬州市，是一条狭长的河流型湖泊。两岸花木扶疏，亭台楼阁点缀其间，五亭桥、白塔、二十四桥等景点如诗如画。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211440526698.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211538892196.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211381494963.jpg"]', '江苏省扬州市邗江区大虹桥路28号', '扬州', '江苏', '邗江区', '5A', 119.421, 32.41, '06:30-17:30', 100.00, 1, '3-4小时', '3月-5月', '独自,情侣,朋友', 3.9, 27593, 6287, 3495, '["园林","湖泊","五亭桥","烟花三月"]', '["自然风光","园林","湖泊"]', '0514-87357803', '["烟花三月下扬州是最佳时节","建议乘船游览"]', 1),
('灵山大佛', 5, '灵山大佛位于无锡市马山太湖之滨，高88米，是中国五方五大佛之一。灵山胜境还包括梵宫、九龙灌浴等景点，是集佛教文化与艺术于一体的旅游胜地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211450100325.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211451893619.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211387157077.jpg"]', '江苏省无锡市滨湖区马山灵山路1号', '无锡', '江苏', '滨湖区', '5A', 120.13, 31.41, '07:00-17:30', 210.00, 1, '1天', '3月-11月', '独自,家庭', 4.29, 79044, 9714, 973, '["大佛","佛教","梵宫","九龙灌浴"]', '["宗教寺庙","佛教","文化"]', '0510-85688228', '["九龙灌浴表演时间需提前确认","梵宫内部非常壮观"]', 1),
('西湖', 1, '杭州西湖位于浙江省杭州市西湖区，是中国十大风景名胜之一。三面云山一面城，湖光山色相映成趣。2011年被列为世界文化景观遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211568664550.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211524387418.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211451299810.jpg"]', '浙江省杭州市西湖区龙井路1号', '杭州', '浙江', '西湖区', '5A', 120.149, 30.243, '全天开放', 0.00, 0, '1天', '3月-5月,9月-11月', '独自,情侣,朋友,家庭', 4.87, 69795, 12576, 1130, '["世界遗产","湖泊","断桥","雷峰塔"]', '["自然风光","湖泊","世界遗产"]', '0571-87179603', '["建议骑行环湖","断桥残雪冬季最美"]', 1),
('乌镇', 2, '乌镇位于浙江省嘉兴市桐乡市，是典型的江南水乡古镇。东栅保留了原汁原味的水乡风貌，西栅则是经过整体开发的休闲度假区。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211532770854.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211375762958.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211449411616.jpg"]', '浙江省嘉兴市桐乡市乌镇石佛南路18号', '嘉兴', '浙江', '桐乡市', '5A', 120.494, 30.745, '07:00-17:30（东栅）；09:00-22:00（西栅）', 150.00, 1, '1-2天', '3月-5月,9月-11月', '独自,情侣,朋友', 4.44, 21268, 10178, 2678, '["水乡","古镇","江南","夜景"]', '["历史文化","古镇","水乡"]', '0573-88731088', '["建议住西栅看夜景","世界互联网大会永久举办地"]', 1),
('普陀山', 5, '普陀山位于浙江省舟山市，是中国佛教四大名山之一，观世音菩萨的道场。岛上寺院林立，香火鼎盛，有海天佛国之称。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211553296344.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211484599959.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211336004243.jpg"]', '浙江省舟山市普陀区普陀山', '舟山', '浙江', '普陀区', '5A', 122.385, 30.005, '全天开放', 160.00, 1, '1-2天', '2月-4月,9月-11月', '独自,家庭', 4.69, 72346, 5204, 4246, '["佛教名山","观音","海岛","朝圣"]', '["宗教寺庙","海岛","朝圣"]', '0580-6091414', '["南海观音像是标志性景点","建议住岛上一晚"]', 1),
('千岛湖', 1, '千岛湖位于浙江省杭州市淳安县，因湖中有1078个岛屿而得名。湖水清澈见底，被誉为天下第一秀水。是国家一级水体，也是农夫山泉的水源地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211436953016.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211406515643.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211376351319.jpg"]', '浙江省杭州市淳安县千岛湖镇', '杭州', '浙江', '淳安县', '5A', 119.05, 29.6, '08:00-17:00', 150.00, 1, '1-2天', '4月-10月', '情侣,朋友,家庭', 4.23, 56908, 7751, 986, '["湖泊","千岛","游船","秀水"]', '["自然风光","湖泊","度假"]', '0571-64831078', '["建议乘船游览中心湖区","鱼头是当地特色美食"]', 1),
('雁荡山', 1, '雁荡山位于浙江省温州市乐清市，是中国十大名山之一。以奇峰怪石、飞瀑流泉著称，灵峰夜景和大龙湫瀑布是其标志性景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211443925316.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211435831272.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211442163308.jpg"]', '浙江省温州市乐清市雁荡镇', '温州', '浙江', '乐清市', '5A', 121.07, 28.38, '08:00-17:00', 50.00, 1, '1-2天', '4月-10月', '独自,朋友', 4.03, 53766, 2669, 4905, '["奇峰","瀑布","夜景","地质"]', '["自然风光","山岳","地质"]', '0577-62243235', '["灵峰夜景不可错过","大龙湫瀑布雨季最壮观"]', 1),
('横店影视城', 3, '横店影视城位于浙江省金华市东阳市，是全球规模最大的影视拍摄基地。拥有秦王宫、清明上河图、明清宫苑等多个拍摄基地，被誉为中国好莱坞。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211441259842.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211421184178.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211421184169.jpg"]', '浙江省金华市东阳市横店镇', '金华', '浙江', '东阳市', '5A', 120.32, 29.17, '08:00-17:00', 190.00, 1, '1-2天', '1月-12月', '朋友,家庭', 4.22, 10646, 12546, 3625, '["影视城","拍摄基地","明星","古装"]', '["主题乐园","影视","体验"]', '0579-86547777', '["建议购买联票","运气好可以看到明星拍戏"]', 1),
('西塘古镇', 2, '西塘古镇位于浙江省嘉兴市嘉善县，是古代吴越文化的发祥地之一。以桥多、弄多、廊棚多为特色，烟雨长廊是其标志性景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211397084101.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211438422676.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211367594688.jpg"]', '浙江省嘉兴市嘉善县西塘镇南苑路258号', '嘉兴', '浙江', '嘉善县', '5A', 120.893, 30.946, '全天开放', 95.00, 1, '1天', '3月-5月,9月-11月', '独自,情侣,朋友', 4.27, 31224, 6858, 2086, '["水乡","古镇","廊棚","夜景"]', '["历史文化","古镇","水乡"]', '0573-84567890', '["夜景比白天更有韵味","送子来凤桥是经典打卡点"]', 1),
('黄山', 1, '黄山位于安徽省黄山市，是中国十大名山之一。以奇松、怪石、云海、温泉四绝著称于世。1990年被列为世界文化与自然双重遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211527389779.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211497111929.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211480449343.jpg"]', '安徽省黄山市黄山区汤口镇', '黄山', '安徽', '黄山区', '5A', 118.167, 30.133, '06:00-17:30', 190.00, 1, '1-2天', '4月-11月', '独自,朋友,家庭', 3.97, 64628, 6726, 3419, '["世界遗产","奇松","怪石","云海"]', '["自然风光","名山","世界遗产"]', '0559-5561111', '["建议住山顶看日出云海","西海大峡谷是精华"]', 1),
('宏村', 2, '宏村位于安徽省黄山市黟县，是皖南古村落的代表。整个村落呈牛形布局，南湖、月沼是其标志性景观。2000年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211563549355.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211562023170.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211432040832.jpg"]', '安徽省黄山市黟县宏村镇', '黄山', '安徽', '黟县', '5A', 117.983, 30.029, '全天开放', 104.00, 1, '3-4小时', '3月-5月,9月-11月', '独自,情侣,朋友', 4.48, 30526, 12753, 4026, '["世界文化遗产","徽派建筑","古村落","写生"]', '["历史文化","古村落","世界遗产"]', '0559-5541158', '["清晨南湖倒影最美","建议与西递联游"]', 1),
('九华山', 5, '九华山位于安徽省池州市青阳县，是中国佛教四大名山之一，地藏菩萨的道场。山上寺院林立，佛教文化底蕴深厚，有莲花佛国之称。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211431374351.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211400749680.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211404672423.jpg"]', '安徽省池州市青阳县九华山镇', '池州', '安徽', '青阳县', '5A', 117.81, 30.48, '全天开放', 160.00, 1, '1-2天', '3月-11月', '独自,家庭', 3.97, 51955, 6560, 4741, '["佛教名山","地藏菩萨","朝圣","云海"]', '["宗教寺庙","名山","朝圣"]', '0566-2831288', '["百岁宫和化城寺是必去景点","建议住山上"]', 1),
('西递古村', 2, '西递古村位于安徽省黄山市黟县，始建于北宋年间，保存了大量明清时期的徽派建筑。2000年与宏村一起被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211353494379.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211363235840.jpeg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211363426618.jpg"]', '安徽省黄山市黟县西递镇', '黄山', '安徽', '黟县', '5A', 117.94, 30.11, '07:00-17:30', 104.00, 1, '2-3小时', '3月-5月,9月-11月', '独自,情侣,朋友', 4.95, 64807, 8041, 839, '["世界文化遗产","徽派建筑","古村落","牌坊"]', '["历史文化","古村落","世界遗产"]', '0559-5154030', '["胡文光牌坊是标志性建筑","建议清晨游览人少"]', 1),
('天柱山', 1, '天柱山位于安徽省安庆市潜山市，因主峰天柱峰如柱擎天而得名。是世界地质公园，以雄奇灵秀著称，有江淮第一山之美誉。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211488306461.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211310005427.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211310087607.jpg"]', '安徽省安庆市潜山市天柱山路112号', '安庆', '安徽', '潜山市', '5A', 116.44, 30.73, '07:00-17:30', 130.00, 1, '1天', '4月-10月', '独自,朋友', 4.92, 30935, 8458, 3403, '["花岗岩","奇峰","地质公园","古南岳"]', '["自然风光","山岳","地质"]', '0556-8146073', '["天柱峰和飞来石是必看景点","建议穿登山鞋"]', 1),
('鼓浪屿', 4, '鼓浪屿位于福建省厦门市，是一座面积1.88平方公里的小岛。岛上建筑风格多样，有万国建筑博览之称。2017年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211430216280.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211444735524.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211444958362.jpg"]', '福建省厦门市思明区鼓浪屿', '厦门', '福建', '思明区', '5A', 118.065, 24.449, '全天开放', 0.00, 0, '1天', '3月-5月,10月-12月', '独自,情侣,朋友', 4.83, 50759, 4552, 216, '["世界文化遗产","万国建筑","钢琴之岛","文艺"]', '["城市观光","海岛","文艺"]', '0592-2060777', '["建议提前预约船票","日光岩是最高点可俯瞰全岛"]', 1),
('武夷山', 1, '武夷山位于福建省南平市，是世界文化与自然双重遗产。以丹霞地貌著称，九曲溪竹筏漂流是经典体验。也是大红袍等名茶的产地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211500796593.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211380989012.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211396960034.jpg"]', '福建省南平市武夷山市武夷山国家旅游度假区', '南平', '福建', '武夷山市', '5A', 117.96, 27.65, '06:30-18:00', 140.00, 1, '1-2天', '3月-11月', '独自,朋友,家庭', 4.63, 67814, 1549, 4749, '["世界遗产","丹霞","九曲溪","大红袍"]', '["自然风光","山岳","世界遗产"]', '0599-5131890', '["九曲溪竹筏漂流是必体验项目","天游峰是精华景点"]', 1),
('福建土楼', 2, '福建土楼分布在福建省龙岩市和漳州市，是客家人独特的民居建筑。以圆形和方形为主，规模宏大，结构精巧。2008年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211430696985.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG41N2149970157.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375302038.jpg"]', '福建省龙岩市永定区湖坑镇洪坑村', '龙岩', '福建', '永定区', '5A', 116.93, 24.66, '08:00-18:00', 90.00, 1, '1天', '3月-11月', '独自,朋友,家庭', 4.09, 60742, 16811, 2731, '["世界文化遗产","客家","土楼","民居"]', '["历史文化","民居","世界遗产"]', '0597-5532888', '["承启楼是最具代表性的圆楼","建议请当地导游讲解"]', 1),
('泰宁大金湖', 1, '泰宁大金湖位于福建省三明市泰宁县，是世界地质公园的核心景区。湖面碧波荡漾，两岸丹霞地貌壮观，有百里湖山灵冠天下之美誉。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211547490278.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211544297907.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211540547384.jpg"]', '福建省三明市泰宁县金湖路', '三明', '福建', '泰宁县', '5A', 117.17, 26.9, '07:30-15:00', 80.00, 1, '4-6小时', '4月-11月', '朋友,家庭', 3.95, 16134, 13618, 2465, '["丹霞","湖泊","地质公园","游船"]', '["自然风光","湖泊","地质"]', '0598-7866355', '["建议乘船游览","甘露岩寺是必看景点"]', 1),
('三坊七巷', 2, '三坊七巷位于福州市中心，是中国保存最完好的明清古建筑街区之一。这里走出了林则徐、严复、冰心等众多名人，被誉为中国城市里坊制度的活化石。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211516481692.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211501272246.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211521916989.jpg"]', '福建省福州市鼓楼区南后街', '福州', '福建', '鼓楼区', '5A', 119.291, 26.083, '全天开放', 0.00, 0, '3-4小时', '1月-12月', '独自,情侣,朋友', 4.13, 65198, 15747, 4682, '["古建筑","名人故居","历史街区","美食"]', '["历史文化","古建筑","名人"]', '0591-87675791', '["林则徐纪念馆值得参观","永和鱼丸是特色小吃"]', 1),
('庐山', 1, '庐山位于江西省九江市，以雄、奇、险、秀闻名于世。自古以来就是文人墨客的钟爱之地，李白的飞流直下三千尺即描写庐山瀑布。1996年被列为世界文化景观遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211410759990.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211375307438.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211390842623.jpeg"]', '江西省九江市庐山市牯岭镇', '九江', '江西', '庐山市', '5A', 115.97, 29.56, '全天开放', 160.00, 1, '2-3天', '5月-10月', '独自,朋友,家庭', 4.89, 55860, 9118, 3570, '["世界遗产","避暑","瀑布","云雾"]', '["自然风光","名山","世界遗产"]', '0792-8296565', '["建议住牯岭镇","含鄱口看日出是经典体验"]', 1),
('婺源', 7, '婺源位于江西省上饶市，被誉为中国最美乡村。春季油菜花盛开时，粉墙黛瓦的徽派建筑掩映在金黄花海中，构成一幅绝美的田园画卷。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211430374798.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211468313003.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211418406589.jpg"]', '江西省上饶市婺源县', '上饶', '江西', '婺源县', '5A', 117.86, 29.25, '07:00-17:30', 210.00, 1, '2-3天', '3月-4月,10月-11月', '独自,情侣,朋友', 4.77, 32887, 11353, 4246, '["油菜花","徽派建筑","乡村","摄影"]', '["乡村田园","赏花","摄影"]', '0793-7410999', '["3月中旬-4月初油菜花最盛","篁岭晒秋也很有特色"]', 1),
('三清山', 1, '三清山位于江西省上饶市，因玉京、玉虚、玉华三峰如三清列坐其巅而得名。以花岗岩峰林地貌著称，2008年被列为世界自然遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211445395057.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211380782763.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211391558937.jpg"]', '江西省上饶市玉山县三清乡', '上饶', '江西', '玉山县', '5A', 118.06, 28.91, '08:00-17:00', 150.00, 1, '1-2天', '4月-6月,9月-11月', '独自,朋友', 4.36, 44171, 16236, 3648, '["世界自然遗产","花岗岩","云海","杜鹃"]', '["自然风光","山岳","世界遗产"]', '0793-2407998', '["南清园和西海岸是精华线路","建议住山上看日出"]', 1),
('井冈山', 8, '井冈山位于江西省吉安市，是中国革命的摇篮。1927年毛泽东在此创建了第一个农村革命根据地，开辟了农村包围城市的革命道路。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211435754693.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211523403616.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211435754696.jpg"]', '江西省吉安市井冈山市茨坪镇', '吉安', '江西', '井冈山市', '5A', 114.17, 26.58, '08:00-17:00', 165.00, 1, '2-3天', '4月-10月', '独自,朋友,家庭', 3.84, 40998, 1585, 4118, '["红色旅游","革命圣地","杜鹃花","自然风光"]', '["红色旅游","革命","自然"]', '0796-6552626', '["黄洋界和茨坪是必去景点","4-5月杜鹃花盛开"]', 1),
('龙虎山', 5, '龙虎山位于江西省鹰潭市，是中国道教发祥地。张天师在此修道炼丹，道教文化源远流长。丹霞地貌与道教文化交相辉映，2010年被列为世界自然遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211444471933.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211389881625.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211404512638.jpg"]', '江西省鹰潭市贵溪市龙虎山大道1号', '鹰潭', '江西', '贵溪市', '5A', 116.99, 28.08, '07:30-17:30', 260.00, 1, '1-2天', '3月-11月', '独自,朋友', 4.08, 17830, 12021, 2194, '["道教","丹霞","悬棺","天师府"]', '["宗教寺庙","丹霞","道教"]', '0701-6658888', '["悬棺表演是特色项目","竹筏漂流可欣赏丹霞地貌"]', 1),
('泰山', 1, '泰山位于山东省泰安市，是五岳之首。自古以来就是帝王封禅之地，有天下第一山之称。1987年被列为世界文化与自然双重遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211430655880.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211536256904.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211488624492.jpg"]', '山东省泰安市泰山区红门路', '泰安', '山东', '泰山区', '5A', 117.101, 36.256, '全天开放', 115.00, 1, '1-2天', '4月-11月', '独自,朋友,家庭', 4.85, 30812, 4715, 4601, '["五岳之首","世界遗产","日出","封禅"]', '["自然风光","名山","世界遗产"]', '0538-6228484', '["建议夜爬看日出","十八盘是最陡路段"]', 1),
('青岛栈桥', 4, '栈桥位于青岛市市南区海滨，始建于1892年，是青岛最早的军事专用人工码头。桥身从海岸探入弯月般的青岛湾深处，尽头建有回澜阁，是青岛的标志性建筑。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211554468115.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211375363263.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375057218.jpg"]', '山东省青岛市市南区太平路14号', '青岛', '山东', '市南区', NULL, 120.318, 36.062, '全天开放', 0.00, 0, '1小时', '4月-10月', '独自,情侣,朋友,家庭', 4.76, 76594, 10769, 271, '["地标","海滨","栈桥","回澜阁"]', '["城市观光","海滨","地标"]', '0532-82884548', '["日落时分景色最美","可与周边八大关联游"]', 1),
('曲阜三孔', 2, '曲阜三孔指孔庙、孔府、孔林，位于山东省曲阜市。是纪念孔子、推崇儒学的表征，1994年被列为世界文化遗产。孔庙是中国现存规模仅次于故宫的古建筑群。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211373790001.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211352970356.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211331142460.jpg"]', '山东省济宁市曲阜市神道路', '济宁', '山东', '曲阜市', '5A', 116.986, 35.597, '08:00-17:30', 140.00, 1, '4-6小时', '3月-5月,9月-11月', '独自,朋友,家庭', 4.08, 26548, 7874, 3475, '["世界文化遗产","孔子","儒学","古建筑"]', '["历史文化","儒学","世界遗产"]', '0537-4712269', '["建议请导游讲解","孔庙大成殿是精华"]', 1),
('崂山', 1, '崂山位于青岛市东部，是中国海岸线上第一高峰。山海相连，云雾缭绕，有海上第一名山之称。也是道教名山，太清宫历史悠久。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211415006470.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211406510862.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380792847.jpg"]', '山东省青岛市崂山区梅岭路29号', '青岛', '山东', '崂山区', '5A', 120.62, 36.16, '07:00-18:00', 90.00, 1, '1天', '4月-10月', '独自,朋友,家庭', 4.63, 28925, 11229, 1011, '["海上名山","道教","崂山道士","海景"]', '["自然风光","山岳","道教"]', '0532-96616', '["太清游览区是精华","崂山绿茶值得品尝"]', 1),
('蓬莱阁', 2, '蓬莱阁位于山东省烟台市蓬莱区，始建于北宋嘉祐六年。素有人间仙境之称，是中国古代四大名楼之一。八仙过海的传说即源于此。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211457823540.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211389543343.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211544375545.jpg"]', '山东省烟台市蓬莱区迎宾路7号', '烟台', '山东', '蓬莱区', '5A', 120.76, 37.81, '07:30-18:00', 100.00, 1, '3-4小时', '4月-10月', '独自,朋友,家庭', 4.88, 28569, 15178, 1089, '["人间仙境","八仙过海","海市蜃楼","古建筑"]', '["历史文化","名楼","传说"]', '0535-5621111', '["运气好可看到海市蜃楼","建议与长岛联游"]', 1),
('龙门石窟', 2, '龙门石窟位于河南省洛阳市，始凿于北魏孝文帝年间，历经400余年营造。现存窟龛2345个，造像10万余尊。2000年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211397895821.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211377774607.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211426056674.jpg"]', '河南省洛阳市洛龙区龙门镇龙门大道', '洛阳', '河南', '洛龙区', '5A', 112.47, 34.56, '08:00-18:30', 90.00, 1, '3-4小时', '4月-10月', '独自,朋友,家庭', 4.4, 29726, 16153, 947, '["世界文化遗产","石窟","佛教","卢舍那大佛"]', '["历史文化","石窟","世界遗产"]', '0379-65980972', '["卢舍那大佛是精华","建议从西山石窟开始游览"]', 1),
('少林寺', 5, '少林寺位于河南省登封市嵩山五乳峰下，始建于北魏太和十九年。是中国佛教禅宗祖庭和少林武术的发源地，2010年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211536067279.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211389125790.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211442917217.jpg"]', '河南省郑州市登封市嵩山少林寺', '郑州', '河南', '登封市', '5A', 112.934, 34.508, '07:00-18:00', 80.00, 1, '3-4小时', '3月-5月,9月-11月', '独自,朋友,家庭', 3.9, 57134, 6781, 4480, '["世界文化遗产","少林武术","禅宗","功夫"]', '["宗教寺庙","武术","世界遗产"]', '0371-62745166', '["武术表演时间需提前确认","塔林是历代高僧墓塔群"]', 1),
('清明上河园', 3, '清明上河园位于河南省开封市，是以宋代张择端的《清明上河图》为蓝本建造的大型宋代文化主题公园。园内再现了北宋东京汴梁的繁华景象。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211515208930.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211402072834.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211402072838.jpg"]', '河南省开封市龙亭区龙亭西路5号', '开封', '河南', '龙亭区', '5A', 114.36, 34.8, '09:00-22:00', 120.00, 1, '1天', '3月-11月', '朋友,家庭', 4.4, 44622, 3326, 3202, '["宋文化","清明上河图","主题公园","表演"]', '["主题乐园","文化","宋代"]', '0371-25663819', '["各种表演节目精彩纷呈","建议购买全天票"]', 1),
('云台山', 1, '云台山位于河南省焦作市修武县，以独特的北方岩溶地貌著称。红石峡是其精华景点，峡谷内红色岩壁与碧绿潭水相映成趣。2004年被列为世界地质公园。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211435280641.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211443798148.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211381688279.jpg"]', '河南省焦作市修武县岸上乡', '焦作', '河南', '修武县', '5A', 113.38, 35.35, '06:00-18:30', 120.00, 1, '1-2天', '3月-11月', '独自,朋友,家庭', 3.86, 16427, 14344, 2383, '["世界地质公园","红石峡","瀑布","峡谷"]', '["自然风光","峡谷","地质"]', '0391-7709300', '["红石峡是必游景点","建议避开节假日"]', 1),
('殷墟', 2, '殷墟位于河南省安阳市，是中国商朝后期都城遗址。出土了大量甲骨文和青铜器，是中国考古学的圣地。2006年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211439654238.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211496390921.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211489455723.jpg"]', '河南省安阳市殷都区殷墟路1号', '安阳', '河南', '殷都区', '5A', 114.31, 36.13, '08:00-17:30', 70.00, 1, '2-3小时', '3月-11月', '独自,朋友', 4.92, 76860, 1187, 1650, '["世界文化遗产","甲骨文","商朝","考古"]', '["历史文化","考古","世界遗产"]', '0372-3161022', '["妇好墓和甲骨文展厅是重点","建议请导游讲解"]', 1),
('武当山', 5, '武当山位于湖北省十堰市，是中国道教名山。明代大修武当山，建成了规模宏大的道教建筑群。1994年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211446124168.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211458516465.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211376095427.jpg"]', '湖北省十堰市丹江口市武当山旅游经济特区', '十堰', '湖北', '丹江口市', '5A', 111.0, 32.4, '07:00-17:30', 235.00, 1, '1-2天', '3月-5月,9月-11月', '独自,朋友', 4.85, 34167, 10861, 4846, '["世界文化遗产","道教","太极","金顶"]', '["宗教寺庙","名山","世界遗产"]', '0719-5668567', '["金顶日出不可错过","建议住山上"]', 1),
('黄鹤楼', 2, '黄鹤楼位于湖北省武汉市武昌区蛇山之巅，始建于三国时期。是中国古代四大名楼之一，因崔颢的诗昔人已乘黄鹤去而闻名天下。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211429415072.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211391025815.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211377054078.jpg"]', '湖北省武汉市武昌区蛇山西山坡特1号', '武汉', '湖北', '武昌区', '5A', 114.302, 30.549, '08:00-18:00', 70.00, 1, '1-2小时', '3月-5月,9月-11月', '独自,情侣,朋友,家庭', 4.47, 64814, 9109, 1635, '["四大名楼","诗词","长江","地标"]', '["历史文化","名楼","地标"]', '027-88875096', '["登楼可远眺长江大桥","夜景灯光秀值得一看"]', 1),
('三峡大坝', 4, '三峡大坝位于湖北省宜昌市，是世界上规模最大的水电站。大坝全长2335米，坝顶高程185米，是中国现代工程的伟大成就。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211379874960.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392450806.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211389539661.jpg"]', '湖北省宜昌市夷陵区三斗坪镇', '宜昌', '湖北', '夷陵区', '5A', 111.0, 30.83, '08:00-17:00', 0.00, 0, '3-4小时', '4月-11月', '独自,朋友,家庭', 3.85, 78931, 4213, 393, '["水利工程","长江","大坝","现代奇迹"]', '["城市观光","工程","科技"]', '0717-6763498', '["坛子岭是最佳观景点","建议提前网上预约"]', 1),
('神农架', 1, '神农架位于湖北省西部，是中国唯一以林区命名的行政区。拥有完好的亚热带森林生态系统，是金丝猴等珍稀动物的栖息地。2016年被列为世界自然遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211419211884.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211396219734.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211401259479.jpg"]', '湖北省神农架林区木鱼镇', '神农架', '湖北', '神农架林区', '5A', 110.68, 31.75, '08:00-17:30', 269.00, 1, '2-3天', '5月-10月', '独自,朋友', 4.9, 36017, 14292, 4390, '["世界自然遗产","原始森林","金丝猴","野人"]', '["自然风光","森林","世界遗产"]', '0719-3456999', '["神农顶和大九湖是精华景点","注意高海拔防寒"]', 1),
('恩施大峡谷', 1, '恩施大峡谷位于湖北省恩施市，全长108公里，是清江流域最美丽的一段。峡谷内绝壁、瀑布、溶洞等地质景观丰富，一炷香是其标志性景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211389886605.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211412413460.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211389886596.jpg"]', '湖北省恩施市屯堡乡', '恩施', '湖北', '恩施市', '5A', 109.25, 30.46, '08:00-16:00', 170.00, 1, '1天', '4月-10月', '独自,朋友', 4.49, 73206, 9906, 578, '["峡谷","绝壁","一炷香","地缝"]', '["自然风光","峡谷","地质"]', '0718-8542333', '["七星寨和云龙地缝是核心景区","建议穿登山鞋"]', 1),
('张家界国家森林公园', 1, '张家界国家森林公园位于湖南省张家界市，是中国第一个国家森林公园。以独特的石英砂岩峰林地貌著称，电影《阿凡达》中悬浮山的原型即取材于此。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211564206228.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211394385701.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211442920250.jpg"]', '湖南省张家界市武陵源区', '张家界', '湖南', '武陵源区', '5A', 110.471, 29.325, '07:00-18:00', 225.00, 1, '2-3天', '4月-10月', '独自,朋友,家庭', 4.41, 40824, 15422, 3959, '["世界自然遗产","峰林","阿凡达","玻璃桥"]', '["自然风光","峰林","世界遗产"]', '0744-5712189', '["天门山玻璃栈道刺激","建议至少安排两天"]', 1),
('凤凰古城', 2, '凤凰古城位于湖南省湘西土家族苗族自治州，始建于清康熙年间。沱江穿城而过，吊脚楼沿江而建，是中国最美的小城之一。沈从文的故乡。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211455310047.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211411325938.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211328187295.jpg"]', '湖南省湘西州凤凰县凤凰古城', '湘西', '湖南', '凤凰县', '4A', 109.6, 27.95, '全天开放', 0.00, 0, '1-2天', '3月-11月', '独自,情侣,朋友', 4.17, 71584, 3870, 3418, '["古城","吊脚楼","沱江","苗族"]', '["历史文化","古城","民族"]', '0743-3502059', '["夜景比白天更美","沱江泛舟是经典体验"]', 1),
('岳麓山', 1, '岳麓山位于湖南省长沙市，是南岳衡山72峰的最后一峰。山上有岳麓书院、爱晚亭等著名景点，秋季红叶满山，是长沙的城市名片。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211545536834.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392906763.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211384452734.jpg"]', '湖南省长沙市岳麓区登高路58号', '长沙', '湖南', '岳麓区', '5A', 112.94, 28.19, '全天开放', 0.00, 0, '3-4小时', '10月-12月', '独自,朋友,家庭', 4.28, 58440, 3039, 1600, '["岳麓书院","爱晚亭","红叶","名山"]', '["自然风光","书院","赏秋"]', '0731-88825011', '["岳麓书院是必看景点","秋季红叶最美"]', 1),
('衡山', 5, '衡山位于湖南省衡阳市，是五岳之南岳。以壮美的自然风光和悠久的宗教文化著称，祝融峰是其最高峰。南岳大庙是中国南方最大的古建筑群之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211399792281.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211439144636.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211383981646.jpg"]', '湖南省衡阳市南岳区南岳镇', '衡阳', '湖南', '南岳区', '5A', 112.69, 27.25, '全天开放', 80.00, 1, '1-2天', '3月-11月', '独自,朋友,家庭', 4.38, 68223, 11562, 2974, '["五岳","南岳","祝融峰","宗教"]', '["宗教寺庙","名山","五岳"]', '0734-5673377', '["祝融峰看日出是经典体验","南岳大庙值得参观"]', 1),
('韶山', 8, '韶山位于湖南省湘潭市，是毛泽东同志的故乡。毛泽东故居、毛泽东纪念馆等红色景点集中于此，是中国重要的红色旅游目的地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211450055745.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211398907527.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211500246428.jpg"]', '湖南省湘潭市韶山市韶山冲', '湘潭', '湖南', '韶山市', '5A', 112.53, 27.92, '08:00-17:00', 0.00, 0, '3-4小时', '1月-12月', '独自,朋友,家庭', 4.37, 31052, 5153, 420, '["毛泽东故居","红色旅游","革命圣地"]', '["红色旅游","故居","革命"]', '0731-55685157', '["毛泽东故居免费但需排队","建议请导游讲解"]', 1),
('广州塔', 4, '广州塔位于广州市海珠区，塔高600米，是中国第一高塔。塔身造型独特，被昵称为小蛮腰。塔上设有摩天轮、跳楼机等极限项目，是广州的标志性建筑。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211440553570.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211434784667.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434784663.jpg"]', '广东省广州市海珠区阅江西路222号', '广州', '广东', '海珠区', '4A', 113.324, 23.106, '09:00-23:00', 150.00, 1, '2-3小时', '1月-12月', '独自,情侣,朋友', 4.58, 41219, 16987, 2607, '["地标","观光塔","夜景","小蛮腰"]', '["城市观光","地标","观景"]', '020-89338222', '["建议傍晚上塔看日落和夜景","摩天轮需另购票"]', 1),
('丹霞山', 1, '丹霞山位于广东省韶关市，是世界自然遗产、世界地质公园。以赤壁丹崖为特色，是丹霞地貌的命名地。阳元石和阴元石是其标志性景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211448423119.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211375800133.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211340734122.jpg"]', '广东省韶关市仁化县丹霞山镇', '韶关', '广东', '仁化县', '5A', 113.74, 25.02, '08:00-18:00', 100.00, 1, '1-2天', '3月-5月,9月-12月', '独自,朋友', 4.47, 74218, 2989, 3748, '["世界自然遗产","丹霞地貌","地质公园"]', '["自然风光","丹霞","世界遗产"]', '0751-6291683', '["长老峰看日出是经典体验","建议安排两天"]', 1),
('开平碉楼', 2, '开平碉楼位于广东省江门市开平市，是中国乡土建筑的一个特殊类型。融合了中西建筑艺术，2007年被列为世界文化遗产。现存碉楼1833座。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211446554260.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211382254748.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211392261673.jpg"]', '广东省江门市开平市塘口镇自力村', '江门', '广东', '开平市', '5A', 112.68, 22.38, '08:30-17:30', 80.00, 1, '1天', '3月-11月', '独自,朋友', 3.88, 58751, 11670, 1776, '["世界文化遗产","碉楼","中西合璧","华侨"]', '["历史文化","建筑","世界遗产"]', '0750-2679788', '["自力村和立园是精华景点","建议租车游览"]', 1),
('长隆旅游度假区', 3, '长隆旅游度假区位于广州市番禺区，包含长隆欢乐世界、长隆野生动物世界、长隆水上乐园等多个主题公园。是中国最大的综合性主题旅游度假区之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211398842679.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211427149934.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211366716093.jpg"]', '广东省广州市番禺区汉溪大道东299号', '广州', '广东', '番禺区', '5A', 113.33, 23.0, '09:30-18:00', 250.00, 1, '1天', '1月-12月', '朋友,家庭', 4.03, 63996, 10879, 1905, '["主题乐园","动物园","水上乐园","亲子"]', '["主题乐园","亲子","娱乐"]', '020-84786600', '["建议分两天玩不同园区","野生动物世界适合亲子"]', 1),
('深圳世界之窗', 3, '世界之窗位于深圳市南山区，是一个把世界奇观、历史遗迹、自然风光微缩复制的主题公园。园内有130个世界著名景观的微缩模型。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211420093299.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211328876656.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211337700559.jpg"]', '广东省深圳市南山区深南大道9037号', '深圳', '广东', '南山区', '5A', 113.975, 22.535, '09:00-21:30', 200.00, 1, '1天', '1月-12月', '朋友,家庭', 3.8, 75625, 10942, 4682, '["微缩景观","世界文化","主题公园"]', '["主题乐园","文化","观光"]', '0755-26608000', '["建议购买夜场票看灯光秀","园区较大建议穿舒适鞋"]', 1),
('珠海长隆海洋王国', 3, '珠海长隆海洋王国位于珠海市横琴新区，是全球最大的海洋主题公园之一。拥有世界最大的海洋鱼类展览馆和亚洲第一台飞行过山车。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346870014.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211313609599.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346870018.jpg"]', '广东省珠海市香洲区横琴岛富祥湾', '珠海', '广东', '香洲区', '5A', 113.54, 22.1, '10:00-20:30', 395.00, 1, '1天', '1月-12月', '情侣,朋友,家庭', 4.7, 17453, 1989, 2386, '["海洋公园","鲸鲨","过山车","烟花"]', '["主题乐园","海洋","亲子"]', '0756-2993399', '["鲸鲨馆和烟花表演是亮点","建议工作日前往人少"]', 1),
('桂林漓江', 1, '漓江是桂林山水的精华所在，从桂林到阳朔83公里的水程中，奇峰夹岸，碧水萦回。桂林山水甲天下的美誉即源于此。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211378179304.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG41N2223147388.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211270218768.jpg"]', '广西壮族自治区桂林市灵川县', '桂林', '广西', '灵川县', '5A', 110.29, 25.27, '08:00-17:00', 210.00, 1, '4-6小时', '4月-10月', '独自,情侣,朋友,家庭', 4.41, 8956, 16565, 1652, '["山水","漓江","竹筏","喀斯特"]', '["自然风光","山水","游船"]', '0773-2825502', '["建议乘竹筏漂流","20元人民币背景图在兴坪"]', 1),
('德天瀑布', 1, '德天瀑布位于广西崇左市大新县，是亚洲第一、世界第四大跨国瀑布。瀑布横跨中越两国，宽200多米，气势磅礴，景色壮观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211430424104.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211379180354.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211328292232.jpg"]', '广西壮族自治区崇左市大新县硕龙镇', '崇左', '广西', '大新县', '5A', 107.07, 22.85, '08:00-18:00', 80.00, 1, '3-4小时', '6月-11月', '独自,朋友,家庭', 4.48, 68839, 6418, 4181, '["跨国瀑布","中越边境","壮观","自然"]', '["自然风光","瀑布","边境"]', '0771-3636999', '["雨季水量大最壮观","可乘竹筏近距离观赏"]', 1),
('龙脊梯田', 7, '龙脊梯田位于广西桂林市龙胜各族自治县，始建于元朝，距今已有700多年历史。梯田从山脚盘绕到山顶，层层叠叠，气势恢宏，被誉为天下一绝。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211403167003.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211420309932.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211405228501.jpg"]', '广西壮族自治区桂林市龙胜县龙脊镇', '桂林', '广西', '龙胜县', '4A', 110.11, 25.8, '08:00-18:00', 80.00, 1, '1天', '4月-5月,9月-10月', '独自,情侣,朋友', 4.86, 11005, 1520, 2052, '["梯田","壮族","苗族","摄影"]', '["乡村田园","梯田","民族"]', '0773-7583188', '["灌水期和收割期景色最美","建议住寨子里体验民族风情"]', 1),
('北海银滩', 6, '北海银滩位于广西北海市，以滩长平、沙细白、水温净、浪柔软而著称。沙滩均由高品位的石英砂堆积而成，在阳光照射下泛出银光，故称银滩。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434754635.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211434754639.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434754636.jpg"]', '广西壮族自治区北海市银海区', '北海', '广西', '银海区', '4A', 109.12, 21.44, '全天开放', 0.00, 0, '3-4小时', '4月-11月', '情侣,朋友,家庭', 4.32, 64558, 3670, 1590, '["银滩","海滨","沙滩","日落"]', '["海滨海岛","沙滩","度假"]', '0779-3880011', '["免费开放","日落时分景色最美"]', 1),
('阳朔西街', 4, '阳朔西街位于广西桂林市阳朔县，是阳朔最古老最繁华的街道。全长约800米，中西文化交融，是背包客和文艺青年的聚集地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211373882450.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211373675416.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211373675417.jpg"]', '广西壮族自治区桂林市阳朔县西街', '桂林', '广西', '阳朔县', NULL, 110.496, 24.773, '全天开放', 0.00, 0, '2-3小时', '3月-11月', '独自,情侣,朋友', 4.82, 60412, 1055, 3815, '["古街","美食","酒吧","文艺"]', '["城市观光","美食","休闲"]', '0773-8822312', '["啤酒鱼是当地特色美食","夜晚氛围最好"]', 1),
('三亚亚龙湾', 6, '亚龙湾位于海南省三亚市东南部，被誉为天下第一湾。海水清澈透明，沙滩洁白细腻，是中国最南端的热带滨海旅游度假区。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346994125.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211332416042.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346994124.jpg"]', '海南省三亚市吉阳区亚龙湾', '三亚', '海南', '吉阳区', '4A', 109.64, 18.19, '全天开放', 0.00, 0, '1天', '10月-次年4月', '情侣,朋友,家庭', 3.96, 48011, 3135, 1502, '["海湾","沙滩","潜水","热带"]', '["海滨海岛","度假","水上运动"]', '0898-88568899', '["冬季是最佳旅游季节","可体验潜水和海上项目"]', 1),
('蜈支洲岛', 6, '蜈支洲岛位于三亚市海棠湾内，是海南岛周围为数不多的有淡水资源和丰富植被的小岛。海水能见度极高，是中国最佳潜水基地之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211395508093.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211400535914.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211406813979.jpg"]', '海南省三亚市海棠区蜈支洲岛', '三亚', '海南', '海棠区', '5A', 109.76, 18.31, '08:00-17:30', 144.00, 1, '1天', '10月-次年5月', '情侣,朋友', 4.51, 24503, 13919, 3800, '["海岛","潜水","热带","珊瑚"]', '["海滨海岛","潜水","度假"]', '0898-88751258', '["潜水是必体验项目","建议避开台风季"]', 1),
('呀诺达雨林', 1, '呀诺达雨林文化旅游区位于海南省保亭县，是中国唯一地处北纬18度的热带雨林。园区内热带植物茂密，瀑布溪流纵横，负氧离子含量极高。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211416231650.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211416231680.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211416231679.jpg"]', '海南省保亭黎族苗族自治县三道镇', '保亭', '海南', '保亭县', '5A', 109.62, 18.38, '07:30-18:00', 170.00, 1, '4-6小时', '11月-次年4月', '朋友,家庭', 4.27, 10099, 14299, 3064, '["热带雨林","负氧离子","瀑布","探险"]', '["自然风光","雨林","探险"]', '0898-83883333', '["踏瀑戏水项目很刺激","建议穿防滑鞋"]', 1),
('南山文化旅游区', 5, '南山文化旅游区位于三亚市南山，以南海观音圣像为核心。108米高的海上观音像是世界上最大的白衣观音造像，是佛教文化与热带海滨风光的完美结合。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346951954.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211346951961.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211314743483.jpg"]', '海南省三亚市崖州区南山村', '三亚', '海南', '崖州区', '5A', 109.2, 18.29, '08:00-17:30', 129.00, 1, '4-6小时', '10月-次年4月', '独自,家庭', 3.88, 37993, 13360, 1336, '["南海观音","佛教","海滨","祈福"]', '["宗教寺庙","佛教","海滨"]', '0898-88837888', '["南海观音像是标志性景点","素斋自助餐值得体验"]', 1),
('洪崖洞', 4, '洪崖洞位于重庆市渝中区，是一处以巴渝传统建筑风格为主体的吊脚楼建筑群。夜晚灯火辉煌，酷似《千与千寻》中的场景，是重庆最热门的网红打卡地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211456175205.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211330132255.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211348811862.jpg"]', '重庆市渝中区嘉陵江滨江路88号', '重庆', '重庆', '渝中区', '4A', 106.584, 29.563, '全天开放', 0.00, 0, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 4.76, 57290, 7156, 2063, '["吊脚楼","夜景","网红","千与千寻"]', '["城市观光","夜景","美食"]', '023-63039995', '["夜景最佳观赏时间为20:00-22:00","建议从千厮门大桥拍全景"]', 1),
('武隆天生三桥', 1, '武隆天生三桥位于重庆市武隆区，是世界上最大的天生桥群。三座天然石拱桥横跨在峡谷之上，气势磅礴。2007年被列为世界自然遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434766109.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211434773838.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434766105.jpg"]', '重庆市武隆区仙女山镇', '重庆', '重庆', '武隆区', '5A', 107.76, 29.33, '08:30-16:30', 95.00, 1, '3-4小时', '3月-11月', '独自,朋友,家庭', 4.63, 31818, 12537, 2512, '["世界自然遗产","天生桥","喀斯特","变形金刚"]', '["自然风光","地质","世界遗产"]', '023-77794266', '["电影《变形金刚4》取景地","建议穿防滑鞋"]', 1),
('大足石刻', 2, '大足石刻位于重庆市大足区，始建于初唐，历经五代至南宋。以佛教题材为主，儒、道教造像并陈，是中国晚期石窟艺术的代表。1999年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211431058635.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211525570864.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211530431334.jpg"]', '重庆市大足区宝顶镇大足石刻路7号', '重庆', '重庆', '大足区', '5A', 105.7, 29.7, '08:30-18:00', 115.00, 1, '3-4小时', '3月-11月', '独自,朋友', 4.94, 56308, 5790, 3328, '["世界文化遗产","石刻","佛教","宋代"]', '["历史文化","石窟","世界遗产"]', '023-43722268', '["宝顶山石刻是精华","建议请导游讲解"]', 1),
('磁器口古镇', 2, '磁器口古镇位于重庆市沙坪坝区，始建于宋代。古镇依山而建，街道由石板铺成，两旁是明清风格的建筑。是体验重庆老城风貌和品尝地道小吃的好去处。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211396790109.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211396899306.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211377261375.jpg"]', '重庆市沙坪坝区磁器口古镇', '重庆', '重庆', '沙坪坝区', '4A', 106.45, 29.58, '全天开放', 0.00, 0, '3-4小时', '1月-12月', '独自,情侣,朋友,家庭', 4.96, 14917, 2224, 2330, '["古镇","小吃","老街","陪都"]', '["历史文化","古镇","美食"]', '023-65322661', '["陈麻花和毛血旺是特色","建议避开节假日"]', 1),
('九寨沟', 1, '九寨沟位于四川省阿坝藏族羌族自治州，以翠海、叠瀑、彩林、雪峰、藏情五绝闻名于世。1992年被列为世界自然遗产，水色斑斓，美不胜收。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211435251587.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211535156767.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211533004427.jpg"]', '四川省阿坝州九寨沟县漳扎镇', '阿坝', '四川', '九寨沟县', '5A', 103.92, 33.26, '08:30-17:00', 169.00, 1, '1天', '9月-11月', '独自,情侣,朋友,家庭', 4.04, 54220, 16938, 1902, '["世界自然遗产","彩林","海子","瀑布"]', '["自然风光","湖泊","世界遗产"]', '0837-7739753', '["秋季10月景色最美","建议网上提前预约"]', 1),
('峨眉山', 5, '峨眉山位于四川省乐山市，是中国佛教四大名山之一，普贤菩萨的道场。山势雄伟，景色秀丽，有峨眉天下秀之称。1996年被列为世界文化与自然双重遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211440770371.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211393071203.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211446366926.jpg"]', '四川省乐山市峨眉山市黄湾镇', '乐山', '四川', '峨眉山市', '5A', 103.33, 29.52, '全天开放', 160.00, 1, '2-3天', '3月-5月,9月-11月', '独自,朋友,家庭', 4.48, 75698, 10347, 3611, '["世界遗产","佛教名山","金顶","云海"]', '["宗教寺庙","名山","世界遗产"]', '0833-5090114', '["金顶看日出云海是经典体验","注意猴子抢食"]', 1),
('乐山大佛', 2, '乐山大佛位于四川省乐山市，开凿于唐代，高71米，是世界上最大的石刻弥勒佛坐像。大佛依凌云山栖霞峰临江峭壁凿造而成，气势恢宏。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211457503173.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211398364796.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211432467078.jpg"]', '四川省乐山市市中区凌云路2435号', '乐山', '四川', '市中区', '5A', 103.77, 29.54, '07:30-18:30', 80.00, 1, '3-4小时', '3月-11月', '独自,朋友,家庭', 4.37, 37435, 12719, 1444, '["世界遗产","大佛","唐代","石刻"]', '["历史文化","石刻","世界遗产"]', '0833-2302296', '["建议乘船远观大佛全貌","栈道排队时间较长"]', 1),
('稻城亚丁', 1, '稻城亚丁位于四川省甘孜藏族自治州，被誉为最后的香格里拉。三座神山（仙乃日、央迈勇、夏诺多吉）巍然耸立，雪山、冰湖、草甸构成绝美画卷。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211513086096.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211418156264.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211410540303.jpg"]', '四川省甘孜州稻城县香格里拉镇', '甘孜', '四川', '稻城县', '4A', 100.3, 28.43, '07:00-18:30', 146.00, 1, '2-3天', '4月-5月,9月-10月', '独自,朋友', 4.52, 34465, 6262, 334, '["雪山","冰湖","草甸","香格里拉"]', '["自然风光","雪山","高原"]', '0836-6966022', '["海拔较高注意高反","牛奶海和五色海是精华"]', 1),
('都江堰', 2, '都江堰位于四川省成都市都江堰市，始建于秦昭王末年，是世界上年代最久、唯一留存的以无坝引水为特征的水利工程。2000年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211557208303.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211436007169.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211378991523.jpg"]', '四川省成都市都江堰市公园路', '成都', '四川', '都江堰市', '5A', 103.61, 30.99, '08:00-18:00', 80.00, 1, '3-4小时', '3月-11月', '独自,朋友,家庭', 4.6, 33900, 4355, 694, '["世界文化遗产","水利工程","李冰","古代科技"]', '["历史文化","水利","世界遗产"]', '028-87293800', '["鱼嘴分水堤是核心景点","建议与青城山联游"]', 1),
('成都大熊猫繁育研究基地', 1, '成都大熊猫繁育研究基地位于成都市成华区，是世界上最大的大熊猫人工繁育基地。园区内模拟大熊猫的自然栖息环境，可近距离观赏大熊猫。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211392507172.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392507157.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211392507159.jpg"]', '四川省成都市成华区熊猫大道1375号', '成都', '四川', '成华区', '4A', 104.15, 30.74, '07:30-18:00', 55.00, 1, '3-4小时', '3月-5月,9月-11月', '朋友,家庭', 4.26, 63810, 9005, 4435, '["大熊猫","国宝","亲子","科普"]', '["自然风光","动物","亲子"]', '028-83510033', '["建议早上去看大熊猫活跃","月亮产房可看熊猫宝宝"]', 1),
('青城山', 5, '青城山位于四川省成都市都江堰市，是中国道教发祥地之一。全山林木青翠，四季常青，有青城天下幽之美誉。2000年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211437789613.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211552373799.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211452973038.jpg"]', '四川省成都市都江堰市青城山镇', '成都', '四川', '都江堰市', '5A', 103.57, 30.9, '08:00-17:00', 80.00, 1, '1天', '3月-11月', '独自,朋友,家庭', 3.94, 49981, 14089, 1628, '["世界文化遗产","道教","幽静","古建筑"]', '["宗教寺庙","名山","世界遗产"]', '028-87111907', '["前山道教文化浓厚","后山自然风光更好"]', 1),
('黄果树瀑布', 1, '黄果树瀑布位于贵州省安顺市，高77.8米，宽101米，是中国最大的瀑布，也是世界著名大瀑布之一。瀑布后有一水帘洞，可从内部观赏瀑布。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211402999923.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211382486187.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211379859571.jpg"]', '贵州省安顺市镇宁县黄果树镇', '安顺', '贵州', '镇宁县', '5A', 105.67, 25.99, '07:30-18:00', 160.00, 1, '4-6小时', '6月-10月', '独自,朋友,家庭', 4.41, 77133, 6505, 2080, '["瀑布","水帘洞","壮观","喀斯特"]', '["自然风光","瀑布","地质"]', '0851-33596132', '["雨季水量最大最壮观","水帘洞可从瀑布后方观赏"]', 1),
('荔波小七孔', 1, '荔波小七孔位于贵州省黔南州荔波县，因景区内一座七孔古桥而得名。集山、水、林、洞、湖、瀑为一体，被誉为地球腰带上的绿宝石。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211441698032.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211432125114.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211405742415.jpg"]', '贵州省黔南州荔波县', '黔南', '贵州', '荔波县', '5A', 107.88, 25.31, '07:30-16:30', 110.00, 1, '1天', '4月-10月', '独自,朋友,家庭', 3.86, 31820, 17391, 1858, '["世界自然遗产","喀斯特","瀑布","绿宝石"]', '["自然风光","喀斯特","世界遗产"]', '0854-3619810', '["卧龙潭和68级跌水瀑布是精华","建议穿防水鞋"]', 1),
('西江千户苗寨', 7, '西江千户苗寨位于贵州省黔东南州雷山县，是世界上最大的苗族聚居村寨。1000多户苗族人家依山而建的吊脚楼层层叠叠，夜晚万家灯火璀璨。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211431149337.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211415255799.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211410357095.jpg"]', '贵州省黔东南州雷山县西江镇', '黔东南', '贵州', '雷山县', '4A', 108.18, 26.49, '全天开放', 90.00, 1, '1-2天', '3月-11月', '独自,情侣,朋友', 4.43, 64913, 6665, 4236, '["苗寨","吊脚楼","苗族","夜景"]', '["乡村田园","民族","苗族"]', '0855-3348826', '["观景台看夜景是经典体验","长桌宴值得体验"]', 1),
('梵净山', 1, '梵净山位于贵州省铜仁市，是武陵山脉的主峰。蘑菇石是其标志性景观，山顶云雾缭绕，佛教文化底蕴深厚。2018年被列为世界自然遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211324979636.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211332489839.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211342576663.jpg"]', '贵州省铜仁市江口县太平镇', '铜仁', '贵州', '江口县', '5A', 108.68, 27.91, '08:00-16:00', 100.00, 1, '1天', '4月-11月', '独自,朋友', 3.91, 44457, 9369, 3992, '["世界自然遗产","蘑菇石","云海","佛教"]', '["自然风光","名山","世界遗产"]', '0856-6720000', '["蘑菇石是必看景点","建议乘索道上山"]', 1),
('镇远古镇', 2, '镇远古镇位于贵州省黔东南州镇远县，有2000多年历史。舞阳河穿城而过，两岸古建筑鳞次栉比，有东方威尼斯之称。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211394647072.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392213343.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211414825873.jpg"]', '贵州省黔东南州镇远县兴隆街21号', '黔东南', '贵州', '镇远县', '5A', 108.43, 27.05, '全天开放', 0.00, 0, '1-2天', '3月-11月', '独自,情侣,朋友', 3.82, 61365, 3431, 4244, '["古镇","舞阳河","夜景","历史"]', '["历史文化","古镇","水乡"]', '0855-5722063', '["夜景灯光秀很美","青龙洞古建筑群值得参观"]', 1),
('丽江古城', 2, '丽江古城位于云南省丽江市，始建于宋末元初，是中国保存最为完整的纳西族古城。1997年被列为世界文化遗产，以四方街为中心，街道依山傍水。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211494343298.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211550847166.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211452917461.jpg"]', '云南省丽江市古城区大研古城', '丽江', '云南', '古城区', '5A', 100.234, 26.872, '全天开放', 0.00, 0, '1-2天', '3月-5月,9月-11月', '独自,情侣,朋友', 4.01, 58353, 15025, 299, '["世界文化遗产","纳西族","古城","酒吧街"]', '["历史文化","古城","世界遗产"]', '0888-5111118', '["大研古城免门票","束河古镇更安静"]', 1),
('大理古城', 2, '大理古城位于云南省大理白族自治州，始建于明洪武十五年。背靠苍山，面朝洱海，风花雪月四景闻名遐迩，是白族文化的重要载体。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211382325978.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211410279252.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211410119036.jpg"]', '云南省大理州大理市复兴路', '大理', '云南', '大理市', '4A', 100.17, 25.69, '全天开放', 0.00, 0, '1天', '3月-5月,9月-11月', '独自,情侣,朋友', 4.07, 16519, 11757, 3678, '["古城","苍山洱海","白族","风花雪月"]', '["历史文化","古城","民族"]', '0872-2670396', '["洱海骑行是经典体验","三月街民族节很热闹"]', 1),
('石林', 1, '石林位于云南省昆明市石林彝族自治县，是世界自然遗产。以喀斯特地貌为主，石峰林立，形态各异，被誉为天下第一奇观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211527574320.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211404861149.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211473558717.jpg"]', '云南省昆明市石林县石林镇', '昆明', '云南', '石林县', '5A', 103.27, 24.77, '07:30-18:00', 130.00, 1, '3-4小时', '3月-10月', '独自,朋友,家庭', 4.67, 72768, 13105, 3309, '["世界自然遗产","喀斯特","石林","彝族"]', '["自然风光","地质","世界遗产"]', '0871-67711439', '["阿诗玛石是标志性景点","建议请导游讲解"]', 1),
('玉龙雪山', 1, '玉龙雪山位于云南省丽江市，是北半球最南的大雪山。主峰扇子陡海拔5596米，山上终年积雪，是纳西族心中的神山。蓝月谷是山脚下的绝美景点。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375486991.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211397173824.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211484616380.jpg"]', '云南省丽江市玉龙县白沙镇', '丽江', '云南', '玉龙县', '5A', 100.18, 27.12, '07:00-18:00', 100.00, 1, '1天', '11月-次年4月', '独自,朋友,家庭', 4.1, 27953, 17730, 1206, '["雪山","冰川","蓝月谷","纳西族"]', '["自然风光","雪山","高原"]', '0888-5131068', '["注意高原反应","蓝月谷免费但需购大门票"]', 1),
('西双版纳热带植物园', 1, '中科院西双版纳热带植物园位于云南省勐腊县，是中国面积最大的植物园。收集了13000多种热带植物，是热带雨林生态系统的缩影。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211378342025.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211469224610.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211391292180.jpg"]', '云南省西双版纳州勐腊县勐仑镇', '西双版纳', '云南', '勐腊县', '5A', 101.25, 21.92, '08:00-18:00', 80.00, 1, '4-6小时', '11月-次年4月', '独自,朋友,家庭', 4.72, 46973, 11308, 954, '["热带植物","雨林","科普","生态"]', '["自然风光","植物园","科普"]', '0691-8715914', '["东区是精华","见血封喉树等奇特植物值得一看"]', 1),
('香格里拉普达措', 1, '普达措国家公园位于云南省迪庆藏族自治州香格里拉市，是中国第一个国家公园。园内有高山湖泊、牧场、原始森林等多种自然景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211550954115.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211550946130.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211550827643.jpg"]', '云南省迪庆州香格里拉市建塘镇', '迪庆', '云南', '香格里拉市', '5A', 99.96, 27.83, '08:00-16:00', 100.00, 1, '4-6小时', '5月-7月,9月-11月', '独自,朋友', 4.71, 78639, 13688, 3590, '["国家公园","高原湖泊","藏族","原始森林"]', '["自然风光","高原","国家公园"]', '0887-8232533', '["属都湖和碧塔海是核心景点","注意高原反应"]', 1),
('泸沽湖', 1, '泸沽湖位于云南省与四川省交界处，是中国第三深淡水湖。湖水清澈如镜，周围摩梭人保留着母系社会的走婚习俗，被誉为东方女儿国。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211491940122.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211405041368.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375486962.jpg"]', '云南省丽江市宁蒗县泸沽湖镇', '丽江', '云南', '宁蒗县', '4A', 100.78, 27.71, '全天开放', 70.00, 1, '2-3天', '3月-5月,9月-11月', '独自,情侣,朋友', 4.21, 33047, 10817, 3085, '["高原湖泊","摩梭族","走婚","猪槽船"]', '["自然风光","湖泊","民族"]', '0888-5532152', '["环湖骑行是经典体验","里格半岛日落最美"]', 1),
('布达拉宫', 2, '布达拉宫位于西藏自治区拉萨市，始建于公元7世纪，是世界上海拔最高的宫殿建筑群。曾是历代达赖喇嘛的冬宫，1994年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211431281176.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211572833069.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211439170366.jpg"]', '西藏自治区拉萨市城关区北京中路35号', '拉萨', '西藏', '城关区', '5A', 91.117, 29.656, '09:00-16:00', 200.00, 1, '2-3小时', '5月-10月', '独自,朋友', 4.18, 71643, 12997, 2205, '["世界文化遗产","藏传佛教","宫殿","圣地"]', '["历史文化","宫殿","世界遗产"]', '0891-6339615', '["需提前一天预约门票","注意高原反应"]', 1),
('纳木错', 1, '纳木错位于西藏自治区中部，是西藏第二大湖泊，也是世界上海拔最高的大型湖泊。湖水湛蓝，背靠念青唐古拉山，景色壮美绝伦。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211431358257.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211384133888.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380748631.jpg"]', '西藏自治区拉萨市当雄县', '拉萨', '西藏', '当雄县', NULL, 90.6, 30.7, '全天开放', 120.00, 1, '1天', '5月-10月', '独自,朋友', 4.74, 62957, 1797, 4267, '["圣湖","高原湖泊","雪山","藏族"]', '["自然风光","湖泊","高原"]', '0891-6110222', '["海拔4718米注意高反","日出日落景色绝美"]', 1),
('大昭寺', 5, '大昭寺位于拉萨市中心，始建于唐贞观年间，是藏传佛教最神圣的寺庙之一。寺内供奉着释迦牟尼12岁等身像，是藏族人民心中的圣地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211397169739.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211389709323.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211438689163.jpg"]', '西藏自治区拉萨市城关区八廓街', '拉萨', '西藏', '城关区', '5A', 91.132, 29.653, '09:00-18:00', 85.00, 1, '1-2小时', '5月-10月', '独自,家庭', 4.32, 11791, 17477, 1798, '["藏传佛教","圣地","八廓街","朝圣"]', '["宗教寺庙","佛教","朝圣"]', '0891-6323129', '["八廓街转经是藏族传统","建议请导游讲解"]', 1),
('珠穆朗玛峰大本营', 1, '珠峰大本营位于西藏日喀则市定日县，海拔5200米，是普通游客能到达的离珠峰最近的地方。在这里可以近距离仰望世界最高峰的雄姿。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211343799095.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211309518272.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380026284.jpg"]', '西藏自治区日喀则市定日县', '日喀则', '西藏', '定日县', NULL, 86.85, 28.14, '全天开放', 180.00, 1, '1-2天', '5月-10月', '独自,朋友', 3.83, 34303, 4641, 3352, '["珠穆朗玛峰","世界之巅","高原","星空"]', '["自然风光","雪山","极限"]', NULL, '["海拔极高务必注意高反","夜晚星空极其壮观"]', 1),
('秦始皇兵马俑', 2, '秦始皇兵马俑位于陕西省西安市临潼区，是秦始皇陵的陪葬坑。1974年发现，被誉为世界第八大奇迹。1987年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211539917128.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211441624859.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211413453382.jpg"]', '陕西省西安市临潼区秦陵北路', '西安', '陕西', '临潼区', '5A', 109.273, 34.384, '08:30-18:00', 120.00, 1, '3-4小时', '3月-11月', '独自,朋友,家庭', 4.93, 70344, 14769, 4972, '["世界文化遗产","兵马俑","秦始皇","考古"]', '["历史文化","考古","世界遗产"]', '029-81399127', '["一号坑规模最大最壮观","建议请导游讲解"]', 1),
('华山', 1, '华山位于陕西省渭南市华阴市，是五岳之西岳。以险峻著称，有奇险天下第一山之称。长空栈道和鹞子翻身是最惊险的体验。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211382640181.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211517951928.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211391289993.jpg"]', '陕西省渭南市华阴市华山镇', '渭南', '陕西', '华阴市', '5A', 110.089, 34.474, '全天开放', 160.00, 1, '1-2天', '4月-10月', '独自,朋友', 4.91, 77962, 16356, 2180, '["五岳","险峻","日出","长空栈道"]', '["自然风光","名山","五岳"]', '0913-4368888', '["建议夜爬看日出","恐高者慎走长空栈道"]', 1),
('西安城墙', 2, '西安城墙始建于明洪武年间，是中国现存规模最大、保存最完整的古代城垣。全长13.74公里，可骑自行车环城一周，是了解古都西安的最佳方式。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211446291235.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211401901696.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211375958212.jpg"]', '陕西省西安市碑林区南大街', '西安', '陕西', '碑林区', '5A', 108.94, 34.26, '08:00-22:00', 54.00, 1, '2-3小时', '3月-11月', '独自,情侣,朋友,家庭', 4.85, 30975, 5086, 730, '["古城墙","明代","骑行","夜景"]', '["历史文化","城墙","古建筑"]', '029-87272792', '["建议租自行车环城骑行","南门灯光秀值得一看"]', 1),
('大雁塔', 2, '大雁塔位于西安市雁塔区大慈恩寺内，始建于唐永徽三年，是玄奘法师为保存从印度带回的经卷而修建。是西安的标志性建筑之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211519581337.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211500178139.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211381620963.jpg"]', '陕西省西安市雁塔区慈恩路1号', '西安', '陕西', '雁塔区', '5A', 108.959, 34.218, '08:00-18:30', 40.00, 1, '1-2小时', '3月-11月', '独自,情侣,朋友,家庭', 3.97, 6491, 9678, 1199, '["唐代","玄奘","佛塔","音乐喷泉"]', '["历史文化","佛塔","唐代"]', '029-85527958', '["北广场音乐喷泉晚上开放","登塔可俯瞰西安城"]', 1),
('壶口瀑布陕西侧', 1, '壶口瀑布陕西侧位于陕西省宜川县，与山西侧隔河相望。这里可以更近距离地感受黄河之水天上来的磅礴气势，是观赏壶口瀑布的另一绝佳角度。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211622921243.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211620866952.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211620809372.jpg"]', '陕西省延安市宜川县壶口镇', '延安', '陕西', '宜川县', '4A', 110.45, 36.15, '08:00-18:00', 90.00, 1, '2-3小时', '4月-5月,9月-11月', '独自,朋友', 4.48, 18803, 2887, 2942, '["黄河","瀑布","壮观","陕北"]', '["自然风光","瀑布","黄河"]', '0911-4838030', '["与山西侧角度不同各有特色","注意防滑和水雾"]', 1),
('莫高窟', 2, '莫高窟位于甘肃省敦煌市，始建于十六国前秦时期，历经千年营造。现存洞窟735个，壁画4.5万平方米，是世界上现存规模最大的佛教艺术宝库。1987年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211445140148.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211549677560.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211578625559.jpg"]', '甘肃省酒泉市敦煌市莫高镇', '酒泉', '甘肃', '敦煌市', '5A', 94.81, 40.04, '08:00-18:00', 238.00, 1, '3-4小时', '5月-10月', '独自,朋友', 4.33, 35598, 8649, 2818, '["世界文化遗产","壁画","佛教","丝绸之路"]', '["历史文化","石窟","世界遗产"]', '0937-8869060', '["必须提前网上预约","A类票含数字展示中心"]', 1),
('鸣沙山月牙泉', 1, '鸣沙山月牙泉位于甘肃省敦煌市南郊，沙泉共处，妙造天成。月牙泉被鸣沙山环抱，历经千年不被流沙掩埋，被誉为沙漠第一泉。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380602191.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211392718127.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211383825004.jpg"]', '甘肃省酒泉市敦煌市鸣沙山路', '酒泉', '甘肃', '敦煌市', '5A', 94.67, 40.08, '05:00-21:30（旺季）', 110.00, 1, '3-4小时', '5月-10月', '独自,情侣,朋友,家庭', 3.93, 5172, 12711, 2673, '["沙漠","月牙泉","骆驼","日落"]', '["自然风光","沙漠","奇观"]', '0937-8883388', '["建议傍晚去看日落","骑骆驼是经典体验"]', 1),
('张掖丹霞', 1, '张掖丹霞国家地质公园位于甘肃省张掖市，以色彩斑斓的丹霞地貌著称。红、黄、白、绿等多种颜色交织，如同上帝打翻的调色板，是中国最美丹霞之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211442964615.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211453034533.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211328300325.jpg"]', '甘肃省张掖市临泽县倪家营乡', '张掖', '甘肃', '临泽县', '4A', 100.06, 38.93, '06:00-19:00（旺季）', 75.00, 1, '3-4小时', '6月-9月', '独自,情侣,朋友', 4.12, 36521, 11634, 2930, '["丹霞","七彩","地质公园","摄影"]', '["自然风光","丹霞","摄影"]', '0936-5623666', '["日出日落时色彩最绚丽","建议乘景区大巴游览"]', 1),
('嘉峪关', 2, '嘉峪关位于甘肃省嘉峪关市，始建于明洪武五年，是明长城最西端的关口。因地势险要、建筑雄伟而被称为天下第一雄关。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211435661335.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211402948170.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211386496596.jpg"]', '甘肃省嘉峪关市雄关区新华南路', '嘉峪关', '甘肃', '雄关区', '5A', 98.23, 39.77, '08:30-18:00', 110.00, 1, '2-3小时', '5月-10月', '独自,朋友,家庭', 4.85, 19818, 5970, 1465, '["天下第一雄关","长城","丝绸之路","明代"]', '["历史文化","长城","关隘"]', '0937-6396110', '["建议请导游讲解历史","日落时分城楼景色最美"]', 1),
('麦积山石窟', 2, '麦积山石窟位于甘肃省天水市，因山形似麦垛而得名。始建于后秦时期，现存窟龛194个，泥塑石雕7800余尊，被誉为东方雕塑陈列馆。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211395935710.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211467600394.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211454881405.jpg"]', '甘肃省天水市麦积区麦积山风景区', '天水', '甘肃', '麦积区', '5A', 106.0, 34.35, '08:00-17:00', 70.00, 1, '3-4小时', '4月-10月', '独自,朋友', 4.2, 59341, 15717, 1088, '["石窟","泥塑","佛教","丝绸之路"]', '["历史文化","石窟","雕塑"]', '0938-2731407', '["栈道较陡注意安全","建议请导游讲解"]', 1),
('青海湖', 1, '青海湖位于青海省海南藏族自治州，是中国最大的内陆湖和咸水湖。湖面海拔3196米，面积4583平方公里，湖水湛蓝，油菜花环湖盛开时景色绝美。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211523729163.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211456825627.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211388789592.jpg"]', '青海省海南州共和县109国道旁', '海南州', '青海', '共和县', '5A', 100.2, 36.53, '全天开放', 90.00, 1, '1-2天', '6月-8月', '独自,情侣,朋友,家庭', 4.21, 67307, 6027, 4968, '["高原湖泊","油菜花","骑行","候鸟"]', '["自然风光","湖泊","高原"]', '0974-8519688', '["7月油菜花盛开最美","环湖骑行是经典体验"]', 1),
('塔尔寺', 5, '塔尔寺位于青海省西宁市湟中区，始建于明洪武年间，是藏传佛教格鲁派六大寺院之一。寺内的酥油花、壁画和堆绣被誉为塔尔寺艺术三绝。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211343773550.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211328403119.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211335181969.jpg"]', '青海省西宁市湟中区金塔路56号', '西宁', '青海', '湟中区', '5A', 101.57, 36.48, '08:00-17:00', 70.00, 1, '2-3小时', '5月-10月', '独自,家庭', 4.72, 34017, 9702, 230, '["藏传佛教","格鲁派","酥油花","壁画"]', '["宗教寺庙","佛教","艺术"]', '0971-2232357', '["酥油花展每年正月十五最盛","建议请导游讲解"]', 1),
('茶卡盐湖', 1, '茶卡盐湖位于青海省海西蒙古族藏族自治州乌兰县，被誉为中国的天空之镜。湖面如镜，倒映着蓝天白云，是中国最美的网红打卡地之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211452090049.jpeg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211387464090.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211396311031.jpg"]', '青海省海西州乌兰县茶卡镇', '海西', '青海', '乌兰县', '4A', 99.08, 36.72, '07:00-19:00（旺季）', 60.00, 1, '3-4小时', '6月-9月', '独自,情侣,朋友', 4.37, 41018, 5684, 4919, '["天空之镜","盐湖","倒影","网红"]', '["自然风光","盐湖","摄影"]', '0977-8240129', '["晴天无风时倒影效果最好","建议穿鲜艳衣服拍照"]', 1),
('沙坡头', 1, '沙坡头位于宁夏回族自治区中卫市，地处腾格里沙漠东南缘。集大漠、黄河、高山、绿洲为一体，是中国治沙成就的典范，也是热门综艺取景地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211501456100.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211452347298.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380551515.jpg"]', '宁夏回族自治区中卫市沙坡头区', '中卫', '宁夏', '沙坡头区', '5A', 104.95, 37.43, '08:00-18:00', 80.00, 1, '1天', '4月-10月', '朋友,家庭', 3.89, 48950, 13105, 1904, '["沙漠","黄河","滑沙","羊皮筏子"]', '["自然风光","沙漠","体验"]', '0955-7658888', '["黄河飞索和羊皮筏子是特色体验","注意防晒"]', 1),
('西夏王陵', 2, '西夏王陵位于宁夏银川市西郊贺兰山东麓，是西夏历代帝王的陵墓群。现存9座帝陵和200多座陪葬墓，被誉为东方金字塔。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211399253302.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211390561410.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211399253305.jpg"]', '宁夏回族自治区银川市西夏区', '银川', '宁夏', '西夏区', '4A', 105.97, 38.49, '08:00-18:00', 60.00, 1, '2-3小时', '4月-10月', '独自,朋友', 4.69, 77466, 17819, 4313, '["西夏","王陵","东方金字塔","历史"]', '["历史文化","陵墓","考古"]', '0951-5668960', '["建议先参观博物馆了解西夏历史","日落时分拍照最美"]', 1),
('水洞沟', 2, '水洞沟位于宁夏灵武市，是中国最早发掘的旧石器时代文化遗址。景区内有明代长城、藏兵洞等历史遗迹，集考古、历史、自然于一体。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211431949630.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211431949629.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211405659199.jpg"]', '宁夏回族自治区银川市灵武市临河镇', '银川', '宁夏', '灵武市', '5A', 106.58, 38.31, '08:00-18:00', 60.00, 1, '3-4小时', '4月-10月', '独自,朋友,家庭', 4.24, 44238, 3853, 4023, '["旧石器","长城","藏兵洞","考古"]', '["历史文化","考古","长城"]', '0951-5015588', '["藏兵洞是亮点","建议乘坐各种交通工具体验"]', 1),
('天山天池', 1, '天山天池位于新疆昌吉回族自治州阜康市，是天山博格达峰北侧的高山湖泊。湖面海拔1910米，湖水清澈碧蓝，四周雪峰环绕，景色如画。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211386827230.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211386827231.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211439642443.jpg"]', '新疆维吾尔自治区昌吉州阜康市', '昌吉', '新疆', '阜康市', '5A', 88.12, 43.88, '09:00-19:00（旺季）', 95.00, 1, '4-6小时', '5月-10月', '独自,情侣,朋友,家庭', 4.46, 27699, 10730, 4772, '["高山湖泊","天山","雪峰","西王母"]', '["自然风光","湖泊","雪山"]', '0994-3258679', '["建议乘区间车到湖边","哈萨克毡房可体验"]', 1),
('喀纳斯湖', 1, '喀纳斯湖位于新疆阿勒泰地区布尔津县，是中国最深的冰碛堰塞湖。湖水随季节变换颜色，神秘的湖怪传说更增添了其神秘色彩。秋季层林尽染，美不胜收。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211537879235.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211395466493.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211396365712.jpg"]', '新疆维吾尔自治区阿勒泰地区布尔津县', '阿勒泰', '新疆', '布尔津县', '5A', 87.02, 48.72, '08:00-20:00（旺季）', 160.00, 1, '2-3天', '6月-10月', '独自,朋友', 4.75, 13809, 5995, 3086, '["湖泊","湖怪","秋色","图瓦人"]', '["自然风光","湖泊","秋色"]', '0906-6525008', '["9月底-10月初秋色最美","禾木村值得住一晚"]', 1),
('那拉提草原', 1, '那拉提草原位于新疆伊犁哈萨克自治州新源县，是世界四大草原之一的亚高山草甸植物区。空中草原海拔2000米以上，6月野花盛开时如同花的海洋。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211390643639.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211379452682.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211338472694.jpg"]', '新疆维吾尔自治区伊犁州新源县那拉提镇', '伊犁', '新疆', '新源县', '5A', 84.01, 43.26, '08:00-20:00（旺季）', 95.00, 1, '1-2天', '6月-9月', '独自,情侣,朋友,家庭', 4.5, 10523, 7532, 1907, '["草原","哈萨克族","雪山","骑马"]', '["自然风光","草原","民族"]', '0999-5290558', '["空中草原是精华","6月野花盛开最美"]', 1),
('赛里木湖', 1, '赛里木湖位于新疆博尔塔拉蒙古自治州博乐市，是新疆海拔最高、面积最大的高山湖泊。湖水湛蓝如宝石，被誉为大西洋最后一滴眼泪。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211513277189.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211547470244.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211547388858.jpg"]', '新疆维吾尔自治区博州博乐市', '博州', '新疆', '博乐市', '5A', 81.17, 44.6, '全天开放', 70.00, 1, '1天', '5月-9月', '独自,情侣,朋友', 4.56, 8815, 7851, 3669, '["高山湖泊","大西洋之泪","雪山","蓝色"]', '["自然风光","湖泊","高原"]', '0909-2318222', '["环湖公路约90公里","6月野花环湖盛开"]', 1),
('吐鲁番葡萄沟', 7, '葡萄沟位于新疆吐鲁番市高昌区，是火焰山下的一处峡谷。沟内泉水淙淙，葡萄架遍布，盛产无核白葡萄等优质品种，是丝绸之路上的绿色明珠。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211444268504.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211409232365.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211359353435.jpg"]', '新疆维吾尔自治区吐鲁番市高昌区葡萄沟路', '吐鲁番', '新疆', '高昌区', '5A', 89.24, 42.95, '08:00-21:00（旺季）', 60.00, 1, '2-3小时', '7月-10月', '朋友,家庭', 4.43, 22371, 6351, 373, '["葡萄","火焰山","维吾尔族","丝绸之路"]', '["乡村田园","葡萄","民族"]', '0995-8536390', '["8-9月葡萄成熟可品尝","火焰山在附近"]', 1),
('独库公路', 1, '独库公路北起独山子、南至库车，全长561公里，横穿天山山脉。沿途可欣赏雪山、峡谷、草原、森林等多种地貌，被誉为中国最美公路之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211502657484.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211388801611.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211407856287.jpg"]', '新疆维吾尔自治区（独山子至库车）', '伊犁', '新疆', '独山子区', NULL, 84.88, 42.62, '全天开放（仅夏季通车）', 0.00, 0, '2-3天', '6月-10月', '独自,朋友', 3.86, 67977, 4305, 4118, '["最美公路","天山","自驾","雪山"]', '["自然风光","公路","自驾"]', NULL, '["仅6-10月通车","建议自驾或包车"]', 1),
('维多利亚港', 4, '维多利亚港位于香港岛和九龙半岛之间，是世界三大天然良港之一。两岸高楼林立，夜晚灯光璀璨，幻彩咏香江灯光秀是经典体验。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211554184657.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211456156224.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211392466982.jpg"]', '香港特别行政区尖沙咀海滨', '香港', '香港', '尖沙咀', NULL, 114.174, 22.293, '全天开放', 0.00, 0, '1-2小时', '10月-次年3月', '独自,情侣,朋友,家庭', 4.2, 56539, 10070, 2730, '["海港","夜景","灯光秀","地标"]', '["城市观光","夜景","海港"]', NULL, '["尖沙咀海滨长廊是最佳观赏点","每晚8点灯光秀"]', 1),
('太平山顶', 4, '太平山顶是香港最高点，海拔552米。乘坐山顶缆车登顶，可360度俯瞰维多利亚港和香港岛全景，是香港最受欢迎的观景点。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211436166537.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211390275864.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG41N1325231544.jpg"]', '香港特别行政区山顶道128号', '香港', '香港', '中西区', NULL, 114.15, 22.271, '10:00-23:00（凌霄阁）', 0.00, 0, '2-3小时', '10月-次年3月', '独自,情侣,朋友,家庭', 3.85, 31666, 1994, 1370, '["山顶","夜景","缆车","全景"]', '["城市观光","观景","地标"]', NULL, '["山顶缆车是经典体验","建议傍晚上山看日落和夜景"]', 1),
('香港迪士尼乐园', 3, '香港迪士尼乐园位于大屿山，是全球第五座迪士尼主题乐园。园区包含七大主题区域，拥有多项独有的游乐设施和娱乐表演。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211498154938.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211491797745.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434391258.jpg"]', '香港特别行政区大屿山', '香港', '香港', '荃湾区', NULL, 114.045, 22.313, '10:00-20:00（因季节调整）', 639.00, 1, '1天', '10月-次年5月', '情侣,朋友,家庭', 3.88, 47676, 14773, 230, '["迪士尼","主题乐园","亲子"]', '["主题乐园","亲子","娱乐"]', NULL, '["建议工作日前往人少","下载官方APP查看排队时间"]', 1),
('天坛大佛', 5, '天坛大佛位于香港大屿山昂坪，高34米，是全球最大的户外青铜坐佛。乘坐昂坪360缆车可欣赏大屿山和南中国海的壮丽景色。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211328695179.jpg", "https://vcg05.cfp.cn/creative/vcg/nowarter800/new/VCG211192752703.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211512667069.jpg"]', '香港特别行政区大屿山昂坪', '香港', '香港', '离岛区', NULL, 113.905, 22.254, '10:00-17:30', 0.00, 0, '3-4小时', '10月-次年4月', '独自,家庭', 4.69, 66464, 14416, 1822, '["大佛","宝莲禅寺","昂坪缆车"]', '["宗教寺庙","观光","佛教"]', NULL, '["昂坪缆车水晶车厢体验更佳","宝莲禅寺素斋值得品尝"]', 1),
('星光大道', 4, '星光大道位于香港尖沙咀海滨花园，是为纪念香港电影业发展而建。大道上有众多电影明星的手印和雕像，李小龙铜像是标志性景点。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211490434177.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211403166812.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211403167676.jpg"]', '香港特别行政区尖沙咀海滨花园', '香港', '香港', '油尖旺区', NULL, 114.176, 22.294, '全天开放', 0.00, 0, '1小时', '10月-次年3月', '独自,情侣,朋友', 3.91, 69500, 7967, 436, '["电影","明星手印","海滨","夜景"]', '["城市观光","电影","海滨"]', NULL, '["李小龙铜像是必拍景点","傍晚来可同时看日落和夜景"]', 1),
('大三巴牌坊', 2, '大三巴牌坊是澳门最具代表性的地标，原为圣保禄大教堂的前壁遗址。融合了欧洲文艺复兴时期与东方建筑风格，2005年作为澳门历史城区的一部分被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211430862618.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211420574964.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211423702957.jpg"]', '澳门特别行政区大巴街', '澳门', '澳门', '花王堂区', NULL, 113.541, 22.197, '全天开放', 0.00, 0, '1小时', '10月-次年3月', '独自,情侣,朋友,家庭', 4.26, 16201, 7862, 1287, '["世界文化遗产","地标","教堂遗址","中西合璧"]', '["历史文化","地标","世界遗产"]', NULL, '["免费参观","旁边有大炮台和澳门博物馆"]', 1),
('威尼斯人度假村', 3, '澳门威尼斯人度假村是亚洲最大的综合性度假村。内部仿照意大利威尼斯水乡风格建造，有人造天空、运河和贡多拉船，购物餐饮娱乐一应俱全。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211398982055.jpeg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211420287727.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211442594570.jpg"]', '澳门特别行政区路氹金光大道', '澳门', '澳门', '路氹城', NULL, 113.561, 22.146, '全天开放', 0.00, 0, '3-4小时', '1月-12月', '情侣,朋友,家庭', 4.31, 17915, 17282, 4037, '["度假村","购物","威尼斯","娱乐"]', '["主题乐园","购物","度假"]', NULL, '["贡多拉船体验很有特色","大运河购物中心品牌齐全"]', 1),
('妈阁庙', 5, '妈阁庙位于澳门半岛西南端，始建于1488年，是澳门最古老的庙宇。供奉妈祖（天后），澳门的葡文名称Macau即源于妈阁。2005年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211436221777.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211500712402.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211436221776.jpg"]', '澳门特别行政区妈阁斜巷', '澳门', '澳门', '风顺堂区', NULL, 113.536, 22.187, '07:00-18:00', 0.00, 0, '1小时', '1月-12月', '独自,家庭', 3.81, 44988, 8342, 1245, '["世界文化遗产","妈祖","古庙","澳门起源"]', '["宗教寺庙","历史","世界遗产"]', NULL, '["免费参观","了解澳门名称由来"]', 1),
('澳门塔', 4, '澳门旅游塔高338米，是澳门的标志性建筑之一。塔上设有全球最高的商业蹦极跳（233米），还有空中漫步等极限项目。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211456605384.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211419952297.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211383510575.jpg"]', '澳门特别行政区观光塔前地', '澳门', '澳门', '大堂区', NULL, 113.537, 22.18, '10:00-21:00', 165.00, 1, '2-3小时', '1月-12月', '情侣,朋友', 4.39, 34071, 6242, 398, '["观光塔","蹦极","极限运动","观景"]', '["城市观光","极限","观景"]', NULL, '["蹦极需提前预约","观光层可俯瞰澳门全景"]', 1),
('议事亭前地', 4, '议事亭前地位于澳门半岛中心，是澳门四大广场之一。广场周围是色彩斑斓的欧式建筑，地面铺设波浪形葡式碎石路，2005年被列为世界文化遗产。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211384472127.jpg", "https://vcg05.cfp.cn/creative/vcg/nowarter800/version23/VCG4185867412.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211372142340.jpg"]', '澳门特别行政区议事亭前地', '澳门', '澳门', '大堂区', NULL, 113.539, 22.193, '全天开放', 0.00, 0, '1小时', '1月-12月', '独自,情侣,朋友', 4.13, 30713, 2830, 4888, '["世界文化遗产","欧式建筑","广场"]', '["城市观光","建筑","世界遗产"]', NULL, '["周边美食众多","民政总署大楼值得参观"]', 1),
('日月潭', 1, '日月潭位于台湾省南投县，是台湾最大的天然淡水湖。因湖面被拉鲁岛分为日潭和月潭而得名，湖光山色秀丽，是台湾八景之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211377219781.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211326814801.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211438055759.jpg"]', '台湾省南投县鱼池乡中山路', '南投', '台湾', '鱼池乡', NULL, 120.916, 23.863, '全天开放', 0.00, 0, '4-6小时', '3月-5月,9月-11月', '独自,情侣,朋友,家庭', 4.45, 8061, 6547, 4912, '["湖泊","台湾八景","自然风光"]', '["自然风光","湖泊","骑行"]', NULL, '["环湖自行车道是经典体验","日月潭红茶值得品尝"]', 1),
('阿里山', 1, '阿里山位于台湾省嘉义县，以日出、云海、晚霞、森林铁路和神木五奇闻名。春季樱花盛开时，森林铁路穿行于花海之中，美不胜收。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211406905470.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211436343187.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211298706332.jpg"]', '台湾省嘉义县阿里山乡', '嘉义', '台湾', '阿里山乡', NULL, 120.733, 23.511, '全天开放', 0.00, 0, '1-2天', '3月-4月,10月-12月', '独自,朋友,家庭', 4.25, 58130, 6511, 363, '["日出","云海","森林铁路","樱花"]', '["自然风光","山岳","铁路"]', NULL, '["祝山观日出是经典体验","森林铁路值得体验"]', 1),
('台北101', 4, '台北101大楼位于台北市信义区，高508米，曾是世界最高建筑。89楼室内观景台可360度俯瞰台北市景，跨年烟火秀是其标志性活动。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211418788746.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211414177824.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211357930997.jpg"]', '台湾省台北市信义区信义路五段7号', '台北', '台湾', '信义区', NULL, 121.565, 25.034, '11:00-21:00', 150.00, 1, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 4.46, 36731, 1688, 2769, '["地标","摩天大楼","观景","跨年"]', '["城市观光","地标","观景"]', NULL, '["89楼观景台视野极佳","跨年烟火秀世界闻名"]', 1),
('太鲁阁国家公园', 1, '太鲁阁国家公园位于台湾省花莲县，以壮丽的大理石峡谷闻名。立雾溪历经千万年切割岩层，形成深达千米的峡谷，是台湾最具代表性的峡谷型国家公园。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG41518102714.jpg", "https://vcg05.cfp.cn/creative/vcg/nowarter800/new/VCG41N1143537903.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211358082456.jpg"]', '台湾省花莲县秀林乡富世村', '花莲', '台湾', '秀林乡', NULL, 121.495, 24.158, '全天开放', 0.00, 0, '1天', '3月-5月,9月-11月', '独自,朋友', 4.26, 10403, 2949, 2660, '["峡谷","大理石","国家公园"]', '["自然风光","峡谷","地质"]', NULL, '["注意落石警告","燕子口步道是精华段"]', 1),
('垦丁国家公园', 6, '垦丁国家公园位于台湾省屏东县恒春半岛南端，三面环海，是台湾唯一涵盖陆地与海域的国家公园。有鹅銮鼻灯塔、猫鼻头、南湾等著名景点。', '["https://vcg02.cfp.cn/creative/vcg/nowarter800/new/VCG211199361779.jpg", "https://vcg05.cfp.cn/creative/vcg/nowarter800/new/VCG211194058391.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG41142963647.jpg"]', '台湾省屏东县恒春镇垦丁路', '屏东', '台湾', '恒春镇', NULL, 120.803, 21.946, '全天开放', 0.00, 0, '1-2天', '4月-10月', '情侣,朋友,家庭', 3.98, 33901, 10312, 363, '["海滨","热带","国家公园","冲浪"]', '["海滨海岛","度假","水上运动"]', NULL, '["春呐音乐节期间非常热闹","水上活动丰富"]', 1),
('九份老街', 4, '九份老街位于台湾省新北市瑞芳区，依山面海，保留了日据时代的旧式建筑。狭窄的街道和陡直的石阶，营造出独特的怀旧氛围。据说是《千与千寻》的灵感来源。', '["https://vcg02.cfp.cn/creative/vcg/nowarter800/new/VCG211204235000.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG41143060055.jpg", "https://vcg02.cfp.cn/creative/vcg/nowarter800/new/VCG211150103826.jpg"]', '台湾省新北市瑞芳区基山街', '新北', '台湾', '瑞芳区', NULL, 121.844, 25.109, '全天开放', 0.00, 0, '3-4小时', '9月-11月', '独自,情侣,朋友', 4.41, 51576, 14593, 2650, '["老街","怀旧","千与千寻","茶坊"]', '["城市观光","老街","美食"]', NULL, '["傍晚灯笼亮起时最有氛围","芋圆是必吃小吃"]', 1),
('故宫博物院台北', 2, '台北故宫博物院位于台北市士林区，收藏了近70万件中华文化瑰宝。翠玉白菜、肉形石、毛公鼎是镇馆三宝，是世界四大博物馆之一。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211347817348.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211310398658.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211152472000.jpg"]', '台湾省台北市士林区至善路二段221号', '台北', '台湾', '士林区', NULL, 121.549, 25.102, '08:30-18:30', 150.00, 1, '3-4小时', '1月-12月', '独自,情侣,朋友,家庭', 3.85, 48192, 4258, 2547, '["博物馆","文物","翠玉白菜","中华文化"]', '["历史文化","博物馆","文物"]', NULL, '["翠玉白菜和肉形石是必看","建议请语音导览"]', 1),
('淡水老街', 4, '淡水老街位于台北市淡水区，沿淡水河而建。这里有红毛城、淡水渔人码头等景点，日落时分淡水河畔的晚霞被誉为台湾最美夕阳。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211367746601.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211546421163.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211544155863.jpg"]', '台湾省新北市淡水区中正路', '新北', '台湾', '淡水区', NULL, 121.44, 25.17, '全天开放', 0.00, 0, '3-4小时', '9月-11月', '独自,情侣,朋友', 4.8, 29189, 16757, 4100, '["老街","日落","淡水河","美食"]', '["城市观光","老街","日落"]', NULL, '["渔人码头看日落是经典体验","阿给和铁蛋是特色小吃"]', 1),
('延安革命纪念馆', 8, '延安革命纪念馆位于陕西省延安市，是中国最早建立的革命纪念馆之一。馆内展示了1935年至1948年中共中央在延安领导中国革命的光辉历史。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211331433872.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211335765746.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211362769105.jpg"]', '陕西省延安市宝塔区王家坪', '延安', '陕西', '宝塔区', '5A', 109.49, 36.59, '08:00-17:00', 0.00, 0, '2-3小时', '1月-12月', '独自,朋友,家庭', 4.92, 35881, 7045, 2261, '["红色旅游","革命","延安精神","历史"]', '["红色旅游","纪念馆","革命"]', '0911-2382610', '["免费参观需预约","宝塔山就在附近"]', 1),
('鸟巢水立方', 4, '国家体育场（鸟巢）和国家游泳中心（水立方）位于北京奥林匹克公园，是2008年北京奥运会的标志性建筑。夜晚灯光效果尤为壮观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380583901.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211380583903.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211380583898.jpg"]', '北京市朝阳区国家体育场南路1号', '北京', '北京', '朝阳区', '5A', 116.396, 39.993, '09:00-21:00', 50.00, 1, '2-3小时', '1月-12月', '独自,朋友,家庭', 4.15, 19260, 12438, 2890, '["奥运","地标","建筑","夜景"]', '["城市观光","地标","体育"]', '010-84373008', '["夜景灯光效果最佳","可购买联票参观两个场馆"]', 1),
('西安回民街', 4, '回民街位于西安市莲湖区，是西安著名的美食文化街区。街道两旁汇聚了各种西北特色小吃，牛羊肉泡馍、肉夹馍、凉皮等美食应有尽有。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211508715021.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211492966665.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211553551707.jpg"]', '陕西省西安市莲湖区北院门', '西安', '陕西', '莲湖区', NULL, 108.94, 34.265, '全天开放', 0.00, 0, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 4.62, 49596, 2315, 2892, '["美食","回民街","小吃","夜市"]', '["城市观光","美食","文化"]', NULL, '["晚上氛围最好","老米家泡馍值得一试"]', 1),
('武汉东湖', 1, '东湖位于湖北省武汉市，是中国最大的城中湖。湖面面积33平方公里，是杭州西湖的6倍。东湖绿道环湖而建，是武汉市民休闲的好去处。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211377422661.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211315709817.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211269203735.jpg"]', '湖北省武汉市武昌区沿湖大道', '武汉', '湖北', '武昌区', '5A', 114.37, 30.56, '全天开放', 0.00, 0, '4-6小时', '3月-5月,9月-11月', '独自,情侣,朋友,家庭', 4.67, 35540, 9187, 3925, '["城中湖","绿道","樱花","骑行"]', '["自然风光","湖泊","骑行"]', '027-86793760', '["东湖绿道骑行是经典体验","磨山樱花园春季最美"]', 1),
('厦门南普陀寺', 5, '南普陀寺位于福建省厦门市思明区，始建于唐代，是闽南佛教胜地。寺后是五老峰，登顶可俯瞰厦门大学和海景。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211358198321.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211359319391.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211346811592.jpg"]', '福建省厦门市思明区思明南路515号', '厦门', '福建', '思明区', '4A', 118.088, 24.441, '03:00-20:00', 0.00, 0, '1-2小时', '1月-12月', '独自,家庭', 4.09, 17267, 17125, 2499, '["佛教","古寺","厦门大学","五老峰"]', '["宗教寺庙","佛教","观景"]', '0592-2087282', '["免费参观","可与厦门大学联游"]', 1),
('成都宽窄巷子', 4, '宽窄巷子位于成都市青羊区，由宽巷子、窄巷子和井巷子三条平行排列的老式街道组成。是成都遗留下来的较成规模的清朝古街道。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211415374001.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211338628587.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211341664422.jpg"]', '四川省成都市青羊区宽窄巷子', '成都', '四川', '青羊区', '4A', 104.055, 30.67, '全天开放', 0.00, 0, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 4.71, 35834, 10304, 2707, '["古街","美食","茶馆","川剧"]', '["城市观光","美食","文化"]', '028-86259233', '["三大必吃：龙抄手、担担面、钟水饺","建议下午去喝茶看川剧变脸"]', 1),
('杭州灵隐寺', 5, '灵隐寺位于杭州市西湖区，始建于东晋咸和元年，是中国佛教禅宗十大古刹之一。寺前飞来峰石刻造像是中国南方石窟艺术的瑰宝。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211572201086.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211443889669.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211415798144.jpg"]', '浙江省杭州市西湖区灵隐路法云弄1号', '杭州', '浙江', '西湖区', '5A', 120.1, 30.24, '07:00-18:15', 75.00, 1, '2-3小时', '3月-5月,9月-11月', '独自,家庭', 4.19, 62645, 7177, 3410, '["古刹","禅宗","飞来峰","佛教"]', '["宗教寺庙","佛教","古建筑"]', '0571-87968665', '["飞来峰石刻值得细看","建议早上去人少清净"]', 1),
('南京总统府', 2, '南京总统府位于南京市玄武区，是中国近代建筑遗存中规模最大、保存最完整的建筑群。既有中国古代传统建筑，又有西方古典风格建筑。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211440804752.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211379827081.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211351232496.jpg"]', '江苏省南京市玄武区长江路292号', '南京', '江苏', '玄武区', '4A', 118.796, 32.048, '08:30-17:00', 35.00, 1, '2-3小时', '3月-11月', '独自,朋友', 3.87, 29907, 16228, 829, '["近代史","总统府","民国","建筑"]', '["历史文化","近代史","建筑"]', '025-84578700', '["建议请导游讲解历史","周一闭馆"]', 1),
('长沙橘子洲', 4, '橘子洲位于湖南省长沙市湘江中心，是世界上最大的内陆洲。洲头矗立着青年毛泽东雕塑，每逢重大节日有烟花表演。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211421679646.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211398942126.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211395556135.jpg"]', '湖南省长沙市岳麓区橘子洲头', '长沙', '湖南', '岳麓区', '5A', 112.96, 28.18, '全天开放', 0.00, 0, '3-4小时', '3月-5月,9月-11月', '独自,情侣,朋友,家庭', 4.62, 36389, 6828, 1304, '["橘子洲头","毛泽东","湘江","烟花"]', '["城市观光","红色旅游","地标"]', '0731-88882152', '["免费参观","周六晚有烟花表演（需确认）"]', 1),
('昆明滇池', 1, '滇池位于云南省昆明市西南部，是云南省最大的淡水湖。每年冬季有大量红嘴鸥从西伯利亚飞来越冬，人鸥和谐共处的场景成为昆明一景。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211542567069.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211537503828.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211393939221.jpg"]', '云南省昆明市西山区滇池路1318号', '昆明', '云南', '西山区', NULL, 102.66, 24.83, '全天开放', 0.00, 0, '3-4小时', '11月-次年3月', '独自,情侣,朋友,家庭', 4.37, 77116, 2641, 1611, '["高原湖泊","红嘴鸥","西山","日落"]', '["自然风光","湖泊","观鸟"]', '0871-64311056', '["冬季看红嘴鸥是经典体验","海埂大坝是最佳观赏点"]', 1),
('南宁青秀山', 1, '青秀山位于广西南宁市，是南宁市最著名的风景区。山上林木茂盛，泉清石奇，有龙象塔、天池等景点，是南宁的绿肺和城市名片。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211344675620.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211344667873.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211396632643.jpg"]', '广西壮族自治区南宁市青秀区凤岭南路6号', '南宁', '广西', '青秀区', '5A', 108.39, 22.78, '06:00-22:00', 20.00, 1, '3-4小时', '1月-12月', '独自,朋友,家庭', 4.27, 53434, 3661, 3852, '["城市公园","兰花","龙象塔","绿肺"]', '["自然风光","公园","休闲"]', '0771-5560662', '["兰花园是亮点","适合晨练和休闲散步"]', 1),
('贵阳甲秀楼', 2, '甲秀楼位于贵州省贵阳市南明区，始建于明万历年间，是贵阳的标志性建筑。楼阁三层三檐四角攒尖顶，矗立在南明河上，夜景尤为秀美。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434790847.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211413211245.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434613816.jpg"]', '贵州省贵阳市南明区翠微巷8号', '贵阳', '贵州', '南明区', NULL, 106.71, 26.58, '09:00-17:00', 0.00, 0, '1小时', '1月-12月', '独自,情侣,朋友', 4.88, 33879, 8406, 1992, '["地标","古建筑","夜景","南明河"]', '["历史文化","地标","古建筑"]', '0851-85503811', '["免费参观","夜景灯光效果很美"]', 1),
('海口骑楼老街', 2, '骑楼老街位于海南省海口市，是中国历史文化名街。街道两旁是具有南洋风格的骑楼建筑，见证了海口作为南洋贸易港口的繁华历史。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211426343910.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211568861302.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211538720510.jpeg"]', '海南省海口市龙华区骑楼老街', '海口', '海南', '龙华区', '4A', 110.35, 20.04, '全天开放', 0.00, 0, '2-3小时', '11月-次年4月', '独自,情侣,朋友', 4.42, 34516, 15082, 2375, '["骑楼","南洋风格","历史街区","美食"]', '["历史文化","建筑","美食"]', '0898-66250521', '["清补凉和老爸茶是特色","建议傍晚去氛围好"]', 1),
('拉萨八廓街', 4, '八廓街位于西藏拉萨市城关区，是拉萨最古老的街道之一。以大昭寺为中心，是藏族人民转经朝圣的必经之路，也是拉萨最繁华的商业街。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211421301671.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211427591005.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211343639862.jpg"]', '西藏自治区拉萨市城关区八廓街', '拉萨', '西藏', '城关区', NULL, 91.133, 29.652, '全天开放', 0.00, 0, '2-3小时', '5月-10月', '独自,情侣,朋友', 4.62, 44575, 1548, 3762, '["转经","藏族","朝圣","手工艺"]', '["城市观光","民族","购物"]', NULL, '["顺时针方向转经是藏族传统","可购买藏族手工艺品"]', 1),
('兰州中山桥', 4, '中山桥位于甘肃省兰州市城关区，始建于1907年，是黄河上第一座铁桥。桥长233米，横跨黄河两岸，是兰州的标志性建筑。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211444748634.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211366008528.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211403175565.jpg"]', '甘肃省兰州市城关区中山路', '兰州', '甘肃', '城关区', NULL, 103.83, 36.06, '全天开放', 0.00, 0, '1小时', '5月-10月', '独自,情侣,朋友', 4.8, 21437, 13459, 2822, '["黄河铁桥","地标","百年历史"]', '["城市观光","地标","历史"]', NULL, '["夜景灯光效果好","旁边可乘坐羊皮筏子"]', 1),
('西宁东关清真大寺', 5, '东关清真大寺位于青海省西宁市城东区，始建于明洪武年间，是西北地区四大清真寺之一。建筑风格融合了中国传统和伊斯兰建筑特色。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211551188934.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211539406079.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211539406074.jpg"]', '青海省西宁市城东区东关大街34号', '西宁', '青海', '城东区', NULL, 101.79, 36.62, '08:00-18:00', 0.00, 0, '1小时', '5月-10月', '独自,朋友', 4.13, 35567, 3637, 806, '["清真寺","伊斯兰","建筑","宗教"]', '["宗教寺庙","伊斯兰","建筑"]', '0971-8175383', '["免费参观但需尊重宗教习俗","周五主麻日不对外开放"]', 1),
('银川沙湖', 1, '沙湖位于宁夏回族自治区石嘴山市，是一处融合了江南水乡与大漠风光的独特景区。湖水、沙山、芦苇、飞鸟、游鱼构成了独特的自然景观。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211435510638.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211435510896.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211299665456.jpg"]', '宁夏回族自治区石嘴山市平罗县', '石嘴山', '宁夏', '平罗县', '5A', 106.36, 38.83, '08:00-18:00', 60.00, 1, '4-6小时', '5月-10月', '朋友,家庭', 4.6, 27852, 8563, 4165, '["沙湖","湿地","沙漠","候鸟"]', '["自然风光","湖泊","沙漠"]', '0952-6685018', '["沙雕大世界值得一看","可体验沙漠和水上项目"]', 1),
('呼和浩特大召寺', 5, '大召寺位于内蒙古呼和浩特市玉泉区，始建于明万历年间，是呼和浩特最早兴建的喇嘛教寺院。寺内有银佛、龙雕、壁画三绝。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211385500601.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211387624182.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211364383807.jpg"]', '内蒙古自治区呼和浩特市玉泉区大召前街', '呼和浩特', '内蒙古', '玉泉区', '4A', 111.66, 40.81, '08:00-18:00', 35.00, 1, '1-2小时', '5月-10月', '独自,家庭', 4.2, 12546, 14476, 4341, '["喇嘛教","银佛","壁画","蒙古族"]', '["宗教寺庙","佛教","民族"]', '0471-6303154', '["银佛是镇寺之宝","大召广场夜景不错"]', 1),
('哈尔滨圣索菲亚教堂', 2, '圣索菲亚教堂位于黑龙江省哈尔滨市道里区，始建于1907年，是远东地区最大的东正教堂。拜占庭式建筑风格，洋葱头穹顶是其标志。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211495669758.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211502005633.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211477539484.jpg"]', '黑龙江省哈尔滨市道里区透笼街88号', '哈尔滨', '黑龙江', '道里区', '4A', 126.63, 45.77, '08:30-17:00', 15.00, 1, '1小时', '1月-12月', '独自,情侣,朋友', 4.21, 72463, 2217, 2239, '["东正教堂","拜占庭","地标","欧式"]', '["历史文化","教堂","建筑"]', '0451-84686904', '["广场上喂鸽子是经典体验","冬季雪景更有异域风情"]', 1),
('沈阳北陵公园', 2, '北陵公园位于辽宁省沈阳市皇姑区，是清太宗皇太极和孝端文皇后的陵墓——昭陵所在地。2004年被列为世界文化遗产，是沈阳最大的公园。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211443012624.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211415273268.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211396933814.jpg"]', '辽宁省沈阳市皇姑区泰山路12号', '沈阳', '辽宁', '皇姑区', '4A', 123.43, 41.83, '07:00-17:00', 50.00, 1, '2-3小时', '4月-10月', '独自,朋友,家庭', 4.59, 69921, 12565, 852, '["世界文化遗产","清朝","皇陵","公园"]', '["历史文化","陵墓","世界遗产"]', '024-86895241', '["隆恩殿是核心建筑","秋季红叶很美"]', 1),
('长春净月潭滑雪场', 3, '净月潭滑雪场位于长春市净月潭国家森林公园内，是亚洲最大的人工林海滑雪场。冬季银装素裹，是东北地区重要的冰雪运动基地。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211413963449.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211309445305.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211550928030.jpg"]', '吉林省长春市南关区净月大街5840号', '长春', '吉林', '南关区', '5A', 125.44, 43.79, '09:00-16:00（冬季）', 160.00, 1, '4-6小时', '12月-次年2月', '朋友,家庭', 4.53, 27092, 8784, 3610, '["滑雪","冰雪","森林","冬季运动"]', '["主题乐园","滑雪","冬季"]', '0431-84518000', '["适合初中级滑雪者","雪具可租赁"]', 1),
('石家庄赵州桥', 2, '赵州桥位于河北省石家庄市赵县，建于隋朝大业年间，是世界上现存最早、保存最好的巨大石拱桥。桥长50.82米，被誉为天下第一桥。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211443396335.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211500871821.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211428964891.jpg"]', '河北省石家庄市赵县赵州镇', '石家庄', '河北', '赵县', '4A', 114.77, 37.75, '08:00-17:30', 40.00, 1, '1-2小时', '3月-11月', '独自,朋友,家庭', 4.73, 16787, 15253, 738, '["古桥","隋朝","建筑奇迹","世界遗产"]', '["历史文化","古桥","建筑"]', '0311-84902618', '["中国四大古桥之一","建议了解李春的建桥故事"]', 1),
('济南趵突泉', 1, '趵突泉位于山东省济南市历下区，是济南三大名胜之一。泉水从地下石灰岩溶洞中涌出，日涌水量约7万立方米，被誉为天下第一泉。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211411617330.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211397292988.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211382366895.jpg"]', '山东省济南市历下区趵突泉南路1号', '济南', '山东', '历下区', '5A', 117.01, 36.66, '07:00-19:00', 40.00, 1, '2-3小时', '1月-12月', '独自,情侣,朋友,家庭', 3.94, 24637, 14661, 1045, '["天下第一泉","泉城","济南三大名胜"]', '["自然风光","泉水","城市"]', '0531-86920680', '["冬季泉水蒸腾如仙境","李清照纪念堂在园内"]', 1),
('南昌滕王阁', 2, '滕王阁位于江西省南昌市东湖区，始建于唐永徽四年，因王勃的《滕王阁序》而闻名天下。是中国古代四大名楼之一，落霞与孤鹜齐飞的千古名句即出于此。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211434694663.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211439947609.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211457970880.jpg"]', '江西省南昌市东湖区仿古街58号', '南昌', '江西', '东湖区', '5A', 115.89, 28.68, '08:00-18:00', 45.00, 1, '1-2小时', '3月-11月', '独自,朋友', 4.81, 12688, 2213, 2787, '["四大名楼","王勃","滕王阁序","赣江"]', '["历史文化","名楼","文学"]', '0791-86702036', '["建议了解王勃的故事再参观","夜景灯光秀值得一看"]', 1),
('合肥包公园', 2, '包公园位于安徽省合肥市包河区，是为纪念北宋名臣包拯而建。园内有包公祠、包公墓、清风阁等景点，是了解包青天文化的最佳去处。', '["https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211382374682.jpg", "https://vcg05.cfp.cn/creative/vcg/nowater800/new/VCG211413016141.jpg", "https://vcg02.cfp.cn/creative/vcg/nowater800/new/VCG211359070697.jpg"]', '安徽省合肥市包河区芜湖路72号', '合肥', '安徽', '包河区', '4A', 117.29, 31.85, '08:00-17:30', 20.00, 1, '2-3小时', '3月-11月', '独自,朋友,家庭', 4.61, 18594, 12603, 727, '["包拯","清官","历史","文化"]', '["历史文化","纪念馆","名人"]', '0551-62884842', '["包公祠是核心景点","清风阁可俯瞰包河"]', 1);

-- ============================================================
-- 景点评分数据
-- ============================================================
INSERT INTO `attraction_rating` (`attraction_id`, `user_id`, `scenery_score`, `fun_score`, `value_score`, `overall_score`, `comment`) VALUES
(1, 3, 3.9, 3.1, 4.0, 3.7, '交通方便，设施完善'),
(1, 2, 4.3, 4.5, 3.4, 4.1, '带孩子来很合适'),
(1, 1, 4.9, 3.9, 4.4, 4.4, '非常推荐，体验很好'),
(2, 3, 4.2, 3.5, 3.3, 3.7, '带孩子来很合适'),
(3, 3, 3.8, 3.4, 4.7, 4.0, '非常推荐，体验很好'),
(4, 1, 4.3, 3.3, 4.9, 4.2, '风景如画，拍照很出片'),
(4, 3, 4.4, 3.8, 4.0, 4.1, '很有特色的地方'),
(5, 2, 4.1, 3.8, 4.8, 4.2, '带孩子来很合适'),
(6, 1, 4.7, 3.6, 4.6, 4.3, '人有点多，但景色不错'),
(6, 2, 4.9, 4.0, 4.6, 4.5, '风景如画，拍照很出片'),
(6, 3, 3.6, 4.2, 5.0, 4.3, '很有特色的地方'),
(7, 2, 4.1, 3.7, 4.1, 4.0, '带孩子来很合适'),
(8, 2, 4.9, 4.7, 5.0, 4.9, '历史文化底蕴深厚'),
(8, 3, 3.8, 4.7, 3.8, 4.1, '性价比很高'),
(9, 2, 4.4, 4.6, 3.8, 4.3, '自然风光很棒'),
(9, 3, 5.0, 4.0, 3.1, 4.0, '带孩子来很合适'),
(9, 3, 3.7, 3.2, 4.7, 3.9, '非常推荐，体验很好'),
(10, 2, 3.0, 3.3, 4.7, 3.7, '还不错，适合周末游'),
(10, 1, 3.9, 4.9, 3.7, 4.2, '值得再来一次'),
(10, 2, 4.3, 4.7, 4.7, 4.6, '空气清新，环境优美'),
(11, 2, 4.9, 4.3, 4.8, 4.7, '很有特色的地方'),
(11, 3, 3.1, 3.1, 4.3, 3.5, '门票价格合理'),
(11, 2, 5.0, 4.5, 3.9, 4.5, '非常推荐，体验很好'),
(12, 3, 4.7, 3.9, 4.4, 4.3, '门票价格合理'),
(12, 1, 3.1, 4.6, 3.6, 3.8, '性价比很高'),
(12, 2, 3.3, 4.1, 4.1, 3.8, '导游讲解很专业'),
(13, 1, 3.4, 4.2, 3.8, 3.8, '自然风光很棒'),
(14, 2, 4.8, 3.3, 3.9, 4.0, '风景如画，拍照很出片'),
(15, 2, 4.3, 4.8, 3.9, 4.3, '风景如画，拍照很出片'),
(15, 3, 4.1, 3.1, 4.9, 4.0, '交通方便，设施完善'),
(16, 3, 4.9, 4.4, 3.9, 4.4, '风景如画，拍照很出片'),
(16, 1, 5.0, 4.7, 3.2, 4.3, '人有点多，但景色不错'),
(17, 2, 4.1, 5.0, 3.6, 4.2, '空气清新，环境优美'),
(17, 3, 3.8, 3.0, 4.7, 3.8, '导游讲解很专业'),
(17, 3, 4.9, 4.8, 4.5, 4.7, '空气清新，环境优美'),
(18, 1, 4.2, 3.7, 4.3, 4.1, '交通方便，设施完善'),
(18, 2, 4.4, 4.4, 4.3, 4.4, '空气清新，环境优美'),
(19, 3, 3.2, 4.3, 3.1, 3.5, '导游讲解很专业'),
(20, 1, 4.2, 4.5, 3.2, 4.0, '风景如画，拍照很出片'),
(20, 2, 4.5, 3.4, 3.3, 3.7, '带孩子来很合适'),
(21, 3, 4.0, 3.5, 3.3, 3.6, '门票价格合理'),
(21, 2, 4.9, 3.6, 4.7, 4.4, '导游讲解很专业'),
(22, 2, 4.9, 3.3, 4.9, 4.4, '空气清新，环境优美'),
(23, 3, 4.3, 3.8, 4.7, 4.3, '带孩子来很合适'),
(23, 2, 3.2, 3.8, 3.5, 3.5, '非常推荐，体验很好'),
(23, 2, 3.7, 4.5, 3.5, 3.9, '人有点多，但景色不错'),
(24, 2, 3.2, 3.5, 3.1, 3.3, '门票价格合理'),
(24, 3, 3.7, 4.2, 4.3, 4.1, '自然风光很棒'),
(24, 2, 4.8, 3.6, 3.8, 4.1, '还不错，适合周末游'),
(25, 1, 3.6, 4.0, 3.2, 3.6, '门票价格合理'),
(26, 1, 3.8, 3.7, 4.9, 4.1, '值得再来一次'),
(26, 3, 3.8, 4.5, 3.3, 3.9, '人有点多，但景色不错'),
(26, 3, 3.2, 4.0, 3.8, 3.7, '风景如画，拍照很出片'),
(27, 3, 3.7, 3.9, 4.9, 4.2, '空气清新，环境优美'),
(28, 1, 5.0, 3.7, 4.8, 4.5, '自然风光很棒'),
(28, 2, 3.1, 3.6, 4.9, 3.9, '空气清新，环境优美'),
(29, 1, 4.3, 4.3, 4.2, 4.3, '景色很美，值得一去！'),
(29, 1, 4.6, 3.5, 3.3, 3.8, '交通方便，设施完善'),
(30, 1, 4.7, 4.1, 4.2, 4.3, '空气清新，环境优美'),
(31, 2, 4.5, 4.6, 4.2, 4.4, '风景如画，拍照很出片'),
(32, 1, 4.1, 4.6, 3.2, 4.0, '空气清新，环境优美'),
(33, 1, 3.0, 4.6, 3.5, 3.7, '性价比很高'),
(34, 1, 3.5, 3.3, 3.8, 3.5, '非常推荐，体验很好'),
(35, 1, 4.0, 4.6, 4.0, 4.2, '非常推荐，体验很好'),
(35, 2, 4.0, 4.9, 3.1, 4.0, '导游讲解很专业'),
(35, 3, 4.0, 3.9, 4.9, 4.3, '景色很美，值得一去！'),
(36, 2, 3.9, 3.2, 4.4, 3.8, '很有特色的地方'),
(36, 1, 4.8, 3.6, 3.3, 3.9, '还不错，适合周末游'),
(37, 3, 4.3, 4.1, 3.7, 4.0, '交通方便，设施完善'),
(37, 3, 3.6, 4.0, 3.9, 3.8, '导游讲解很专业'),
(38, 1, 4.7, 4.3, 4.5, 4.5, '值得再来一次'),
(38, 1, 3.9, 4.8, 3.8, 4.2, '空气清新，环境优美'),
(38, 2, 3.8, 4.5, 3.6, 4.0, '性价比很高'),
(39, 2, 3.7, 3.3, 4.8, 3.9, '非常推荐，体验很好'),
(39, 1, 4.7, 3.2, 3.2, 3.7, '值得再来一次'),
(39, 2, 4.6, 4.1, 4.2, 4.3, '带孩子来很合适'),
(40, 2, 4.3, 3.8, 4.7, 4.3, '导游讲解很专业'),
(40, 2, 4.7, 4.4, 4.9, 4.7, '交通方便，设施完善'),
(40, 2, 3.7, 4.2, 3.4, 3.8, '自然风光很棒'),
(41, 1, 4.7, 3.7, 4.1, 4.2, '非常推荐，体验很好'),
(41, 2, 4.1, 4.6, 4.7, 4.5, '导游讲解很专业'),
(42, 3, 4.3, 4.1, 4.2, 4.2, '自然风光很棒'),
(42, 3, 3.5, 3.4, 4.4, 3.8, '风景如画，拍照很出片'),
(42, 2, 3.7, 3.4, 3.3, 3.5, '自然风光很棒'),
(43, 1, 3.3, 4.3, 3.1, 3.6, '值得再来一次'),
(43, 3, 3.4, 3.8, 3.7, 3.6, '性价比很高'),
(44, 3, 3.6, 4.9, 4.2, 4.2, '门票价格合理'),
(44, 1, 3.3, 4.5, 3.1, 3.6, '非常推荐，体验很好'),
(45, 2, 4.3, 4.0, 3.9, 4.1, '风景如画，拍照很出片'),
(45, 1, 4.5, 3.2, 3.9, 3.9, '风景如画，拍照很出片'),
(46, 3, 4.2, 4.1, 3.6, 4.0, '历史文化底蕴深厚'),
(46, 2, 5.0, 3.1, 3.4, 3.8, '历史文化底蕴深厚'),
(46, 1, 4.5, 3.6, 3.2, 3.8, '很有特色的地方'),
(47, 2, 3.4, 3.8, 4.4, 3.9, '带孩子来很合适'),
(47, 3, 4.7, 4.6, 3.1, 4.1, '空气清新，环境优美'),
(47, 3, 3.1, 3.0, 4.8, 3.6, '空气清新，环境优美'),
(48, 3, 3.9, 3.8, 4.3, 4.0, '风景如画，拍照很出片'),
(48, 1, 3.8, 4.9, 3.3, 4.0, '交通方便，设施完善'),
(49, 3, 4.8, 3.2, 4.7, 4.2, '历史文化底蕴深厚'),
(49, 2, 4.2, 4.0, 3.8, 4.0, '风景如画，拍照很出片'),
(50, 2, 3.4, 4.6, 3.2, 3.7, '自然风光很棒'),
(50, 1, 4.1, 3.4, 4.6, 4.0, '性价比很高'),
(50, 3, 4.9, 4.3, 3.3, 4.2, '非常推荐，体验很好'),
(51, 2, 3.4, 4.2, 4.5, 4.0, '自然风光很棒'),
(52, 1, 4.9, 4.3, 3.9, 4.4, '交通方便，设施完善'),
(53, 2, 4.4, 4.8, 4.3, 4.5, '交通方便，设施完善'),
(53, 2, 4.7, 4.3, 3.3, 4.1, '非常推荐，体验很好'),
(53, 2, 3.9, 3.6, 3.5, 3.7, '景色很美，值得一去！'),
(54, 3, 3.1, 3.9, 3.1, 3.4, '性价比很高'),
(54, 2, 3.2, 4.7, 4.7, 4.2, '交通方便，设施完善'),
(55, 3, 3.8, 4.2, 4.9, 4.3, '值得再来一次'),
(55, 1, 3.9, 4.6, 4.3, 4.3, '性价比很高'),
(55, 3, 4.0, 3.3, 3.1, 3.5, '非常推荐，体验很好'),
(56, 3, 3.2, 4.3, 3.1, 3.5, '值得再来一次'),
(56, 2, 5.0, 4.0, 4.2, 4.4, '性价比很高'),
(57, 2, 3.8, 4.5, 4.4, 4.2, '景色很美，值得一去！'),
(57, 3, 4.3, 3.1, 3.2, 3.5, '自然风光很棒'),
(58, 2, 3.5, 4.7, 4.9, 4.4, '交通方便，设施完善'),
(58, 1, 3.7, 4.2, 3.3, 3.7, '性价比很高'),
(59, 3, 4.4, 3.8, 4.2, 4.1, '非常推荐，体验很好'),
(59, 2, 4.1, 4.3, 3.7, 4.0, '还不错，适合周末游'),
(60, 3, 4.7, 4.5, 4.4, 4.5, '带孩子来很合适'),
(60, 1, 4.3, 3.8, 3.7, 3.9, '性价比很高'),
(60, 2, 3.4, 3.4, 4.9, 3.9, '很有特色的地方'),
(61, 1, 5.0, 3.3, 3.6, 4.0, '导游讲解很专业'),
(62, 3, 4.5, 4.7, 4.8, 4.7, '景色很美，值得一去！'),
(63, 2, 4.8, 4.2, 4.2, 4.4, '还不错，适合周末游'),
(63, 1, 3.4, 4.4, 4.2, 4.0, '门票价格合理'),
(63, 1, 4.4, 3.1, 3.7, 3.7, '值得再来一次'),
(64, 2, 4.2, 4.5, 4.6, 4.4, '历史文化底蕴深厚'),
(65, 1, 3.6, 4.6, 3.9, 4.0, '空气清新，环境优美'),
(65, 1, 3.7, 4.9, 5.0, 4.5, '很有特色的地方'),
(65, 2, 4.6, 4.0, 3.8, 4.1, '还不错，适合周末游'),
(66, 3, 3.3, 3.5, 4.3, 3.7, '空气清新，环境优美'),
(67, 3, 4.9, 4.4, 4.0, 4.4, '非常推荐，体验很好'),
(67, 2, 3.2, 3.3, 3.9, 3.5, '带孩子来很合适'),
(68, 2, 3.2, 4.8, 4.6, 4.2, '门票价格合理'),
(69, 1, 3.8, 3.8, 3.7, 3.8, '人有点多，但景色不错'),
(69, 1, 3.7, 3.1, 4.9, 3.9, '空气清新，环境优美'),
(70, 3, 3.7, 3.3, 3.1, 3.4, '门票价格合理'),
(70, 2, 4.4, 3.3, 4.4, 4.0, '很有特色的地方'),
(70, 3, 3.0, 3.2, 3.5, 3.2, '空气清新，环境优美'),
(71, 3, 4.9, 4.2, 3.8, 4.3, '导游讲解很专业'),
(72, 1, 3.6, 3.1, 3.8, 3.5, '导游讲解很专业'),
(72, 3, 3.9, 3.2, 4.8, 4.0, '交通方便，设施完善'),
(73, 1, 4.3, 4.2, 4.4, 4.3, '风景如画，拍照很出片'),
(73, 2, 3.0, 3.7, 4.1, 3.6, '还不错，适合周末游'),
(73, 3, 4.3, 4.0, 3.7, 4.0, '带孩子来很合适'),
(74, 3, 4.6, 4.0, 3.0, 3.9, '空气清新，环境优美'),
(74, 2, 3.1, 3.8, 3.7, 3.5, '值得再来一次'),
(74, 1, 3.7, 3.1, 3.5, 3.4, '自然风光很棒'),
(75, 1, 4.5, 4.5, 3.7, 4.2, '景色很美，值得一去！'),
(75, 2, 4.1, 4.6, 3.4, 4.0, '导游讲解很专业'),
(75, 3, 3.9, 4.4, 4.3, 4.2, '导游讲解很专业'),
(76, 1, 4.4, 4.9, 3.1, 4.1, '历史文化底蕴深厚'),
(77, 1, 4.8, 3.6, 3.6, 4.0, '人有点多，但景色不错'),
(78, 2, 3.5, 4.5, 3.4, 3.8, '性价比很高'),
(78, 1, 4.7, 3.7, 3.2, 3.9, '性价比很高'),
(78, 2, 4.8, 4.5, 4.8, 4.7, '人有点多，但景色不错'),
(79, 1, 4.0, 4.0, 4.8, 4.3, '带孩子来很合适'),
(79, 2, 4.6, 4.5, 3.2, 4.1, '交通方便，设施完善'),
(80, 3, 3.6, 3.2, 3.7, 3.5, '风景如画，拍照很出片'),
(81, 2, 4.2, 3.9, 4.4, 4.2, '性价比很高'),
(81, 2, 3.1, 4.7, 3.7, 3.8, '人有点多，但景色不错'),
(82, 3, 5.0, 4.9, 3.2, 4.4, '自然风光很棒'),
(82, 2, 3.7, 4.6, 4.4, 4.2, '景色很美，值得一去！'),
(83, 3, 4.4, 3.9, 3.3, 3.9, '历史文化底蕴深厚'),
(84, 2, 3.7, 4.9, 3.1, 3.9, '还不错，适合周末游'),
(84, 3, 3.9, 3.7, 3.9, 3.8, '非常推荐，体验很好'),
(84, 3, 3.3, 3.7, 3.6, 3.5, '风景如画，拍照很出片'),
(85, 1, 3.1, 3.4, 4.0, 3.5, '门票价格合理'),
(86, 1, 3.5, 3.5, 3.9, 3.6, '交通方便，设施完善'),
(86, 2, 4.4, 5.0, 3.4, 4.3, '还不错，适合周末游'),
(86, 1, 3.9, 4.8, 3.9, 4.2, '非常推荐，体验很好'),
(87, 2, 4.3, 4.4, 4.1, 4.3, '风景如画，拍照很出片'),
(87, 2, 4.7, 4.4, 4.9, 4.7, '自然风光很棒'),
(87, 1, 4.6, 4.0, 3.2, 3.9, '历史文化底蕴深厚'),
(88, 1, 4.6, 3.1, 4.1, 3.9, '性价比很高'),
(89, 3, 3.3, 4.8, 3.1, 3.7, '人有点多，但景色不错'),
(89, 3, 4.4, 4.1, 4.5, 4.3, '空气清新，环境优美'),
(90, 1, 3.3, 3.6, 3.1, 3.3, '很有特色的地方'),
(90, 3, 4.0, 3.3, 4.7, 4.0, '带孩子来很合适'),
(90, 3, 4.2, 4.5, 3.3, 4.0, '带孩子来很合适'),
(91, 1, 4.0, 3.6, 3.3, 3.6, '门票价格合理'),
(92, 1, 3.7, 4.9, 4.8, 4.5, '空气清新，环境优美'),
(92, 1, 3.5, 4.3, 4.1, 4.0, '还不错，适合周末游'),
(92, 3, 3.6, 4.1, 4.0, 3.9, '还不错，适合周末游'),
(93, 3, 3.3, 4.3, 4.4, 4.0, '交通方便，设施完善'),
(93, 2, 4.7, 4.1, 4.7, 4.5, '景色很美，值得一去！'),
(93, 1, 3.1, 4.3, 4.2, 3.9, '自然风光很棒'),
(94, 3, 3.8, 4.3, 4.0, 4.0, '自然风光很棒'),
(95, 2, 4.3, 3.6, 3.5, 3.8, '导游讲解很专业'),
(95, 3, 3.8, 3.9, 4.4, 4.0, '还不错，适合周末游'),
(95, 2, 3.8, 3.9, 3.7, 3.8, '还不错，适合周末游'),
(96, 3, 3.6, 5.0, 3.7, 4.1, '人有点多，但景色不错'),
(96, 1, 4.5, 4.0, 3.2, 3.9, '历史文化底蕴深厚'),
(97, 1, 3.5, 3.5, 3.2, 3.4, '风景如画，拍照很出片'),
(97, 2, 4.7, 3.8, 4.9, 4.5, '门票价格合理'),
(98, 2, 4.9, 4.4, 3.4, 4.2, '还不错，适合周末游'),
(99, 3, 3.9, 4.8, 4.0, 4.2, '非常推荐，体验很好'),
(99, 2, 4.0, 4.9, 3.4, 4.1, '性价比很高'),
(100, 1, 3.6, 4.2, 4.7, 4.2, '自然风光很棒'),
(101, 2, 4.1, 4.7, 3.9, 4.2, '门票价格合理'),
(101, 2, 4.5, 4.5, 3.7, 4.2, '人有点多，但景色不错'),
(102, 3, 4.1, 3.7, 3.6, 3.8, '人有点多，但景色不错'),
(103, 2, 4.5, 3.6, 5.0, 4.4, '自然风光很棒'),
(103, 3, 3.2, 5.0, 3.8, 4.0, '导游讲解很专业'),
(103, 2, 4.1, 3.7, 3.3, 3.7, '交通方便，设施完善'),
(104, 2, 4.0, 3.1, 3.3, 3.5, '性价比很高'),
(104, 2, 4.0, 3.3, 4.8, 4.0, '还不错，适合周末游'),
(104, 2, 4.9, 3.6, 3.8, 4.1, '交通方便，设施完善'),
(105, 2, 4.2, 4.0, 4.0, 4.1, '很有特色的地方'),
(105, 3, 4.1, 3.9, 3.0, 3.7, '性价比很高'),
(105, 3, 3.2, 3.8, 3.6, 3.5, '门票价格合理'),
(106, 3, 4.3, 4.2, 3.5, 4.0, '自然风光很棒'),
(106, 3, 4.2, 4.4, 4.2, 4.3, '还不错，适合周末游'),
(106, 3, 4.3, 4.2, 4.7, 4.4, '还不错，适合周末游'),
(107, 1, 3.1, 4.9, 3.2, 3.7, '景色很美，值得一去！'),
(107, 2, 3.6, 3.3, 4.4, 3.8, '人有点多，但景色不错'),
(107, 3, 4.5, 4.8, 4.7, 4.7, '值得再来一次'),
(108, 1, 4.4, 4.0, 4.1, 4.2, '自然风光很棒'),
(108, 2, 4.1, 3.6, 3.3, 3.7, '门票价格合理'),
(108, 3, 3.7, 3.7, 4.5, 4.0, '值得再来一次'),
(109, 3, 4.6, 3.5, 4.0, 4.0, '历史文化底蕴深厚'),
(109, 2, 4.1, 3.4, 4.9, 4.1, '风景如画，拍照很出片'),
(109, 2, 4.4, 4.4, 4.0, 4.3, '很有特色的地方'),
(110, 3, 4.9, 4.6, 3.5, 4.3, '非常推荐，体验很好'),
(110, 3, 4.4, 3.8, 5.0, 4.4, '空气清新，环境优美'),
(111, 1, 3.6, 3.6, 4.4, 3.9, '性价比很高'),
(111, 2, 4.3, 4.5, 3.4, 4.1, '空气清新，环境优美'),
(112, 2, 4.9, 4.4, 3.3, 4.2, '交通方便，设施完善'),
(112, 3, 4.2, 3.5, 4.3, 4.0, '带孩子来很合适'),
(112, 1, 4.3, 3.1, 3.8, 3.7, '值得再来一次'),
(113, 1, 4.4, 3.3, 4.9, 4.2, '门票价格合理'),
(113, 1, 3.8, 4.9, 4.8, 4.5, '很有特色的地方'),
(114, 1, 4.5, 3.6, 3.6, 3.9, '景色很美，值得一去！'),
(114, 1, 4.3, 3.5, 4.5, 4.1, '空气清新，环境优美'),
(114, 1, 4.8, 3.4, 4.8, 4.3, '还不错，适合周末游'),
(115, 2, 4.6, 3.4, 4.5, 4.2, '空气清新，环境优美'),
(116, 1, 3.0, 4.1, 3.2, 3.4, '性价比很高'),
(116, 2, 3.9, 4.3, 4.0, 4.1, '门票价格合理'),
(117, 3, 3.9, 3.4, 3.2, 3.5, '还不错，适合周末游'),
(118, 2, 4.3, 4.4, 3.0, 3.9, '性价比很高'),
(118, 2, 4.1, 4.5, 3.4, 4.0, '空气清新，环境优美'),
(118, 3, 4.8, 4.7, 4.0, 4.5, '非常推荐，体验很好'),
(119, 3, 3.2, 5.0, 4.0, 4.1, '门票价格合理'),
(119, 1, 3.0, 3.8, 3.9, 3.6, '风景如画，拍照很出片'),
(120, 2, 4.2, 4.1, 3.5, 3.9, '性价比很高'),
(120, 3, 3.5, 4.3, 3.9, 3.9, '还不错，适合周末游'),
(121, 3, 4.0, 4.8, 4.4, 4.4, '风景如画，拍照很出片'),
(121, 3, 3.2, 4.8, 3.6, 3.9, '交通方便，设施完善'),
(122, 1, 4.9, 4.0, 3.9, 4.3, '交通方便，设施完善'),
(123, 2, 4.4, 4.8, 4.5, 4.6, '导游讲解很专业'),
(123, 3, 4.7, 4.5, 4.4, 4.5, '交通方便，设施完善'),
(124, 3, 3.9, 4.4, 3.2, 3.8, '交通方便，设施完善'),
(125, 1, 4.7, 4.8, 3.4, 4.3, '带孩子来很合适'),
(126, 1, 3.7, 4.0, 3.2, 3.6, '历史文化底蕴深厚'),
(127, 3, 4.0, 4.8, 3.9, 4.2, '景色很美，值得一去！'),
(127, 2, 3.3, 3.8, 4.1, 3.7, '带孩子来很合适'),
(127, 2, 4.3, 3.6, 3.0, 3.6, '风景如画，拍照很出片'),
(128, 3, 3.4, 3.1, 3.8, 3.4, '值得再来一次'),
(129, 3, 5.0, 4.9, 3.1, 4.3, '门票价格合理'),
(130, 1, 3.6, 3.4, 3.3, 3.4, '自然风光很棒'),
(130, 3, 4.3, 3.8, 5.0, 4.4, '人有点多，但景色不错'),
(131, 2, 3.8, 4.4, 4.3, 4.2, '带孩子来很合适'),
(131, 1, 4.3, 3.7, 3.4, 3.8, '带孩子来很合适'),
(132, 3, 3.3, 5.0, 4.7, 4.3, '导游讲解很专业'),
(132, 1, 5.0, 3.9, 4.4, 4.4, '人有点多，但景色不错'),
(133, 2, 4.6, 3.5, 3.7, 3.9, '风景如画，拍照很出片'),
(133, 1, 4.9, 4.5, 3.2, 4.2, '导游讲解很专业'),
(133, 3, 3.8, 4.5, 4.8, 4.4, '景色很美，值得一去！'),
(134, 1, 3.1, 4.2, 3.9, 3.7, '自然风光很棒'),
(135, 1, 3.5, 3.1, 3.9, 3.5, '带孩子来很合适'),
(135, 1, 4.5, 4.6, 3.3, 4.1, '风景如画，拍照很出片'),
(135, 1, 4.6, 4.5, 3.6, 4.2, '交通方便，设施完善'),
(136, 2, 3.5, 4.8, 5.0, 4.4, '带孩子来很合适'),
(136, 1, 3.8, 3.6, 4.1, 3.8, '交通方便，设施完善'),
(136, 3, 4.8, 4.3, 3.9, 4.3, '很有特色的地方'),
(137, 2, 4.9, 4.3, 4.6, 4.6, '性价比很高'),
(138, 2, 3.8, 3.6, 3.4, 3.6, '带孩子来很合适'),
(138, 2, 3.5, 3.5, 4.7, 3.9, '还不错，适合周末游'),
(138, 2, 4.8, 4.1, 4.9, 4.6, '带孩子来很合适'),
(139, 1, 3.8, 3.5, 3.7, 3.7, '非常推荐，体验很好'),
(139, 2, 4.7, 3.2, 4.4, 4.1, '历史文化底蕴深厚'),
(139, 1, 3.5, 4.3, 4.2, 4.0, '空气清新，环境优美'),
(140, 1, 4.6, 4.2, 4.3, 4.4, '历史文化底蕴深厚'),
(141, 1, 4.7, 3.7, 4.9, 4.4, '景色很美，值得一去！'),
(141, 1, 4.9, 4.9, 3.2, 4.3, '导游讲解很专业'),
(142, 1, 4.4, 4.4, 3.8, 4.2, '历史文化底蕴深厚'),
(142, 3, 3.6, 3.6, 3.9, 3.7, '自然风光很棒'),
(143, 2, 3.5, 4.0, 4.0, 3.8, '非常推荐，体验很好'),
(143, 3, 3.9, 4.7, 4.8, 4.5, '性价比很高'),
(144, 2, 3.1, 4.7, 4.5, 4.1, '景色很美，值得一去！'),
(144, 2, 4.1, 4.2, 4.4, 4.2, '风景如画，拍照很出片'),
(145, 1, 3.7, 3.8, 4.7, 4.1, '很有特色的地方'),
(146, 3, 4.9, 3.7, 4.5, 4.4, '很有特色的地方'),
(146, 3, 4.3, 4.0, 3.3, 3.9, '导游讲解很专业'),
(146, 2, 3.3, 4.4, 4.0, 3.9, '门票价格合理'),
(147, 3, 4.9, 4.0, 4.7, 4.5, '非常推荐，体验很好'),
(147, 3, 3.1, 3.0, 3.3, 3.1, '自然风光很棒'),
(147, 1, 3.1, 3.3, 3.4, 3.3, '很有特色的地方'),
(148, 1, 4.2, 4.3, 4.2, 4.2, '很有特色的地方'),
(148, 3, 4.0, 3.0, 4.1, 3.7, '景色很美，值得一去！'),
(149, 3, 4.4, 4.1, 3.6, 4.0, '带孩子来很合适'),
(150, 3, 3.9, 4.9, 3.4, 4.1, '门票价格合理'),
(150, 1, 4.0, 3.5, 4.2, 3.9, '风景如画，拍照很出片'),
(150, 2, 3.5, 3.8, 3.7, 3.7, '人有点多，但景色不错'),
(151, 3, 3.5, 4.4, 3.6, 3.8, '空气清新，环境优美'),
(151, 1, 4.3, 4.7, 4.5, 4.5, '非常推荐，体验很好'),
(152, 2, 3.8, 4.0, 4.3, 4.0, '值得再来一次'),
(152, 1, 3.2, 4.7, 4.3, 4.1, '性价比很高'),
(153, 1, 4.8, 4.8, 3.5, 4.4, '门票价格合理'),
(153, 3, 4.8, 4.0, 3.1, 4.0, '非常推荐，体验很好'),
(153, 3, 4.9, 4.8, 4.1, 4.6, '景色很美，值得一去！'),
(154, 2, 3.0, 3.4, 3.3, 3.2, '空气清新，环境优美'),
(155, 2, 4.9, 3.6, 4.1, 4.2, '交通方便，设施完善'),
(155, 2, 4.4, 4.1, 3.8, 4.1, '值得再来一次'),
(155, 1, 4.6, 4.4, 3.2, 4.1, '值得再来一次'),
(156, 1, 3.2, 3.5, 3.2, 3.3, '交通方便，设施完善'),
(156, 3, 4.9, 3.8, 4.5, 4.4, '景色很美，值得一去！'),
(157, 2, 4.6, 3.9, 3.5, 4.0, '带孩子来很合适'),
(157, 2, 3.9, 4.4, 4.3, 4.2, '非常推荐，体验很好'),
(157, 3, 3.6, 3.5, 3.1, 3.4, '风景如画，拍照很出片'),
(158, 2, 4.4, 4.3, 4.5, 4.4, '性价比很高'),
(159, 1, 3.2, 4.9, 3.9, 4.0, '导游讲解很专业'),
(159, 2, 3.2, 3.2, 3.9, 3.4, '带孩子来很合适'),
(160, 3, 3.8, 3.1, 3.7, 3.5, '非常推荐，体验很好'),
(160, 3, 4.2, 3.6, 3.8, 3.9, '风景如画，拍照很出片'),
(160, 2, 3.8, 3.2, 4.8, 3.9, '门票价格合理'),
(161, 3, 4.0, 4.4, 3.3, 3.9, '风景如画，拍照很出片'),
(162, 2, 4.0, 3.1, 3.9, 3.7, '人有点多，但景色不错'),
(162, 2, 4.0, 3.2, 3.5, 3.6, '历史文化底蕴深厚'),
(163, 3, 4.0, 4.2, 3.9, 4.0, '还不错，适合周末游'),
(163, 2, 3.0, 3.8, 4.7, 3.8, '人有点多，但景色不错'),
(163, 2, 3.9, 3.9, 4.6, 4.1, '自然风光很棒'),
(164, 1, 3.6, 4.2, 4.4, 4.1, '很有特色的地方'),
(164, 2, 3.4, 3.8, 3.6, 3.6, '值得再来一次'),
(164, 3, 4.3, 4.9, 4.6, 4.6, '性价比很高'),
(165, 2, 4.6, 3.8, 3.2, 3.9, '交通方便，设施完善'),
(165, 1, 4.2, 4.1, 4.4, 4.2, '带孩子来很合适'),
(166, 3, 3.9, 4.4, 3.9, 4.1, '风景如画，拍照很出片'),
(167, 1, 4.6, 4.6, 3.8, 4.3, '很有特色的地方'),
(167, 1, 4.3, 3.5, 4.3, 4.0, '值得再来一次'),
(167, 2, 4.6, 3.1, 5.0, 4.2, '很有特色的地方'),
(168, 2, 4.1, 4.8, 4.7, 4.5, '人有点多，但景色不错'),
(168, 3, 3.1, 4.9, 4.4, 4.1, '导游讲解很专业'),
(168, 3, 4.2, 4.8, 3.2, 4.1, '历史文化底蕴深厚'),
(169, 3, 3.7, 4.3, 3.1, 3.7, '自然风光很棒'),
(169, 3, 4.3, 4.9, 3.7, 4.3, '导游讲解很专业'),
(169, 2, 3.2, 3.2, 3.4, 3.3, '值得再来一次'),
(170, 3, 4.2, 4.2, 3.9, 4.1, '人有点多，但景色不错'),
(170, 2, 4.5, 4.2, 4.0, 4.2, '性价比很高'),
(170, 1, 4.0, 3.6, 4.8, 4.1, '非常推荐，体验很好'),
(171, 3, 3.3, 3.6, 3.9, 3.6, '历史文化底蕴深厚'),
(172, 2, 3.7, 3.2, 4.4, 3.8, '很有特色的地方'),
(172, 2, 3.1, 3.1, 4.4, 3.5, '景色很美，值得一去！'),
(172, 2, 4.3, 4.4, 3.3, 4.0, '值得再来一次'),
(173, 3, 3.3, 3.3, 4.1, 3.6, '很有特色的地方'),
(174, 2, 4.3, 3.4, 4.3, 4.0, '人有点多，但景色不错'),
(175, 3, 4.2, 3.4, 4.2, 3.9, '人有点多，但景色不错'),
(176, 3, 3.4, 3.6, 4.4, 3.8, '导游讲解很专业'),
(177, 3, 3.2, 4.7, 3.4, 3.8, '性价比很高'),
(178, 2, 3.2, 4.3, 4.0, 3.8, '自然风光很棒'),
(179, 3, 3.8, 3.8, 3.2, 3.6, '性价比很高'),
(179, 2, 4.2, 4.6, 4.4, 4.4, '风景如画，拍照很出片'),
(180, 3, 3.2, 3.3, 3.2, 3.2, '风景如画，拍照很出片'),
(180, 1, 3.4, 4.4, 4.0, 3.9, '人有点多，但景色不错'),
(180, 3, 3.3, 3.8, 3.4, 3.5, '自然风光很棒'),
(181, 3, 4.8, 4.9, 3.3, 4.3, '还不错，适合周末游'),
(181, 2, 3.6, 3.4, 4.7, 3.9, '交通方便，设施完善'),
(181, 1, 4.7, 4.9, 4.0, 4.5, '历史文化底蕴深厚'),
(182, 3, 4.0, 4.0, 4.4, 4.1, '人有点多，但景色不错'),
(182, 3, 4.7, 4.0, 3.2, 4.0, '景色很美，值得一去！'),
(183, 2, 3.1, 3.4, 3.6, 3.4, '导游讲解很专业'),
(184, 3, 4.5, 4.5, 4.1, 4.4, '非常推荐，体验很好'),
(184, 3, 3.2, 4.6, 4.6, 4.1, '性价比很高'),
(185, 1, 4.5, 3.9, 3.4, 3.9, '人有点多，但景色不错'),
(185, 1, 4.5, 4.3, 3.5, 4.1, '空气清新，环境优美'),
(185, 1, 3.9, 3.7, 4.6, 4.1, '很有特色的地方'),
(186, 1, 3.4, 4.5, 4.4, 4.1, '空气清新，环境优美'),
(187, 3, 4.3, 3.3, 3.3, 3.6, '历史文化底蕴深厚'),
(187, 1, 4.6, 4.1, 4.1, 4.3, '交通方便，设施完善'),
(187, 3, 3.3, 5.0, 3.6, 4.0, '交通方便，设施完善'),
(188, 3, 5.0, 4.4, 3.2, 4.2, '导游讲解很专业'),
(188, 2, 4.8, 5.0, 4.7, 4.8, '还不错，适合周末游'),
(189, 1, 3.5, 4.4, 4.7, 4.2, '导游讲解很专业'),
(189, 3, 3.8, 3.3, 4.3, 3.8, '自然风光很棒'),
(189, 2, 3.8, 3.2, 4.6, 3.9, '导游讲解很专业'),
(190, 2, 4.0, 3.6, 4.9, 4.2, '性价比很高'),
(191, 2, 3.7, 3.0, 4.5, 3.7, '值得再来一次'),
(191, 1, 4.5, 3.9, 4.3, 4.2, '性价比很高'),
(192, 3, 3.8, 4.9, 4.6, 4.4, '很有特色的地方'),
(193, 2, 4.7, 3.7, 4.2, 4.2, '交通方便，设施完善'),
(193, 2, 4.6, 4.8, 4.5, 4.6, '景色很美，值得一去！'),
(194, 3, 4.6, 3.9, 3.2, 3.9, '空气清新，环境优美'),
(195, 2, 3.9, 5.0, 3.9, 4.3, '值得再来一次'),
(196, 3, 3.6, 4.7, 4.8, 4.4, '非常推荐，体验很好'),
(197, 2, 3.5, 3.7, 4.8, 4.0, '还不错，适合周末游'),
(197, 1, 3.8, 3.6, 4.4, 3.9, '历史文化底蕴深厚'),
(197, 2, 4.3, 4.4, 3.0, 3.9, '很有特色的地方'),
(198, 1, 4.2, 4.0, 3.9, 4.0, '景色很美，值得一去！'),
(199, 2, 4.6, 5.0, 3.8, 4.5, '还不错，适合周末游'),
(199, 2, 3.1, 3.8, 4.5, 3.8, '非常推荐，体验很好'),
(199, 1, 3.6, 4.8, 3.3, 3.9, '导游讲解很专业'),
(200, 3, 5.0, 3.3, 4.1, 4.1, '景色很美，值得一去！');

-- ============================================================
-- 用户足迹数据
-- ============================================================
INSERT INTO `user_footprint` (`user_id`, `attraction_id`, `visit_date`, `note`) VALUES
(1, 167, '2025-12-13', '推荐朋友来'),
(1, 43, '2024-06-26', '下次还来'),
(1, 113, '2023-05-21', '很棒的体验'),
(1, 90, '2023-09-20', '值得一去'),
(1, 185, '2025-10-10', NULL),
(1, 55, '2025-01-28', NULL),
(1, 161, '2025-05-07', '值得一去'),
(1, 192, '2025-09-07', '印象深刻'),
(1, 39, '2024-05-21', '很棒的体验'),
(1, 106, '2023-08-13', '很棒的体验'),
(1, 158, '2023-08-21', '值得一去'),
(1, 176, '2023-09-01', '推荐朋友来'),
(1, 114, '2024-03-13', NULL),
(1, 56, '2025-07-12', '值得一去'),
(1, 23, '2025-03-25', '风景很美'),
(1, 26, '2023-01-19', '很棒的体验'),
(1, 36, '2023-05-07', '很棒的体验'),
(1, 193, '2023-07-17', '推荐朋友来'),
(1, 32, '2025-12-17', NULL),
(2, 109, '2025-11-22', '印象深刻'),
(2, 180, '2023-05-23', '风景很美'),
(2, 103, '2025-04-02', '下次还来'),
(2, 22, '2023-07-04', '风景很美'),
(2, 164, '2025-10-02', '推荐朋友来'),
(2, 138, '2025-12-22', '印象深刻'),
(2, 158, '2023-02-01', '印象深刻'),
(2, 40, '2023-12-01', '下次还来'),
(2, 72, '2024-06-17', '值得一去'),
(2, 80, '2025-05-06', '推荐朋友来'),
(2, 21, '2023-12-25', NULL),
(2, 131, '2024-12-04', NULL),
(2, 53, '2023-06-26', '风景很美'),
(2, 84, '2024-09-03', '推荐朋友来'),
(2, 101, '2025-10-21', '很棒的体验'),
(2, 151, '2024-08-17', '推荐朋友来'),
(3, 128, '2025-02-08', '印象深刻'),
(3, 146, '2025-01-02', NULL),
(3, 43, '2023-04-02', '风景很美'),
(3, 95, '2024-07-05', '下次还来'),
(3, 41, '2023-09-26', '印象深刻'),
(3, 66, '2025-11-14', '下次还来'),
(3, 198, '2024-04-14', '印象深刻'),
(3, 27, '2024-05-27', '很棒的体验'),
(3, 147, '2025-06-04', '值得一去'),
(3, 173, '2025-01-06', '下次还来');

-- ============================================================
-- 攻略数据
-- ============================================================
INSERT INTO `strategy` (`id`, `user_id`, `title`, `cover_image`, `destination`, `days`, `budget`, `season`, `content`, `images`, `tags`, `view_count`, `like_count`, `favorite_count`, `comment_count`, `is_ai_generated`, `featured`, `pinned`, `pinned_time`, `audit_status`, `audit_reason`, `summary`, `root_strategy_id`, `status`, `visibility`, `deleted`, `create_time`, `update_time`) VALUES (1,1,'重庆三日网红打卡','/uploads/20260513/be51e63710b04578807bc902b6c3e7f5.webp','重庆',3,2500.00,'summer','<h2 style=\"text-align: start;\">🏠住宿建议</h2><p style=\"text-align: start;\">住宿首选<strong>解放碑或洪崖洞周边</strong>，步行可达核心景点，交通、餐饮最方便。预算200-400元/晚的亚朵或全季酒店性价比不错，追求沉浸式体验的可选洪崖洞附近的江景酒店。</p><h2 style=\"text-align: start;\">🍲行前TIPS（必看！）</h2><p style=\"text-align: start;\">提前在支付宝领取“重庆乘车码”，地铁覆盖核心景点，非常方便。山城爬坡下坎是常态，<strong>一定要穿舒适防滑的运动鞋</strong>，高跟鞋会让你怀疑人生！日均2万步是基本操作。晴雨伞、防晒霜、肠胃药也建议备上，微辣的火锅足以让外地朋友“菊花残”。</p><h2 style=\"text-align: start;\">📅Day 1：魔幻都市 · 渝中经典线</h2><p style=\"text-align: start;\"><strong>主题定位</strong>：初识山城魅力，打卡最经典的城市地标与魔幻夜景</p><p style=\"text-align: start;\"><strong>上午</strong>：从<strong>解放碑</strong>核心地标拍下第一张游客照开始，步行3分钟钻进<strong>八一好吃街</strong>，用一碗“好又来酸辣粉”或软糯的山城小汤圆开启味觉之旅。随后前往<strong>白象居</strong>——这座24层无电梯的老居民楼完美展现了重庆的魔幻地形，<strong>3号楼连廊</strong>是拍长江索道穿楼的爆款机位，还可以拍到索道与来福士同框的绝美照片，门票免费。</p><p style=\"text-align: start;\"><strong>下午</strong>：体验“空中巴士”<strong>长江索道</strong>（单程20元），建议提前在“长江索道”公众号线上购票，买单程即可。随后漫步<strong>山城步道</strong>，推荐“山城巷”段，沿崖壁而建的悬空栈道视野极佳，沉浸式感受老重庆的旧时光与烟火气。</p><p style=\"text-align: start;\"><strong>傍晚/晚上</strong>：夜幕降临，直奔<strong>洪崖洞</strong>——现实版《千与千寻》，吊脚楼夜景封神。18:00-21:00是亮灯黄金期，美得令人窒息。<strong>最佳机位</strong>不要挤在洪崖洞楼下，推荐去<strong>千厮门大桥中段</strong>或<strong>大剧院江滩公园</strong>拍全景，完全避开人挤人的观景台。</p><p style=\"text-align: start;\"><strong>晚餐</strong>推荐本地人常去的<strong>珮姐老火锅</strong>或防空洞里的<strong>洞亭火锅</strong>，微辣锅底配上鲜毛肚、鸭肠，再点一瓶唯怡豆奶解辣，幸福感直接拉满。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/628317d1f5144c9a82db49d7f018d513.webp\" alt=\"\" data-href=\"\" style=\"\"></p><blockquote style=\"text-align: start;\">📍今日景点汇总：解放碑（免费）→ 八一好吃街 → 白象居（免费）→ 长江索道（20元）→ 山城步道（免费）→ 洪崖洞夜景（免费）→ 千厮门大桥</blockquote><h2 style=\"text-align: start;\">📅Day 2：文艺市井 · 沉浸烟火气</h2><p style=\"text-align: start;\"><strong>主题定位</strong>：深入老街古镇，感受山城的文艺浪漫与市井烟火</p><p style=\"text-align: start;\"><strong>上午</strong>：第一站<strong>李子坝轻轨站</strong>，观看轻轨穿楼的魔幻奇观。<strong>网红打卡机位</strong>不止A出口观景台——B出口的“小黄楼”街角拍照更出片，或去“新都巷步道”俯瞰轻轨入楼与江景同框。步行约10分钟到<strong>鹅岭二厂</strong>，这个由旧印刷厂改造的文创公园充满工业复古与文艺清新的混搭气质，在楼顶观景平台可同时看到长江和嘉陵江交汇，随手一拍都是文艺大片。</p><p style=\"text-align: start;\"><strong>下午</strong>：前往千年古镇<strong>磁器口</strong>，主街比较商业化，但拐进“金碧巷”“小街巷”能找到老茶馆和手工糍粑店。<strong>陈昌银麻花</strong>是必带伴手礼。</p><p style=\"text-align: start;\"><strong>傍晚</strong>：推荐登上<strong>南山一棵树观景台</strong>（门票30元）俯瞰渝中半岛璀璨夜景，或选择<strong>南滨路</strong>吹江风看夜景，比千厮门大桥更安静，适合慢慢散步。</p><p style=\"text-align: start;\"><strong>晚餐</strong>试试地道的重庆江湖菜，尖椒鸡、辣子肥肠都是硬菜。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/6d80db7d043d4bd785ddbf8349900fd6.webp\" alt=\"\" data-href=\"\" style=\"\"></p><blockquote style=\"text-align: start;\">📍今日景点汇总：李子坝轻轨穿楼（免费）→ 鹅岭二厂（免费）→ 磁器口古镇（免费）→ 南山一棵树（30元）</blockquote><h2 style=\"text-align: start;\">📅Day 3：人文探索 · 浪漫收尾</h2><p style=\"text-align: start;\"><strong>主题定位</strong>：回归人文底蕴，轻松漫步结束山城之旅</p><p style=\"text-align: start;\"><strong>上午</strong>：参观<strong>三峡博物馆</strong>（免费，需提前预约），重点看“壮丽三峡”展厅和环幕电影。对面就是<strong>人民大礼堂</strong>，这座仿古建筑气势恢宏，门票8元，很适合拍照打卡。</p><p style=\"text-align: start;\"><strong>下午</strong>：前往<strong>龙门浩老街</strong>，这里是夜晚氛围极佳的网红古建筑街区，烟火气息浓郁，石门框住东水门大桥是经典构图。时间充裕还可去<strong>黄桷坪涂鸦街</strong>和<strong>交通茶馆</strong>，感受老重庆的慢生活艺术气息。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/91a389d350be47debfedb5d5eaeb5167.webp\" alt=\"\" data-href=\"\" style=\"\"></p><blockquote style=\"text-align: start;\">📍今日景点汇总：三峡博物馆（免费）→ 人民大礼堂（8元）→ 龙门浩老街/黄桷坪涂鸦街</blockquote>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','重庆，这座号称“8D魔幻山城”的地方，简直是现实版的赛博朋克世界——你以为你在一楼，其实你在22楼；导航在这里常常“失灵”，但那种迷失感恰恰是山城给你的独特拥抱。这份攻略整理了重庆最值得去的网红打卡点和本地人私藏机位，帮你三天玩转魔幻山城，吃好、拍好、不赶场！',1,1,1,0,'2026-05-13 10:56:06','2026-05-13 10:56:06'),(2,1,'盐城三日游','/uploads/20260513/d18efddb06a34d079559c605e99115eb.webp','盐城',3,1500.00,'spring','<h3 style=\"text-align: start;\"><strong>Day 1：探秘鹤舞东方，初遇湿地精灵</strong></h3><p style=\"text-align: start;\"><strong>上午：抵达盐城，寻味海盐古韵与市井烟火</strong></p><p style=\"text-align: start;\">抵达盐城后，第一站建议前往<strong>盐城市博物馆</strong>（开放时间：9:00-17:30，17:00停止入馆）。这里从历史文化、生态文明、城市发展、民俗非遗四个维度，展现了盐城上下五千年的文明史，能让你对这座城市有个系统性的了解。紧邻博物馆的是<strong>新四军重建军部旧址</strong>，一砖一瓦都记录着新四军抗战的辉煌史迹。</p><p style=\"text-align: start;\">中午可以到<strong>盐城民俗博物馆（竹林大饭店）</strong> 用餐。它不只是一家饭店，更是一座城市的记忆博物馆，通过老字号、老手艺、老建筑、老场景四大主题，让你在品味<strong>盐城八大碗</strong>、<strong>东台鱼汤面</strong>等非遗美食的同时，感受老盐城的市井风情。</p><p style=\"text-align: start;\"><strong>下午：仙鹤之约，赴一场生命的交响乐</strong></p><p style=\"text-align: start;\">下午前往<strong>丹顶鹤湿地生态旅游区</strong>（开放时间：09:00-17:00）。这里是全球最大的丹顶鹤越冬地之一，也是东亚—澳大利西亚候鸟迁徙路线上至关重要的“服务区”。景区很大，建议乘坐观光车游览。最震撼的体验是观看丹顶鹤野化训练或放飞表演，成群的丹顶鹤从头顶飞过，优雅的身姿在蓝天白沙间翩跹，那一刻心灵仿佛都被净化了。别忘了参观全球唯一以丹顶鹤为主题的博物馆，还可以近距离邂逅网红鹤“盐·加利福尼亚”。</p><p style=\"text-align: start;\"><strong>晚上：入住亭湖区酒店，品尝当地海鲜</strong></p><p style=\"text-align: start;\">傍晚返回亭湖区，入住酒店。晚餐推荐尝试盐城的“鲜”，大纵湖的醉蟹、滩涂上的蟛蜞等都是不可错过的野性鲜味。饭后可以逛逛水街夜景，千年盐韵与现代光影交融，别有一番风味。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/79323d989ce840f4bd788838e0961f3f.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\"><strong>Day 2：东方肯尼亚，与“神兽”糜鹿的不期而遇</strong></h3><p style=\"text-align: start;\"><strong>上午：深入荒野，探访“东方肯尼亚”</strong></p><p style=\"text-align: start;\">上午出发前往<strong>中华麋鹿园</strong>，这是全球唯一以“湿地生态、麋鹿保护”为主题的国家5A级旅游景区，也是世界上面积最大、种群数量最多的野生麋鹿园。乘坐专用观光车进入核心区，是一片真正狂野的天地——成群的麋鹿在滩涂上奔腾，仿佛来到了世界边缘，场面壮观震撼，被誉为“东方肯尼亚”。准备十块钱胡萝卜，还能体验零距离投喂麋鹿的乐趣。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/8aba0115813b488da2ae29163b71dfe4.webp\" alt=\"\" data-href=\"\" style=\"\"></p><p style=\"text-align: start;\"><strong>下午：野鹿荡，邂逅野生精灵的自由</strong></p><p style=\"text-align: start;\">从中华麋鹿园自驾片刻就到了<strong>野鹿荡</strong>，这是一片更加原始开阔的野生麋鹿自然家园，林茂草丰，湿地湖泊星罗棋布。在这里静静坐着，看野生麋鹿在波光与蹄印间漫步，不远处还有白鹭沙鸥翩飞，这种蓬勃的野生感会让所有烦恼消散。</p><p style=\"text-align: start;\"><strong>晚上：入住东台/黄海森林公园周边</strong></p><p style=\"text-align: start;\">傍晚驱车前往东台方向，入住黄海森林公园周边或东台市区的酒店。如果你预订了<strong>黄海森森小木屋</strong>，那就拥有了整片森林——夜晚在林间散步，星空和虫鸣伴你入眠。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/97ebf4378cfe4fd281f4b5d42fdcbcfb.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\"><strong>Day 3：漫步“天空之镜”，走进绿野仙踪</strong></h3><p style=\"text-align: start;\"><strong>清晨：黄海森林，天然氧吧的治愈之旅</strong></p><p style=\"text-align: start;\"><strong>黄海森林公园</strong>是华东地区规模最大的人造生态林园，总面积超6万亩，园内遍布水杉、池杉等高大乔木，被称为“天然氧吧”。清晨入园，雾气在林间弥漫，仿佛走进绿野仙踪童话世界。必打卡的是“森林之眼”观景台——8层高的建筑，下层是透明玻璃栈道，上层是镜面地面，站在上面仿佛双脚踩在“天空”上，能拍出超梦幻的亲子合影。乘坐森林小火车慢悠悠穿梭于林海间，全程约3公里，途经红杉林、管轨滑道等景观，阳光透过枝叶洒下斑驳光影。</p><p style=\"text-align: start;\"><strong>中午：前往条子泥，观候鸟与滩涂奇观</strong></p><p style=\"text-align: start;\">午饭后前往<strong>条子泥景区</strong>，这里是世界自然遗产黄（渤）海候鸟栖息地的核心区。退潮后的滩涂宛如一幅巨大的沙画，波光粼粼，被称为盐城的“天空之镜”。条子泥位于东亚—澳大利西亚候鸟迁徙的关键路线上，每年数百万只鸟类来此“朝圣”，是全球最重要的滨海湿地生态系统之一。带上望远镜，还能邂逅被称作“鸟中大熊猫”的极危鸟类勺嘴鹬。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/e311ed795ffd4cc786cd1fbf6c9a0364.webp\" alt=\"\" data-href=\"\" style=\"\"></p><p style=\"text-align: start;\"><strong>下午：返程</strong></p><p style=\"text-align: start;\">游览结束后，驱车返回盐城市区或高铁站（约1.5-2小时）。时间充裕的话可以去<strong>巴斗渔乡</strong>尝尝新鲜捕捞的海鲜，带走一份属于黄海之滨的鲜甜记忆。</p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','盐城三日游全攻略：湿地仙鹤、糜鹿森林、古韵文化\n盐城，这座镶嵌在黄海之滨的“东方湿地之都”，以得天独厚的自然生态和两千年海盐古韵，仿佛一幅徐徐展开的诗意画卷。这里既有世界自然遗产黄海湿地的壮阔，也有丹顶鹤翩跹飞舞的灵动与麋鹿踏浪奔跑的野性奔放，更有穿越千年的古风遗韵。\n这份盐城三日游攻略，为你安排了三种不同风格的旅行线路。你可以根据自己的兴趣随意切换，将每日的行程自由重组，拼凑出专属于你的假期',2,1,1,0,'2026-05-13 10:58:58','2026-05-13 10:58:58'),(3,1,'西安古都三日游','/uploads/20260513/ebf9f77ce747428984fae9e6130f4cc6.webp','西安',3,2500.00,'summer','<h3 style=\"text-align: start;\">🗺️ 你的西安古都三日游行程</h3><h4 style=\"text-align: start;\"><strong>Day 1: 初识长安，触摸盛唐烟火</strong></h4><ul><li style=\"text-align: start;\">上午：登临西安城墙，俯瞰古城脊梁从历史最悠久、保存最完整的西安城墙（推荐从南门（永宁门） 登城）开启旅程。在南门城墙上，还能找到刻有“西安”字样的地标，是绝佳的打卡点。强烈建议租一辆自行车在城墙上骑行，体验“古今交融”的独特景致。骑行一圈耗时约1-2小时，可根据体力自行调整。</li><li style=\"text-align: start;\">中午：漫步书院门，寻味地道陕菜从南门下城墙后，步行前往充满墨香的书院门文化街。这里文房四宝、碑帖拓片店铺林立，非常值得一逛。午餐选择在这附近解决，避开网红店，找一家本地小馆，点上肉夹馍、凉皮，最能感受西安人的日常滋味。</li><li style=\"text-align: start;\">下午：探访陕西历史博物馆，夜游大雁塔陕西历史博物馆：中国第一座大型现代化国家级博物院，是了解十三朝古都历史的精华所在。文物浩如烟海，强烈建议租一个语音导览器或聘请讲解员。⚠️ 这是全国最难预约的博物馆之一，务必提前通过官方渠道抢票。晚餐与夜游：晚上前往大雁塔北广场，欣赏壮观的音乐喷泉。之后，灯火璀璨的大唐不夜城就是你的下一站。璀璨的灯光与唐风建筑让这里充满“梦回长安”的沉浸感，你还能看到“不倒翁小姐姐”等街头表演。</li><li style=\"text-align: start;\">🏠 住宿建议行程首日集中在城区，建议住在钟楼/鼓楼附近或大雁塔附近。这两个区域交通便利（地铁覆盖），且周边美食选择丰富，能省去不少通勤时间。</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/e7fffc66c49944018ed6adb4573c0f90.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h4 style=\"text-align: start;\"><strong>Day 2: 梦回大秦，邂逅皇家温泉</strong></h4><ul><li style=\"text-align: start;\">上午：探秘秦始皇兵马俑上午前往被誉为“世界第八大奇迹”的秦始皇兵马俑博物馆。⚠️ 请务必通过官方公众号“秦始皇帝陵博物院”提前预约门票。景区内部分为1、2、3号坑和文物陈列厅。建议请一位专业讲解员，为这“千人千面”的地下军团注入生命。如果你是自驾或打车，请注意在官方指定停车场停车，拒绝任何路边拉客人员的导游或购物推荐。</li><li style=\"text-align: start;\">中午：华清宫附近用餐从兵马俑出发，车程约15分钟即可抵达华清宫。午餐可以在景区周边解决，推荐选择明码标价的餐馆，品尝油泼面、Biangbiang面等陕西特色面食，实惠又美味。</li><li style=\"text-align: start;\">下午：游览皇家园林华清宫游览华清宫，这里是唐玄宗与杨贵妃的爱情故事发生地，也是“西安事变”的旧址。重点参观唐代御汤遗址和五间厅等历史遗迹。如果体力有限，可以选择乘坐景区观光车。</li><li style=\"text-align: start;\">晚上（可选）：观赏《长恨歌》如果预算和体力允许，强烈推荐观看在华清宫上演的大型实景历史舞剧 《长恨歌》 。华丽的舞美、动人的故事将带你梦回大唐盛世。演出门票需求极大，务必提前预订。</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/7085f4cdf03d46dc945ca43003dd4b87.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h4 style=\"text-align: start;\"><strong>Day 3: 钟鼓回响，悠享古都余韵</strong></h4><ul><li style=\"text-align: start;\">上午：漫步中心地标与美食探索钟楼 &amp; 鼓楼：作为西安的城市中心地标，你可以不必登楼，只需在广场上拍照打卡即可。开元商场五楼的观景台是拍摄钟楼全景的最佳位置。回民街探索：紧邻鼓楼的回民街是著名的美食聚集地，但建议多往侧街（如大皮院、洒金桥等）探索，那里的本地小店更多，味道更纯正。</li><li style=\"text-align: start;\">中午：品尝地道非遗美食午餐可以安排一顿地道的羊肉泡馍。本地人认为，掰馍是影响泡馍味道最关键的一步，要掰成黄豆大小才够味。记得配上糖蒜和辣酱，这才是地道吃法。</li><li style=\"text-align: start;\">下午：游览市内小众景点，适时返程下午的行程比较灵活，可以补充游览市区内的小众景点，或直接准备返程：小雁塔 &amp; 西安博物院：环境清幽，是感受长安古韵的好去处。青龙寺：除樱花季（3-4月）外，这里古寺宁静，适合休闲漫步。永兴坊：比回民街更平价，子长煎饼、粉汤羊血等都是不错的选择。</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/a85c86839f714ca0bcf05b592d12f5d0.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','准备去西安开启三天古都之旅，这一趟会很精彩。我为你整理了一份涵盖经典景点、地道美食和实用贴士的三日游攻略，希望能让你的行程安排得更顺畅。',3,1,1,0,'2026-05-13 11:00:52','2026-05-13 11:00:52'),(4,3,'香港维多利亚港三日游','/uploads/20260513/798b25a95d0b40448cb7e13eef2f946b.webp','香港',3,5000.00,'all','<h2 style=\"text-align: start;\">🌊 Day 1：维港经典线 —— 两岸精华一日游</h2><p style=\"text-align: start;\"><strong>关键词：天星小轮 · 星光大道 · 摩天轮 · 幻彩咏香江</strong></p><h3 style=\"text-align: start;\">上午：尖沙咀海滨漫步（09:30-12:00）</h3><p style=\"text-align: start;\">从尖沙咀地铁站E出口出发，步行5分钟即可到达星光大道。全长约800米，你能找到成龙、周润发等多位香港影坛巨星的掌印和签名牌匾，还有李小龙雕像可合影。对岸便是中环摩天大楼群构成的维港天际线，白天海面船来船往、气象万千。</p><p style=\"text-align: start;\">👉 <strong>拍照提示</strong>：上午光线较好，适合在星光大道栏杆旁拍摄维港全景，<strong>李小龍雕像和李嘉诚同款打卡机位</strong>一定要去！</p><p style=\"text-align: start;\">💡 <strong>人文小彩蛋</strong>：wwwtc mall L5的“维港空中花园”设有观景望远镜，每日中午还能听到独特的“午炮鸣响”，是鲜为人知的维港文化体验。</p><h3 style=\"text-align: start;\">中午：地道港式茶餐厅（12:00-13:30）</h3><p style=\"text-align: start;\">午餐体验最正宗的港式茶餐厅，推荐必点：菠萝油+丝袜奶茶+滑蛋叉烧饭/咸柠七。人均约50-80港币就能吃到撑。</p><p style=\"text-align: start;\">尖沙咀附近推荐：兰芳园（丝襪奶茶鼻祖）、华嫂冰室（番茄雞翼通粉）、K11 MUSEA商场内也有众多选择。</p><h3 style=\"text-align: start;\">下午：天星小轮 → 中环码头 → 摩天轮（14:00-17:00）</h3><p><br></p><table style=\"width: auto;\"><tbody><tr><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">活动</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">票价</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">时长</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">小贴士</th></tr><tr><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">天星小轮（尖沙咀→中环）</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">约HK$3.2-5</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">约10分钟</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">上层风景更佳，日落时段更美</td></tr><tr><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">香港摩天轮</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">约HK$20</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">3圈约15分钟</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">中环码头旁，俯瞰维港全景</td></tr></tbody></table><p style=\"text-align: start;\">乘坐百年历史的天星小轮穿梭维港，几块港币就能饱览两岸风光，是最地道的维港体验。下船后，在中环海滨漫步，欣赏香港摩天轮和立法会大楼等特色建筑。</p><p style=\"text-align: start;\"><strong>💡 隐藏体验</strong>：如果时间允许，可步行至中环街市打卡复古時鐘樓梯，这里是电影《无名》的取景地之一，港片氛围感拉满。</p><h3 style=\"text-align: start;\">傍晚：太平山顶，俯瞰维港（17:30-20:00）</h3><p style=\"text-align: start;\">由中环码头乘地铁或步行至中环缆车站（约15分钟），乘坐百年历史的山顶缆车上太平山顶——车程约7分钟，车厢倾斜陡行，窗外高楼仿佛“快要倒下”，极为震撼。</p><p style=\"text-align: start;\"><br></p><table style=\"width: auto;\"><tbody><tr><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">方案</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">票价参考</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">小贴士</th></tr><tr><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">缆车往返 + 凌霄阁摩天台套票</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">约HK$148</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">上山坐右侧，下山坐左侧，风景最佳</td></tr><tr><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">15号巴士</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">约HK$11</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">省钱之选，沿途风景也不错，适合下山时使用</td></tr></tbody></table><p style=\"text-align: start;\">山顶的夜景与日本函馆、意大利那不勒斯并称“世界三大夜景”，登临观景台的那一刻，整个维港灯火璀璨尽收眼底。</p><p style=\"text-align: start;\">⭐ <strong>本地人私藏</strong>：如果不想花钱进凌霄阁，可走太平山的「卢吉道」环山步道，免费且人少，视角完全不输观景台。</p><h3 style=\"text-align: start;\">晚上：幻彩咏香江灯光秀（20:00-21:30）</h3><p style=\"text-align: start;\">下山后返回尖沙咀海滨，赶在每晚8点前抢占最佳观赏位置，欣赏“幻彩咏香江”灯光秀——由港岛两岸超过40栋摩天大楼同步亮灯，搭配雷射和音乐，历時约10分钟。</p><p style=\"text-align: start;\">🌃 <strong>晚餐变奏选项</strong>：看完灯光秀后可前往庙街夜市体验港式夜生活。兴记煲仔饭的蠔饼和煲仔饭是必试地道口味。如果爱吃海鲜，庙街大排档的椒盐濑尿虾也绝不能错过。</p><p style=\"text-align: start;\">📸 <strong>别忘了这组“香港特色”照片</strong>：感受完灯火通明的热闹，搭乘天星小輪回九龍，船程中隔海可欣赏香港摩天轮，中环的摩登大樓和古老建筑在波光粼粼的夜色中尤其治愈。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/8549996f9c63499dba446b38f938caa1.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h2 style=\"text-align: start;\">🚃 Day 2：港岛文艺线 —— 中环复古与山野奇观</h2><p style=\"text-align: start;\"><strong>关键词：叮叮车 · 中环街市 · 大館 · 半山扶梯 · 南丫岛/海洋公园二选一</strong></p><h3 style=\"text-align: start;\">上午：百年叮叮车体验 → 中环街市 → 大馆（09:30-13:00）</h3><p style=\"text-align: start;\">从铜锣湾或金钟上车，仅需HK$3即可乘坐复古的双层叮叮车，穿梭港岛北岸的老街，最适合拍摄港风街景。</p><p style=\"text-align: start;\">下車后步行至中環街市（复古时钟楼梯，电影《无名》取景地），随后到荷李活道参观「大馆」，这座由前中区警署改造的艺术文化空间，红砖墙建筑和巨型穹顶极具历史魅力，内部多是免费开放。</p><p style=\"text-align: start;\">然后体验「中环半山手扶梯」，这是全世界最长的户外有盖扶梯系统，沿途经过許多港片中的经典場景，拍照，打卡网红文艺小巷，非常适合拍有故事感的照片。</p><p style=\"text-align: start;\"><strong>🍜 午餐推荐</strong>：想追求物美价廉，可以绕一点路去「文苑饭庄」原班底开的「九龙饭馆」（必比登推介），梨木烟熏熟成乳鸽皇非常惊艳；或者前往湾仔的「美丽小厨」，他们家的峰哥煲仔饭锅气十足，非常入味。</p><h3 style=\"text-align: start;\">下午：根据兴趣二选一（13:30-17:00）</h3><p><br></p><table style=\"width: auto;\"><tbody><tr><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">选择</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">交通方式</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">体验亮点</th><th colspan=\"1\" rowspan=\"1\" width=\"auto\" style=\"text-align: left;\">适合谁</th></tr><tr><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\"><strong>海洋公园</strong></td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">地铁南港島线直达</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">世界级水族馆、刺激过山车、熊猫馆</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">家庭亲子、机动游戏爱好者</td></tr><tr><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\"><strong>南丫岛悠闲半日游</strong></td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">中环码头乘船约30分钟</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">徒步海岸线、榕树湾漁村、新鲜海鮮、阿婆豆花</td><td colSpan=\"1\" rowSpan=\"1\" width=\"auto\">文青、情侣、逃离都市者</td></tr></tbody></table><p style=\"text-align: start;\">💡 <strong>生态彩蛋</strong>：如果选择动物园线，<strong>一定要去看香港新诞生的大熊猫龙凤胎“家姐细佬”！他们目前非常活泼，是全城的焦点明星，非常适合带孩子去。</strong></p><p style=\"text-align: start;\">🏖️ <strong>岛民体验</strong>：如果选择南丫岛，徒步路线推荐「榕树湾 → 索罟湾」。沿途会经过洪圣爷湾泳滩、观景亭和芦须城泳滩，全程约1.5-2小时。途中可品尝手工豆花和海鲜大餐，感受香港慢生活的一面。</p><h3 style=\"text-align: start;\">傍晚：坚尼地城日落（17:00-19:00）</h3><p style=\"text-align: start;\">傍晚前往「坚尼地城」海边篮球场或西环泳棚，这里是看日落的绝佳地点。落日余晖洒在海面上，配上对岸灯火，氛围感拉满。注：西环泳棚木桥結構簡陋，需注意安全。</p><h3 style=\"text-align: start;\">晚上：铜锣湾美食之旅（19:00-21:30）</h3><p style=\"text-align: start;\">回到港岛繁华夜市区，走访「铜锣湾」逛街或前往附近的咖啡馆。推荐义顺牛奶公司的双皮奶，口感顺滑奶香浓郁，是完美的宵夜收尾。另外「一樂燒鵝」的皮脆肉嫩也是必试。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/253a73a4217e4e17aaeee39503822935.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h2 style=\"text-align: start;\">🏛️ Day 3：九龍市井烟火线 —— 庙街夜市与复古街区</h2><p style=\"text-align: start;\"><strong>关键词：黄大仙 · 旺角油麻地 · 庙街夜市 · 西九文化区</strong></p><h3 style=\"text-align: start;\">上午：黄大仙祠祈福（09:30-11:30）</h3><p style=\"text-align: start;\">乘坐地铁观塘线至黄大仙站，前往香港香火最鼎盛的道教庙宇「黄大仙祠」。在这里可以祈福求签，感受中式传统建筑的庄重风格和浓郁的文化氛围。</p><h3 style=\"text-align: start;\">中午：深水埗地道美食探索（12:00-13:30）</h3><p style=\"text-align: start;\">从黄大仙乘坐地铁前往深水埗。深水埗是香港舊區，充滿市井生活氣息。这里的鸭寮街是淘旧货的天堂，黄金电脑商场可逛。街边小店多，推荐品尝当地鱼蛋和咖喱牛杂。</p><h3 style=\"text-align: start;\">下午：旺角油麻地复古打卡（14:00-17:00）</h3><p style=\"text-align: start;\"><strong>必拍点：旺角＆油麻地的大街小巷</strong>：</p><ul><li style=\"text-align: start;\">石板街：怀旧的石板路配老招牌，随手一拍港风拉满。</li><li style=\"text-align: start;\">女人街、花园街：在密集的红色的士和招牌霓虹灯下，能拍出极具港味的大片。</li><li style=\"text-align: start;\">怪兽大厦：前往鲗鱼涌的「怪兽大厦」，感受香港独特的建筑拥抱密集美学，这里是不少大片的摄影取景地。</li></ul><p style=\"text-align: start;\">🛍️ <strong>逛街tips</strong>：这一路的小摊小店可以买点冰箱贴、手机壳之类的小纪念品，记得货比三家，合理砍价。</p><h3 style=\"text-align: start;\">傍晚：西九文化区滨海长廊（17:00-19:00）</h3><p style=\"text-align: start;\">前往西九文化区，滨海长廊面朝维港、视野开阔，可同时看到港岛和西九龙的天际线。夕阳时分，这里是看维港落日的隐藏秘境，比尖沙咀人少、更安静。</p><p style=\"text-align: start;\">🍸 <strong>轻奢晚餐</strong>：在海港城或西九龙文化區内的咖啡厅边喝小酒边看晚霞，結束這趟豐富的香港行。</p><h3 style=\"text-align: start;\">晚上：返程前采购伴手礼</h3><p style=\"text-align: start;\">最后根据航班时间，在尖沙咀海港城或铜锣湾、佐敦一带购买手信。推荐买杏仁饼、蛋卷、老婆饼、机场可以顺便带走免税酒水。<strong>建议提前2.5小时到达机场</strong>，留足出境和安检时间。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/e1a8202d6c084923b06444af210a5b47.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h2 style=\"text-align: start;\">💡 维港旅行深度小贴士</h2><ul><li style=\"text-align: start;\">天气应对：无论是否下雨，维港的日与夜都有不一样的魅力。如果下雨，湿漉漉的路面汇聚高楼霓虹灯的倒影，充满电影感，拍照也很出片</li><li style=\"text-align: start;\">出行建议：建议穿舒适的运动鞋，每日步数约15000步</li><li style=\"text-align: start;\">省钱技巧：上山别只坐缆车，中环15路巴士仅需11港币直达，省钱还能看风景</li><li style=\"text-align: start;\">礼仪提醒：地铁内全程严禁进食，否则将被处以高额罚款；室内公众地方和部分户外地方禁止吸烟，乱抛垃圾或吐痰也会被处罚</li><li style=\"text-align: start;\">交通安全：香港靠左行驶，过马路注意遵守当地交通规则；乘坐港铁东铁线注意车厢级别，误入头等车厢会自动被扣罚HK$1000附加费</li></ul><p style=\"text-align: start;\">希望这份攻略助你畅游香港，度过一个难忘的维多利亚港三日之旅！如有其他问题，欢迎随时咨询～</p><p><br></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','',4,1,1,0,'2026-05-13 11:03:39','2026-05-13 11:03:39'),(5,1,'苏州园林两日游','/uploads/20260513/6b0f22d414974b499081a00e39e9413c.webp','苏州',2,2000.00,'spring','<h2 style=\"text-align: start;\">🌿 Day 1：江南园林巅峰 × 古城水巷慢生活</h2><p style=\"text-align: start;\"><strong>行程主题</strong>：沉浸式解锁苏州“大园林”精髓，一步一景读懂江南造园艺术。</p><h3 style=\"text-align: start;\">☀️ 上午（7:30-12:00）| 拙政园</h3><p style=\"text-align: start;\"><strong>门票</strong>：旺季80-90元，淡季70元（学生半价）<br><strong>开放时间</strong>：7:30-17:30（17:00停止检票）<br><strong>建议游览时长</strong>：2.5-3小时</p><p style=\"text-align: start;\">首站直奔<strong>拙政园</strong>——中国四大名园之首、苏州园林中面积最大、保存最完整的古典山水园林。务必在<strong>7:30一开园即进</strong>，此时游人稀少、晨光柔和，拍照极佳，还可避开9:30后涌入的旅行团大潮。</p><p style=\"text-align: start;\"><strong>核心看点</strong>：以“水”为中心的精妙布局，精华在<strong>中部花园</strong>。重点游览<strong>远香堂</strong>（全园核心，四面观景）、<strong>梧竹幽居</strong>（匾额经典）、<strong>小飞虹</strong>（江南园林少见的廊桥）、<strong>与谁同坐轩</strong>（取意“明月、清风、我”）、<strong>卅六鸳鸯馆</strong>（古典戏台）。园内设有免费讲解牌，跟着解说走可深入理解“移步换景”的精妙。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/b0291f4d8d5443869143b08c45cb7355.webp\" alt=\"\" data-href=\"\" style=\"\"></p><blockquote style=\"text-align: start;\">💡 拍照Tips：香洲码头拍园林倒影非常出片，建议不要只走主路，侧边小径更显清幽，常有意外的好景致。</blockquote><h3 style=\"text-align: start;\">🌞 中午（12:00-13:30）| 平江路午餐</h3><p style=\"text-align: start;\">拙政园出口步行约600米即达<strong>平江路</strong>（直线距离<strong>10分钟内可达</strong>，地铁6号线“拙政园苏博站”出站即达）。平江路是苏州保存最完好的历史街区，“水陆并行、河街相邻”的格局尽显江南水乡风情。</p><p style=\"text-align: start;\"><strong>午餐推荐</strong>：</p><ul><li style=\"text-align: start;\">哑巴生煎：皮薄馅大、焦脆爆汁，苏州生煎的天花板</li><li style=\"text-align: start;\">松鹤楼：百年老字号，招牌松鼠桂鱼外脆里嫩，酸甜适口，地道苏帮菜必点</li><li style=\"text-align: start;\">李百蟹·蟹黄面：平江路总店，蟹黄浇头满满溢出，落地窗加河景搭配“嗦面+平江”同框照</li><li style=\"text-align: start;\">拙林里糖粥：隐藏在小巷侧边，糯米裹着桂花酱，甜而不腻</li></ul><h3 style=\"text-align: start;\">☁️ 下午（13:30-16:00）| 苏州博物馆 &amp; 狮子林</h3><p style=\"text-align: start;\"><strong>苏州博物馆（本馆）</strong> 与拙政园隔壁，贝聿铭大师的封山之作，白墙黛瓦与池塘光影将现代建筑与江南美学完美交融。重点看片石假山、紫藤园、大玉璧。本馆必须<strong>提前7天预约</strong>，周一闭馆。建议15:00前入馆，预留1.5-2小时。</p><p style=\"text-align: start;\">苏博出来直接步行至<strong>狮子林</strong>（相距约10分钟步行）。狮子林以“假山王国”闻名，上千吨太湖石堆叠而成，九条路线、21个洞口形成天然迷宫，乾隆皇帝曾盛赞“神妙夺天工”。必看<strong>假山群</strong>（可慢慢“钻”40分钟以上），每一拐弯都有新发现；<strong>真趣亭</strong>为乾隆亲题“真趣”二字；<strong>问梅阁</strong>的梅花窗纹精美典雅。</p><p style=\"text-align: start;\">门票旺季40元/淡季30元，开放时间7:30-17:30。</p><h3 style=\"text-align: start;\">🌙 傍晚 &amp; 夜间 | 古运河漫步</h3><p style=\"text-align: start;\">继续漫步平江路，体验<strong>手摇船</strong>（40元/人，约40分钟船程，从拙政园码头出发，船娘常唱苏州评弹，韵味十足）。</p><p style=\"text-align: start;\">逛罢沿大儒巷转至<strong>观前街</strong>——苏州百年商业老街，玄妙观牌坊是经典打卡点。晚餐推荐：<strong>得月楼</strong>（苏帮菜老字号，松鼠鳜鱼、响油鳝糊正宗）或<strong>姑苏家宴</strong>，饭后<strong>采芝斋</strong>买酥糖、<strong>黄天源</strong>买现做糕团当伴手礼。</p><p style=\"text-align: start;\">结束充实的一天，返回观前街/平江路周边酒店休息。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/8dbb249c00914e30935457dab045e232.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h2 style=\"text-align: start;\">🌿 Day 2：千年禅意古迹 × 水乡烟火余韵</h2><p style=\"text-align: start;\"><strong>行程主题</strong>：探寻苏州千年历史余韵与园林造景的极致美学。</p><h3 style=\"text-align: start;\">☀️ 上午（7:30-11:30）| 虎丘</h3><p style=\"text-align: start;\"><strong>门票</strong>：旺季70元，淡季60元<br><strong>开放时间</strong>：7:30-18:00（5-10月）<br><strong>建议游览时长</strong>：3-4小时</p><p style=\"text-align: start;\">“到苏州不游虎丘，乃憾事也”。虎丘是苏州的标志性景观，<strong>云岩寺塔</strong>（中国第一斜塔）屹立千年，倾斜程度超过比萨斜塔。经典游览路线从南门牌坊进入，沿途参观<strong>断梁殿</strong>（无梁建筑奇迹）、<strong>剑池</strong>（传说藏吴王宝剑）、<strong>千人石</strong>、<strong>虎丘塔</strong>（登塔俯瞰姑苏全景）。</p><blockquote style=\"text-align: start;\">💡 景区山高仅30多米，景点密集，老人孩子游逛亦不觉吃力，建议清晨8点前入园避开团队人流。</blockquote><h3 style=\"text-align: start;\">🌞 中午（11:30-13:30）| 午餐休息</h3><p style=\"text-align: start;\">虎丘附近简餐，或返回市区（推荐前往观前街/山塘街区域午餐）。推荐<strong>朱鸿兴奥灶面</strong>（焖肉浇头，肉酥烂汤底鲜）或同得兴枫镇大肉面。</p><h3 style=\"text-align: start;\">☁️ 下午 &amp; 傍晚 | 留园 + 寒山寺 / 枫桥 + 七里山塘</h3><p style=\"text-align: start;\"><strong>留园</strong>（约2-3小时）：中国四大名园之一，“小中见大、精巧雅致”誉为“吴中名园之冠”。必看<strong>三绝</strong>：<strong>冠云峰</strong>（江南园林最大湖石）、大理石座屏、鱼化石。门票旺季55元/淡季45元，开放时间7:30-17:30。</p><p style=\"text-align: start;\"><strong>寒山寺 + 枫桥景区</strong>（约1-1.5小时）：因唐代张继“姑苏城外寒山寺，夜半钟声到客船”驰名。门票约20元，开放时间7:30-17:00，可进寺听钟声祈福，枫桥边拍古运河怀旧照。</p><p style=\"text-align: start;\"><strong>七里山塘街</strong>（傍晚至夜晚）：被誉为“苏州的缩影”，运河两岸古桥老宅，红灯笼点缀河面，乘画舫夜游（约40元/人）是最佳体验方式，船娘唱评弹增色不少。</p><p style=\"text-align: start;\"><strong>山塘街美食必吃</strong>：</p><ul><li style=\"text-align: start;\">荣阳楼油氽团子/生煎包：本土老店，皮薄馅足爆汁</li><li style=\"text-align: start;\">朱新年点心店：汤团、馄饨承载老苏州人民记忆</li><li style=\"text-align: start;\">江南蟹壳黄（山塘街地铁口）：现烤酥饼，每日大排长龙</li></ul><p style=\"text-align: start;\">夜游山塘之后结束第二天的园林文化之旅。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/1b7c97cc098446c3937fc8c5b93985a7.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h2 style=\"text-align: start;\">🌿 Day 3：江南市井烟火 × 现代苏州收尾</h2><p style=\"text-align: start;\"><strong>行程主题</strong>：感受苏州别样的市井烟火气，再以一景现代苏州风光收尾。</p><p style=\"text-align: start;\"><strong>推荐路线</strong>：<strong>双塔市集</strong> + <strong>苏州公园 / 沧浪亭</strong> + <strong>东园 / 耦园</strong> + <strong>返程前弹性安排</strong></p><p style=\"text-align: start;\"><strong>方案A：市井烟火风（推荐）</strong><br>上午前往由老菜场改造而成的<strong>双塔市集</strong>——几何美学与市井烟火碰撞的网红打卡地。必尝虾仁面、碧螺春奶茶、糕团点心。之后漫步至<strong>苏州公园</strong>感受市民日常，或前往<strong>沧浪亭</strong>（苏州现存历史最悠久的园林之一，“沧浪之水清兮”的诗意之源）。若时间充裕，顺路探访小园林<strong>耦园</strong>（枕河而建，“枕河听橹”的一日之趣），避开人流，慢品江南水乡与园林的结合之美。</p><p style=\"text-align: start;\"><strong>方案B：现代苏州风</strong><br>前往<strong>金鸡湖景区</strong>（免费），打卡“大裤衩”<strong>东方之门</strong>，逛大陆首家<strong>诚品书店</strong>。如逢周五/周六晚，可欣赏音乐喷泉（19:30开放，需提前官方查公告并早些占位）。在<strong>李公堤</strong>国际风情街品尝苏帮创意融合菜，逛够了直接乘地铁返程。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/742e38f71b53439db7af277f7a391517.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','以下是一份苏州三日游（以苏州园林为主题安排两日核心行程）攻略，涵盖每日详细行程安排、实用贴士以及地道美食推荐，助你轻松畅游姑苏园林与江南古韵。',5,1,1,0,'2026-05-13 11:06:26','2026-05-13 11:06:26'),(6,3,'上海三日文艺之旅','/uploads/20260513/f590ef1bfa884a07a00b408e5ddcec02.webp','上海',3,3500.00,'autumn','<h3 style=\"text-align: start;\">🥐 Day 1 | 梧桐区漫步：在梧桐树下喝咖啡、逛书店、看洋房</h3><p style=\"text-align: start;\"><strong>上午 | 武康路与安福路：上海的文艺底片</strong></p><p style=\"text-align: start;\">在武康大楼与天平路交叉口的店铺买一个新鲜出炉的可颂，在梧桐树荫下开始一天的漫步。</p><p style=\"text-align: start;\">上午的时光留给<strong>武康路—安福路街区</strong>。武康大楼当然是经典的起始点，但这片区域真正的魅力藏在那些交错的小巷之中。梧桐林荫铺满街道，一栋栋民国时期的洋房错落有致地分布在道路两侧。近期，武康路星巴克门店与舞蹈诗剧《只此青绿》推出了跨界联名，店内设置了“青绿腰”专属打卡点和青绿主题装饰，让宋韵风雅与咖啡醇香在日常空间里温柔相拥。</p><p style=\"text-align: start;\">从安福路继续前行，沿途会经过多家值得一逛的店铺。午餐可以选择武康路或安福路沿线的Brunch店，例如RAC Bar或O‘mills Bakery，都是口碑不错的治愈系选择。</p><p style=\"text-align: start;\"><strong>下午 | 独立书店寻访：书香浸润的午后时光</strong></p><p style=\"text-align: start;\">从安福路延伸至华山路、常熟路片区，有几家各具特色的独立书店值得专程探访。</p><p style=\"text-align: start;\">如果是艺术爱好者，可以前往长宁区红宝石路的<strong>香蕉鱼书店</strong>。这是一家主打艺术家书籍和独立出版物的艺术设计书店，进门一整面杂志墙格外吸睛，藏书涵盖设计、摄影、生活方式等小众精品，书架上不少书籍贴着主理人手写的推荐语，随手翻阅总能偶遇惊喜。</p><p style=\"text-align: start;\">如果偏爱文学与海派文化的交融，可以往陕西南路方向步行至作家协会附近的某家书店。这里是紧邻上海市作家协会、小说《繁花》中众多场景的“见证者”，被称为“邂逅作家概率最高”的地方，各类文学活动与“繁花”同款特色特饮，最适合在座位区坐下来慢慢品味。</p><p style=\"text-align: start;\">或者，去奉贤路一座不起眼的老楼里寻找那家让人沉浸的“魔法空间”。电梯门打开后，满墙旧书、复古涂鸦和暖黄灯光瞬间将人拉进塞纳河左岸的文艺氛围里，捧一本书配一杯咖啡，就能坐上整个下午。</p><p style=\"text-align: start;\"><strong>晚上 | 话剧或小剧场演出</strong></p><p style=\"text-align: start;\">傍晚时分，可以前往<strong>上海话剧艺术中心</strong>或同样精彩的<strong>宛平剧院、长江剧场</strong>。上海话剧艺术中心·后浪新潮演出季常年有高质量的小剧场作品上演；宛平剧院与长江剧场则以戏曲瑰宝著称，集结了京、昆、沪、越、淮等传统戏曲剧种。建议提前在官方票务平台查询当期演出安排并购票。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/06a9f708fa634d759fb1076c17f998e3.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">🎨 Day 2 | 西岸艺术带：一天泡在美术馆与“建筑美学”中</h3><p style=\"text-align: start;\"><strong>上午 | 西岸美术馆：蓬皮杜典藏与高规格特展</strong></p><p style=\"text-align: start;\">搭乘地铁11号线至云锦路站，出站步行约8分钟即可抵达<strong>西岸美术馆</strong>。这座由英国建筑师大卫·奇普菲尔德设计的极简“三盒子”建筑本身就是一件艺术品，落地玻璃窗将黄浦江景与室内空间无缝衔接。</p><p style=\"text-align: start;\">馆内常年与法国蓬皮杜艺术中心合作。2至3楼的《重塑景观——蓬皮杜中心典藏展》中展有毕加索、杜尚等现代艺术大师的珍品，其中《空间概念》系列尤为值得驻足。此外，近期的《VOGUE：秀场风云》也极具看点，追溯时装秀从20世纪初高级定制沙龙演变至今的历程，依托《VOGUE》杂志珍贵档案呈现跨越世纪的时尚叙事。建议给美术馆留出2到3小时的参观时间。</p><p style=\"text-align: start;\"><strong>中午 | 西岸凤巢的午间休憩</strong></p><p style=\"text-align: start;\">美术馆不远处就是<strong>西岸凤巢</strong>。这座被艺术场馆环抱的开放式商业空间集购物、餐饮等于一体，是最便利的午间休息点。也可以大胆一些，前往西岸凤巢·遇外滩，体验米其林推荐的福建菜现代演绎，环境简约现代，适合看展后的放松时刻。</p><p style=\"text-align: start;\"><strong>下午 | 油罐艺术中心与龙美术馆</strong></p><p style=\"text-align: start;\">在西岸凤巢用餐之后，沿着滨江步道向北徒步，串联起<strong>油罐艺术中心 → 龙美术馆（西岸馆）</strong>。这条路线被称为“从工业锈带到艺术秀带”的浪漫蜕变之路，沿途可一并邂逅连片春日花海与公共艺术装置。</p><p style=\"text-align: start;\">油罐艺术中心由龙华机场废弃的五个巨型储油罐改造而成，工业风的粗粝与现代艺术碰撞出独特的魅力。继续往北步行，<strong>龙美术馆（西岸馆）</strong> 近期正在展出《回响：她们的世纪——全球女性艺术特展》，精选了来自20多个国家和地区近200位女性艺术家的作品，时间跨度从20世纪初至今逾百年，堪称一部属于“她们”的百年艺术史。</p><p style=\"text-align: start;\"><strong>晚上 | 滨江夜游与第二场展览</strong></p><p style=\"text-align: start;\">如果精力充沛，傍晚时分从龙美术馆沿滨江步道继续往北，可以在西岸美术馆露台等待日落——16:30到17:00期间光线最佳，以东方明珠为背景拍摄的画面层次极其丰富。至于晚餐，西岸滨江沿线分布着多家风格各异的餐厅，从精致西餐到融合小馆选择很多。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/49cc29682cea491fa5e867b3b93c6657.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">🎪 Day 3 | 文学印象与海派烟火：鲁迅的虹口、鲜活的老城厢</h3><p style=\"text-align: start;\"><strong>上午 | 虹口文学地图：甜爱路、多伦路与鲁迅足迹</strong></p><p style=\"text-align: start;\">搭乘地铁或公交前往虹口区。从<strong>甜爱路</strong>出发，短短的路途却能带来满满的浪漫感。顺路走到<strong>鲁迅公园</strong>，这里不仅有绿树成荫的静谧感，更藏着那座见证历史的<strong>鲁迅与内山纪念书局</strong>。书店保留了当年的建筑风貌，几个房间以鲁迅的文集名被命名为“南腔北调集”“而已集”等，书架上陈列着大量鲁迅的作品。推荐点一杯名为“朝花夕拾”的特调拿铁，栀子花的清甜与咖啡的醇厚交织在一起，坐在窗边看着街景，整个上午都会变得温柔。</p><p style=\"text-align: start;\">从书店出来步行约10分钟，就是<strong>多伦路文化名人街</strong>。这里是鲁迅、茅盾、郭沫若、丁玲等文学巨匠曾居住生活过的地方，被誉为“现代文学重镇”。石板路两旁的旧式洋房隔绝了外部喧嚣，街边错落的丁玲、鲁迅等雕像在低语着那个文学激荡的年代。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/a235e15ddc4045b1a846f6f593a5599d.webp\" alt=\"\" data-href=\"\" style=\"\"></p><p style=\"text-align: start;\"><strong>下午 | 穿梭于田子坊的石库门迷宫里</strong></p><p style=\"text-align: start;\">乘坐地铁9号线至打浦桥站，前往<strong>田子坊</strong>。这里保留着最原汁原味的上海石库门弄堂格局，纵横交错的巷弄里藏着各式手作工坊、艺术工作室、创意产品和独立设计小店，烟火气与文艺感在这里奇妙地交融。在田子坊内，还可以找到沪上首家女性主题书店<strong>馨巢书屋</strong>，它在马年春节巧妙地搬入了田子坊的石库门弄堂中，藏身于熙攘的创意小店之间，成为一方独特的阅读角落。</p><p style=\"text-align: start;\">下午茶可在田子坊内随意选择一家咖啡馆小憩。</p><p style=\"text-align: start;\"><strong>晚上 | 滨江夜景与晚风中的告别</strong></p><p style=\"text-align: start;\">傍晚从田子坊出发前往<strong>外滩</strong>，或许略显传统，但无法否认，站在外滩望向陆家嘴的璀璨灯火，依然是认识上海最为情深的一课。濱江步道上的晚风会为三天充实的文艺之旅画上一个温柔的句号。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/0c06cacf472f4efb9749c50f87f3cea7.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','说到上海，外滩、陆家嘴、南京路步行街固然是经典打卡地，但如果你期待的旅行是“慢慢逛的书店、细细看的展览、在老洋房里喝一杯咖啡”，那么这份攻略也许更适合你。\n这趟三天的行程，刻意绕开了那些摩天大楼与购物商圈的喧嚣，将脚步集中在徐汇、黄浦和虹口的文艺片区。我会借住在地铁10号线沿线（例如交通大学站、陕西南路站附近），出行会非常便利。',6,1,1,0,'2026-05-13 11:08:07','2026-05-13 11:08:07'),(7,1,'青岛三日漫游','/uploads/20260513/cde6b30833144c57af5d68f27a016085.webp','青岛',3,1500.00,'autumn','<h3 style=\"text-align: start;\"><strong>第一日：红瓦绿树的诗意漫游</strong></h3><p style=\"text-align: start;\">来青岛的第一天，用Citywalk的方式，走进老城的红瓦绿树间，感受最纯粹的欧陆风情与文艺气息。</p><ul><li style=\"text-align: start;\">🏞️ 上午：初见栈桥与百年教堂09:00 | 栈桥：作为旅程的起点最适合不过。这座440米的百年海上长廊，不仅是青岛的象征，也是青岛近代历史的见证。漫步桥上欣赏青岛湾风光，若在冬季，成群的海鸥盘旋觅食，场面十分壮观。11:00 | 圣弥厄尔天主教堂：从栈桥步行即达。这座宏伟的哥特式建筑是青岛老城的制高点之一。即使不进教堂内部，在门前的广场上拍照打卡也非常出片。</li><li style=\"text-align: start;\">🍽️ 午餐 | 古香古色品鲁味：推荐前往位于中山路的春和楼，这家百年老字号是体验传统鲁菜魅力的绝佳选择。</li><li style=\"text-align: start;\">🌆 下午：文艺山巅俯瞰老城13:30 | 大学路：午餐后漫步至大学路与鱼山路交叉口，这里有青岛最著名的“网红墙”，红墙黄瓦是绝佳的拍照背景。附近还有许多独立书店和咖啡馆，值得慢慢探索。15:00 | 信号山公园：登上这座“老城区的天然观景台”，在山顶的旋转观景楼里，可以将青岛“红瓦绿树、碧海蓝天”的经典全景尽收眼底。</li><li style=\"text-align: start;\">🌙 晚上：夜市烟火与台东逛吃19:00 | 台东步行街：这里灯火通明，是感受青岛市井夜生活的好去处。“恣儿街”上小吃摊鳞次栉比，从辣炒蛤蜊到烤海星，各种美食琳琅满目。夜宵：别忘了配上一袋新鲜的青岛散装啤酒，边走边喝，体验最地道的青岛风情。</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/866ce1b235964377a2055b29ea020678.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\"><strong>第二日：山海仙境的虔诚朝圣</strong></h3><p style=\"text-align: start;\">青岛的魅力不止于海，更在于山。今天，我们将走进“海上第一名山”——崂山，感受山海相拥的壮阔奇观。</p><ul><li style=\"text-align: start;\">🏔️ 上午：攀登海上第一名山10:00 | 崂山风景区：时间有限，一日游建议选择“仰口线”（近期也推出了轻松的618路公交玩法）。仰口以其山海相连、奇峰异石著称。你可以乘索道上山（推荐索道上+徒步下），沿途游览太平宫、寿字峰，挑战神秘的觅天洞，最后登顶天苑，俯瞰山下绝美的海岸线、沙滩与渔村。</li><li style=\"text-align: start;\">🍽️ 午餐 | 山海间的渔家风味：从仰口下山后，推荐在附近渔村（如青山渔村，风光很美）的农家宴用餐。可以品尝最新鲜的渔家海鲜，比如辣炒蛤蜊和特色海鲜水饺，感受山海间的淳朴滋味。</li><li style=\"text-align: start;\">🌄 下午：青山碧海间的隐世渔村13:30 | 青山渔村：从仰口游览区出来，可乘公交或打车前往被誉为“中国最美渔村”之一的青山渔村。在观景台俯瞰层层茶田、红瓦石屋与湛蓝大海交织出的宁静画卷，感受时间慢下来的惬意。</li><li style=\"text-align: start;\">🌙 晚上：休整与自由探索18:00 | 市区自由活动：从崂山返回市区后，体力消耗较大，晚餐可以选择在市南区寻觅一家本地菜馆，或在酒店附近轻松解决。如果尚有余力，可以去海边再吹吹晚风，拾起一天的记忆。</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/a8bad1816e5b4bd69c60b2d1ba9ca9ae.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\"><strong>第三日：海岸线与现代都市的协奏</strong></h3><p style=\"text-align: start;\">行程的最后一天，从一步一景的建筑美学，走向活力四射的现代都市，为青岛之旅画上圆满句号。</p><ul><li style=\"text-align: start;\">🌳 上午：徜徉万国建筑博览10:00 | 八大关：这里被誉为“万国建筑博览馆”，汇集了20多个国家的建筑风格。重点打卡著名的花石楼和充满童话色彩的公主楼。在林荫道上慢走，每一栋老洋房都像在诉说自己的故事。</li><li style=\"text-align: start;\">🍽️ 午餐 | 本地宝藏小馆：推荐前往当地人也常光顾的王姐烧烤（市南店），尝尝特色的烤鱿鱼、烤肉筋，再配上地道的海鲜疙瘩汤或海菜凉粉，体验平价又美味的青岛日常。</li><li style=\"text-align: start;\">⛵ 下午：帆船之都的魅力14:00 | 五四广场 &amp; 奥帆中心：午后乘车前往著名的五四广场，与地标雕塑“五月的风”合影。随后来到旁边的奥林匹克帆船中心，感受2008年奥运会的余韵。提前“探路”：你可以在奥帆中心的情人坝附近走走，这里也是晚上观赏灯光秀的绝佳位置之一。</li><li style=\"text-align: start;\">🌙 晚上：海鲜市场终极挑战17:00 | 营口路市场：留足胃口，今晚体验青岛最市井的乐趣！先去市场采购新鲜海鲜（记得多对比，大胆砍价）。然后到市场周边找一家啤酒屋代加工（加工费通常是清蒸/水煮好便宜，辣炒/油焖稍贵）。坐在市井小馆里，喝着原浆啤酒，享用亲手挑选的鲜美海味，惬意地结束这趟三日旅程。进阶玩法：若想获享VIP票含啤酒品鉴并看醉酒小屋+原浆生产线体验，可提前在线上预订“青岛啤酒博物馆”并将此行程替换安排在第三天上午，再取消八大关的行程。</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/c2df5bdeefb044b9b84675991ffd4120.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','以“青岛三日漫游”为题，开始你的山海城之旅吧。这份攻略融合了青岛最具代表性的红瓦绿树、碧海蓝天与鲜活的人间烟火，希望能帮你勾勒出这座城市的浪漫轮廓，让旅程更加从容尽兴。',7,1,1,0,'2026-05-13 11:09:39','2026-05-13 11:09:39'),(8,3,'南京两日历史文化之旅','/uploads/20260513/bb21b9286f76492f9630f2d0dd214f32.webp','南京',2,1500.00,'all','<h4 style=\"text-align: start;\">🏛️ Day 1：钟山访古 + 民国风云</h4><p style=\"text-align: start;\"><strong>上午 8:30-12:00 | 钟山风景区</strong></p><p style=\"text-align: start;\">行程首站来到钟山风景区，这是南京历史文化的精华所在。乘坐地铁2号线至苜蓿园站，出站后沿陵园路步行，两侧高耸的梧桐树带来浓厚的民国风情。</p><p style=\"text-align: start;\">抵达<strong>中山陵</strong>后，沿392级台阶登顶——这一数字象征着当年全国三亿九千两百万同胞。站在祭堂前俯瞰整座金陵城，气势恢宏。</p><p style=\"text-align: start;\">接着前往紧邻的<strong>音乐台</strong>（门票约10元），环形座椅和成群的白鸽构成浪漫画面，运气好还能捕捉到群鸽环飞的动人时刻。</p><p style=\"text-align: start;\">午餐安排在钟山风景区内的简餐，或下山后前往大行宫附近用餐。</p><p style=\"text-align: start;\"><strong>下午 13:30-16:30 | 明孝陵 + 总统府</strong></p><p style=\"text-align: start;\">下午继续游览<strong>明孝陵</strong>，这是明太祖朱元璋与马皇后的陵寝，神道上的石象路被誉为“南京最美600米”。最佳游览顺序为：四方城→神功圣德碑→神道→城墙→御桥。</p><p style=\"text-align: start;\">游览结束后乘坐地铁2号线至大行宫站，参观<strong>总统府</strong>（门票40元）。这座600多年历史的近代史博物馆见证了从明清到民国的重要历史变迁，可以参观门楼、孙中山临时大总统办公室、子超楼等。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/1bcba090b3424d14ac817029d8235f0e.webp\" alt=\"\" data-href=\"\" style=\"\"></p><p style=\"text-align: start;\"><strong>晚上 17:30-21:00 | 老门东 &amp; 秦淮夜游</strong></p><p style=\"text-align: start;\">傍晚前往<strong>老门东</strong>历史文化街区。这里是老南京风貌保存最完整的区域之一，青石板路平坦宽阔，非遗手作店和传统老字号鳞次栉比。想品尝地道南京小吃可以在老门东搞定——蓝老大糖粥藕店的糖粥藕和赤豆元宵、徐家鸭子的烤鸭都值得一试。</p><p style=\"text-align: start;\">晚餐后前往秦淮河畔。建议从武定门码头乘坐画舫夜游（约100元），听桨声灯影里的金陵典故。下船后可游览<strong>夫子庙</strong>步行街，感受灯火辉煌的秦淮夜色。</p><p style=\"text-align: start;\"><strong>🍜 第一日美食推荐</strong></p><ul><li style=\"text-align: start;\">午餐：大行宫周边简餐，或钟山风景区内的餐食</li><li style=\"text-align: start;\">晚餐：老门东街区的蒋有记牛肉锅贴（“秦淮八绝”之一，外皮金黄酥脆）、蓝老大糖粥藕店的赤豆元宵与糖粥藕</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/eab0bc0f20894aa68c6ddfb2d6d8ff44.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h4 style=\"text-align: start;\">📜 Day 2：文脉溯源 + 山水闲游</h4><p style=\"text-align: start;\"><strong>上午 9:00-12:00 | 南京博物院</strong></p><p style=\"text-align: start;\">第二天从<strong>南京博物院</strong>开始（免费需提前预约）。作为中国三大博物馆之一，南博馆藏丰富，尤其推荐重点参观<strong>民国馆</strong>——地下复刻的老南京街市，邮局、理发店、老茶馆均可真实进入，坐在茶馆听评弹，仿佛一秒穿越回百年时光。</p><p style=\"text-align: start;\">此外，竹林七贤砖画、金缕玉衣等镇馆之宝也值得一看。</p><p style=\"text-align: start;\"><strong>午餐 12:00-13:30 | 大行宫 / 科巷</strong></p><p style=\"text-align: start;\">从南博出来就近用餐。科巷是本地人爱去的美食聚集地，可以尝尝科巷三姐酒酿的酒酿元宵，顺路买些蜜汁藕当零食。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/0ac9bb8f783e475ea1fe57bbb8de5cfc.webp\" alt=\"\" data-href=\"\" style=\"\"></p><p style=\"text-align: start;\"><strong>下午 14:00-17:30 | 鸡鸣寺 + 明城墙 + 玄武湖</strong></p><p style=\"text-align: start;\">下午前往<strong>鸡鸣寺</strong>——南京最古老的寺庙之一，登顶后凭门票可免费领三支香。赵雅芝版《新白娘子传奇》的雷峰塔曾在此取景。</p><p style=\"text-align: start;\">鸡鸣寺北门出来即可登上<strong>明城墙（台城段）</strong>。站在城墙上俯瞰，山水城林尽收眼底，远眺紫峰大厦与玄武湖，颇有古都气象。</p><p style=\"text-align: start;\">从城墙下来便是<strong>玄武湖公园</strong>。作为江南最大的皇家园林湖泊，这里以环湖步道为主，沿途有多处休息长椅。可以租一艘电动船（60元/小时）泛舟湖上，看城墙倒影在水波中摇曳。</p><p style=\"text-align: start;\"><strong>晚上 18:00-21:00 | 新街口 / 科巷美食扫街</strong></p><p style=\"text-align: start;\">晚餐时间直奔<strong>新街口</strong>或<strong>科巷</strong>。</p><ul><li style=\"text-align: start;\">明瓦廊：金宏兴鸭子店的烤鸭和盐水鸭品质稳定、卤汁一绝，永远排着本地老饕的长队</li><li style=\"text-align: start;\">丰富路：藏着小潘记鸭血粉丝汤，汤底醇厚、粉丝滑嫩</li><li style=\"text-align: start;\">科巷：尝一尝南京第一家冰糖蜜汁藕，软糯香甜</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/fe2332830c0e42118996bcf0ee59c947.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','南京，这座承载着六朝古都厚重底蕴的城，既有紫金山的巍峨，亦有秦淮河的温柔。两日虽短，却足以让你在梧桐树下、明城墙边，触摸到千年文脉的脉动。这份攻略为你串联起南京最精华的历史文化地标，兼顾游览节奏与烟火美食，助你轻松开启金陵之行。',8,1,1,0,'2026-05-13 11:11:19','2026-05-13 11:11:19'),(9,1,'丽江大理五日慢旅行','/uploads/20260513/8090fa957b5047b49c252dbc07093467.jpg','丽江',5,3500.00,'autumn','<h3 style=\"text-align: start;\">Day 1：抵达丽江，漫步古城初遇</h3><p style=\"text-align: start;\"><strong>上午</strong>：抵达丽江三义机场，乘坐机场大巴直达古城南门（票价约20元），入住酒店放好行李。住宿建议选择在大研古城（即丽江古城）核心区周边的客栈，既方便逛古城，又避免了直接住在古城核心区石板路拖行李的困扰。</p><p style=\"text-align: start;\"><strong>中午</strong>：在古城内品尝丽江特色午餐。推荐去五一街的 <strong>88号小吃店</strong>，这是一家开了30多年的老店，鸡豆凉粉是招牌，凉拌酸辣爽口，煎着吃外皮焦脆、内里绵密，再配上一份包浆豆腐，人均约30元，地道又实惠。</p><p style=\"text-align: start;\"><strong>下午</strong>：逛 <strong>丽江古城（大研古城）</strong> 。这座始建于宋末元初的古城是世界文化遗产，因形似一方大砚台而得名。建议慢悠悠地走，不必刻意追求打卡——古城之内全靠双脚丈量最佳，那些错综复杂的小巷，每一条拐弯都可能遇见一树繁花或一池游鱼，坐车反而会错过太多细节。推荐去 <strong>木府</strong> 参观，这里曾是丽江木氏土司的府邸，建筑宏伟，可以了解纳西族的历史文化。</p><p style=\"text-align: start;\"><strong>傍晚</strong>：登上 <strong>狮子山万古楼</strong>，这里是俯瞰丽江古城全景的绝佳位置。日落时分，看青瓦白墙在暮色中渐渐亮起灯火，非常治愈。</p><p style=\"text-align: start;\"><strong>晚上</strong>：古城灯火阑珊时，可以在四方街附近找一家小酒馆坐坐，听听民谣。也可以去 <strong>忠义市场</strong> 逛逛夜市的本地小吃，尝一尝米灌肠（5元一小份）、纳西烤鱼等地道风味。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/c7eb25fbae9d43dc8cfdc3a159fffdcc.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">Day 2：玉龙雪山，冰川与碧水的震撼一日</h3><p style=\"text-align: start;\">这是丽江行程的重头戏。务必提前做好门票和索道票的预订。</p><p style=\"text-align: start;\"><strong>⚠️ 重要提示</strong>：</p><ul><li style=\"text-align: start;\">玉龙雪山门票和索道票可通过微信小程序“丽江旅游集团”提前7天实名预约购买</li><li style=\"text-align: start;\">冰川公园大索道票每天限量，通常在出发日前一天晚上8点开放抢票，几分钟内就可能售罄，建议提前录入个人信息，准点开抢</li><li style=\"text-align: start;\">抢不到大索道票可改订云杉坪索道或牦牛坪索道，景观各有特色，也可考虑观看张艺谋导演的《印象丽江》户外实景秀作为替代</li><li style=\"text-align: start;\">景区海拔高，昼夜温差大，建议带足保暖衣物，备好氧气瓶和葡萄糖</li></ul><p style=\"text-align: start;\"><strong>行程安排</strong>：</p><p style=\"text-align: start;\">早上7:30左右出发，约80分钟车程抵达玉龙雪山景区。可先在 <strong>甘海子</strong> 看日出，这里是拍摄日照金山的经典机位。之后按 <strong>蓝月谷→冰川公园</strong> 的顺序游览：</p><ul><li style=\"text-align: start;\">蓝月谷：湖水呈现梦幻的蓝绿色，被誉为“小九寨沟”，非常适合拍照</li><li style=\"text-align: start;\">冰川公园：乘大索道上至4506米的观景台，再沿木栈道徒步至4680米高碑，近距离感受万年冰川的震撼</li></ul><p style=\"text-align: start;\">大约下午五点左右结束行程，返回丽江市区。</p><p style=\"text-align: start;\"><strong>晚餐</strong>：辛苦一天，推荐去吃一顿地道的 <strong>腊排骨火锅</strong>。阿婆情腊排骨是开了20多年的老店，排骨用野生香料腌制风干，汤底奶白鲜醇，涮上水性杨花、豆腐、洋芋，再蘸糊辣椒蘸水，小份58元够两人吃。或者在束河古镇的“老地方腊排骨火锅”，开了16年的老店，本地人认可度很高，人均60-80元。</p><h3 style=\"text-align: start;\">Day 3：白沙古镇的慢时光 + 束河的静谧</h3><p style=\"text-align: start;\">如果说大研古城是丽江面向世界的热闹名片，那白沙古镇就是丽江原来的样子。</p><p style=\"text-align: start;\"><strong>上午</strong>：前往 <strong>白沙古镇</strong>。这里位于玉龙雪山脚下，是丽江三个古镇中最小众的一个。主街不长，两边是纳西老人围坐打麻将的老院子，门口的摊贩卖乳扇、手工艺品。随意站在路口抬头就能望见雪山，雪顶衬着蓝天，干净纯粹。镇上的 <strong>白沙壁画</strong> 融合了汉、藏、纳西三族艺术风格，已有500多年历史。找一家能看雪山的咖啡馆坐下，点一杯咖啡，看雪山云卷云舒，是很丽江的体验。</p><p style=\"text-align: start;\"><strong>中午</strong>：在白沙古镇午餐。推荐去藏在小巷里的纳西风味小馆，或者找一家能看雪山的绝美餐厅，原木风设计与纳西风情完美融合，一边享受美食一边看雪景。</p><p style=\"text-align: start;\"><strong>下午</strong>：前往 <strong>束河古镇</strong>。束河比大研古城更安静从容，是纳西族先民最早的聚居地之一。必看点： <strong>青龙桥</strong>——明代修建的古石桥，桥面被近千年的马蹄踩出深浅不一的马蹄印，是茶马历史的活化石； <strong>九鼎龙潭</strong>——潭水清澈见底，晴天能看到玉龙雪山的清晰倒影。在束河不用赶景点，沿着青龙河慢慢走，坐在潭边的茶馆里发呆，时间仿佛真的停了下来。</p><p style=\"text-align: start;\"><strong>晚上</strong>：可以在束河晚餐，推荐 <strong>壹餐厅</strong>（束河大石桥左转）的泡菜鱼，或 <strong>朵朵妈妈菜</strong>（仁里巷）的酸梅汤、土豆炒饭。之后返回大理或继续在丽江住宿，准备次日前往大理。</p><h2 style=\"text-align: start;\">三、大理·详细行程安排（两天）</h2><h3 style=\"text-align: start;\">Day 4：洱海西线——才村骑行至磻溪S弯</h3><p style=\"text-align: start;\"><strong>上午</strong>：从丽江出发前往大理。两地之间动车班次密集，最快的直达班次约1.9小时，票价约80元。抵达大理站后，可打车或乘专线车前往才村入住。</p><p style=\"text-align: start;\"><strong>中午</strong>：入住才村客栈。才村位于洱海西岸，距离大理古城约5公里，这里没有古城的喧嚣，却拥有洱海最美的日落景观。</p><p style=\"text-align: start;\"><strong>下午</strong>：沿洱海向西岸方向骑行。洱海周边有专门的骑行道，路况良好。从才村出发，向北骑行约10公里到达 <strong>磻溪村</strong>。这里的 <strong>S弯公路</strong> 沿着洱海边延伸，两旁是绿树和农田，是观赏洱海日落的最佳地点，也很适合拍大片。沿路可以欣赏苍山倒影映在洱海中的美景，仿佛天然的水墨画。</p><p style=\"text-align: start;\"><strong>傍晚</strong>：返回才村，在洱海边等待日落。夕阳西下时，洱海被染成金色，苍山在余晖中显得格外神圣。才村的海边有许多海景餐厅，可以一边用餐一边欣赏日落。</p><p style=\"text-align: start;\"><strong>晚餐</strong>：品尝大理特色美食。推荐 <strong>大理砂锅鱼</strong>——用洱海弓鱼砂锅慢炖，汤汁鲜美；以及 <strong>大理烤乳扇</strong>——牛奶制成，烤后外酥里嫩。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/5c0908c3e1e641fcbeb3948c157adc12.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">Day 5：喜洲古镇 / 大理古城</h3><p style=\"text-align: start;\"><strong>上午</strong>：前往 <strong>喜洲古镇</strong>（距离才村约20公里，车程约30分钟）。喜洲是白族传统民居的集中地，保存着大量明清时期的“三坊一照壁”格局。推荐参观 <strong>严家大院</strong>——喜洲最大的白族民居，占地约3000平方米，共99间房，照壁雕刻精美绝伦。</p><p style=\"text-align: start;\">在喜洲一定要吃 <strong>喜洲粑粑</strong>，有甜味和咸味两种，外酥里嫩，现场制作的老店很多，可以边看边吃。</p><p style=\"text-align: start;\"><strong>下午</strong>：如果时间充裕，可前往 <strong>大理古城</strong> 逛逛。古城内有洋人街、五华楼等经典打卡点。也可以选择去 <strong>崇圣寺三塔</strong> 远观拍照，三塔倒影是经典画面。</p><p style=\"text-align: start;\"><strong>傍晚</strong>：根据返程时间安排送机/送站。建议从大理站或大理机场离港，结束五天完美行程。</p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','大理和丽江是云南最经典的旅游目的地，两地相距约180公里，动车仅需约2小时。本攻略以“慢旅行”为主旨，不赶路、不早起，在五天时间里深度感受雪山下的柔软时光和洱海边的诗意生活。\n推荐进港方案：建议先飞抵丽江三义机场，从丽江玩起，最后从大理或丽江返程。也可以根据你的航班情况灵活调整顺序。',9,1,1,0,'2026-05-13 11:11:57','2026-05-13 11:11:57'),(10,1,'杭州西湖两日漫游','/uploads/20260513/cb02990945234fb296d0fcf652a2f2b9.webp','杭州',2,2500.00,'','<h3 style=\"text-align: start;\">Day 1：北线人文探幽</h3><p style=\"text-align: start;\"><strong>上午：断桥→白堤→孤山</strong></p><p style=\"text-align: start;\">早晨8点左右抵达断桥，此时游客稀少，湖面薄雾缭绕，最有诗意。从断桥出发，沿着白堤慢慢走，桃红柳绿，一步一景——白居易笔下“最爱湖东行不足，绿杨阴里白沙堤”说的就是这里。</p><p style=\"text-align: start;\">穿过白堤抵达孤山，这里虽高不过35米，却是文物荟萃之处。沿途可游览浙江省博物馆孤山馆、西泠印社和秋瑾墓，感受杭州深厚的人文底蕴。</p><p style=\"text-align: start;\"><strong>中午：午餐推荐</strong></p><p style=\"text-align: start;\">孤山脚下的楼外楼是百年名店，西湖醋鱼、东坡肉都是招牌。知味观的小笼包和猫耳朵也很地道。</p><p style=\"text-align: start;\"><strong>下午：岳王庙→曲院风荷→北山街</strong></p><p style=\"text-align: start;\">沿白堤继续西行，途经西泠桥，抵达岳王庙（门票25元），瞻仰岳飞铜像与“青山有幸埋忠骨”的墓阙。随后前往曲院风荷，夏季荷花盛开时最为壮观，亭台楼阁点缀其间，是西湖赏荷的最佳去处。</p><p style=\"text-align: start;\">傍晚骑行或漫步北山街，欣赏民国时期的老建筑群，感受西湖的历史肌理。夜幕降临后可去湖滨银泰逛逛，或在西湖边欣赏音乐喷泉。</p><p style=\"text-align: start;\"><strong>晚餐推荐</strong>：湖滨银泰附近的新白鹿餐厅性价比高，西湖银泰店的人气很旺。想吃地道杭帮菜的还可以选择杭州酒家或奎元馆。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/3753cb0feb8945c398ccc6fd378492a5.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">Day 2：南线山水咏叹</h3><p style=\"text-align: start;\"><strong>上午：柳浪闻莺→三潭印月</strong></p><p style=\"text-align: start;\">从龙翔桥地铁站C口出发，步行至柳浪闻莺。这里曾是南宋皇家御花园旧址，垂柳如浪，莺啼声声。随后前往钱王祠码头乘船（船票+登岛70元），约15分钟抵达三潭印月岛。这里是1元人民币的同款打卡地，湖中三塔颇为震撼。游览后从岛上乘船前往花港方向，在花港码头下船。</p><p style=\"text-align: start;\"><strong>中午：花港观鱼→午餐</strong></p><p style=\"text-align: start;\">花港观鱼是西湖十景之一，万尾锦鲤在水中畅游，买一包鱼粮（5元）就能体验鱼群环绕的乐趣。附近的知味观味庄（杨公堤店）是午餐的好选择。</p><p style=\"text-align: start;\"><strong>下午：雷峰塔→南屏晚钟→河坊街</strong></p><p style=\"text-align: start;\">雷峰塔门票40元，可提前在网上预约购票。塔内有电梯直达顶层，俯瞰西湖全景视野极佳。雷峰塔旁的长桥公园是拍摄“雷峰夕照”的最佳机位。傍晚时分，前往净慈寺（门票10元）聆听南屏晚钟，这里曾是济公出家之地，钟声回荡极有禅意，若花20元还可亲自撞钟祈福。</p><p style=\"text-align: start;\"><strong>晚上：河坊街</strong></p><p style=\"text-align: start;\">从长桥乘坐公交车前往河坊街，这里是杭州最有烟火气的美食街。葱包烩、定胜糕、吴山烤鸡都是必尝的小吃。穿过熙攘的人群，在热闹的夜市氛围中结束一天的行程。</p><p style=\"text-align: start;\"><strong>晚餐推荐</strong>：河坊街各类小吃琳琅满目，也可选择新周记或外婆家（滨湖店）。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/42187a90f5d14a63921c9800e9216e9e.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,0,0,0,0,0,0,0,NULL,1,'','为你规划了一份以西湖为核心、兼顾周边精华的杭州三日游攻略。前两日围绕西湖深度漫游，第三日可根据兴趣选择文化禅意或自然湿地路线。\n\n',10,1,1,0,'2026-05-13 11:13:45','2026-05-13 11:13:45'),(11,3,'成都四日游','/uploads/20260513/b8aaed7afdad4a8191ec82c6c7921f19.webp','成都',4,4000.00,'autumn','<h3 style=\"text-align: start;\">▸ Day 1（下午抵达 → 晚上）：商圈初印象——春熙路 &amp; 太古里 &amp; 九眼桥</h3><p style=\"text-align: start;\">抵达成都、入住酒店后，休整片刻便可开启Citywalk。</p><p style=\"text-align: start;\"><strong>下午</strong>：前往 <strong>春熙路</strong>，这里是成都最具代表性的商圈，搭配IFS楼上那只爬墙的巨型熊猫合影打卡，是每位来蓉游客的“必修课”。随后步行至 <strong>太古里</strong>，感受现代潮流与川西古建的巧妙碰撞。在太古里的核心地带，有一座“大隐隐于市”的 <strong>大慈寺</strong>——玄奘正式剃度之所，红墙竹影间香火袅袅，与周遭的繁华形成强烈的古今反差。</p><p style=\"text-align: start;\"><strong>晚上</strong>：前往 <strong>九眼桥</strong> 欣赏锦江夜景，逛酒吧街感受成都的夜生活氛围。相比玉林路的喧嚣，九眼桥的酒吧街更具层次，既有热闹的狂欢场，也藏着许多安静的小酒馆。</p><p style=\"text-align: start;\"><strong>🍜 美食推荐</strong>（午餐/晚餐）：</p><ul><li style=\"text-align: start;\">钟水饺、龙抄手、蛋烘糕：春熙路商圈随处可寻，川味小吃代表，起步必尝</li><li style=\"text-align: start;\">大龙燚火锅（春熙路店）：正宗麻辣汤底，用内黄辣椒、花椒和牛油熬制，还可选鸳鸯锅照顾不同口味</li><li style=\"text-align: start;\">巴蜀大宅门（高升桥店）：本地人也会吃的老牌火锅，千层肚、鸭肠、耗儿鱼必点。记住一个真理——在成都，火锅好不好吃，看锅底沸腾时有没有密集的小泡</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/1d85e4b86841460f9c2245f3811087e8.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">▸ Day 2：熊猫与古韵——大熊猫基地 &amp; 武侯祠 &amp; 锦里</h3><p style=\"text-align: start;\"><strong>上午（务必早起！）</strong> ：前往 <strong>成都大熊猫繁育研究基地</strong>（门票55元/人），是联合国环保奖“500佳”获得者，也是目前饲养大熊猫数量最多的基地之一。<strong>7:30–9:00</strong> 是熊猫最活跃的时段，务必在此之前入园。直接冲向<strong>月亮产房</strong>和<strong>太阳产房</strong>，观赏幼年熊猫的憨态——这是整个基地人气最火爆的区域。</p><p style=\"text-align: start;\"><strong>温馨建议</strong>：如果幼年熊猫区人挤人排不上队，别死磕。后面的<strong>成年熊猫别墅</strong>和<strong>熊猫厨房</strong>（可看饲养员准备食物）也很有意思，人流相对宽松很多。基地内绿树成荫，步行游览反而更有可能偶遇熊猫，不一定要坐观光车。</p><p style=\"text-align: start;\"><strong>中午</strong>：前往 <strong>奎星楼街</strong> 用午餐。这条街紧邻宽窄巷子，但性价比高得多，是本地人的美食聚集地——</p><p style=\"text-align: start;\"><strong>下午</strong>：参观 <strong>武侯祠</strong>（门票50元/人），中国唯一的君臣合祀祠庙，“红墙竹影”是绝佳的拍照出片地。武侯祠东侧紧邻 <strong>锦里古街</strong>，傍晚的红灯笼夜幕降临后尤其出片，可以顺便在这里看一场川剧变脸表演。</p><p style=\"text-align: start;\"><strong>关于宽窄巷子的建议</strong>：Day2并不强制完成宽窄巷子游览，但您的行程节奏需要在第三天继续取舍——宽窄巷子免费免预约，适合放在Day3上午快速打卡拍照，下午转入更深入的主题，或者不绕路直接放弃。</p><p style=\"text-align: start;\"><strong>🍜 美食推荐</strong>（中餐/晚餐）：</p><ul><li style=\"text-align: start;\">奎星楼街：推荐冒椒火辣、成都吃客等地道川菜与串串名店</li><li style=\"text-align: start;\">浣花北路/抚琴夜市：隐藏着大量本地人私藏的“苍蝇馆子”</li><li style=\"text-align: start;\">符妈成都巷子火锅（玉林店）：2026年成都苍蝇馆子50强之一</li></ul><p style=\"text-align: start;\"><img src=\"/uploads/20260513/a6218af466d64db8bccf035cdd91b63e.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">▸ Day 3（全天的核心）：都江堰 &amp; 青城山 一日经典（需尽早出发）</h3><p style=\"text-align: start;\">“拜水都江堰，问道青城山”，这是对成都周边景致最经典的诠释。</p><p style=\"text-align: start;\"><strong>上午</strong>：从<strong>犀浦站</strong>搭乘城际列车直达 <strong>都江堰站</strong>（约20–30分钟），游览 <strong>都江堰景区</strong>（门票80元/人）。核心看点为：<strong>鱼嘴分水堤</strong>、<strong>飞沙堰溢洪道</strong>和<strong>宝瓶口进水口</strong>。这是全世界迄今为止唯一留存、仍在使用、以无坝引水为特征的宏大水利工程，堪称“活着的千年奇迹”。旺季建议下午4点以后或上午8点前入园，避开人流高峰。</p><p style=\"text-align: start;\"><strong>中午</strong>：在灌县古城附近用简餐，尝一碗<strong>醪糟</strong>和<strong>甜水面</strong>——这是都江堰本地人强推的平民美食，甜咸微辣，别有风味。</p><p style=\"text-align: start;\"><strong>下午</strong>：前往 <strong>青城山前山</strong>（门票80元/人），体验“青城天下幽”的道教名山魅力。徒步路线轻松版（新手友好）：<strong>山门 → 月城湖乘船 → 索道上山（单程35元） → 上清宫 → 老君阁（山巅俯瞰全貌） → 徒步下撤</strong>（途经天师洞、天然图画）→ 山门。</p><p style=\"text-align: start;\"><strong>核心提醒</strong>：</p><ul><li style=\"text-align: start;\">青城山前山以人文道观为主，如偏爱纯自然山水徒步，可选择后山</li><li style=\"text-align: start;\">务必穿防滑运动鞋，山间楼梯多；带少量水和零食，山上物价相对偏高</li><li style=\"text-align: start;\">建议8点从成都出发，下午4点前必须下山，否则错过返程列车</li><li style=\"text-align: start;\">都江堰/青城山建议提前 1天 预约购票，往返高铁远比包车或自驾高效</li></ul><p style=\"text-align: start;\"><strong>晚上</strong>：返回成都市区后，用一顿<strong>热辣的牛油火锅</strong>或<strong>麻辣串串</strong>画上完美句号（微辣起步，循序渐进）。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/cba0e75931874061b05e84b65c3c9072.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">▸ Day 4（上午收尾 + 返程）：人民公园慢生活 &amp; 采耳 &amp; 送站/送机</h3><p style=\"text-align: start;\"><strong>上午</strong>：前往 <strong>人民公园</strong>，这是成都慢生活的“流动博物馆”。在<strong>鹤鸣茶社</strong>点一碗盖碗茶（约18元），体验一次<strong>掏耳朵</strong>（基础套餐约30元，不一定要选高价套餐），感受成都人“采耳饮茶、摆龙门阵”的市井烟火气。人民公园内部还有<strong>相亲角</strong>和<strong>人工湖</strong>，如果时间充裕可以划一小船。</p><p style=\"text-align: start;\"><strong>中午</strong>：返程前购买一些伴手礼——推荐前往<strong>居民区附近的平价超市</strong>购买<strong>牛肉干、蛋烘糕</strong>等特产，比景区高价店划算得多。</p><p style=\"text-align: start;\"><strong>返程交通提示</strong>：天府机场乘地铁18号线（约1小时）；双流机场乘地铁10号线。市内绝大多核心景点均可乘地铁直达。</p>',NULL,NULL,2,1,1,0,0,0,0,NULL,1,'','本攻略以四日游起题，考虑到“来去天数”的实际情况，将行程组织为紧凑型三日半日程，四日的框架涵盖出行准备、每日安排和实用贴士，让您在有限时间内最大程度领略成都的“巴适”与精彩。',11,1,1,0,'2026-05-13 11:15:01','2026-05-13 11:15:01'),(12,1,'北京五日深度游','/uploads/20260513/0ca2f05a2f3d4a6696a6e4e052c88ec6.webp','北京',5,NULL,'all','<h3 style=\"text-align: start;\">第一天：皇城中轴与故宫深度</h3><p style=\"text-align: start;\"><strong>上午（8:30—12:30）：故宫博物院</strong></p><p style=\"text-align: start;\">从午门进入故宫，建议沿中轴线游览午门→太和门→太和殿（金銮殿）→中和殿→保和殿→乾清宫→交泰殿→坤宁宫→御花園→神武门。时间充裕的话，东路的珍宝馆和钟表馆强烈推荐，大量稀世文物和精美古董钟表令人叹为观止。建议预留4小时左右游览。门票：旺季60元，珍宝馆/钟表馆各10元。周一闭馆（法定节假日除外）。</p><p style=\"text-align: start;\"><strong>中午（12:30—13:30）：午餐</strong></p><ul><li style=\"text-align: start;\">四季民福故宫店：观景位吃烤鸭，氛围感十足</li><li style=\"text-align: start;\">神武门附近冰窖餐厅简餐</li></ul><p style=\"text-align: start;\"><strong>下午（13:30—15:00）：景山公园</strong></p><p style=\"text-align: start;\">从神武门出宫，步行10分钟即可抵达景山公园（门票2元）。登上万春亭，俯瞰故宫全貌——红墙黄瓦层层铺展，气势恢弘。</p><p style=\"text-align: start;\"><strong>傍晚（15:30—19:00）：前门大街与大栅栏</strong></p><p style=\"text-align: start;\">漫步前门大街，感受老北京商业街的古朴气息，大栅栏里藏着诸多老字号。晚餐可在前门附近品尝烤鸭或北京小吃。如果时间允许，还可逛逛鲜鱼口老字号美食街，吃一碗地道炸酱麵。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/4ed4b93624a94b7b85aac2b0bf6115d8.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">第二天：不到长城非好汉</h3><p style=\"text-align: start;\"><strong>上午（7:00—13:00）：八达岭长城</strong></p><p style=\"text-align: start;\">前往八达岭长城可选多种方式——高铁从北京北站/清河站出发约半小时直达；直通巴士从北土城地铁口发车，7:00-12:00多个时间点可选；也可乘坐877路公交直达。</p><p style=\"text-align: start;\">长城分为南长城和北长城，推荐新手选择经典省力线：乘坐空中缆车到北七楼，步行至北八楼打卡好汉坡，再乘缆车下山，全程约2小时。体力较好的朋友可尝试北城全程徒步至北十二楼。长城门票35元。开放时间：旺季6:30开始入园。4月30日起至10月6日，八达岭夜长城面向游客开放（18:30-21:00入园，亮灯至22:00），可选择夜间错峰游览。</p><p style=\"text-align: start;\"><strong>下午（15:00—18:00）：奥林匹克公园</strong></p><p style=\"text-align: start;\">返回市区后前往奥林匹克公园，打卡鸟巢、水立方外景（免费），在体育圣地上感受2008年奥运会的辉煌与荣耀。</p><p style=\"text-align: start;\"><strong>晚上：亚运村地区</strong> 品尝老北京涮肉，暖胃休整。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/087a899bed5041b7ac59493cfb3bf453.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">第三天：皇家园林巡礼</h3><p style=\"text-align: start;\"><strong>上午（8:30—12:00）：颐和园</strong></p><p style=\"text-align: start;\">作为中国现存最大的皇家园林，颐和园以昆明湖和万寿山为核心，佛香阁、长廊、石舫皆是必看之处。园内可乘船游湖，感受湖光山色的惬意。颐和园旺季开放时间为6:00-20:00，建议预留3.5小时，联票60元。游船项目约200元/小时（电瓶船），航线游船单程30-40元/人。</p><p style=\"text-align: start;\"><strong>中午（12:00—13:30）：午餐</strong> 园内简餐或周边餐馆。</p><p style=\"text-align: start;\"><strong>下午（14:00—16:00）：圆明园</strong></p><p style=\"text-align: start;\">圆明园是清代皇家园林的旷世杰作，西洋楼遗址区的大水法遗迹，无声诉说着历史的沧桑。门票25元。建议预留1.5-2小时，遗址区是感受历史的首选之地。</p><p style=\"text-align: start;\"><strong>傍晚（16:30—19:00）：清华/北大外景或中关村逛吃</strong></p><p style=\"text-align: start;\">圆明园毗邻清华北大，可在校门前打卡留念（入校需提前预约，目前基本仅限校友及参加特定活动人员进入，建议提前确认相关规定）。晚上可前往五道口或中关村一带寻觅美食，这里学术氛围浓厚，餐饮价格相对亲民。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/afa6c9b14ef94072a4eddc7bbd8a4ade.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">第四天：祭天祈福与老北京风情</h3><p style=\"text-align: start;\"><strong>上午（8:30—11:00）：天坛公园</strong></p><p style=\"text-align: start;\">天坛是明清两代皇帝“祭天”的场所，祈年殿是北京最具标志性的建筑之一。祈年殿前拍一张照片，红墙蓝顶，极富古典美感。天坛开放时间：公园早6:00-晚10:00（晚8:00停止售票），室内建筑早8:00-下午5:30。建议购买联票（含祈年殿等核心建筑），约34元。游览约2小时。</p><p style=\"text-align: start;\"><strong>中午（11:30—13:00）：雍和宫附近午餐</strong></p><p style=\"text-align: start;\">推荐雍和宫旁的京兆尹（King‘s Joy），餐厅紧邻雍和宫，环境雅致、品质卓越。</p><p style=\"text-align: start;\"><strong>下午（13:30—15:00）：雍和宫</strong></p><p style=\"text-align: start;\">雍和宫是清代雍正皇帝即位前的府邸，乾隆皇帝亦诞生于此，被誉为“龙潜福地”。这里是北京规模最大、保存最完好的藏传佛教寺院之一，寺内一尊高耸的檀香木佛像令人惊叹。门票25元，入口处可免费领取香火。目前雍和宫已取消强制预约，可现场购票。游览约1.5小时。参观时请穿着端庄，遮蓋肩膀和膝盖，跨门勿踩踏门框。</p><p style=\"text-align: start;\"><strong>傍晚（16:00—19:00）：什刹海与胡同漫步</strong></p><p style=\"text-align: start;\">什刹海是老北京风情的精华所在，烟袋斜街、银锭桥依次串联。推荐南锣鼓巷→鼓楼→烟袋斜街→什刹海→银锭桥这条约3公里的CityWalk路线。沿湖漫步，看夕阳将古都的黄昏染成暖金色，感受老北京的烟火气。傍晚时分，选一家胡同小馆，来一碗卤煮或炸酱面，品味最地道的京味儿。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/063e52ab5cc4419590b8c57fe082d42a.webp\" alt=\"\" data-href=\"\" style=\"\"></p><h3 style=\"text-align: start;\">第五天：孔孟之学与文艺胡同</h3><p style=\"text-align: start;\"><strong>上午（9:00—12:00）：孔庙与国子监</strong></p><p style=\"text-align: start;\">孔庙是元、明、清三代皇帝祭祀孔子的场所，国子监则是当时的中央最高学府（太学）。这里建筑庄严古朴，古柏参天，见证了古代王朝对儒家思想的尊崇与科举制度的辉煌。建议游览约2小时。两处相邻可一并参观。</p><p style=\"text-align: start;\"><strong>中午（12:30—14:00）：簋街午餐</strong></p><p style=\"text-align: start;\">簋街是北京最负盛名的美食街，大红灯笼高高挂，麻辣小龙虾、馋嘴蛙、烤鱼是最出名的招牌菜。</p><p style=\"text-align: start;\"><strong>下午（14:30—17:00）：五道营胡同</strong></p><p style=\"text-align: start;\">五道营胡同紧邻雍和宫，相比南锣鼓巷人少安静，文艺气息浓厚，遍布独立设计小店、咖啡馆和特色餐厅，适合慢悠悠地逛吃和拍照。或者反之，也可考虑上午五道营、下午孔庙国子监，均在同一区域，灵活安排即可。</p><p style=\"text-align: start;\">行程结束后，视返程时间前往机场或火车站，满载京城记忆踏上归途。</p><p style=\"text-align: start;\"><img src=\"/uploads/20260513/8b436b6201894f409fa3661d9524325c.webp\" alt=\"\" data-href=\"\" style=\"\"></p>',NULL,NULL,1,1,1,0,0,0,0,NULL,1,'','“读万卷书，行万里路。”北京，这座三千年建城史、八百年建都史的古老城市，既承载着明清两代的皇家气韵，也跃动着当代中国的蓬勃脉搏。五天时间，虽不能说尽览京城全貌，但足以带你深入其精华所在。这份攻略精心规划了每日路线，按区域串联景点，不走回头路，兼顾经典与深度，助你开启一场难忘的京城之旅。',12,1,1,0,'2026-05-13 11:42:47','2026-05-13 11:42:47');
-- ============================================================
-- 收藏数据
-- ============================================================
INSERT INTO `favorite` (`user_id`, `item_type`, `item_id`) VALUES
(1, 'attraction', 103),
(1, 'attraction', 18),
(1, 'attraction', 120),
(1, 'attraction', 73),
(1, 'attraction', 199),
(1, 'attraction', 80),
(1, 'attraction', 84),
(1, 'attraction', 23),
(1, 'attraction', 142),
(1, 'attraction', 117),
(1, 'attraction', 3),
(1, 'attraction', 95),
(1, 'attraction', 52),
(1, 'attraction', 75),
(1, 'attraction', 145),
(1, 'attraction', 78),
(2, 'attraction', 119),
(2, 'attraction', 95),
(2, 'attraction', 152),
(2, 'attraction', 127),
(2, 'attraction', 51),
(2, 'attraction', 188),
(2, 'attraction', 140),
(2, 'attraction', 63),
(2, 'attraction', 39),
(2, 'attraction', 2),
(2, 'attraction', 105),
(2, 'attraction', 7),
(2, 'attraction', 60),
(2, 'attraction', 139),
(2, 'attraction', 89),
(2, 'attraction', 163),
(2, 'attraction', 178),
(2, 'attraction', 3),
(2, 'attraction', 86),
(2, 'attraction', 1),
(2, 'attraction', 169),
(2, 'attraction', 97),
(3, 'attraction', 27),
(3, 'attraction', 53),
(3, 'attraction', 136),
(3, 'attraction', 61),
(3, 'attraction', 108),
(3, 'attraction', 126),
(3, 'attraction', 16),
(3, 'attraction', 37),
(3, 'attraction', 183),
(3, 'attraction', 72),
(3, 'attraction', 24),
(3, 'attraction', 12),
(3, 'attraction', 60),
(3, 'attraction', 134),
(3, 'attraction', 106),
(3, 'attraction', 180),
(3, 'attraction', 96),
(3, 'attraction', 118),
(3, 'attraction', 22),
(3, 'attraction', 149),
(3, 'attraction', 25),
(3, 'attraction', 130),
(3, 'attraction', 35),
(3, 'attraction', 164);

-- 将收藏记录的时间分散到最近60天内
UPDATE favorite SET create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND(id + 1000) * 25 + 1) DAY)
WHERE MOD(id, 4) != 0;

UPDATE favorite SET create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND(id + 1000) * 30 + 31) DAY)
WHERE MOD(id, 4) = 0;

-- ============================================================
-- 用户行为记录数据
-- ============================================================
INSERT INTO `user_behavior` (`user_id`, `item_type`, `item_id`, `behavior_type`, `rating`, `duration`) VALUES
(1, 'attraction', 20, 'view', NULL, 143),
(1, 'attraction', 61, 'favorite', NULL, NULL),
(1, 'attraction', 81, 'share', 4.47, NULL),
(1, 'attraction', 82, 'share', NULL, NULL),
(1, 'attraction', 20, 'click', NULL, 121),
(1, 'attraction', 40, 'view', NULL, 487),
(1, 'attraction', 81, 'share', NULL, NULL),
(1, 'attraction', 92, 'click', NULL, 484),
(1, 'attraction', 68, 'view', NULL, 321),
(1, 'attraction', 181, 'view', NULL, 345),
(1, 'attraction', 38, 'view', 4.94, 415),
(1, 'attraction', 132, 'view', 3.52, 188),
(1, 'attraction', 8, 'share', 4.11, NULL),
(1, 'attraction', 141, 'click', NULL, 114),
(1, 'attraction', 36, 'view', NULL, 237),
(1, 'attraction', 34, 'click', 3.74, 96),
(1, 'attraction', 150, 'favorite', 3.15, NULL),
(1, 'attraction', 17, 'click', 3.89, 141),
(1, 'attraction', 24, 'favorite', NULL, NULL),
(1, 'attraction', 119, 'view', NULL, 597),
(1, 'attraction', 111, 'share', NULL, NULL),
(1, 'attraction', 98, 'share', NULL, NULL),
(1, 'attraction', 55, 'click', NULL, 466),
(1, 'attraction', 39, 'view', 4.23, 312),
(1, 'attraction', 128, 'share', 3.96, NULL),
(1, 'attraction', 23, 'favorite', NULL, NULL),
(1, 'attraction', 108, 'click', 4.59, 265),
(1, 'attraction', 162, 'view', 4.56, 377),
(1, 'attraction', 123, 'share', NULL, NULL),
(1, 'attraction', 145, 'favorite', NULL, NULL),
(1, 'attraction', 47, 'view', NULL, 234),
(1, 'attraction', 8, 'favorite', NULL, NULL),
(1, 'attraction', 121, 'favorite', 5.0, NULL),
(1, 'attraction', 41, 'view', NULL, 256),
(1, 'attraction', 69, 'view', 4.9, 599),
(1, 'attraction', 100, 'favorite', 4.98, NULL),
(1, 'attraction', 45, 'click', NULL, 335),
(1, 'attraction', 189, 'favorite', 3.06, NULL),
(1, 'attraction', 180, 'favorite', 4.13, NULL),
(1, 'attraction', 145, 'click', 4.62, 514),
(1, 'attraction', 139, 'favorite', NULL, NULL),
(1, 'attraction', 10, 'view', NULL, 234),
(2, 'attraction', 135, 'share', NULL, NULL),
(2, 'attraction', 158, 'click', 3.68, 191),
(2, 'attraction', 83, 'view', NULL, 155),
(2, 'attraction', 199, 'view', 4.72, 84),
(2, 'attraction', 96, 'share', 3.67, NULL),
(2, 'attraction', 83, 'click', 4.46, 457),
(2, 'attraction', 164, 'share', 3.35, NULL),
(2, 'attraction', 144, 'favorite', NULL, NULL),
(2, 'attraction', 151, 'click', NULL, 311),
(2, 'attraction', 33, 'click', 4.9, 596),
(2, 'attraction', 101, 'view', NULL, 335),
(2, 'attraction', 157, 'click', 3.21, 518),
(2, 'attraction', 37, 'favorite', 3.48, NULL),
(2, 'attraction', 82, 'click', 4.93, 353),
(2, 'attraction', 115, 'view', NULL, 265),
(2, 'attraction', 178, 'view', NULL, 120),
(2, 'attraction', 185, 'view', NULL, 458),
(2, 'attraction', 188, 'share', 4.77, NULL),
(2, 'attraction', 127, 'share', NULL, NULL),
(2, 'attraction', 97, 'view', 4.58, 240),
(2, 'attraction', 42, 'share', NULL, NULL),
(2, 'attraction', 9, 'favorite', NULL, NULL),
(2, 'attraction', 67, 'view', NULL, 184),
(2, 'attraction', 97, 'click', 4.09, 105),
(2, 'attraction', 87, 'share', 3.86, NULL),
(2, 'attraction', 168, 'click', 3.87, 64),
(2, 'attraction', 26, 'favorite', NULL, NULL),
(2, 'attraction', 49, 'share', 4.44, NULL),
(2, 'attraction', 187, 'favorite', 3.59, NULL),
(2, 'attraction', 25, 'view', NULL, 572),
(3, 'attraction', 40, 'view', NULL, 447),
(3, 'attraction', 23, 'favorite', 4.25, NULL),
(3, 'attraction', 123, 'click', NULL, 221),
(3, 'attraction', 44, 'favorite', 3.95, NULL),
(3, 'attraction', 30, 'favorite', 3.94, NULL),
(3, 'attraction', 170, 'favorite', NULL, NULL),
(3, 'attraction', 140, 'share', NULL, NULL),
(3, 'attraction', 98, 'favorite', NULL, NULL),
(3, 'attraction', 12, 'favorite', NULL, NULL),
(3, 'attraction', 68, 'click', 4.13, 164),
(3, 'attraction', 193, 'view', NULL, 362),
(3, 'attraction', 139, 'share', 4.18, NULL),
(3, 'attraction', 142, 'view', 4.64, 438),
(3, 'attraction', 138, 'share', 3.47, NULL),
(3, 'attraction', 77, 'view', NULL, 45),
(3, 'attraction', 129, 'click', NULL, 469),
(3, 'attraction', 45, 'click', 4.62, 139),
(3, 'attraction', 11, 'share', NULL, NULL),
(3, 'attraction', 175, 'share', NULL, NULL),
(3, 'attraction', 161, 'click', 3.52, 78),
(3, 'attraction', 23, 'share', 4.1, NULL),
(3, 'attraction', 71, 'share', 3.02, NULL),
(3, 'attraction', 180, 'share', NULL, NULL),
(3, 'attraction', 39, 'click', 4.17, 112),
(3, 'attraction', 28, 'view', 4.73, 362),
(3, 'attraction', 106, 'favorite', NULL, NULL),
(3, 'attraction', 125, 'share', NULL, NULL),
(3, 'attraction', 71, 'share', NULL, NULL),
(3, 'attraction', 156, 'share', NULL, NULL),
(3, 'attraction', 85, 'click', NULL, 496),
(3, 'attraction', 69, 'favorite', NULL, NULL),
(3, 'attraction', 78, 'favorite', NULL, NULL),
(3, 'attraction', 126, 'view', 3.46, 317),
(3, 'attraction', 191, 'share', 3.64, NULL),
(3, 'attraction', 187, 'share', 3.18, NULL),
(3, 'attraction', 114, 'click', 3.81, 506),
(3, 'attraction', 195, 'favorite', 4.03, NULL),
(3, 'attraction', 41, 'view', NULL, 528),
(3, 'attraction', 104, 'click', 4.05, 592),
(3, 'attraction', 8, 'click', NULL, 215),
(3, 'attraction', 15, 'click', NULL, 61),
(3, 'attraction', 93, 'click', NULL, 485),
(3, 'attraction', 130, 'share', 3.25, NULL),
(3, 'attraction', 8, 'click', NULL, 466),
(3, 'attraction', 46, 'share', 3.7, NULL);

-- ============================================================
-- 将用户行为记录的时间分散到最近60天内，使热度和增长率数据真实有效
-- 约60%的记录分布在最近30天（近期），40%分布在30-60天前（上期）
-- ============================================================
UPDATE user_behavior SET create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND(id) * 25 + 1) DAY)
WHERE MOD(id, 5) != 0;

UPDATE user_behavior SET create_time = DATE_SUB(NOW(), INTERVAL FLOOR(RAND(id) * 30 + 31) DAY)
WHERE MOD(id, 5) = 0;

-- ============================================================
-- 修正景点统计数据（基于实际行为和收藏记录）
-- ============================================================

-- 浏览量 = user_behavior 中 behavior_type='view' 的记录数
UPDATE attraction a SET view_count = (
    SELECT COUNT(*) FROM user_behavior ub
    WHERE ub.item_type = 'attraction' AND ub.item_id = a.id AND ub.behavior_type = 'view'
);

-- 收藏量 = favorite 表中 item_type='attraction' 的记录数
UPDATE attraction a SET favorite_count = (
    SELECT COUNT(*) FROM favorite f
    WHERE f.item_type = 'attraction' AND f.item_id = a.id
);

-- 评论数 = attraction_rating 表中的记录数
UPDATE attraction a SET review_count = (
    SELECT COUNT(*) FROM attraction_rating ar
    WHERE ar.attraction_id = a.id
);

-- 所有景点季节性状态设为正常开放
UPDATE attraction SET seasonal_status = 0, seasonal_note = NULL;

