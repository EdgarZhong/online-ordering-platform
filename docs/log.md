# 开发日志

## 2025-11-15 - by 钟丞

### 本次提交内容：菜品&菜单管理阶段总结（Sprint 2 迭代中）

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

---
### 后续工作（暂缓项）
- 用“差异更新”替代“删除重建”，避免主键重建与潜在误删。
- 在服务端进行重复 `dishId` 校验与友好错误提示。
- `.gitattributes` 规范行尾策略，提升跨平台协作一致性。


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