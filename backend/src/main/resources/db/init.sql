-- 家庭点餐：空库全量初始化（禁止 DROP，无示例业务数据）
-- 新电脑 / 空 MySQL 只执行本文件。已有数据的库请改用 upgrade.sql

CREATE DATABASE IF NOT EXISTS `home_ordering` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `home_ordering`;

CREATE TABLE IF NOT EXISTS `family` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '家庭名称',
  `invite_code` VARCHAR(6) NOT NULL COMMENT '邀请码',
  `admin_uuid` VARCHAR(100) NOT NULL COMMENT '管理员UUID（家庭创建者）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  INDEX `idx_admin_uuid` (`admin_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表';

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `uuid` VARCHAR(100) NOT NULL COMMENT '用户唯一标识',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像：本站上传存 /uploads/...，外链可存完整 URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `address` VARCHAR(200) DEFAULT NULL COMMENT '地址',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色缓存: 0-成员 1-家庭创建者，权限以 family.admin_uuid 为准',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `family_id` BIGINT DEFAULT NULL COMMENT '所属家庭ID',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_uuid` (`uuid`),
  INDEX `idx_family_id` (`family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标：本站上传存 /uploads/...',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `family_id` BIGINT DEFAULT NULL COMMENT '所属家庭ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_name` (`family_id`, `name`),
  INDEX `idx_family_status_sort` (`family_id`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';

CREATE TABLE IF NOT EXISTS `dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '菜品图：本站上传存 /uploads/...',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '菜品简介',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '菜品价格',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
  `order_count` INT NOT NULL DEFAULT 0 COMMENT '点单次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `extensions` TEXT DEFAULT NULL COMMENT '扩展选项配置(JSON)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `family_id` BIGINT DEFAULT NULL COMMENT '所属家庭ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_family_status_sort` (`family_id`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: -1已取消 0待接单 1制作中 2已完成',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `customer_uuid` VARCHAR(100) DEFAULT NULL COMMENT '下单人UUID',
  `customer_name` VARCHAR(50) DEFAULT NULL COMMENT '下单人昵称',
  `maker_uuid` VARCHAR(100) DEFAULT NULL COMMENT '制作人UUID',
  `maker_name` VARCHAR(50) DEFAULT NULL COMMENT '制作人昵称',
  `family_id` BIGINT DEFAULT NULL COMMENT '家庭ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  INDEX `idx_family_status` (`family_id`, `status`),
  INDEX `idx_customer_uuid` (`customer_uuid`),
  INDEX `idx_maker_uuid` (`maker_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `dish_image` VARCHAR(500) DEFAULT NULL COMMENT '下单时菜品图快照：本站上传存 /uploads/...',
  `dish_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '菜品价格',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';
