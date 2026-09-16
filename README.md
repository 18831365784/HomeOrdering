# 家庭点餐

微信小程序：家庭成员点餐、指定制作人、用余额结算。每个家庭只有创建者一名管理员。

详细产品规则与开发约束见 [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md)。改功能时必须同步维护本文件与那份规则文档，不要再新增介绍/需求类 Markdown。

## 技术栈

- 后端：Java 17、Spring Boot 3.1.5、MyBatis-Plus、MySQL 8
- 前端：uni-app（Vue 3 + Vite），源码只在 `frontend/src/`

## 目录

```
HomeOrdering/
├── README.md
├── docs/REQUIREMENTS.md
├── backend/src/main/resources/
│   ├── application.properties    # 后端唯一配置源
│   └── db/init.sql | upgrade.sql
└── frontend/
    ├── .env.development          # 前端开发唯一配置源
    ├── .env.production           # 前端生产/真机唯一配置源
    └── src/                      # 小程序源码
```

## 换电脑 / 换服务器只改这里

| 端 | 文件 | 必改项 |
|---|---|---|
| 后端 | `backend/src/main/resources/application.properties` | `server.url`、数据源、`file.upload.path` |
| 前端开发 | `frontend/.env.development` | `VITE_API_BASE_URL` |
| 前端真机/发布 | `frontend/.env.production` | `VITE_API_BASE_URL` |
| 微信密钥 | 启动前设环境变量 `WX_APPID`、`WX_SECRET`（说明见 `backend/.env.example`；本机也可直接改 properties，但不要提交真实密钥） | 不要写进业务代码 |

`application.properties` 中形如 `${NAME:默认值}`：有环境变量用环境变量，没有就用冒号后的默认值。日常本机开发直接改默认值即可。

`server.url` 用于拼接上传后的图片绝对地址，须与小程序实际能访问到的后端一致（本机调试用 `http://localhost:8080`，真机用内网穿透或公网域名，不要带 `/api`）。

前端 `VITE_API_BASE_URL` 须带 `/api`，例如 `http://localhost:8080/api`。改完 `.env*` 后要重新执行 `npm run dev:mp-weixin`。

## 数据库

- **新空库**：只执行 `backend/src/main/resources/db/init.sql`
- **已有数据的旧库**：只执行 `backend/src/main/resources/db/upgrade.sql`（幂等补表补列，不删业务数据）

不要再使用仓库里曾经存在的其它 SQL 文件。

```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

## 启动

环境：JDK 17+、Maven 3.6+、MySQL 8、Node.js 16+、微信开发者工具。

1. 配置 `application.properties`；微信登录需提供 `WX_APPID` / `WX_SECRET`（环境变量或写入 properties 本地不提交）。
2. `cd backend && mvn spring-boot:run`  
   探活：`curl http://localhost:8080/api/health` 应返回成功。
3. 配置 `frontend/.env.development`。
4. `cd frontend && npm install && npm run dev:mp-weixin`
5. 微信开发者工具导入 `frontend/dist/dev/mp-weixin`。开发阶段可关闭域名校验。真机调试需配置合法 request / uploadFile / downloadFile 域名。

## 使用流程

微信授权登录 → 创建家庭或填邀请码加入 → 管理员维护分类和菜品 → 成员加购并选择制作人下单（扣余额）→ 制作人接单、完成；下单人可在待接单时取消（退余额）。
