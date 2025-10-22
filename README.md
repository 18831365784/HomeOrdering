# 家庭点餐微信小程序 💕

一个温馨的情侣点餐微信小程序，用于家庭点餐使用。

## 项目简介

这是一款专为情侣/家庭设计的点餐小程序，支持菜品管理、订单创建、状态流转等功能。特色的订单状态设计（等待老公确认 → 老公大人已许可 → 已完成）让点餐变得更加有趣和温馨！❤️

## 功能特性

### 核心功能
- ✅ **菜品管理**: 支持添加、编辑、删除菜品，包含图片、名称、简介、价格
- ✅ **点单次数统计**: 每个菜品显示已点次数，了解最受欢迎的菜
- ✅ **购物车**: 方便的购物车功能，支持数量调整
- ✅ **订单管理**: 创建订单、查看历史订单、订单状态更新
- ✅ **订单状态流转**: 
  - 等待老公确认 ⏰
  - 老公大人已许可 💕
  - 已完成 ✅
- ✅ **图片上传**: 支持本地存储，可扩展云存储
- ✅ **搜索功能**: 快速查找菜品

## 技术栈

### 后端
- **语言**: Java 17
- **框架**: Spring Boot 3.1.5
- **持久层**: MyBatis 3.0.3
- **数据库**: MySQL 8.0.25
- **构建工具**: Maven

### 前端
- **框架**: uni-app (Vue 3)
- **平台**: 微信小程序

## 项目结构

```
HomeOrdering/
├── backend/                 # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/homeordering/
│   │   │   │   ├── common/       # 通用类
│   │   │   │   ├── config/       # 配置类
│   │   │   │   ├── controller/   # 控制器
│   │   │   │   ├── dto/          # 数据传输对象
│   │   │   │   ├── entity/       # 实体类
│   │   │   │   ├── mapper/       # MyBatis Mapper
│   │   │   │   ├── service/      # 服务接口
│   │   │   │   └── vo/           # 视图对象
│   │   │   └── resources/
│   │   │       ├── mapper/       # MyBatis XML
│   │   │       ├── db/           # 数据库脚本
│   │   │       └── application.properties
│   │   └── pom.xml
│   └── README.md
│
└── frontend/               # 前端小程序
    ├── pages/             # 页面目录
    │   ├── index/        # 菜品列表
    │   ├── dish/         # 菜品详情和添加
    │   ├── cart/         # 购物车
    │   └── order/        # 订单管理
    ├── utils/            # 工具类
    │   ├── api.js       # API封装
    │   └── cart.js      # 购物车管理
    ├── App.vue
    ├── pages.json
    ├── manifest.json
    └── README.md
```

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0.25
- Node.js 14+
- 微信开发者工具

### 后端启动

1. **创建数据库**
```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

2. **修改配置**

编辑 `backend/src/main/resources/application.properties`:
```properties
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

3. **启动后端服务**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

服务启动后访问: `http://localhost:8080/api`

### 前端启动

1. **安装依赖**
```bash
cd frontend
npm install
```

2. **修改后端地址**

编辑 `frontend/utils/api.js`:
```javascript
const BASE_URL = 'http://localhost:8080/api'  // 修改为你的后端地址
```

3. **运行项目**
```bash
npm run dev:mp-weixin
```

4. **导入微信开发者工具**
- 打开微信开发者工具
- 导入项目，选择 `frontend/dist/dev/mp-weixin` 目录
- 关闭域名校验（开发阶段）
- 开始调试

## API接口文档

### 菜品接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/dish | 新增菜品 |
| GET | /api/dish/{id} | 查询菜品详情 |
| GET | /api/dish/list | 查询菜品列表 |
| PUT | /api/dish | 更新菜品 |
| DELETE | /api/dish/{id} | 删除菜品 |

### 订单接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/order | 创建订单 |
| GET | /api/order/{id} | 查询订单详情 |
| GET | /api/order/list | 查询订单列表 |
| PUT | /api/order/{id}/status | 更新订单状态 |
| DELETE | /api/order/{id} | 删除订单 |

### 文件接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/file/upload | 上传文件 |
| DELETE | /api/file/delete | 删除文件 |

## 数据库表结构

### 菜品表 (dish)
- id: 主键
- name: 菜品名称
- image_url: 菜品图片
- description: 菜品简介
- price: 菜品价格
- order_count: 点单次数
- status: 状态 (0-停用 1-启用)
- create_time: 创建时间
- update_time: 更新时间

### 订单表 (order)
- id: 主键
- order_no: 订单号
- total_amount: 订单总金额
- status: 订单状态 (0-等待老公确认 1-老公大人已许可 2-已完成)
- remark: 备注
- create_time: 创建时间
- update_time: 更新时间

### 订单详情表 (order_detail)
- id: 主键
- order_id: 订单ID
- dish_id: 菜品ID
- dish_name: 菜品名称
- dish_image: 菜品图片
- dish_price: 菜品价格
- quantity: 数量
- subtotal: 小计金额

## 扩展功能

### 切换到云存储

修改 `backend/src/main/resources/application.properties`:
```properties
file.upload.type=cloud
file.upload.cloud.accessKey=你的accessKey
file.upload.cloud.secretKey=你的secretKey
file.upload.cloud.bucket=你的bucket
file.upload.cloud.endpoint=你的endpoint
```

然后在 `LocalFileUploadServiceImpl.uploadToCloud()` 方法中实现云存储逻辑。

## 待扩展功能
- 🔐 微信登录授权
- 👥 多用户/家庭支持
- 📂 菜品分类管理
- ⭐ 收藏功能
- 💬 评价功能
- 📊 数据统计

## 注意事项
1. 开发阶段需要关闭微信开发者工具的域名校验
2. 图片存储默认使用本地存储，生产环境建议切换到云存储
3. 数据库密码请勿提交到版本控制
4. 真机调试需要配置合法域名

## 截图预览
（待添加小程序截图）

## 许可证
MIT License

## 作者
开发于 2025年10月 💕

---
祝您使用愉快！如有问题欢迎提issue~
