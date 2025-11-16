# 当前阶段项目说明（可直接克隆运行）

## 项目概览
- 技术栈：后端 Java Servlet/JSP/JDBC（War，Tomcat9）；前端 Vue3 + Vite（Pinia/Router）；数据库 PostgreSQL。
- 多租户约束：所有后台 `/admin/*` 控制器与 DAO 操作严格绑定 `restaurant_id`，数据按餐厅隔离。
- 核心模块：
  - 商户后台：仪表盘、订单管理（列表/详情）、厨房面板（触发式刷新）。
  - 消费者前端：餐厅浏览、菜单查看、购物车、下单、订单详情与历史订单。

## 功能实现进度
- 后台订单管理（JSP MVC）：
  - 列表页 `/admin/orders`：状态/时间/关键词筛选，分页整数化，固定列宽与不换行；状态标签（黄/蓝/绿/红）；右侧“查看详情”按钮；不允许直接在列表修改状态。
  - 详情页 `/admin/orders/{id}`：Bootstrap 美化；明细按“菜单→菜品”二级分组；套餐菜单在标题追加【套餐】；列名“购买单价”（快照价）；顶部概览卡片（ID/顾客/时间/总价/状态）。
  - DAO：分页列表、总数统计、详情（含菜单/菜品）、状态更新；所有 SQL 变量以 `sql_` 前缀，严格过滤 `restaurant_id`。
- 厨房面板 `/admin/kitchen`：
  - 左右两栏（待处理/备餐中）按日期分组竖排显示；卡片含流水号/时间/总价/状态标签；底部“查看详情”；操作按钮颜色匹配目标状态。
  - 触发式刷新（SSE）：新订单创建后自动刷新（事件总线 + SSE 端点 + 前端 EventSource 自动重连）。
- 仪表盘 `/admin/dashboard`：展示今日订单数/收入/待处理/备餐中数据卡片；“今日新订单”卡片跳转到订单列表并带今日筛选；底部仅保留“菜单管理/菜品管理”。
- 消费者端：餐厅列表→详情→购物车→下单→订单详情与历史订单闭环已完成。

## 依赖与环境
- JDK：8+（建议 11）。
- Maven：3.6+
- Node.js：16+（建议 18+）
- PostgreSQL：13+（本地端口 `5432`）
- IDE 与容器：IntelliJ IDEA、Tomcat 9

## 克隆与目录结构
```bash
git clone https://github.com/EdgarZhong/online-ordering-platform.git
cd online-ordering-platform
```
- `backend/`：后端（Maven、Servlet/JSP、War）
- `frontend/`：前端（Vite + Vue3）
- `database/`：`schema.sql`（表结构）、`initial-data.sql`（初始化数据）
- `docs/`：文档（本说明、开发日志等）

## 数据库准备
1. 创建数据库：`ordering_platform`
2. 导入表结构：执行 `database/schema.sql`
3. 导入示例数据：执行 `database/initial-data.sql`
4. 配置连接：`backend/src/main/resources/db.properties`
   - `db.driver=org.postgresql.Driver`
   - `db.url=jdbc:postgresql://127.0.0.1:5432/ordering_platform`
   - `db.username=postgres`
   - `db.password=postgres`

## 后端部署（IDEA + Tomcat9）
1. IDEA 导入 `backend`（Maven 项目），等待依赖下载完成。
2. 添加 Tomcat9 本地运行配置，部署 `backend` 打包产物（War）。
3. 运行后端，默认上下文根示例：`http://localhost:8080/online-ordering-platform`（按你的 Tomcat 配置为准）。
4. 登录入口：`/login.jsp`；后台路由：`/admin/dashboard`、`/admin/orders`、`/admin/kitchen`。

## 前端运行（Vite）
1. 进入 `frontend` 目录：`cd frontend`
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 默认地址：`http://localhost:5173/`
5. 代理说明：`frontend/vite.config.js` 已配置到后端 API（开发环境代理到 Tomcat）。

## 访问入口与体验
### 商家后台
- 仪表盘：`/admin/dashboard`（需商户或超管登录）
- 订单管理：`/admin/orders`（筛选/分页/查看详情）
- 厨房面板：`/admin/kitchen`（按日期分组竖排；新订单触发自动刷新）

### 消费者前端
- 访问：`http://localhost:5173/`（若未登录，路由守卫会引导至登录）
- 流程：餐厅列表→餐厅详情→加入购物车→提交订单→订单详情与历史订单。

## 测试账号（来自 `initial-data.sql`）
- 商户：
  - 餐厅1：用户名 `testmerchant`，密码 `123123`
  - 餐厅2：用户名 `merchant2`，密码 `123123`
- 消费者：
  - 用户 `testcustomer`，密码 `123123`
  - 用户 `testcustomer2`，密码 `123123`

## 演示步骤（推荐）
1. 用商户账号登录后台，打开 `/admin/kitchen` 保持页面连接（顶部导航进入厨房面板）。
2. 在消费者前端登录（`testcustomer/123123`），选择餐厅1，加入“工作日午市套餐”，提交订单。
3. 返回商户厨房面板，页面将收到 `new_order` 事件并自动刷新，出现新订单卡片（按日期分组）。
4. 进入订单详情查看分组与单价快照（“购买单价”列）。

## 常见问题（FAQ）
- 后端端口与上下文：确保 Tomcat 启动成功且上下文根与你的代理匹配；`frontend` 代理指向后端 API。
- 数据库连接失败：检查 `db.properties` 与 PostgreSQL 是否启动、用户名密码是否匹配。
- SSE 自动刷新无效：确认厨房面板已打开并保持连接；浏览器网络面板应显示 `/admin/kitchen/events` 为“挂起”，且周期性收到 `ping`；提交订单后触发刷新。
- 列宽抖动或页码小数：已修复为固定列宽与整数页码；若样式异常，刷新或清空浏览器缓存。

---
如需进一步优化（快捷筛选标签、CSV 导出、详情折叠/打印视图等），可按照 `docs/development.md` 的迭代计划继续推进。