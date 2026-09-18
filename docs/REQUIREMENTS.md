# 家庭点餐 — 需求与开发规则

本文件是唯一需求与规则文档。功能、权限、配置、数据库约定以本文为准。变更行为时必须同步更新本文与根目录 [README.md](../README.md)。禁止再新增介绍、清单、更新日志类 Markdown。

## 产品

面向家庭内部点餐，不是对外餐厅系统，也不是早期「情侣点餐 / 等待老公确认」版本。

### 账号

- 必须微信登录（`code` 换 openid）。禁止游客、禁止前端伪造 `guest_*` 用户。
- 用户以 `uuid` 标识；登录后服务端下发 `token = uuid + "_" + 时间戳`。请求须带 `Authorization` 头（拦截器校验）。
- 退出登录：调用 `/auth/logout` 后清除本地用户与 token；服务端无会话存储，logout 仅表示客户端应丢弃凭证。401 时前端自动清凭证并回登录页。

### 家庭

- 用户必须加入一个家庭后才能看到菜品和下单。未加入则进入家庭页：创建或邀请码加入。
- 一人同时只属于一个家庭。
- 邀请码 6 位，管理员可刷新。
- 管理员可修改家庭名称。

### 管理员

- **每个家庭仅创建者一人管理员。**
- **唯一依据：`family.admin_uuid`。** 接口权限全部按该字段判断。是否管理员请调 `/family/is-admin`。
- `user.role` 只是缓存：创建家庭时写 `1`，加入者保持 `0`。禁止用 `role==1` 作权限依据。
- 管理员可以：分类/菜品增删改上下架排序、刷新邀请码、设置本家庭任意成员余额（含自己）。
- 普通成员：浏览上架菜品、购物车、下单、查看自己点的单和自己做的单、接单/完成（若被选为制作人）、待接单时取消自己的单。

### 菜品与分类

- 按 `family_id` 隔离。分类名称在同一家庭内唯一。
- 菜品可有 JSON `extensions` 扩展选项（加价）。
- 点单次数随下单累加。
- 菜品新增/更新/删除均须管理员。

### 订单与余额

状态：`-1` 已取消，`0` 待接单，`1` 制作中，`2` 已完成。

- 下单必须选择同家庭制作人；金额按菜品价与扩展加价计算；校验余额后扣款。
- 下单人、家庭 ID 一律取登录态，客户端不可伪造。
- 状态流转只用：`accept`（待接单→制作中）、`finish`（制作中→已完成）、`cancel`（待接单由下单人取消并退余额）。不提供任意改状态或物理删除订单接口。
- 订单列表按当前用户家庭过滤，禁止跨家庭。
- 制作人必须是同家庭成员；下单菜品必须属于该家庭。

### 用户资料

- 更新资料统一走 `PUT /user/profile`（只能改自己）。
- 查他人信息仅限同家庭成员。

### 文件

- 图片本地存储。库内本站上传只存相对路径 `/uploads/...`（不含主机）；接口返回时用当前 `server.url` + `context-path` 拼绝对地址。微信头像等外链可存完整 URL。
- 上传目录由 `file.upload.path` 配置（`application-dev.yml` / `application-prod.yml` 或环境变量 `FILE_UPLOAD_PATH`）。相对路径相对进程启动目录；生产可改为绝对路径。
- 不做云存储实现。
- 旧库若已存完整 ngrok/域名地址：执行 `upgrade.sql` 会剥成 `/uploads/...`。即使未迁移，接口读出时也会按当前 `server.url` 重写本站上传地址。

## 配置唯一源

| 用途 | 唯一位置 |
|---|---|
| 后端端口、context-path、微信占位 | `backend/src/main/resources/application.yml` |
| 本机 `server.url`、数据源、上传路径 | `backend/src/main/resources/application-dev.yml` |
| 生产 `server.url`、数据源、上传路径 | `backend/src/main/resources/application-prod.yml` |
| 微信密钥等敏感项 | 环境变量，示例说明见 `backend/.env.example`（该文件不会被程序自动加载），禁止提交真实密钥 |
| 前端 API 根路径 | `frontend/.env.development` 与 `frontend/.env.production` 的 `VITE_API_BASE_URL` |

默认 profile 为 `dev`。服务器用 `--spring.profiles.active=prod` 或环境变量 `SPRING_PROFILES_ACTIVE=prod`。

域名 `selfcode.top` 未备案前：生产 `server.url` 与前端生产 API 用 `http://82.157.3.231:8080`（不要走 80/443）。备案并启用 HTTPS 后改为 `https://selfcode.top`。

强制：

- 业务 Java / Vue / JS **不得**硬编码主机、端口、ngrok 域名。
- 前端只能读取 `import.meta.env.VITE_API_BASE_URL`。
- 换机器只改上表文件后重启后端 / 重新编译小程序。
- yml 里 `${NAME:默认值}`：有环境变量用环境变量，没有则用默认值。本机可直接改对应 profile 的默认值，不必设环境变量。

## 数据库唯一源

- 空库：`backend/src/main/resources/db/init.sql`（禁止 `DROP`，禁止无家庭的全局示例菜）。
- 旧库保数据：`backend/src/main/resources/db/upgrade.sql`（幂等）。
- 禁止第三份 schema / dump。`family.id` 与其它表一致使用自增。
- 表：`user`、`family`、`category`、`dish`、`order`、`order_detail`。

## 前端源码

- 唯一源码目录：`frontend/src/`。
- 菜品新增只走「菜品管理」页，不单独保留添加页。

## 后端分层

- Controller 只做参数与 `Result` 封装，业务在 Service。
- 管理员校验统一走家庭 `admin_uuid`（`FamilyAccessService`）。
- 当前用户以登录 token 解析结果为准（`AuthContext`），不信任客户端随意指定的身份 uuid（改他人余额时，操作者来自 token，目标用户来自请求体）。

## 文档维护

改了用户可见行为、权限、表结构、配置项或启动方式：同一提交内更新 README 与本文对应段落。
