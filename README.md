# 钓友圈 · Fishing Forum

> 面向钓鱼爱好者的全栈社区平台：把每一次出钓经验沉淀成可搜索、可讨论、可复用的社区内容。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](#许可)

## 目录

- [项目简介](#项目简介)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [开发环境运行](#开发环境运行)
- [API 概览](#api-概览)
- [设计系统](#设计系统)
- [测试](#测试)
- [部署](#部署)
- [贡献指南](#贡献指南)
- [许可](#许可)

## 项目简介

钓友圈是一个钓鱼主题的垂直社区，集论坛交流、渔获分享、装备测评、钓点地图、钓鱼百科、实时天气于一体。项目采用前后端分离架构，后端 Spring Boot + PostgreSQL，前端 Vue 3 + Element Plus，整体支持 PC 与移动端响应式自适应，并提供「自动 / PC / 手机」三种视图切换。

**适用场景**：

- 钓友记录每次出钓的渔获、饵料、天气与钓点
- 通过装备测评帮助新手挑选鱼竿、鱼线、浮漂等
- 通过百科共建钓鱼知识库（鱼种 / 饵料 / 装备 / 技巧）
- 基于经纬度的钓点收录与点评
- 关注、私信、通知等社交互动

## 核心功能

### 内容
- **论坛**：板块 + 标签 + 搜索 + 多维排序（最新 / 最热 / 点赞）
- **渔获日记**：结构化记录鱼种、重量、饵料、天气、垂钓日期
- **装备测评**：品牌、型号、价格、评分、优缺点
- **钓鱼百科**：Markdown 编辑、图片插入、版本历史、词条讨论
- **钓点地图**：经纬度收录、类型筛选、用户评价、谷歌/高德跳转
- **天气查询**：实时天气、空气质量、未来 7 天预报、钓鱼指数

### 社交
- 注册 / 登录 / JWT 鉴权
- 关注 / 粉丝 / 关注动态信息流
- 点赞 / 评论 / 收藏
- 私信（WebSocket 实时推送）
- 通知中心（点赞、评论、关注、私信、@提及）
- 个人主页 + 成长等级 + 徽章

### 管理
- 内容审核：举报处理、帖子置顶 / 加精 / 删除
- 用户管理：封禁 / 禁言 / 角色调整
- 系统公告
- 数据看板

### 体验
- 双轨响应式：自动检测 + 手动切换
- 移动端浮岛 tabbar + 首页快捷宫格
- 完整的 `el-form` 字段级校验
- 统一设计 token 与 typography 工具类
- 全局键盘焦点环（`:focus-visible`）+ 可点击元素键盘可达
- DOMPurify 富文本 XSS 防护
- 草稿自动保存（localStorage）

## 技术栈

### 后端

| 类别 | 技术 |
|---|---|
| 框架 | Spring Boot 3.2.5 |
| 语言 | Java 17 |
| 安全 | Spring Security 6 + JWT (jjwt 0.12) |
| 实时 | Spring WebSocket / STOMP |
| 数据库 | PostgreSQL 16 |
| ORM | MyBatis-Plus 3.5 |
| 校验 | Jakarta Validation |
| 工具 | Lombok、Commons IO |
| 测试 | Spring Boot Test、JUnit 5、Mockito |

### 前端

| 类别 | 技术 |
|---|---|
| 框架 | Vue 3.4（Composition API + `<script setup>`） |
| 构建 | Vite 5 |
| 路由 | Vue Router 4 |
| 状态 | Pinia 2 |
| UI | Element Plus 2.7 |
| 富文本 | Vue Quill |
| Markdown | marked + DOMPurify |
| HTTP | axios |

### 基础设施
- Docker + Docker Compose
- Nginx（前端反向代理）
- 数据库迁移：`schema.sql` + `migrations/`

## 项目结构

```
fishing-forum/
├── backend/                            # Spring Boot 后端
│   ├── src/main/java/com/fishforum/
│   │   ├── config/                     # CORS / Security / WebSocket / 限流 / 全局异常 / DB 迁移
│   │   ├── controller/                 # REST 控制器（10 个）
│   │   ├── service/                    # 业务服务
│   │   ├── mapper/                     # MyBatis-Plus Mapper
│   │   ├── entity/                     # 数据库实体
│   │   ├── dto/                        # 请求 DTO（带校验注解）
│   │   ├── vo/                         # 响应视图对象
│   │   └── common/                     # PageUtils / Result 等公共工具
│   ├── src/main/resources/
│   │   ├── application.yml             # 开发配置
│   │   ├── application-prod.yml        # 生产配置（变量占位）
│   │   ├── schema.sql                  # 表结构
│   │   ├── data.sql                    # 初始数据
│   │   └── migrations/                 # 增量迁移
│   ├── src/test/                       # 单测 / 集成测试
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                           # Vue 3 前端
│   ├── src/
│   │   ├── api/                        # axios 封装
│   │   ├── components/layout/          # AppHeader 等布局组件
│   │   ├── views/                      # 页面（首页 / 论坛 / 帖子详情 / 钓点 / 百科 / 天气 / 个人 / 消息 / 通知 / 登录 / 注册 / admin）
│   │   ├── router/                     # 路由
│   │   ├── stores/                     # Pinia store（user, viewMode）
│   │   ├── styles/global.css           # 设计 token + 工具类 + 响应式
│   │   └── utils/                      # postType 等工具
│   ├── tests/                          # PC / 移动端 UI 探针测试
│   ├── Dockerfile
│   ├── nginx.conf
│   └── vite.config.js
├── docker-compose.yml                  # 一键编排（db + backend + frontend）
├── docker-init-data.sql                # 初始化数据
├── start.sh / stop.sh                  # 本地启停脚本
└── init-db.sh                          # 数据库初始化脚本
```

## 快速开始

### 方式 A：Docker Compose（推荐）

只需 Docker 与 Docker Compose 即可一键启动整套服务。

```bash
git clone git@github.com:wu336269328/fishing-forum.git
cd fishing-forum/fishing-forum

# 可选：自定义 JWT 密钥
export JWT_SECRET="your-secret-here"

docker compose up -d --build
```

启动后访问：

| 服务 | 地址 |
|---|---|
| 前端 | http://localhost:3000 |
| 后端 | http://localhost:8080 |
| PostgreSQL | localhost:5432（postgres / postgres） |

默认管理员账号：`admin / admin123`

### 方式 B：本地脚本

需要本地已安装 JDK 17、Maven、Node 18+、PostgreSQL 16。

```bash
cd fishing-forum
bash start.sh    # 一键启动（数据库 → 后端 → 前端）
bash stop.sh     # 停止全部服务
```

## 开发环境运行

### 后端

```bash
cd fishing-forum/backend
mvn spring-boot:run
# 默认监听 :8080
```

### 前端

```bash
cd fishing-forum/frontend
npm install
npm run dev
# 默认监听 :5173 或 :3000
```

### 数据库

首次运行需要执行 `schema.sql` 与 `data.sql`，`start.sh` 会自动处理。

```bash
psql -U postgres -d fishing_forum -f backend/src/main/resources/schema.sql
psql -U postgres -d fishing_forum -f backend/src/main/resources/data.sql
```

## API 概览

REST 风格，统一 `code / message / data` 响应格式。

| 模块 | 路径前缀 | 说明 |
|---|---|---|
| 鉴权 | `/api/auth` | 登录、注册 |
| 用户 | `/api/users` | 个人资料、关注、粉丝、成长 |
| 帖子 | `/api/posts` | 帖子 CRUD、热门、详情 |
| 互动 | `/api/likes`、`/api/favorites`、`/api/comments` | 点赞、收藏、评论 |
| 社交 | `/api/follows`、`/api/messages`、`/api/notifications` | 关注、私信、通知 |
| 板块/标签 | `/api/sections`、`/api/tags` | 板块与标签 |
| 钓点 | `/api/spots` | 钓点 CRUD、点评 |
| 百科 | `/api/wiki` | 词条 CRUD、版本历史、讨论 |
| 天气 | `/api/weather` | 实时天气 + 钓鱼指数 |
| 上传 | `/api/upload/image` | 图片上传 |
| 公告 | `/api/announcements` | 系统公告 |
| 管理 | `/api/admin/**` | 用户管理、内容审核、举报处理、统计 |

WebSocket：`/ws`，主要用于私信与通知实时推送。

## 设计系统

前端基于一组完整的 CSS 设计 token，集中在 `frontend/src/styles/global.css`：

```css
:root {
  --color-primary: #4f7fbf;       /* 湖水蓝主色 */
  --color-primary-dark: #365f93;
  --color-success / --color-warning / --color-danger
  --muted: #5b6472;               /* WCAG AA 文本对比度 */
  --rank-1/2/3                    /* 热门排行配色 */
  --tabbar-offset                 /* 移动端 sticky 元素统一偏移 */
  --focus-ring                    /* 全局键盘焦点环 */
  --radius-card / --radius-md / --radius-lg
  --shadow-soft / --shadow-card / --shadow-hover
}
```

工具类：`.t-h1` / `.t-h2` / `.t-h3` / `.t-h4` / `.t-body` / `.t-caption` / `.t-meta` / `.t-eyebrow`

通用组件类：`.ui-chip`（含 `.ui-chip--solid/section/success/warning/danger/neutral/outline` 修饰符）、`.empty-state`、`.hero-panel`、`.list-card`、`.page-toolbar`

## 测试

### 后端单测

```bash
cd fishing-forum/backend
mvn test
```

### 前端 UI 探针

```bash
cd fishing-forum/frontend
npm run test:pc-ui          # PC 视图布局检查
npm run test:mobile-ui      # 移动端布局检查
npm run test:post-type      # 帖子类型工具
npm run test:request        # 请求拦截器策略
```

## 部署

### Docker 生产部署

```bash
cd fishing-forum
JWT_SECRET=$(openssl rand -hex 32) docker compose up -d --build
```

### 自定义环境变量

后端通过 `application-prod.yml` 读取以下变量：

| 变量 | 说明 | 默认 |
|---|---|---|
| `DB_PASSWORD` | PostgreSQL 密码 | 必填 |
| `JWT_SECRET` | JWT 签名密钥 | 必填 |
| `JWT_EXPIRATION` | JWT 有效期（毫秒） | 86400000 |
| `CORS_ORIGINS` | 允许的前端域名（逗号分隔） | `http://localhost:3000,http://localhost:5173` |
| `TRUSTED_PROXIES` | 反向代理白名单 | 空 |
| `UPLOAD_PATH` | 文件上传目录 | `./uploads/` |

### Nginx 反向代理示例

前端容器内置 `nginx.conf`，将 `/api/**` 反代到后端 `:8080`，静态资源由 Nginx 直出。

## 贡献指南

欢迎 issue 与 PR。提交规范遵循 [Conventional Commits](https://www.conventionalcommits.org/)：

```
feat:     新功能
fix:      Bug 修复
refactor: 重构（不影响功能）
style:    样式 / 格式
chore:    依赖或构建相关
docs:     文档
test:     测试
```

提交前请确保：

1. 后端 `mvn test` 通过
2. 前端 `npm run test:pc-ui && npm run test:mobile-ui` 通过
3. 本地构建 `mvn package` 与 `npm run build` 无错误

## 许可

MIT License — 详见 [LICENSE](LICENSE)（如未提供则视为保留所有权利）。

---

如果这个项目对你有帮助，欢迎 ⭐ Star。
