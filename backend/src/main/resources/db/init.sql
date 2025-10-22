-- 家庭点餐系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `home_ordering` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `home_ordering`;

-- 菜品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '图标',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  INDEX `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
  `uuid` VARCHAR(100) DEFAULT NULL COMMENT '用户唯一标识',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
  `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  INDEX `idx_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 菜品表
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片URL',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '菜品简介',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '菜品价格',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '菜品分类: 肉类、蔬菜、主食、凉菜、汤',
  `order_count` INT NOT NULL DEFAULT 0 COMMENT '点单次数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_order_count` (`order_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-等待老公确认 1-老公大人已许可 2-已完成',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  INDEX `idx_status` (`status`),
  INDEX `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单详情表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `dish_image` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片',
  `dish_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '菜品价格',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- 插入示例菜品数据
INSERT INTO `dish` (`name`, `image_url`, `description`, `price`, `category`, `order_count`, `status`) VALUES
('宫保鸡丁', NULL, '经典川菜，鸡肉鲜嫩，花生酥脆', 38.00, '肉类', 0, 1),
('麻婆豆腐', NULL, '麻辣鲜香，豆腐嫩滑入味', 28.00, '蔬菜', 0, 1),
('红烧肉', NULL, '色泽红亮，肥而不腻', 48.00, '肉类', 0, 1),
('清蒸鹈鱼', NULL, '鱼肉鲜嫩，原汁原味', 58.00, '肉类', 0, 1),
('番茄炒蛋', NULL, '家常美味，酸甜可口', 18.00, '蔬菜', 0, 1),
('手擕包菜', NULL, '清脆爽口，蒜香浓郁', 22.00, '蔬菜', 0, 1),
('米饭', NULL, '香糯米饭', 3.00, '主食', 0, 1),
('凉拌黄瓜', NULL, '清爽解腻', 15.00, '凉菜', 0, 1),
('紫菜蛋花汤', NULL, '清淡鲜美', 12.00, '汤', 0, 1);

-- 数据库初始化完成
SELECT '数据库初始化完成！' AS message;
