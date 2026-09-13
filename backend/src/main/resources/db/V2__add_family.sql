-- 家庭功能迁移脚本 v2
-- 执行时间：2025-05-30

-- 1. 创建 family 表
CREATE TABLE IF NOT EXISTS `family` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '家庭名称',
  `invite_code` varchar(6) NOT NULL COMMENT '邀请码',
  `admin_uuid` varchar(100) NOT NULL COMMENT '管理员UUID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭表';

-- 2. user 表添加 family_id 字段
ALTER TABLE `user` ADD COLUMN `family_id` bigint COMMENT '所属家庭ID' AFTER `balance`;

-- 3. order 表添加新字段
ALTER TABLE `order` ADD COLUMN `customer_uuid` varchar(100) COMMENT '下单人UUID' AFTER `status`;
ALTER TABLE `order` ADD COLUMN `customer_name` varchar(50) COMMENT '下单客户昵称' AFTER `customer_uuid`;
ALTER TABLE `order` ADD COLUMN `maker_uuid` varchar(100) COMMENT '制作者UUID' AFTER `customer_name`;
ALTER TABLE `order` ADD COLUMN `maker_name` varchar(50) COMMENT '制作者昵称' AFTER `maker_uuid`;
ALTER TABLE `order` ADD COLUMN `family_id` bigint COMMENT '家庭ID' AFTER `maker_name`;

-- 4. dish 表添加 family_id 字段
ALTER TABLE `dish` ADD COLUMN `family_id` bigint COMMENT '所属家庭ID' AFTER `sort`;

-- 5. category 表添加 family_id 字段
ALTER TABLE `category` ADD COLUMN `family_id` bigint COMMENT '所属家庭ID' AFTER `status`;

-- 6. 更新现有订单数据
UPDATE `order` o
SET o.customer_uuid = u.uuid,
    o.customer_name = u.nickname,
    o.maker_uuid = u.uuid,
    o.maker_name = u.nickname
FROM `user` u
WHERE u.role = 1 AND o.customer_uuid IS NULL LIMIT 1;
