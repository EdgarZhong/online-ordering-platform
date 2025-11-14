# 开发日志

## 2025-10-29 - by 团队 (A, B, C, D, E)

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