-- 家庭点餐：已有库保数据升级（可重复执行）
-- 不要在已有数据的库上执行 init.sql 以外的 DROP。本脚本只补表、补列、修正索引。

USE `home_ordering`;

-- 工具：按需加列
DROP PROCEDURE IF EXISTS `ho_add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `ho_add_column_if_missing`(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  ) THEN
    SET @sql = p_ddl;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

-- 工具：按需加索引
DROP PROCEDURE IF EXISTS `ho_add_index_if_missing`;
DELIMITER $$
CREATE PROCEDURE `ho_add_index_if_missing`(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index
  ) THEN
    SET @sql = p_ddl;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CREATE TABLE IF NOT EXISTS `family` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '家庭名称',
  `invite_code` VARCHAR(6) NOT NULL COMMENT '邀请码',
  `admin_uuid` VARCHAR(100) NOT NULL COMMENT '管理员UUID（家庭创建者）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表';

CALL ho_add_column_if_missing('user', 'phone',
  'ALTER TABLE `user` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT ''手机号'' AFTER `avatar_url`');
CALL ho_add_column_if_missing('user', 'address',
  'ALTER TABLE `user` ADD COLUMN `address` VARCHAR(200) DEFAULT NULL COMMENT ''地址'' AFTER `phone`');
CALL ho_add_column_if_missing('user', 'balance',
  'ALTER TABLE `user` ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT ''余额'' AFTER `role`');
CALL ho_add_column_if_missing('user', 'family_id',
  'ALTER TABLE `user` ADD COLUMN `family_id` BIGINT DEFAULT NULL COMMENT ''所属家庭ID'' AFTER `balance`');
CALL ho_add_column_if_missing('user', 'last_login_time',
  'ALTER TABLE `user` ADD COLUMN `last_login_time` DATETIME DEFAULT NULL COMMENT ''最后登录时间'' AFTER `family_id`');

CALL ho_add_index_if_missing('user', 'idx_family_id', 'ALTER TABLE `user` ADD INDEX `idx_family_id` (`family_id`)');
CALL ho_add_index_if_missing('user', 'uk_uuid', 'ALTER TABLE `user` ADD UNIQUE KEY `uk_uuid` (`uuid`)');

CALL ho_add_column_if_missing('category', 'family_id',
  'ALTER TABLE `category` ADD COLUMN `family_id` BIGINT DEFAULT NULL COMMENT ''所属家庭ID'' AFTER `status`');

-- 去掉全局分类名唯一，改为家庭内唯一
DROP PROCEDURE IF EXISTS `ho_fix_category_uk`;
DELIMITER $$
CREATE PROCEDURE `ho_fix_category_uk`()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'category' AND INDEX_NAME = 'uk_name'
  ) THEN
    ALTER TABLE `category` DROP INDEX `uk_name`;
  END IF;
END$$
DELIMITER ;
CALL ho_fix_category_uk();
DROP PROCEDURE IF EXISTS `ho_fix_category_uk`;

CALL ho_add_index_if_missing('category', 'uk_family_name',
  'ALTER TABLE `category` ADD UNIQUE KEY `uk_family_name` (`family_id`, `name`)');

CALL ho_add_column_if_missing('dish', 'extensions',
  'ALTER TABLE `dish` ADD COLUMN `extensions` TEXT DEFAULT NULL COMMENT ''扩展选项配置(JSON)'' AFTER `status`');
CALL ho_add_column_if_missing('dish', 'sort',
  'ALTER TABLE `dish` ADD COLUMN `sort` INT NOT NULL DEFAULT 0 COMMENT ''排序'' AFTER `extensions`');
CALL ho_add_column_if_missing('dish', 'family_id',
  'ALTER TABLE `dish` ADD COLUMN `family_id` BIGINT DEFAULT NULL COMMENT ''所属家庭ID'' AFTER `sort`');

CALL ho_add_column_if_missing('order', 'customer_uuid',
  'ALTER TABLE `order` ADD COLUMN `customer_uuid` VARCHAR(100) DEFAULT NULL COMMENT ''下单人UUID'' AFTER `remark`');
CALL ho_add_column_if_missing('order', 'customer_name',
  'ALTER TABLE `order` ADD COLUMN `customer_name` VARCHAR(50) DEFAULT NULL COMMENT ''下单人昵称'' AFTER `customer_uuid`');
CALL ho_add_column_if_missing('order', 'maker_uuid',
  'ALTER TABLE `order` ADD COLUMN `maker_uuid` VARCHAR(100) DEFAULT NULL COMMENT ''制作人UUID'' AFTER `customer_name`');
CALL ho_add_column_if_missing('order', 'maker_name',
  'ALTER TABLE `order` ADD COLUMN `maker_name` VARCHAR(50) DEFAULT NULL COMMENT ''制作人昵称'' AFTER `maker_uuid`');
CALL ho_add_column_if_missing('order', 'family_id',
  'ALTER TABLE `order` ADD COLUMN `family_id` BIGINT DEFAULT NULL COMMENT ''家庭ID'' AFTER `maker_name`');

-- 旧脚本曾用非法的 UPDATE ... FROM，这里用 MySQL 可执行写法尽量补历史订单身份
UPDATE `order` o
JOIN `user` u ON u.role = 1
SET o.customer_uuid = IFNULL(o.customer_uuid, u.uuid),
    o.customer_name = IFNULL(o.customer_name, u.nickname),
    o.maker_uuid = IFNULL(o.maker_uuid, u.uuid),
    o.maker_name = IFNULL(o.maker_name, u.nickname)
WHERE o.customer_uuid IS NULL;

-- 本站上传图只存相对路径 /uploads/...，换域名/IP 时由后端用 server.url 拼接
UPDATE `dish`
SET `image_url` = CONCAT('/uploads/', SUBSTRING_INDEX(`image_url`, '/uploads/', -1))
WHERE `image_url` IS NOT NULL
  AND `image_url` LIKE '%/uploads/%'
  AND `image_url` NOT LIKE '/uploads/%';

UPDATE `category`
SET `icon_url` = CONCAT('/uploads/', SUBSTRING_INDEX(`icon_url`, '/uploads/', -1))
WHERE `icon_url` IS NOT NULL
  AND `icon_url` LIKE '%/uploads/%'
  AND `icon_url` NOT LIKE '/uploads/%';

UPDATE `user`
SET `avatar_url` = CONCAT('/uploads/', SUBSTRING_INDEX(`avatar_url`, '/uploads/', -1))
WHERE `avatar_url` IS NOT NULL
  AND `avatar_url` LIKE '%/uploads/%'
  AND `avatar_url` NOT LIKE '/uploads/%';

UPDATE `order_detail`
SET `dish_image` = CONCAT('/uploads/', SUBSTRING_INDEX(`dish_image`, '/uploads/', -1))
WHERE `dish_image` IS NOT NULL
  AND `dish_image` LIKE '%/uploads/%'
  AND `dish_image` NOT LIKE '/uploads/%';

DROP PROCEDURE IF EXISTS `ho_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `ho_add_index_if_missing`;
