/*
 Navicat Premium Data Transfer

 Source Server         : 172.16.1.134
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : 172.16.1.134:3306
 Source Schema         : home_ordering

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 27/10/2025 19:03:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name`) USING BTREE,
  INDEX `idx_status_sort`(`status`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '肉菜', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_551d9d0f9bce4bbcb1a09954e1ba64ca.png', 1, 0, '2025-10-22 17:32:53', '2025-10-24 16:26:01');
INSERT INTO `category` VALUES (2, '主食', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_e35cfc0a9aa44e3fb9448b041968aff7.png', 5, 1, '2025-10-23 01:11:40', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (3, '素菜', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_f3b461957f8f4a79b2eb84706ace474e.png', 2, 1, '2025-10-23 01:11:54', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (4, '凉菜', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_8d628305e001449f9273ca7c4ea7c310.png', 3, 1, '2025-10-23 01:12:07', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (5, '火锅', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_71e4b517268e46e3bb2e54731f5296c5.png', 8, 1, '2025-10-23 01:12:16', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (6, '炸食', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_b3db2bfc0c744c4e94e0038536b15c77.png', 4, 1, '2025-10-23 01:12:37', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (7, '汤', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_f8d1f77e196c40a8bb7bc89bb5388ab0.png', 6, 1, '2025-10-23 01:55:18', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (8, '饮品', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_91b78a5c4d254c8ba07378dbaaa528f6.png', 7, 1, '2025-10-23 01:55:40', '2025-10-23 01:55:58');
INSERT INTO `category` VALUES (9, '测试满', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_e62590c8593547e7929aecb6fa78ed8a.jpg', 9, 0, '2025-10-23 01:56:56', '2025-10-24 01:45:25');

-- ----------------------------
-- Table structure for dish
-- ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品图片URL',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品简介',
  `price` decimal(10, 2) NOT NULL COMMENT '菜品价格',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品分类: 肉类、蔬菜、主食、凉菜、汤',
  `order_count` int(0) NOT NULL DEFAULT 0 COMMENT '点单次数',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `extensions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '扩展选项配置(JSON)',
  `sort` int(0) NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_order_count`(`order_count`) USING BTREE,
  INDEX `idx_sort`(`sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish
-- ----------------------------
INSERT INTO `dish` VALUES (1, '葱烧豆腐', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_05f77c76e29f4c979b63a972bec1368d.jpg', '师从隋卞大师', 6.00, '素菜', 0, 1, NULL, 18, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (2, '耗油生菜', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_dd7e8f09c79243fdabf5992aa44b9325.jpg', '解腻小绿菜', 5.00, '素菜', 0, 1, NULL, 19, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (3, '红烧肉', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_f79f53f173b7479c8eecc4b6bbd51fff.jpg', '微枣香做法，好吃不好吃不负责', 30.00, '肉菜', 0, 1, NULL, 20, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (4, '豆角肉沫拌面', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_4469cd7e114540c1b24fa5e970a167ce.jpg', '别嫌定价高～豆角 肉沫都不便宜 ', 12.00, '主食', 2, 1, NULL, 21, '2025-10-19 15:43:02', '2025-10-25 06:42:10');
INSERT INTO `dish` VALUES (5, '长寿面', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_aea050dc293a42ccb26a858c4b2a9743.jpg', '生日限定哦 前一天会上架 希望你记得下单', 0.00, '主食', 0, 1, NULL, 22, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (6, '虾仁炒鸡蛋', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_f82a1514c85b4f9e97bc2028da83044b.jpg', '忘记虾仁多少钱了 好像28一斤', 30.00, '肉菜', 0, 1, NULL, 23, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (7, '米饭', NULL, '香糯米饭', 3.00, '主食', 0, 1, NULL, 24, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (8, '西红柿鸡蛋打卤面', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_6da033ae42e64ca097b98e4b8d0f2b8b.jpg', '也可以做炒面哦（面换成方便面）', 10.00, '主食', 0, 1, NULL, 25, '2025-10-19 15:43:02', '2025-10-23 23:26:40');
INSERT INTO `dish` VALUES (9, '酸辣木耳', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_2d5542d2223a468ca9a9e68d7f202b10.jpg', '清肺小凉菜', 5.00, '凉菜', 0, 1, NULL, 26, '2025-10-19 15:43:02', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (15, '金汤板栗鸡', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251022_ad280a4e2c7d4aa692cf88f50ca5658a.jpg', '滋补～', 25.00, '汤', 0, 1, '{\"options\":[]}', 16, '2025-10-22 14:50:06', '2025-10-24 13:56:58');
INSERT INTO `dish` VALUES (16, '香菇滑鸡', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_352a0a3457984dd3880adbdae79b8df2.jpg', '', 15.00, '肉菜', 0, 1, '{\"options\":[]}', 15, '2025-10-22 14:51:23', '2025-10-24 13:55:28');
INSERT INTO `dish` VALUES (17, '蒜苔炒肉', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251022_021508ce45964ccfb37341c4d5e9d111.jpg', '', 12.00, '肉菜', 0, 1, '{\"options\":[]}', 14, '2025-10-22 14:53:22', '2025-10-24 13:55:11');
INSERT INTO `dish` VALUES (18, '麻辣烫', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_b01cac22025847fcb824556a4a112f4f.jpg', '', 30.00, '肉菜', 0, 1, '{\"options\":[{\"selectionType\":\"multiple\",\"required\":false,\"choices\":[{\"id\":\"choice_1761273961483_ucmc\",\"name\":\"鱼丸\",\"price\":0},{\"id\":\"choice_1761273962268_nqge\",\"name\":\"虾丸\",\"price\":0},{\"id\":\"choice_1761273962615_fyok\",\"name\":\"桂花肠\",\"price\":0},{\"id\":\"choice_1761274015494_u06e\",\"name\":\"鱼豆腐\",\"price\":0},{\"id\":\"choice_1761274015708_7orn\",\"name\":\"撒尿牛丸\",\"price\":0},{\"id\":\"choice_1761274015903_pvaj\",\"name\":\"娃娃菜\",\"price\":0},{\"id\":\"choice_1761274016132_jmkd\",\"name\":\"方便面\",\"price\":0}],\"id\":\"opt_mh491a0f_s07bhe\",\"name\":\"\"}]}', 13, '2025-10-22 14:55:28', '2025-10-24 13:57:43');
INSERT INTO `dish` VALUES (19, '金汤力', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251022_293dc686d4f648cbb02f0d34eb2f7f2f.jpg', '', 8.00, '饮品', 0, 1, '{\"options\":[]}', 17, '2025-10-22 14:56:19', '2025-10-24 13:55:51');
INSERT INTO `dish` VALUES (20, '话梅金汤力', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_05f1d6ac625643538536aecc5ef8a483.jpg', '限定饮品，可遇不可求～', 10.00, '饮品', 0, 1, NULL, 12, '2025-10-23 15:37:19', '2025-10-23 23:26:38');
INSERT INTO `dish` VALUES (21, '糖醋排骨', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_b116308739ca46789a24a3c28fb8387f.jpg', '精排好吃 贵就贵点吧', 48.00, '肉菜', 0, 1, NULL, 11, '2025-10-23 15:57:38', '2025-10-23 23:26:37');
INSERT INTO `dish` VALUES (22, '私人蛋炒饭', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_0b54cc767c6b443699db5ff056b3a95a.jpg', '私人做法哦', 10.00, '主食', 0, 1, NULL, 10, '2025-10-23 15:58:45', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (23, '鱼香鸡丝', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_18f0b72aa00b47a898c46272df7e4b3c.jpg', '想吃得给买刀（咱家那刀切肉丝好费劲的）', 15.00, '肉菜', 0, 1, NULL, 8, '2025-10-23 16:00:59', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (24, '木须肉', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_d2f5d589a3f742e2a6a03a38de487880.jpg', '配菜多 肉占一半', 16.00, '肉菜', 0, 1, NULL, 7, '2025-10-23 16:02:33', '2025-10-23 23:26:37');
INSERT INTO `dish` VALUES (25, '雪碧鸡翅', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_82038af6a6d947bcb2a5914dfce2f065.jpg', '内心OS：还是觉得可乐鸡翅好吃', 30.00, '肉菜', 1, 1, NULL, 9, '2025-10-23 16:03:27', '2025-10-25 06:42:10');
INSERT INTO `dish` VALUES (26, '土豆炖鸡块', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_5d551e7e09eb446bba44376aee97a6af.jpg', '热乎乎一大锅～', 35.00, '肉菜', 0, 1, NULL, 5, '2025-10-23 16:04:08', '2025-10-23 23:26:37');
INSERT INTO `dish` VALUES (27, '辣椒炒肉', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_12ea2e581cbd427ba437fbd0ab4b59e4.jpg', '超级下饭菜！', 16.00, '肉菜', 0, 1, NULL, 6, '2025-10-23 16:04:59', '2025-10-23 23:26:37');
INSERT INTO `dish` VALUES (28, '香菇油菜', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_460d038b3bb94071bc333e3abf8b2971.jpg', '曾经被我誉为个人最拿得出手的菜', 10.00, '素菜', 0, 1, NULL, 4, '2025-10-23 16:05:42', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (29, '麻婆豆腐', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_dbc82a6dfda2416a9d9044331ad60984.jpg', '辣椒放不好 得贴便签分类了 辣的 不辣的 超级辣的…', 6.00, '素菜', 0, 1, NULL, 3, '2025-10-23 16:07:01', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (30, '家常菜花', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_19508f089b17424d88dd3c5d2673cdce.jpg', '个人家常版', 10.00, '素菜', 0, 1, NULL, 1, '2025-10-23 16:07:32', '2025-10-24 05:53:52');
INSERT INTO `dish` VALUES (31, '测试菜品', '', '1', 1.00, '肉菜', 0, 0, '{\"options\":[{\"selectionType\":\"single\",\"required\":true,\"choices\":[{\"id\":\"mmid\",\"name\":\"微微辣\",\"price\":0},{\"id\":\"mild\",\"name\":\"微辣\",\"price\":0},{\"id\":\"medium\",\"name\":\"中辣\",\"price\":0},{\"id\":\"hot\",\"name\":\"重辣\",\"price\":1}],\"id\":\"spicy\",\"name\":\"辣度\"},{\"selectionType\":\"multiple\",\"required\":false,\"choices\":[{\"id\":\"choice_1761270826127_g4u2\",\"name\":\"椰果\",\"price\":0},{\"id\":\"choice_1761270826381_0nr8\",\"name\":\"红豆\",\"price\":0},{\"id\":\"choice_1761270839687_wgy9\",\"name\":\"奥利奥碎\",\"price\":0}],\"id\":\"opt_mh475wd9_9zz0db\",\"name\":\"小料选择\",\"min\":1,\"max\":2},{\"selectionType\":\"number\",\"required\":false,\"choices\":[],\"id\":\"opt_mh47a9xo_y71eom\",\"name\":\"糖量\",\"unit\":\"份\",\"placeholder\":\"输入所需糖量\"},{\"selectionType\":\"boolean\",\"required\":false,\"choices\":[],\"id\":\"opt_mh47aa6y_8g6ayq\",\"name\":\"需要加冰吗\"}]}', 2, '2025-10-23 16:46:25', '2025-10-26 21:00:25');
INSERT INTO `dish` VALUES (32, '螺丝起子', '', '橙汁小饮料～', 8.00, '饮品', 1, 1, '{\"options\":[]}', 0, '2025-10-24 22:32:52', '2025-10-26 21:03:24');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '订单状态: 0-等待老公确认 1-老公大人已许可 2-已完成',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (18, '202510241333155706', 12.00, 1, '老公想吃吃老公给做做', '2025-10-24 13:33:16', '2025-10-24 13:39:33');
INSERT INTO `order` VALUES (19, '202510251442101639', 50.00, 0, '', '2025-10-25 14:42:11', '2025-10-25 14:42:11');

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint(0) NOT NULL COMMENT '订单ID',
  `dish_id` bigint(0) NOT NULL COMMENT '菜品ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `dish_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜品图片',
  `dish_price` decimal(10, 2) NOT NULL COMMENT '菜品价格',
  `quantity` int(0) NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_dish_id`(`dish_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_detail
-- ----------------------------
INSERT INTO `order_detail` VALUES (30, 18, 4, '豆角肉沫拌面', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_4469cd7e114540c1b24fa5e970a167ce.jpg', 12.00, 1, 12.00);
INSERT INTO `order_detail` VALUES (31, 19, 32, '螺丝起子', '', 8.00, 1, 8.00);
INSERT INTO `order_detail` VALUES (32, 19, 25, '雪碧鸡翅', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_82038af6a6d947bcb2a5914dfce2f065.jpg', 30.00, 1, 30.00);
INSERT INTO `order_detail` VALUES (33, 19, 4, '豆角肉沫拌面', 'https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_4469cd7e114540c1b24fa5e970a167ce.jpg', 12.00, 1, 12.00);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信openid',
  `uuid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户唯一标识',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像地址',
  `role` tinyint(0) NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_openid`(`openid`) USING BTREE,
  INDEX `idx_uuid`(`uuid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (14, 'oU4JB1z8Dpqy8NEhTCX0PVzXRhRU', 'c1684bfd-4e60-40af-9bbd-6fda8972a75e', '用户', '', 1, '2025-10-24 03:06:39', '2025-10-26 14:03:57');
INSERT INTO `user` VALUES (64, 'oU4JB18gA_arK0F3iB5W_Zi8Tocs', 'e321de62-fb66-4769-b3b8-41e85fa36967', '用户', '', 0, '2025-10-24 03:16:14', '2025-10-24 03:35:18');
INSERT INTO `user` VALUES (65, 'oU4JB1zXjERezuzuHiNtE6zLVsGY', 'f71886a6-914d-459f-a3a9-5c655f9b4141', '用户', '', 0, '2025-10-24 03:21:22', '2025-10-24 03:21:22');
INSERT INTO `user` VALUES (66, 'oU4JB1zDFh0_lRAAb4i1Bx9ZF3CY', 'dbf9b6b0-6d7b-4715-8ee3-8bc141a214e0', '用户', '', 0, '2025-10-24 03:21:22', '2025-10-24 03:21:22');
INSERT INTO `user` VALUES (67, 'oU4JB146MNUFC-8kg_3JSTV76E0w', '1ab7243c-9032-431f-91c6-9f244b9c5b43', '用户', '', 0, '2025-10-24 03:21:22', '2025-10-24 03:21:22');
INSERT INTO `user` VALUES (68, 'oU4JB19l0J8EfREk7UzEE4wximcg', '2540dd40-fddd-4d91-947f-f8f7e4b95964', '用户', '', 0, '2025-10-24 05:31:43', '2025-10-25 08:37:36');

SET FOREIGN_KEY_CHECKS = 1;
