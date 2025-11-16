# Final大作业项目规划与技术选型

**请你们查看 [当前阶段项目说明](docs/CurrentStageProjectGuide.md)**

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
  - **会话管理**: `Session` + `Cookie`。
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

分工开发:
- 后端团队成员（A, B, C）在 backend/ 目录下进行他们的Maven项目开发。
- 前端团队成员（D, E）在 frontend/ 目录下进行他们的Vue.js项目开发。
两个子项目统一在主仓库下管理，主分支为main

**请前往[项目开发文档](./docs/development.md)仔细阅读并提出你的想法**