# 家庭点餐系统 - 后端服务

## 项目介绍
这是一个温馨的情侣点餐微信小程序后端服务，用于家庭点餐使用。

## 技术栈
- Java 17
- Spring Boot 3.1.5
- MyBatis 3.0.3
- MySQL 8.0.25

## 功能特性
- 菜品管理（增删改查、图片上传）
- 订单管理（创建订单、订单列表、订单状态更新）
- 菜品点单次数统计
- 订单状态流转：等待老公确认 → 老公大人已许可 → 已完成
- 图片本地存储（可扩展云存储）

## 快速开始

### 1. 环境准备
- JDK 17+
- Maven 3.6+
- MySQL 8.0.25

### 2. 数据库初始化
执行 `src/main/resources/db/init.sql` 脚本创建数据库和表

```bash
mysql -u root -p < src/main/resources/db/init.sql
```

### 3. 配置修改
修改 `src/main/resources/application.properties` 中的数据库配置：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/home_ordering?...
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

### 4. 运行项目
```bash
mvn clean install
mvn spring-boot:run
```

或者在IDE中直接运行 `HomeOrderingApplication` 主类

### 5. 访问接口
启动成功后，接口地址为：`http://localhost:8080/api`

## API接口文档

### 菜品接口
- `POST /api/dish` - 新增菜品
- `GET /api/dish/{id}` - 查询菜品详情
- `GET /api/dish/list` - 查询菜品列表
- `PUT /api/dish` - 更新菜品
- `DELETE /api/dish/{id}` - 删除菜品

### 订单接口
- `POST /api/order` - 创建订单
- `GET /api/order/{id}` - 查询订单详情
- `GET /api/order/list` - 查询订单列表
- `PUT /api/order/{id}/status` - 更新订单状态
- `DELETE /api/order/{id}` - 删除订单

### 文件接口
- `POST /api/file/upload` - 上传文件
- `DELETE /api/file/delete` - 删除文件

## 订单状态说明
- 0: 等待老公确认
- 1: 老公大人已许可
- 2: 已完成

## 扩展云存储
如需切换到云存储，请修改 `application.properties`:
```properties
file.upload.type=cloud
file.upload.cloud.accessKey=你的accessKey
file.upload.cloud.secretKey=你的secretKey
file.upload.cloud.bucket=你的bucket
file.upload.cloud.endpoint=你的endpoint
```

然后在 `LocalFileUploadServiceImpl.uploadToCloud()` 方法中实现云存储逻辑。

## 项目结构
```
backend/
├── src/main/java/com/homeordering/
│   ├── common/          # 通用类
│   ├── config/          # 配置类
│   ├── controller/      # 控制器
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── mapper/          # MyBatis Mapper
│   ├── service/         # 服务接口
│   └── vo/              # 视图对象
├── src/main/resources/
│   ├── mapper/          # MyBatis XML
│   ├── db/              # 数据库脚本
│   └── application.properties
└── pom.xml
```
