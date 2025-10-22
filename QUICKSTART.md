# 🚀 快速启动指南

## 第一次使用本项目？按照以下步骤操作

### 步骤1: 准备环境 ✅

确保你已经安装了以下工具：
- [ ] JDK 17 或更高版本
- [ ] Maven 3.6+
- [ ] MySQL 8.0.25
- [ ] Node.js 14+
- [ ] 微信开发者工具

### 步骤2: 配置数据库 🗄️

1. 启动 MySQL 服务

2. 创建数据库（两种方式任选其一）：

   **方式A: 使用命令行**
   ```bash
   mysql -u root -p < backend/src/main/resources/db/init.sql
   ```

   **方式B: 使用数据库工具**
   - 打开 Navicat/DataGrip 等工具
   - 连接到 MySQL
   - 执行 `backend/src/main/resources/db/init.sql` 脚本

3. 确认数据库创建成功
   - 数据库名: `home_ordering`
   - 应包含3个表: `dish`, `order`, `order_detail`
   - 已插入6条示例菜品数据

### 步骤3: 启动后端服务 ⚙️

1. 修改数据库配置

   编辑文件: `backend/src/main/resources/application.properties`
   
   ```properties
   # 修改这两行
   spring.datasource.username=你的MySQL用户名
   spring.datasource.password=你的MySQL密码
   ```

2. 启动后端

   **方式A: 使用命令行**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

   **方式B: 使用IDE**
   - 用 IDEA 打开 backend 目录
   - 找到 `HomeOrderingApplication.java`
   - 右键运行 main 方法

3. 验证启动成功
   - 看到控制台输出 "家庭点餐系统启动成功！"
   - 浏览器访问: http://localhost:8080/api/dish/list
   - 应该能看到菜品列表数据

### 步骤4: 启动前端小程序 📱

1. 安装依赖
   ```bash
   cd frontend
   npm install
   ```

2. 修改配置（如果后端地址不是本地）

   编辑文件: `frontend/utils/api.js`
   
   ```javascript
   const BASE_URL = 'http://localhost:8080/api'  // 改成你的后端地址
   ```

3. 运行开发模式
   ```bash
   npm run dev:mp-weixin
   ```

4. 导入微信开发者工具
   - 打开微信开发者工具
   - 点击 "导入项目"
   - 选择目录: `frontend/dist/dev/mp-weixin`
   - AppID: 选择 "测试号" 或填写你的 AppID
   - **重要**: 点击"详情" → "本地设置" → 勾选"不校验合法域名..."

5. 查看效果
   - 在模拟器中应该能看到小程序界面
   - 点击各个 tab 测试功能

### 步骤5: 准备图标（可选）📦

底部导航栏需要图标文件，两种方式：

**方式A: 临时禁用tabBar（快速测试）**

编辑 `frontend/pages.json`，注释掉 tabBar 配置：
```json
// "tabBar": { ... }  // 整个tabBar都注释掉
```

**方式B: 添加图标文件**

在 `frontend/static/icons/` 目录下放置以下文件：
- home.png, home-active.png
- cart.png, cart-active.png
- order.png, order-active.png
- add.png, add-active.png

推荐图标网站: https://www.iconfont.cn/

### 常见问题 ❓

**Q1: 后端启动失败，提示数据库连接错误**
- 检查 MySQL 服务是否启动
- 检查用户名密码是否正确
- 检查数据库 `home_ordering` 是否存在

**Q2: 前端无法获取数据**
- 检查后端是否启动成功
- 检查 `utils/api.js` 中的 BASE_URL 是否正确
- 检查微信开发者工具是否关闭域名校验

**Q3: 图片上传失败**
- 检查后端 `application.properties` 中的 `file.upload.path` 目录是否存在
- Windows系统路径示例: `E:/uploads/`
- 确保该目录有写入权限

**Q4: 编译报错找不到某个类**
- 运行 `mvn clean install` 重新构建
- 检查 JDK 版本是否为 17+

### 测试功能清单 ✨

启动成功后，建议按以下顺序测试：

1. [ ] 查看菜品列表（应该有6个示例菜品）
2. [ ] 点击菜品查看详情
3. [ ] 添加菜品到购物车
4. [ ] 进入购物车，调整数量
5. [ ] 提交订单（状态应为"等待老公确认"）
6. [ ] 在订单列表中点击"老公确认"
7. [ ] 点击"完成订单"
8. [ ] 尝试添加新菜品（需要上传图片）

### 开发建议 💡

1. **热更新**: 后端和前端都支持热更新
   - 后端: IDEA 开启自动编译即可
   - 前端: npm run dev 模式自动监听文件变化

2. **查看日志**: 
   - 后端日志在控制台
   - 前端日志在微信开发者工具的控制台

3. **调试接口**: 
   - 使用 Postman 或 Apifox 测试后端接口
   - 推荐先用工具测试接口，再调试前端

### 下一步 🎯

- 📖 阅读 README.md 了解更多功能
- 🎨 自定义主题色（修改 `app.css` 中的颜色）
- 🖼️ 替换默认图标
- 📝 根据需求添加新功能

---

🎉 祝您使用愉快！如有问题，请查看各目录下的 README.md
