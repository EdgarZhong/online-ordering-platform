## Final大作业项目概述与技术选型

### 1. 项目概述
本项目为“网上订餐系统”，根据课程要求，分为两大模块：
- **系统管理模块**：供管理员使用，负责管理菜品、订单、用户等。
- **客户端模块**：供普通用户使用，实现浏览菜品、下单、查看订单等功能。

### 2. 核心技术选型（混合模式）
为了在完全满足课程所有硬性要求的基础上，展示对现代Web开发架构的理解，本项目将采用“传统后端渲染”与“现代前后端分离”相结合的混合模式。

#### A. 系统管理模块（Admin Backend）
- **目标**：100%满足课程基础要求，保证基础分数。
- **技术栈**：
  - **架构**: 传统MVC模式
  - **后端**: `Servlet` 作为控制器 (Controller)。
  - **视图 (View)**: `JSP` + `JSTL` + `EL` + **自定义标签 (Custom Tags)**。
  - **数据持久化**: `JDBC` + `DAO` 模式。
  - **前端样式**: `Bootstrap`。
  - **会话管理**: `Session` + `Cookie`
  - **过滤器**: `Filter` (用于编码和权限控制)。

#### B. 客户端模块（User Frontend）
- **目标**：作为项目亮点，实现媲美原生App的流畅体验，展示对现代Web开发模式的掌握。
- **技术栈**：
  - **架构**: 现代前后端分离模式。
  - **后端 (API Provider)**:
    - `Servlet` 作为API接口，不再转发JSP，而是返回 `JSON` 数据。
    - 其他技术（JDBC, DAO, Session, Filter等）与管理模块后端共用。
  - **前端 (Single Page Application)**:
    - **核心框架**: `Vue.js` (负责数据绑定、组件化、路由和API请求)。
    - **UI框架**: `Bootstrap` (负责页面布局和美化)。
    - **运行环境**: 纯HTML/CSS/JavaScript，运行在浏览器端。

### 3. 方案优势
- **合规性**: 通过管理模块的传统实现，确保了对 `JSP`, `JSTL`, `自定义标签` 等所有硬性指标的完全覆盖。
- **先进性**: 通过客户端模块的前后端分离实践，展示了对 RESTful API 设计、单页应用（SPA）等现代主流技术的理解和应用能力。
- **学习价值**: 能够同时深入实践两种不同时代但都极为重要的Web开发模式，为未来职业发展打下坚实基础。

## 项目结构
### 项目目录结构

```
online-ordering-platform/
│
├── .gitignore               # Git忽略文件配置
├── README.md                # 项目总说明文档
│
├── backend/                 # 【后端】Java Maven项目根目录 (由成员A, B, C负责)
│   ├── pom.xml              # Maven项目配置文件
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/platform/ordering/  # Java源代码根包
│       │   │       ├── controller/         # Servlet控制器层
│       │   │       ├── dao/                # 数据访问对象层 (JDBC)
│       │   │       ├── filter/             # 过滤器 (编码, 权限)
│       │   │       ├── model/              # 实体/JavaBean模型
│       │   │       ├── service/            # 业务逻辑服务层
│       │   │       └── util/               # 工具类 (如DBUtil)
│       │   │
│       │   ├── resources/
│       │   │   └── db.properties           # 数据库连接配置 (应被gitignore忽略)
│       │   │
│       │   └── webapp/                     # Web应用资源根目录
│       │       ├── WEB-INF/
│       │       │   ├── web.xml             # 部署描述符
│       │       │   └── lib/                # (Maven会自动管理, 通常为空)
│       │       │
│       │       ├── admin/                  # 商户后台JSP页面目录
│       │       │   ├── login.jsp
│       │       │   ├── dashboard.jsp
│       │       │   └── menu-management.jsp
│       │       │
│       │       ├── assets/                 # 后台静态资源 (CSS, JS, Images)
│       │       │   ├── css/
│       │       │   └── images/
│       │       │
│       │       └── index.jsp               # 应用入口或公共页面
│       │
│       └── test/                           # 测试代码目录
│           └── java/
│
├── frontend/                # 【前端】Vue.js项目根目录 (由成员D, E负责)
│   ├── package.json         # npm项目配置文件
│   ├── vite.config.js       # (或 vue.config.js) Vite/Vue CLI配置文件
│   ├── public/              # 公共静态资源
│   │   └── index.html       # SPA单页应用入口HTML
│   └── src/
│       ├── assets/          # 组件静态资源 (CSS, images)
│       ├── components/      # 可复用Vue组件 (如NavBar.vue, DishCard.vue)
│       ├── router/          # Vue Router路由配置
│       ├── services/        # API服务层 (封装axios请求)
│       ├── views/           # 路由页面级组件 (如HomeView.vue, RestaurantView.vue)
│       ├── App.vue          # 根组件
│       └── main.js          # 应用入口JS
│
├── docs/                    # 项目文档目录
│   ├── db-diagram/          # ER图和数据库设计文档所在目录
│   ├── image/               # 图片文件所在目录
│   ├── Agile.md             # 敏捷开发简化版流程指导文件
│   ├── development.md       # 项目开发指导文件，包含概念构思、开发计划，分工表格
│   ├── product-backlog.md   # 项目的产品待办列表，指导功能规划、进度管理和任务分配
│   ├── log.md               # 项目的开发详细日志，**每人在每次commit到远程时务必按照示例撰写日志**
│   └── API.md               # 前后端API文档
│
└── database/                # 数据库脚本目录
    ├── schema.sql           # 数据库表结构创建脚本
    └── initial-data.sql     # 初始数据插入脚本
```

两个子项目统一在主仓库下管理，主分支为main

## Quick Start（后端 WAR 与前端 ZIP 的快速部署）

### 前提条件
- JDK `8+`（建议 `11`）、Tomcat `9.x`（`javax.servlet`）、PostgreSQL `13+`
- 后端包：`online-ordering.war`
- 前端包：`frontend-dist.zip`（Vite 构建产物 `dist/` 压缩包）

### 1. 数据库准备
- 创建数据库：`ordering_platform`
- 执行脚本：`database/schema.sql`、`database/initial-data.sql`
- 账户与端口：默认 `postgres@127.0.0.1:5432`（按实际环境调整）

### 2. 部署后端（Tomcat 手工部署）
- 将 `online-ordering.war` 拷贝到 `TOMCAT_HOME/webapps/`，建议重命名为 `online_ordering.war`，上下文路径为 `/online_ordering`
- 放置数据库配置 `db.properties` 到 `WEB-INF/classes`：
  - 路径：`TOMCAT_HOME/webapps/online_ordering/WEB-INF/classes/db.properties`
  - 内容：
    ```properties
    db.driver=org.postgresql.Driver
    db.url=jdbc:postgresql://127.0.0.1:5432/ordering_platform
    db.username=postgres
    db.password=postgres
    ```
  - 代码读取位置：`backend/src/main/java/com/platform/ordering/util/DBUtil.java:29-46`
- 校验 `WEB-INF/web.xml` 两个关键参数（开发/生产值参考 `docs/path-and-environment-variables.md`）：
  - `consumerDefaultRedirect`（登录后消费者默认回跳地址）
  - `corsAllowedOrigin`（允许跨域的前端来源）
- 启动 Tomcat：`TOMCAT_HOME/bin/startup.bat`（Windows）
- 验证后端：访问 `http://localhost:8080/online_ordering/login.jsp`

### 3. 部署前端（静态资源）
- 解压 `frontend-dist.zip` 到静态站点或反向代理根目录（例如 Nginx 的 `html/` 或子路径 `consumer/`）
- 两种部署模式：
  - 不同域（推荐）：
    - 前端：`https://app.example.com/`
    - 后端：`https://api.example.com/online_ordering`
    - 前端构建前 `.env` 示例：`docs/path-and-environment-variables.md` 的生产方案 A
    - 后端 `web.xml`：`consumerDefaultRedirect=https://app.example.com/`、`corsAllowedOrigin=https://app.example.com`
    - 跨域 Cookie：生产需 HTTPS，服务端已设置 `SameSite=None`，并在安全连接下添加 `Secure`（`backend/src/main/java/com/platform/ordering/controller/LoginServlet.java:39-44`）
  - 同域不同上下文：
    - 前端：`http://localhost:8080/consumer/`
    - 后端：`http://localhost:8080/online_ordering`
    - 前端 `.env` 可使用相对路径（生产方案 B）；Tomcat 需为前端配置 SPA 回退到 `index.html`

### 3A. 前端快速调试（开发模式，推荐用于验证）
- 适用：拿到前端源码 ZIP（包含 `package.json/src/` 等）
- 步骤：
  - 解压到 `frontend/`
  - 在 `frontend/.env` 写入开发配置（详情见 `docs/path-and-environment-variables.md`）：
    ```env
    VITE_BACKEND_BASE=http://localhost:8080/online_ordering
    VITE_API_BASE=http://localhost:8080/online_ordering/api
    VITE_BACKEND_CONTEXT=/online_ordering
    VITE_BACKEND_TARGET=http://localhost:8080
    VITE_DEV_PORT=5173
    ```
  - 安装依赖并启动调试：
    - `npm install`
    - `npm run dev`
  - 浏览器访问 `http://localhost:5173/`，路由守卫会在未登录时跳转到后端 `login.jsp`；登录成功后按 `redirect` 或默认前端地址回跳。
- 注意：
  - 跨域 Cookie 在开发环境 `http://localhost` 下无需 `Secure`；后端已按是否 HTTPS 决定是否添加 `Secure`。
  - 若拿到的是构建产物 ZIP（仅 `dist/`），请使用上面的“静态资源部署”方案而非开发模式。

### 4. 快速验证
- 登录与重定向：
  - 打开前端受保护页，未登录将跳转后端 `login.jsp`；登录成功后回跳到前端或后台仪表盘（`backend/src/main/java/com/platform/ordering/controller/LoginServlet.java:66-74`）
- 消费者端 API：
  - `GET /api/restaurants`、`GET /api/restaurants/:id` 正常返回（`backend/src/main/java/com/platform/ordering/api/RestaurantsApiServlet.java:19-52`、`RestaurantResourceApiServlet.java:21-86`）
  - `GET /api/menus/:menuId/items` 响应头含 `X-Menu-Version/X-Menu-Signature/ETag`（`backend/src/main/java/com/platform/ordering/api/MenusResourceApiServlet.java:64-83`）
  - `POST /api/orders` 返回 `201 Created` 与 `orderId/status/totalPrice`（`backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java:480-525`）
  - `GET /api/orders`（分页头 `X-Page/X-Size`）、`GET /api/orders/:orderId`（`backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java:30-110`）
  - `POST /api/orders/:orderId/cancel`（仅 `PENDING`，`OrdersResourceApiServlet.java:283-335`）
- 商户端：
  - 列表与详情：`/admin/orders`、`/admin/orders/:id`（`backend/src/main/java/com/platform/ordering/controller/OrdersAdminServlet.java:59-97, 99-169`）
  - 厨房面板与 SSE：`/admin/kitchen`、`/admin/kitchen/events`（`backend/src/main/java/com/platform/ordering/controller/KitchenBoardServlet.java:29-48`、`KitchenEventsServlet.java:13-48`）

### 5. 常见问题
- Tomcat 版本不匹配：请使用 Tomcat `9.x`（`javax.servlet`）；Tomcat `10` 为 `jakarta.servlet` 不兼容。
- WAR 上下文名变更后忘记同步：前端 `.env` 与后端 `web.xml` 必须一致（详见 `docs/path-and-environment-variables.md`）。
- 跨域 Cookie 未携带（消费者登录后无法重定向至前端，卡在登录页）：生产需 HTTPS，确保前端域与 `web.xml`中的参数`corsAllowedOrigin` 需要精确匹配，浏览器允许凭据携带。

## 文档快捷访问

| 文档名称 | 简述 | 文档链接 |
| :--- | :--- | :--- |
| management-interact.md | 商家后台菜品与菜单管理交互流程说明 | [docs/management-interact.md](docs/management-interact.md) |
| development.md | 项目开发指导（概念构思、开发计划、分工与演示） | [docs/development.md](docs/development.md) |
| API.md | 消费者端 RESTful API 文档 | [docs/API.md](docs/API.md) |
| path-and-environment-variables.md | 前后端路径与环境变量配置教程 | [docs/path-and-environment-variables.md](docs/path-and-environment-variables.md) |
| log.md | 开发日志 | [docs/log.md](docs/log.md) |
| CurrentStageProjectGuide.md | 当前阶段项目说明（克隆运行、依赖环境、测试账号） | [docs/CurrentStageProjectGuide.md](docs/CurrentStageProjectGuide.md) |
| 前端UIUX设计文档.md | 前端 UI/UX 设计规范与用户旅程逻辑 | [docs/前端UIUX设计文档.md](docs/%E5%89%8D%E7%AB%AFUIUX%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3.md) |
| product-backlog.md | 产品待办列表（用户故事与优先级） | [docs/product-backlog.md](docs/product-backlog.md) |
| Agile.md | 敏捷开发流程指导与实践 | [docs/Agile.md](docs/Agile.md) |

