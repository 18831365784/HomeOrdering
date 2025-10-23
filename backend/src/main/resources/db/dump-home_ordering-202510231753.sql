-- MySQL dump 10.13  Distrib 8.0.37, for Win64 (x86_64)
--
-- Host: 172.16.1.134    Database: home_ordering
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序，越小越靠前',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-停用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_status_sort` (`status`,`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'肉菜','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_551d9d0f9bce4bbcb1a09954e1ba64ca.png',1,1,'2025-10-22 17:32:53','2025-10-23 01:55:58'),(2,'主食','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_e35cfc0a9aa44e3fb9448b041968aff7.png',5,1,'2025-10-23 01:11:40','2025-10-23 01:55:58'),(3,'素菜','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_f3b461957f8f4a79b2eb84706ace474e.png',2,1,'2025-10-23 01:11:54','2025-10-23 01:55:58'),(4,'凉菜','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_8d628305e001449f9273ca7c4ea7c310.png',3,1,'2025-10-23 01:12:07','2025-10-23 01:55:58'),(5,'火锅','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_71e4b517268e46e3bb2e54731f5296c5.png',8,1,'2025-10-23 01:12:16','2025-10-23 01:55:58'),(6,'炸食','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_b3db2bfc0c744c4e94e0038536b15c77.png',4,1,'2025-10-23 01:12:37','2025-10-23 01:55:58'),(7,'汤','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_f8d1f77e196c40a8bb7bc89bb5388ab0.png',6,1,'2025-10-23 01:55:18','2025-10-23 01:55:58'),(8,'饮品','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_91b78a5c4d254c8ba07378dbaaa528f6.png',7,1,'2025-10-23 01:55:40','2025-10-23 01:55:58'),(9,'测试满','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/icon/20251023_e62590c8593547e7929aecb6fa78ed8a.jpg',9,0,'2025-10-23 01:56:56','2025-10-23 02:35:04');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dish`
--

DROP TABLE IF EXISTS `dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品图片URL',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品简介',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '菜品价格',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品分类: 肉类、蔬菜、主食、凉菜、汤',
  `order_count` int NOT NULL DEFAULT '0' COMMENT '点单次数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-停用 1-启用',
  `extensions` text COLLATE utf8mb4_unicode_ci COMMENT '扩展选项配置(JSON)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序，越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_order_count` (`order_count` DESC),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish`
--

LOCK TABLES `dish` WRITE;
/*!40000 ALTER TABLE `dish` DISABLE KEYS */;
INSERT INTO `dish` VALUES (1,'葱烧豆腐','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_05f77c76e29f4c979b63a972bec1368d.jpg','师从隋卞大师',6.00,'素菜',2,1,NULL,17,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(2,'耗油生菜','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_dd7e8f09c79243fdabf5992aa44b9325.jpg','解腻小绿菜',5.00,'素菜',2,1,NULL,18,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(3,'红烧肉','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_f79f53f173b7479c8eecc4b6bbd51fff.jpg','微枣香做法，好吃不好吃不负责',30.00,'肉菜',1,1,NULL,19,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(4,'豆角肉沫拌面','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_4469cd7e114540c1b24fa5e970a167ce.jpg','别嫌定价高～豆角 肉沫都不便宜 ',12.00,'主食',1,1,NULL,20,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(5,'长寿面','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_aea050dc293a42ccb26a858c4b2a9743.jpg','生日限定哦 前一天会上架 希望你记得下单',0.00,'主食',1,1,NULL,21,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(6,'虾仁炒鸡蛋','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_f82a1514c85b4f9e97bc2028da83044b.jpg','忘记虾仁多少钱了 好像28一斤',30.00,'肉菜',3,1,NULL,22,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(7,'米饭',NULL,'香糯米饭',3.00,'主食',2,1,NULL,23,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(8,'西红柿鸡蛋打卤面','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_6da033ae42e64ca097b98e4b8d0f2b8b.jpg','也可以做炒面哦（面换成方便面）',10.00,'主食',0,1,NULL,24,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(9,'酸辣木耳','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_2d5542d2223a468ca9a9e68d7f202b10.jpg','清肺小凉菜',5.00,'凉菜',1,1,NULL,25,'2025-10-19 15:43:02','2025-10-23 16:46:47'),(15,'金汤板栗鸡','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251022_ad280a4e2c7d4aa692cf88f50ca5658a.jpg','',58.00,'汤',0,1,NULL,15,'2025-10-22 14:50:06','2025-10-23 16:46:47'),(16,'香菇滑鸡','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_352a0a3457984dd3880adbdae79b8df2.jpg','',38.00,'肉菜',0,1,NULL,14,'2025-10-22 14:51:23','2025-10-23 16:46:47'),(17,'蒜苔炒肉','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251022_021508ce45964ccfb37341c4d5e9d111.jpg','',38.00,'肉类',0,1,NULL,13,'2025-10-22 14:53:22','2025-10-23 16:46:47'),(18,'麻辣烫','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_b01cac22025847fcb824556a4a112f4f.jpg','',38.00,'肉菜',0,1,NULL,12,'2025-10-22 14:55:28','2025-10-23 16:46:47'),(19,'金汤力','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251022_293dc686d4f648cbb02f0d34eb2f7f2f.jpg','',18.00,'饮品',0,1,NULL,16,'2025-10-22 14:56:19','2025-10-23 16:46:47'),(20,'话梅金汤力','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_05f1d6ac625643538536aecc5ef8a483.jpg','限定饮品，可遇不可求～',10.00,'饮品',0,1,NULL,11,'2025-10-23 15:37:19','2025-10-23 16:46:47'),(21,'糖醋排骨','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_b116308739ca46789a24a3c28fb8387f.jpg','精排好吃 贵就贵点吧',48.00,'肉菜',0,1,NULL,10,'2025-10-23 15:57:38','2025-10-23 16:46:46'),(22,'私人蛋炒饭','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_0b54cc767c6b443699db5ff056b3a95a.jpg','私人做法哦',10.00,'主食',1,1,NULL,9,'2025-10-23 15:58:45','2025-10-23 16:46:46'),(23,'鱼香鸡丝','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_18f0b72aa00b47a898c46272df7e4b3c.jpg','想吃得给买刀（咱家那刀切肉丝好费劲的）',15.00,'肉菜',1,1,NULL,7,'2025-10-23 16:00:59','2025-10-23 16:46:46'),(24,'木须肉','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_d2f5d589a3f742e2a6a03a38de487880.jpg','配菜多 肉占一半',16.00,'肉菜',0,1,NULL,6,'2025-10-23 16:02:33','2025-10-23 16:46:46'),(25,'雪碧鸡翅','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_82038af6a6d947bcb2a5914dfce2f065.jpg','内心OS：还是觉得可乐鸡翅好吃',30.00,'肉菜',0,1,NULL,8,'2025-10-23 16:03:27','2025-10-23 16:46:46'),(26,'土豆炖鸡块','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_5d551e7e09eb446bba44376aee97a6af.jpg','热乎乎一大锅～',35.00,'肉菜',0,1,NULL,4,'2025-10-23 16:04:08','2025-10-23 16:46:46'),(27,'辣椒炒肉','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_12ea2e581cbd427ba437fbd0ab4b59e4.jpg','超级下饭菜！',16.00,'肉菜',0,1,NULL,5,'2025-10-23 16:04:59','2025-10-23 16:46:46'),(28,'香菇油菜','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_460d038b3bb94071bc333e3abf8b2971.jpg','曾经被我誉为个人最拿得出手的菜',10.00,'素菜',0,1,NULL,3,'2025-10-23 16:05:42','2025-10-23 16:46:46'),(29,'麻婆豆腐','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_dbc82a6dfda2416a9d9044331ad60984.jpg','辣椒放不好 得贴便签分类了 辣的 不辣的 超级辣的…',6.00,'素菜',0,1,NULL,2,'2025-10-23 16:07:01','2025-10-23 16:46:46'),(30,'家常菜花','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_19508f089b17424d88dd3c5d2673cdce.jpg','个人家常版',10.00,'素菜',1,1,NULL,1,'2025-10-23 16:07:32','2025-10-23 09:50:08'),(31,'测试菜品','','1',1.00,'肉菜',1,1,'{\"options\":[{\"selectionType\":\"single\",\"required\":true,\"choices\":[{\"id\":\"mmid\",\"name\":\"微微辣\",\"price\":0},{\"id\":\"mild\",\"name\":\"微辣\",\"price\":0},{\"id\":\"medium\",\"name\":\"中辣\",\"price\":0},{\"id\":\"hot\",\"name\":\"重辣\",\"price\":1}],\"id\":\"spicy\",\"name\":\"辣度\"},{\"id\":\"12\",\"name\":\"冰量\",\"selectionType\":\"input\",\"required\":false,\"choices\":[]}]}',0,'2025-10-23 16:46:25','2025-10-23 09:50:08');
/*!40000 ALTER TABLE `dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态: 0-等待老公确认 1-老公大人已许可 2-已完成',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'202510191615341425',66.00,2,'','2025-10-19 16:15:34','2025-10-19 16:32:34'),(2,'202510191723043706',41.00,1,'','2025-10-19 17:23:04','2025-10-23 06:25:42'),(3,'202510202319221016',106.00,2,'','2025-10-20 23:19:22','2025-10-20 23:20:46'),(5,'202510231428476457',40.00,0,'','2025-10-23 06:28:46','2025-10-23 06:28:46'),(6,'202510231435362395',3.00,0,'','2025-10-23 06:35:36','2025-10-23 06:35:36'),(7,'202510231439278839',22.00,0,'','2025-10-23 06:39:27','2025-10-23 06:39:27'),(8,'202510231440353592',12.00,0,'','2025-10-23 06:40:35','2025-10-23 06:40:35'),(9,'202510231445505147',22.00,0,'','2025-10-23 14:45:51','2025-10-23 14:45:51'),(10,'202510231609168580',25.00,1,'下个单试试水啦～','2025-10-23 16:09:17','2025-10-23 16:09:29'),(11,'202510231750089681',11.00,0,'【菜品选项】:\n测试菜品(1份):\n  辣度: 重辣','2025-10-23 17:50:09','2025-10-23 17:50:09');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_detail`
--

DROP TABLE IF EXISTS `order_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `dish_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品图片',
  `dish_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '菜品价格',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `subtotal` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_detail`
--

LOCK TABLES `order_detail` WRITE;
/*!40000 ALTER TABLE `order_detail` DISABLE KEYS */;
INSERT INTO `order_detail` VALUES (1,1,1,'宫保鸡丁',NULL,38.00,1,38.00),(2,1,2,'麻婆豆腐',NULL,28.00,1,28.00),(3,2,7,'米饭',NULL,3.00,1,3.00),(4,2,1,'宫保鸡丁',NULL,38.00,1,38.00),(5,3,3,'红烧肉',NULL,48.00,1,48.00),(6,3,4,'清蒸鹈鱼',NULL,58.00,1,58.00),(8,5,6,'手擕包菜',NULL,22.00,1,22.00),(9,5,5,'番茄炒蛋',NULL,18.00,1,18.00),(10,6,7,'米饭',NULL,3.00,1,3.00),(11,7,6,'手擕包菜',NULL,22.00,1,22.00),(12,8,9,'紫菜蛋花汤',NULL,12.00,1,12.00),(13,9,6,'手擕包菜',NULL,22.00,1,22.00),(14,10,23,'鱼香鸡丝','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_18f0b72aa00b47a898c46272df7e4b3c.jpg',15.00,1,15.00),(15,10,22,'私人蛋炒饭','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_0b54cc767c6b443699db5ff056b3a95a.jpg',10.00,1,10.00),(16,11,31,'测试菜品','',1.00,1,1.00),(17,11,30,'家常菜花','https://unperverted-neida-noncounterfeit.ngrok-free.dev/api/uploads/20251023_19508f089b17424d88dd3c5d2673cdce.jpg',10.00,1,10.00);
/*!40000 ALTER TABLE `order_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信openid',
  `uuid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户唯一标识',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像地址',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色: 0-普通用户 1-管理员',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'oU4JB1z8Dpqy8NEhTCX0PVzXRhRU','415a6118-c478-44c1-9560-7c0d4ca56618','用户','',1,'2025-10-23 03:51:36','2025-10-23 09:48:49'),(8,'oU4JB18gA_arK0F3iB5W_Zi8Tocs','4dbaff4d-8dca-44cf-82cb-1abdaea03822','用户','',0,'2025-10-23 06:54:48','2025-10-23 06:54:48');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'home_ordering'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-23 17:53:25
