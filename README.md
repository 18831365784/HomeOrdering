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
│   ├── application.yml           # 后端公共配置
│   ├── application-dev.yml       # 本机开发
│   ├── application-prod.yml      # 服务器生产
│   └── db/init.sql | upgrade.sql
└── frontend/
    ├── .env.development          # 前端开发唯一配置源
    ├── .env.production           # 前端生产/真机唯一配置源
    └── src/                      # 小程序源码
```

## 换电脑 / 换服务器只改这里

| 端 | 文件 | 必改项 |
|---|---|---|
| 后端公共 | `backend/src/main/resources/application.yml` | 端口、context-path、微信占位 |
| 后端开发 | `backend/src/main/resources/application-dev.yml` | `server.url`、数据源、`file.upload.path` |
| 后端生产 | `backend/src/main/resources/application-prod.yml` | `server.url`、数据源、`file.upload.path` |
| 前端开发 | `frontend/.env.development` | `VITE_API_BASE_URL` |
| 前端真机/发布 | `frontend/.env.production` | `VITE_API_BASE_URL` |
| 微信密钥 | 启动前设环境变量 `WX_APPID`、`WX_SECRET`（说明见 `backend/.env.example`；本机也可直接改 yml 默认值，但不要提交真实密钥） | 不要写进业务代码 |

yml 中形如 `${NAME:默认值}`：有环境变量用环境变量，没有就用冒号后的默认值。日常本机开发直接改对应 profile 的默认值即可。

`server.url` 用于拼接上传后的图片绝对地址，须与小程序实际能访问到的后端一致（不要带 `/api`）：

- 本机调试：`http://localhost:8080`
- 域名未备案前：`http://82.157.3.231:8080`（腾讯云未备案不能走 80/443，小程序开发者工具需关闭域名校验）
- 备案通过并配好 HTTPS 后：`https://selfcode.top`

数据库里本站图片只存 `/uploads/...`，换服务器只改 `server.url`（并搬迁上传目录文件）即可，不必改库。

`file.upload.path` 控制磁盘目录：默认 `./uploads/`（相对 `java -jar` 或 `mvn spring-boot:run` 启动时所在目录）。换目录时改对应 yml 或环境变量 `FILE_UPLOAD_PATH` 为绝对路径，把旧文件拷过去后重启。

前端 `VITE_API_BASE_URL` 须带 `/api`。改完 `.env*` 后要重新执行 `npm run dev:mp-weixin`。

## 数据库

- **新空库**：只执行 `backend/src/main/resources/db/init.sql`
- **已有数据的旧库**：只执行 `backend/src/main/resources/db/upgrade.sql`（幂等补表补列，不删业务数据）

不要再使用仓库里曾经存在的其它 SQL 文件。

```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

## 启动

环境：JDK 17+、Maven 3.6+、MySQL 8、Node.js 16+、微信开发者工具。默认 profile 为 `dev`。

1. 本机改 `application-dev.yml`；服务器改 `application-prod.yml`。微信登录需提供 `WX_APPID` / `WX_SECRET`（环境变量或写入 yml 本地不提交）。
2. 本机：`cd backend && mvn spring-boot:run`  
   服务器：`java -jar home-ordering-backend-1.0.0.jar --spring.profiles.active=prod`  
   探活：本机 `curl http://localhost:8080/api/health`；服务器 `curl http://82.157.3.231:8080/api/health`，应返回成功。
3. 本机前端用 `frontend/.env.development`；真机/发布用 `frontend/.env.production`。
4. `cd frontend && npm install && npm run dev:mp-weixin`
5. 微信开发者工具导入 `frontend/dist/dev/mp-weixin`。备案完成前须关闭 request / uploadFile / downloadFile 域名校验。备案后把合法域名配成 `https://selfcode.top`。

## 使用流程

微信授权登录 → 创建家庭或填邀请码加入 → 管理员维护分类和菜品 → 成员加购并选择制作人下单（扣余额）→ 制作人接单或拒绝、完成；下单人可在待接单时取消（退余额）。制作人拒绝后状态为「制作人已拒绝」并退余额。
