-- 迁移脚本：为 user 表添加 balance 字段
-- 执行时间：2025-05-30

-- 添加余额字段（如果不存在）
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '余额' AFTER `avatar_url`;

-- 为管理员设置初始余额 100
UPDATE `user` SET `balance` = 100.00 WHERE `role` = 1;
