-- 为 user 表添加 balance 字段（手动执行）
-- 在 MySQL 控制台或 Navicat 中执行此 SQL

ALTER TABLE `user` ADD COLUMN `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '余额' AFTER `avatar_url`;

-- 为管理员设置初始余额
UPDATE `user` SET `balance` = 100.00 WHERE `role` = 1;
