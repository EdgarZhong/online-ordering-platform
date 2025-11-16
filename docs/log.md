# 开发日志
## 2025-11-16 - by 钟丞（登录路由与跨域会话修复、配置统一）

### 问题与根因
- 消费者登录后无法回到前端深链；直接登录后也未跳至前端首页。
- 登录成功后前端在 `http://localhost:5173/` 无法加载餐厅/订单数据，跨源 XHR 不携带会话导致 401。
- 根因：
  - `AuthFilter` 重定向登录页不携带原始目标 `redirect`；`LoginServlet` 未限制消费者跳后台路径。
  - 跨域会话默认 `SameSite=Lax`，跨站 XHR 不附带 `JSESSIONID`；多数 API 未统一返回 CORS 允许头。
  - 前后端存在地址回退与硬编码，环境切换需要改动多处。

### 修复与统一配置
- 路由与回跳：
  - `AuthFilter` 将未登录/权限不足访问重定向到 `login.jsp?redirect=<原目标>`（URL 编码）。位置：`backend/src/main/java/com/platform/ordering/filter/AuthFilter.java:80-89`。
  - `LoginServlet` 读取 `redirect`，白名单允许回跳到前端（`consumerDefaultRedirect` 前缀）或本应用内路径；消费者禁止跳 `/admin/*`。位置：`backend/src/main/java/com/platform/ordering/controller/LoginServlet.java:47-74`。
- 跨域会话：
  - 登录成功后设置 `JSESSIONID` 为 `SameSite=None`，HTTPS 下附加 `Secure`。位置：`backend/src/main/java/com/platform/ordering/controller/LoginServlet.java:39-45`。
- CORS 统一：
  - 新增 `CorsFilter` 覆盖 `/api/*`，允许凭据、预检并暴露头（`X-Page/X-Size/X-Menu-Version/X-Menu-Signature/ETag`）。位置：`backend/src/main/java/com/platform/ordering/filter/CorsFilter.java:1`。
  - 允许来源改为从 `web.xml` 读取 `context-param: corsAllowedOrigin`，不再硬编码。配置：`backend/src/main/webapp/WEB-INF/web.xml:13-16`。
- 字符编码与过滤器映射：
  - `CharacterEncodingFilter` 仅设置响应编码，不覆盖 `Content-Type`。位置：`backend/src/main/java/com/platform/ordering/filter/CharacterEncodingFilter.java:30-34`。
  - 过滤器统一由 `web.xml` 映射，移除类上的注解以避免重复执行。映射见：`backend/src/main/webapp/WEB-INF/web.xml:24-42`。
- 前端配置统一：
  - 移除 `window.location.origin` 等地址回退，统一从 `.env` 读取后端地址：`frontend/src/router/index.js:20-23`，`frontend/src/api/index.js:3-8`。
  - 现有 `.env` 示例：`VITE_BACKEND_BASE=http://localhost:8080/online_ordering_backend_war_exploded`、`VITE_API_BASE=.../api`。

### 生产环境配置指引
- 仅需修改两处配置即可完成环境切换：
  - 后端：`backend/src/main/webapp/WEB-INF/web.xml`
    - `consumerDefaultRedirect` → 前端根地址（如 `https://app.example.com/`）
    - `corsAllowedOrigin` → 前端来源（如 `https://app.example.com`）
  - 前端：`frontend/.env`
    - `VITE_BACKEND_BASE` 与 `VITE_API_BASE` 指向后端域名（如 `https://api.example.com` 与 `https://api.example.com/api`）
- 若生产采用同源部署，可将前端通过反向代理挂载到同域下，`corsAllowedOrigin` 可与后端同域，甚至关闭 CORS 映射。

### 验收要点
- 登录场景：
  - 消费者深链被重定向登录后，成功回到原始前端页面（A-1）；直接访问登录页后回到前端首页（A-2）。
  - 商户与超级管理员按角色进入后台，消费者无法被引导到后台路径（B-1/B-2）。
- 前端数据加载：
  - `GET /api/session` 返回用户信息；`GET /api/restaurants`、`GET /api/menus/:id/items`、`GET /api/orders` 正常返回 JSON。
  - 响应头包含 `Access-Control-Allow-Origin`（等于 `web.xml` 的 `corsAllowedOrigin`）与 `Access-Control-Allow-Credentials: true`；菜单接口暴露版本/签名头。
- 登录失败保留 `redirect` 参数，便于重试：`backend/src/main/java/com/platform/ordering/controller/LoginServlet.java:76-81,87-91`。

---
## 2025-11-16 - by 钟丞（消费者端订单管理增强与串联商户刷新）

### 增量内容

- 订单字段与展示：
  - 列表与详情 `items[]` 增加 `dishId`；顶层增加 `serialNumber`（每日餐厅流水号）。
  - 历史订单卡片标题改为“餐厅名称：流水号”；详情页顶部最大字号显示“流水号：xx”，其下按钮显示餐厅名称并可跳转点单页；“订单id”字样替换。
- 再次购买与购物车：
  - 再次购买前先清空当前餐厅购物车；重新获取菜单与菜品，仅保留仍存在的内容；非套餐支持按 `dishId` 优先、`dishName` 回退匹配；数量统一下限 1。
- 取消订单：
  - 新增消费者接口 `POST /api/orders/{orderId}/cancel`，仅允许取消 `PENDING` 状态；并在商户端通过 SSE `order_updated` 自动刷新厨房面板。
- API 文档补充：
  - 下单契约改为“菜单维度”（含 `menus[].items[].sortOrder/quantity`）；订单列表 `GET /api/orders` 支持分页与筛选；订单详情返回 `menus[]` 分组；SSE 事件说明。

### 代码位置
- 后端：`backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java`（字段增补、取消接口、列表分页头）；`util/KitchenEventBus.java`（`order_updated`）。
- 前端：`frontend/src/pages/OrderDetail.vue`（取消/再次购买/流水号与餐厅按钮）；`frontend/src/pages/OrderHistory.vue`（卡片标题显示流水号）；`frontend/src/stores/cart.js`（清空与重建、数量下限）；`frontend/src/api/index.js`（取消接口）。
- 文档：`docs/API.md`（契约与字段补充）。

### 验收要点
- 再次购买能够在菜单变更后仅保留有效项，并准确计价；取消成功后消费者端状态变化立刻可见，商户厨房面板自动刷新；历史与详情页显示的流水号一致且可跳转餐厅页。

---
## 2025-11-16 - by 钟丞（商户端订单管理与厨房面板）

### 本次工作汇总

- 后台订单管理（JSP MVC）：
  - 新增 `OrdersAdminServlet` 提供列表/详情与状态更新入口（按 `restaurantId` 隔离）。
  - 列表页 `admin/orders.jsp`：筛选（状态/时间/关键词）、分页整数化、固定列宽与不换行、状态标签（黄/蓝/绿/红），右侧查看详情按钮；移除状态修改入口。
  - 详情页 `admin/order-detail.jsp`：Bootstrap 美化；菜品“菜单→菜品”二级分组；套餐菜单在标题追加【套餐】；列名“购买单价”（快照价）；顶部概览卡片（ID/顾客/时间/总价/状态）。
  - DAO 层：新增 `OrderAdminDAO/Impl`，支持分页列表、总数统计、详情（含菜单/菜品）、状态更新；所有 SQL 以 `sql_` 前缀，严格过滤 `restaurant_id`。

- 厨房面板（触发式刷新）：
  - 新增 `KitchenBoardServlet`：左右两栏（待处理/备餐中）按日期分组竖排显示，卡片含流水号/时间/总价/状态标签，底部“查看详情”。
  - 引入 SSE：
    - 事件总线 `KitchenEventBus`（餐厅级发布与订阅）。
    - 端点 `KitchenEventsServlet` 输出 `hello/ping` 心跳与 `new_order` 事件，保持长连接；断线自动重连。
    - 下单成功 `OrdersResourceApiServlet` 发布 `new_order`；面板监听后自动刷新。

- 仪表盘与导航：
  - 新增 `DashboardServlet` 提供今日订单数/收入/待处理/备餐中数据卡片；“今日新订单”卡片跳转订单列表并带今日筛选；底部仅保留“菜单管理/菜品管理”。
  - 导航栏去重并按厨房→订单→菜单→菜品→店铺顺序。

### 关键修复

- 资源关闭编译错误：统一采用 `DBUtil.close(conn, ps, rs)` 与阶段性关闭 `DBUtil.close(null, ps, rs)`。
- 分页页码为小数：控制器计算 `pages = (total+size-1)/size`，JSP 使用整数值。
- 表格切页列宽抖动：`table-layout: fixed + colgroup` 固定列宽并 `nowrap`。
- 厨房面板 JSP 循环不对称：修复 `c:forEach` 闭合；保持两栏与日期分割线。
- SSE 初版连接立即断开：移除 `resp.isCommitted` 判断，改用无限心跳循环；前端 `EventSource` 断线自动重连。

### 文件索引

- 控制器：`controller/OrdersAdminServlet.java`, `controller/KitchenBoardServlet.java`, `controller/DashboardServlet.java`, `controller/KitchenEventsServlet.java`
- 视图：`webapp/admin/orders.jsp`, `webapp/admin/order-detail.jsp`, `webapp/admin/kitchen.jsp`, `webapp/admin/dashboard.jsp`, `webapp/admin/header.jsp`
- DAO：`dao/OrderAdminDAO.java`, `dao/OrderAdminDAOImpl.java`
- 工具：`util/KitchenEventBus.java`
- API 触发：`api/OrdersResourceApiServlet.java`（`commit` 后发布 `new_order`）

### 验收要点

- 商户登录后进入仪表盘，卡片链接跳转正确；厨房面板能在新订单创建时自动刷新；订单列表筛选与分页稳定；详情页分组清晰、套餐标识正确。

## 2025-11-16 - by 钟丞

### 本次提交内容：消费者端 Vue SPA 联调闭环（购物车/下单/订单历史）与套餐打包销售逻辑

在 Sprint 3 基础上完成前端骨架、路由与状态管理，实现从餐厅浏览→菜单查看→购物车→下单→订单详情/历史的闭环；同时将下单契约改造为“菜单为单位”，支持套餐严格校验与计价，并优化缓存与导航体验。

#### 变更摘要
- 前端项目骨架与代理（Vite + Vue 3 + Pinia + vue-router）：
  - `frontend/package.json`, `frontend/vite.config.js`, `frontend/index.html`, `frontend/src/main.js`, `frontend/src/App.vue`
  - 代理上下文：`/online_ordering_backend_war_exploded/api`
- 路由与页面：
  - 餐厅列表与详情：`frontend/src/pages/RestaurantList.vue`, `frontend/src/pages/RestaurantDetail.vue`
  - 订单详情与历史：`frontend/src/pages/OrderDetail.vue`, `frontend/src/pages/OrderHistory.vue`
- 状态管理（购物车按菜单维度）：
  - `frontend/src/stores/cart.js` 结构 `{ [restaurantId]: { menus:[{menuId,isPackage,quantity,items[{dishId,sortOrder,quantity,name,price}]}], total } }`
  - 动作：`addItem/addPackage/updateItemQty/updatePackageQty/removeItem/clearCart/toOrderPayload`
- API 封装：
  - `frontend/src/api/index.js` 新增/改造 `getRestaurants/getRestaurant/getMenus/getMenuItems/getOrders/getOrder/createOrder/getSession`；对只读接口加时间戳绕缓存
- 套餐打包销售与下单契约改造：
  - `backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java` 将 `POST /api/orders` 从 `items[]` 改造为 `menus[]`；支持套餐校验（`sort_order/id/quantity` 全匹配）、非套餐校验、计价以 `menu_items.price` 为准；写入 `order_items.menu_id`
  - 订单详情 `GET /api/orders/{orderId}` 增加分组字段 `menus[]`（含 `menuUnitPrice`、`menuTotalPrice`、每项 `perPackageQuantity/sortOrder`）
- 只读接口禁用缓存：
  - `RestaurantResourceApiServlet.java`, `MenusResourceApiServlet.java`, `RestaurantsApiServlet.java` 设置 `Cache-Control/Pragma/Expires`
- 前端导航与统一排版：
  - 顶栏新增“餐厅列表/历史订单”导航；右侧保留“欢迎，用户名”与退出
  - 统一文本样式类：`.text-menu-name/.text-dish-name/.text-price/.text-qty/.text-muted`（`frontend/src/App.vue`）
- 购物车与页面交互优化：
  - 套餐：名称下显示“单价”，份数加减按钮；菜品行统一显示“￥价格 × 数量”，不显示公式
  - 非套餐：名称同一行右侧显示数量与加减；名称下方显示“单价”；数量减至 0 自动移除，菜单清空后标题消失
  - 外部套餐页：列表显示“总价：￥xx，包含：”；每行右侧统一“￥价格 × 每份数量”；内容区域限宽避免被购物车遮挡
- 历史订单页改为摘要卡片：
  - 仅显示餐厅、总价（两位小数）、状态、时间；卡片可点击跳转详情；按时间倒序
- 登录与会话：
  - 登录成功重定向支持 `redirect` 并默认按配置项返回前端；`web.xml` 新增 `consumerDefaultRedirect`
  - 新增 `GET /api/session` 供前端路由守卫使用；未登录或商户登录访问消费者前端即跳转登录
- 商家后台提醒：
  - `admin/dish-management.jsp` 在编辑菜品默认价格且发生变更时弹窗提示“仅修改默认价格，菜单中的实际价格不变”，引导前往菜单管理修改
- 测试页：
  - `backend/src/main/webapp/test/api-tests.jsp` 新增菜单维度下单示例与非法用例（跨餐厅、套餐不匹配、非法菜品/缺项）

#### 验证结果
- 端到端闭环：从首页进入餐厅详情，套餐/非套餐加入购物车，提交订单返回 `201`；跳转订单详情页显示按“菜单-菜品”分组的明细；历史订单页摘要正确并可跳转详情
- 套餐校验：缺项/数量不匹配返回 `400 {"error":"套餐<menu_id>不匹配"}`；计价以 `menu_items.price` 为准，同菜不同菜单价格独立
- 缓存刷新：商家修改菜单项价格后，前端刷新即可显示最新价格（只读接口禁用缓存 + 请求加时间戳）
- 导航与样式：顶栏导航可用；菜单名大于菜品名；价格统一两位小数；购物车与订单详情页的菜单-菜品显示逻辑一致

#### 后续待办
- 套餐清单签名/版本校验：返回 `ETag/version` 与签名令牌，避免下单时菜单变更导致提示不一致
- 历史订单分页与筛选（进行中/已完成）
- 商家菜单管理增加批量改价与排序的更友好交互
- 前端组件化与样式进一步统一（卡片、行距、对齐）

## 2025-11-15 - by 钟丞

### 本次提交内容：为 order_items 持久化菜单ID并完善查询响应

完成订单项持久化 `menu_id` 字段及相关联动，提升审计与对账能力，并在订单详情响应中返回 `menuId/menuName`。

#### 变更摘要
- Schema 扩展：`database/schema.sql` 在 `order_items` 新增 `menu_id` 外键至 `menus(menu_id)`；初始化脚本适配（写入 `menu_id`）。
- 模型与接口：
  - `backend/src/main/java/com/platform/ordering/model/OrderItem.java` 增加 `menuId/menuName` 字段
  - `backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java`：
    - 下单写入 `menu_id`（按 `menuId+dishId` 联合验证并取价）
    - 订单详情查询联接 `menus` 返回 `menuId/menuName` 并在 JSON 输出
- 初始化数据：`database/initial-data.sql` 示例订单项补充 `menu_id`，并新增多用户/多餐厅订单样例（A/B/C/D）
- 测试页：`backend/src/main/webapp/test/api-tests.jsp` 增加订单详情快捷查询与错误用例按钮（跨餐厅、无效组合）
- 文档：`docs/API.md` 增加“测试指引”与订单项字段说明（含历史兼容备注）

#### 测试结果
- 订单A（餐厅1、套餐菜单）在详情中正确显示 `menuId=2`、`menuName=工作日午市套餐`，总价与单价快照一致。
- 成功下单与错误用例（跨餐厅、无效组合）返回符合预期的状态码与错误信息。

---

## 2025-11-15 - by 钟丞

### 本次提交内容：Sprint 3 实现 GET API

完成消费者端只读 API 的首批落地与部署测试页面，契约与文档对齐，支持在无前端情况下完成端到端验证。

#### 新增/修改内容概览
- API 实现（Servlet）：
  - `GET /api/restaurants` 列表（`backend/src/main/java/com/platform/ordering/api/RestaurantsApiServlet.java`）
  - `GET /api/restaurants/{id}` 详情（`backend/src/main/java/com/platform/ordering/api/RestaurantResourceApiServlet.java`）
  - `GET /api/restaurants/{id}/menus` 菜单列表（同上）
  - `GET /api/menus/{menuId}/items` 菜单项（返回 `dishId,name,imageUrl,description,price`，`backend/src/main/java/com/platform/ordering/api/MenusResourceApiServlet.java`）
  - `GET /api/orders/{orderId}` 订单详情（需登录，会话鉴权，仅本人订单；`backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java`）
- DAO 扩展：`RestaurantDAO.listAll()` 与实现（`backend/src/main/java/com/platform/ordering/dao/RestaurantDAO.java`, `RestaurantDAOImpl.java`）
- 测试页面：`backend/src/main/webapp/test/api-tests.jsp`（按钮触发上述所有 GET 接口）
- 文档对齐：完善 `docs/API.md`（认证与状态码、字段来源映射、curl 示例、事务与校验规则）

#### 验证结果（以测试用户 testcustomer 登录）
- 餐厅列表与详情、菜单列表、菜单项均返回预期 JSON；订单详情返回本人订单的菜品明细与总价。

---

## 2025-11-15 - by 钟丞

### 本次提交内容：菜品&菜单管理阶段总结（Sprint 2 已完成）

本次变更聚焦“菜单编辑拖拽排序保存丢失”的后端稳健化修复与交互统一，确保在最小改动前提下彻底解决问题，并进行少量安全与显示优化。

#### 已测试无误的功能列表：
- 编辑菜单页内菜品拖拽仅在把手按住时触发；保存后顺序与菜品项均能正确持久化（`backend/src/main/webapp/admin/menu-edit-panel.jsp:105-137`，`backend/src/main/java/com/platform/ordering/controller/MenuServlet.java:157-178,224-263,373-407`）。
- 展示页菜单列表拖拽仅在把手按住时触发；重排后服务端接收并更新排序（`backend/src/main/webapp/admin/menu-management.jsp:109-133`，`backend/src/main/java/com/platform/ordering/controller/MenuServlet.java:338-350`）。
- 新建与编辑菜单保存的数据库事务逻辑正常提交；编辑视图中的草稿数据模型（`sessionScope.menuDraft`）正确构建并转换为待持久化行（`backend/src/main/java/com/platform/ordering/controller/MenuServlet.java:47-69,76-92,157-178,224-263`）。
- 菜单管理页底部只读菜品列表为空时显示“去菜品页添加菜品”按钮，跳转至新建菜品视图（`backend/src/main/webapp/admin/menu-management.jsp:172-176`，`backend/src/main/java/com/platform/ordering/controller/DishServlet.java:54-57`）。
- 所有列表的表头列标题与操作按钮统一为单行展示，不再换行（`backend/src/main/webapp/admin/menu-management.jsp:25,71,142-144`，`backend/src/main/webapp/admin/dish-management.jsp:24`）。
- 删除菜单操作弹窗确认，避免误删（`backend/src/main/webapp/admin/menu-management.jsp:46-50`）。

---
### 代码修改 Timeline
*在“菜单&菜品管理前后端既CRUD基本完成”commit基础上：*

1. 后端保存稳健化：新增 `rowsPayload` 解析方法，统一行数据原子性，避免多数组索引错位（`MenuServlet.java:373-407`）。
2. 新建/编辑保存路径优先使用 `rowsPayload` 构造持久化行；为空时回退旧数组逻辑或草稿（`MenuServlet.java:157-178,224-237`）。
3. 编辑保存事务的删除语句增加租户过滤，确保多租户隔离（`DELETE ... USING menus ...`）（`MenuServlet.java:227-229`）。
4. 编辑面板拖拽交互：加入把手类与 `mousedown` 标记，仅在把手拖拽时允许 `dragstart`（`menu-edit-panel.jsp:64,89,111-121`）。
5. 展示页菜单列表拖拽交互统一为只在把手触发（`menu-management.jsp:52,109-133`）。
6. 只读菜品列表空状态替换为“去菜品页添加菜品”入口（`menu-management.jsp:172-176`）。
7. 表头与操作列统一 `text-nowrap`，防止列标题与按钮换行（`menu-management.jsp:25,71,142-144`；`dish-management.jsp:24,36-43`）。
8. 删除菜单加入确认弹窗（`menu-management.jsp:46-50`）。
9. 新建测试脚本验证租户隔离 `webapp/test/tenant-data.jsp`
---
### 遇到的问题与解决历程

1) 拖拽保存后丢失项（根因与修复）
- 根因：编辑保存采用“先删后重建”，对 `dishId/price/quantity/sortOrder` 多数组索引对齐依赖强；拖拽与数量清零会造成索引错位或行被前端剔除，导致漏插入。
- 修复：新增 `parseRowsPayload`，将每行封装为 `dishId:price:qty:idx` 的原子数据，后端统一解析并重建；当 `rowsPayload` 缺失时回退旧数组逻辑或草稿（`MenuServlet.java:373-407,157-178,224-237`）。

2) 多租户隔离缺失（安全修复）
- 现象：编辑保存事务中的 `DELETE FROM menu_items WHERE menu_id=?` 未携带租户过滤，有跨租户误删风险。
- 修复：改为 `DELETE FROM menu_items USING menus WHERE menu_items.menu_id=? AND menu_items.menu_id=menus.menu_id AND menus.restaurant_id=?`（`MenuServlet.java:227-229`）。

3) 把手限制后拖拽不可用（交互修复）
- 现象：`dragstart` 的事件 `target` 常为整行，直接检测把手节点会阻止拖拽。
- 修复：加入 `mousedown` 标记 `allowRowDrag` 并在 `dragstart` 基于标记判断，仅当把手按下时允许拖拽；释放后重置标记（`menu-edit-panel.jsp:111-121`，`menu-management.jsp:109-117`）。

---
### 设计方法补充：编辑/新建菜单的事务与草稿数据模型

- 事务设计（JDBC 手动事务）：
  - 新建保存：先插入 `menus` 获取新 `menu_id`，再批量插入 `menu_items`，成功后 `commit`，异常时 `rollback`（`MenuServlet.java:141-191`）。
  - 编辑保存：更新 `menus` 基本信息后，删除旧 `menu_items` 并按表单或草稿重建，最后 `commit`，异常时 `rollback`（`MenuServlet.java:213-263`）。

- 草稿数据模型（`sessionScope.menuDraft`）：
  - 进入编辑视图时从数据库现状构建 `DraftMenu` + `DraftMenuItem` 列表，便于前端编辑与顺序控制（`MenuServlet.java:47-69`）。
  - 保存时若前端未提供完整数组，回退使用草稿列表作为持久化数据源，保证编辑体验与数据完整性（`MenuServlet.java:175-176,248-251`）。



## 2025-11-14 - by 钟丞

### 本次提交内容：Sprint 1 全部开发任务完成

本次提交由一人扮演所有角色，完成了Sprint 1计划中的全部开发任务，并成功通过了核心功能测试。项目现在具备了完整的、带事务安全的注册功能，以及包含权限验证的登录/退出功能。

#### 已测试无误的功能列表：
1.  **消费者/商户注册**：可通过UI页面成功创建两种角色的用户。
2.  **用户登录/退出**：可使用正确的凭据登录，并安全退出。
3.  **后台权限过滤**：未登录用户无法访问`/admin/`下的任何资源。
4.  **事务安全**：商户注册时，若用户名或餐厅名重复，数据库不会产生任何不一致的“脏数据”。
5.  **字符编码**：所有页面均能正确显示中文。
6.  **路径引用**：所有JSP页面的资源包含（include）均使用绝对路径，无500错误。

---
### 代码修改 Timeline

1.  **创建核心业务实体与DAO**:
    *   `model/Restaurant.java`: 新建，用于表示餐厅实体。
    *   `dao/RestaurantDAO.java` & `dao/RestaurantDAOImpl.java`: 新建，用于处理餐厅数据的持久化，其`save`方法能返回自增ID。

2.  **实现注册后端逻辑**:
    *   `controller/RegistrationServlet.java`: 新建，作为`/register`接口，处理消费者和商户的注册请求。

3.  **构建前端注册与登录页面**:
    *   `webapp/register-customer.jsp` & `webapp/register-merchant.jsp`: 新建，提供用户注册的UI表单。
    *   `webapp/login.jsp`: 新建/美化，提供统一登录入口，并能根据URL参数显示不同提示信息。

4.  **实现登录/退出后端逻辑**:
    *   `controller/LoginServlet.java`: 新建，作为`/login`接口，验证用户凭据，管理Session。
    *   `controller/LogoutServlet.java`: 新建，作为`/logout`接口，负责销毁Session。
    *   `admin/header.jsp`: 修改，将“退出登录”链接指向`/logout`。

5.  **修复JSP页面中文乱码**:
    *   `login.jsp`, `header.jsp`, `footer.jsp`, `dashboard.jsp`, `register-*.jsp`: 全面添加`pageEncoding="UTF-8"`指令，解决Tomcat解析JSP时的编码问题。

6.  **修复JSP Include路径错误**:
    *   `login.jsp`, `register-*.jsp`: 修改`<jsp:include>`的`page`属性，从相对路径改为相对于Web应用根目录的绝对路径（以`/`开头），解决了500服务器错误。

7.  **实现注册事务安全性 (核心重构)**:
    *   `dao/UserDAO.java` & `dao/RestaurantDAO.java`: 修改接口，为`save`方法增加一个接收`Connection`的重载版本。
    *   `dao/UserDAOImpl.java` & `dao/RestaurantDAOImpl.java`: 同步修改实现类，使其能参与外部事务。
    *   `controller/RegistrationServlet.java`: **重大重构**，为商户注册逻辑引入了完整的JDBC事务控制（`setAutoCommit`, `commit`, `rollback`），解决了因部分操作失败导致数据库产生“脏数据”的严重BUG。

8.  **修复DAO层编译错误**:
    *   `dao/UserDAO.java` & `dao/RestaurantDAO.java`: 再次修改接口，明确定义事务性和非事务性两个`save`方法，解决了Servlet中因方法签名不匹配导致的编译失败问题。
    *   `dao/UserDAOImpl.java` & `dao/RestaurantDAOImpl.java`: 同步修改，为所有接口方法正确添加`@Override`注解。

---
### 遇到的问题与解决历程

1.  **问题：JSP页面中文乱码**
    *   **现象**: `footer.jsp`中的中文显示为`ç½®ä¸è®¢é¤å¹³å°`。
    *   **原因分析**: `CharacterEncodingFilter`只保证了请求和响应的编码是UTF-8，但Tomcat在解析JSP文件本身时，可能采用了系统默认编码（如GBK），导致在服务器内部就已经产生了乱码。
    *   **解决历程**: 在所有JSP文件顶部添加`<%@ page pageEncoding="UTF-8" %>`指令，强制Tomcat使用UTF-8编码来读取和解析JSP文件，从根源上解决了问题。

2.  **问题：JSP include 500错误**
    *   **现象**: 访问注册页面时，服务器报`无法包含[/../admin/header.jsp]`的500错误。
    *   **原因分析**: JSP的`<jsp:include>`指令出于安全和规范考虑，不允许使用`../`这样的相对路径来访问上级目录的资源。
    *   **解决历程**: 将所有`<jsp:include>`的`page`属性值修改为从Web应用根目录开始的绝对路径，例如`page="/admin/header.jsp"`。

3.  **问题：商户注册时产生脏数据**
    *   **现象**: 第一次注册，若用户名已存在，会导致用户创建失败，但餐厅却创建成功了。第二次使用相同的餐厅名注册，会直接报“餐厅名重复”的数据库异常。
    *   **原因分析**: `RegistrationServlet`中的“创建餐厅”和“创建用户”是两个独立的数据库操作，没有被一个原子性的事务包裹。任何一步的失败都会导致数据不一致。
    *   **解决历程**: 对`RegistrationServlet`进行了**重大重构**。首先修改了DAO层接口和实现，使其`save`方法能接收外部传入的`Connection`对象。然后在Servlet中，手动开启JDBC事务：通过`conn.setAutoCommit(false)`关闭自动提交，在所有操作成功后执行`conn.commit()`，在任何`catch`块中执行`conn.rollback()`。这确保了两个操作要么都成功，要么都失败，彻底解决了脏数据问题。

4.  **问题：DAO层重构引发的系列编译错误**
    *   **现象**: 修改DAO接口后，实现类出现`@Override`错误，Servlet出现“方法找不到”的错误。
    *   **原因分析**: 对Java接口、实现、重载和多态的理解与实践不够严谨。最初只在实现类中添加重载方法，但上层（Servlet）持有的是接口引用，“看”不到实现类中独有的方法。
    *   **解决历程**: 最终采取了最规范的方案：在DAO接口中明确定义所有需要暴露的方法（包括事务性和非事务性版本），然后在实现类中完整实现它们。这保证了接口的契约性和代码的清晰性，一劳永逸地解决了所有连锁编译问题。**教训：修改接口后，必须立即同步修改所有实现类。**

## 2025-10-28 - by 钟丞

### 本次提交内容：

1.  **数据库架构升级 (V2)**: 
    *   根据“套餐”和“菜品单点”需求，将原有的“分类-菜品”一对多模型，重构为“菜单-菜品”多对多模型。
    *   核心改动：`categories` 表重命名为 `menus`；`dishes` 表移除价格；新增 `menu_items` 中间表，用于定义菜品在特定菜单中的价格。
    *   同步更新了 `schema.sql`, `initial-data.sql` 和 `development.md` 中的ER图。

2.  **后端实体类同步**:
    *   根据新的数据库 schema，创建和重构了 `Menu.java`, `Dish.java`, `MenuItem.java` 三个核心实体类。

3.  **数据库连接与DAO层验证**:
    *   因登录功能尚未开发，为验证数据链路，创建了临时测试页面 `test_db.jsp`。
    *   通过该页面成功验证了 `DBUtil` 的数据库连接功能，以及 `UserDAO` 能正确查询到 `initial-data.sql` 中插入的测试用户。
    *   根据约定，测试页面已保留并存放于 `webapp/test/` 目录下。

### 后续计划：
*   基于新的实体类，继续重构和实现DAO层（`MenuDAO`, `DishDAO`, `MenuItemDAO`）。
*   开发用户注册与登录功能的Servlet。

---

## 2025-10-27 - by 钟丞

### 本次提交内容：

完成了 **Sprint 1** 中由组长A负责的全部基础架构搭建工作，为项目建立了一个可运行的骨架。主要包括：

1.  **项目结构**: 创建了完整的 `backend`, `frontend`, `database`, `docs` 目录结构。
2.  **数据库**: 编写了 `database/schema.sql`，定义了项目所有数据表结构。
3.  **后端骨架**: 
    *   配置了 `pom.xml`，引入了所有必要的Java Web依赖。
    *   提供了 `DBUtil.java` 作为数据库连接的唯一入口。
    *   实现了 `CharacterEncodingFilter` 和 `AuthFilter` 两个核心过滤器。
    *   配置了 `web.xml` 部署描述符。
4.  **开发范例**: 
    *   提供了 `User.java` (实体), `UserDAO.java` (接口), `UserDAOImpl.java` (实现) 作为数据访问层的标准开发示例。
    *   提供了 `header.jsp`, `footer.jsp`, `dashboard.jsp` 作为后台页面UI的构建示例。

---

### **重要提醒：给所有团队成员**

**1. 代码仅作范例，尚未测试：**
   本次提交的代码是为了搭建项目结构和统一开发规范，**并未经过实际的功能测试**。数据库连接、DAO操作、过滤器逻辑等都需要在后续开发中进行联调和验证。

**2. 先阅读文档，再进行开发：**
   **请不要立即着手编写业务代码！** 请务必花时间仔细阅读 `docs` 目录下的所有文档，特别是：
   *   `README.md`: 了解项目总体技术选型。
   *   `development.md`: 了解我们的**迭代计划、任务分工和协作流程**。
   *   `product-backlog.md`: 了解我们要做哪些具体功能。

**3. 先交流，后编码：**
   在理解了文档之后，请找到 `development.md` 中分配给你的 **Sprint 1** 任务。如果你对任务有任何不清楚的地方，或者对我的范例代码有疑问，请**务必先在团队群里提出并交流**，达成共识后再开始编码。

我预计将在明天测试好项目基础，随后大家就可以尽情编码了
我们的目标是高效协作，避免返工。谢谢大家！
## 2025-11-16 - by 钟丞（Sprint 3 收官增量）

### 增量内容：套餐版本/签名校验、历史订单分页/筛选、前端错误提示细化

- 套餐版本/签名校验：
  - `GET /api/menus/{menuId}/items` 在响应头返回 `ETag/X-Menu-Signature/X-Menu-Version`（基于有序串 `dishId|sortOrder|quantity|price` 的 SHA-256）。
  - 前端 `getMenuItems` 读取签名/版本并在加入套餐时写入购物车；`toOrderPayload` 随菜单传递 `menuVersion/menuSignature`。
  - `POST /api/orders` 可选校验签名/版本；不一致或套餐项差异返回 `409 Conflict`，`details` 含 `versionMismatch/quantityDiffs/missingItems`。
- 历史订单分页与筛选：
  - `GET /api/orders` 支持 `page/size/status/from/to` 查询参数；按时间倒序；分页元数据通过响应头 `X-Page/X-Size` 返回。
- 前端错误提示细化：
  - 下单遇到 `409`（菜单变更）时弹窗提示“下单失败：菜单已变更，请刷新页面”。

### 代码位置
- 后端：
  - `backend/src/main/java/com/platform/ordering/api/MenusResourceApiServlet.java`（签名/版本头返回）
  - `backend/src/main/java/com/platform/ordering/api/OrdersResourceApiServlet.java`（签名/版本校验、返回409细节；分页查询实现与头返回）
- 前端：
  - `frontend/src/api/index.js`（返回 `items+version+signature`）
  - `frontend/src/stores/cart.js`（在菜单维度购物车存储并随单传递 `menuVersion/menuSignature`）
  - `frontend/src/pages/RestaurantDetail.vue`（读取并写入签名/版本到购物车）
  - `frontend/src/components/CheckoutForm.vue`（409 弹窗提示）

### 测试用例
- `backend/src/main/webapp/test/api-tests.jsp`：
  - 套餐数量不匹配与缺项用例（返回 409）。
  - 订单分页查询按钮（显示 `X-Page/X-Size` 并输出内容）。