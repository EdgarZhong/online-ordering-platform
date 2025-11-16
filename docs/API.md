# 网上订餐平台 - 消费者端 API 文档 (V1)

## 1. 概述 (Overview)

本文档定义了网上订餐平台面向消费者前端 (Vue SPA) 的 RESTful API。

- **数据格式**: 所有请求和响应的主体 (body) 均为 `application/json` 格式。
- **字符编码**: 所有文本数据均为 `UTF-8` 编码。
- **基本路径**: 所有 API 的基本路径为 `/api`。
- **状态码**:
  - `200 OK`: 请求成功。
  - `201 Created`: 资源创建成功（例如，下单成功）。
  - `400 Bad Request`: 请求参数无效或格式错误。
  - `401 Unauthorized`: 未登录或会话失效（如订单查询）。
  - `404 Not Found`: 请求的资源不存在或无权限访问。
  - `409 Conflict`: 资源冲突（如订单在当前状态不可取消或已被他人更新）。
  - `500 Internal Server Error`: 服务器内部错误。

### 通用约定 (Conventions)
- 请求头：`Accept: application/json`
- 响应头：`Content-Type: application/json; charset=UTF-8`
- 错误返回统一为：`{"error": "message"}`（字符串信息，不包含错误码字段）
- 分页与排序：订单列表已支持分页与筛选（见 3.6 历史订单接口）。
- 版本管理：当前版本为 `v1`，路径不含版本号（后续可能引入 `/api/v2`）

## 2. 认证 (Authentication)

在 Sprint 3 阶段，为了简化开发和测试：
- 只读 `GET` 接口中，**订单查询** `GET /api/orders/{orderId}` 需要登录；其他 `GET` 接口公开。
- `POST /api/orders`（下单）请求需要用户认证。认证信息通过 `Session` 获取，前端需确保用户已登录。

---

## 3. API 端点 (Endpoints)

### 模块一: 餐厅 (Restaurants)

#### 3.1 获取餐厅列表

- **Endpoint**: `GET /api/restaurants`
- **描述**: 获取所有已上线的餐厅列表，用于首页展示。
- **请求参数**: 无
- **响应字段**:
  - `restaurantId` (int) 餐厅ID
  - `name` (string) 名称
  - `address` (string) 地址
  - `phone` (string) 电话
  - `logoUrl` (string) LOGO 图片URL（可为空）
  - `description` (string) 描述
- **成功响应 (200 OK)**:
  ```json
  [
    {
      "restaurantId": 1,
      "name": "王记私房菜",
      "address": "学院路101号",
      "phone": "13800138000",
      "logoUrl": "/images/logos/wangji.png",
      "description": "传承百年的家乡味道。"
    },
    {
      "restaurantId": 2,
      "name": "李师傅快餐",
      "address": "学苑路202号",
      "phone": "13900139000",
      "logoUrl": "/images/logos/lishifu.png",
      "description": "快速、健康、美味。"
    }
  ]
  ```

#### 3.2 获取餐厅详情

- **Endpoint**: `GET /api/restaurants/{id}`
- **描述**: 根据餐厅 ID 获取单个餐厅的详细信息。
- **URL 参数**:
  - `id` (integer, required): 餐厅的唯一ID。
- **请求参数**: 无
- **响应字段**: 同 3.1（单对象）
- **成功响应 (200 OK)**:
  ```json
  {
    "restaurantId": 1,
    "name": "王记私房菜",
    "address": "学院路101号",
    "phone": "13800138000",
    "logoUrl": "/images/logos/wangji.png",
    "description": "传承百年的家乡味道。"
  }
  ```
- **错误响应 (404 Not Found)**:
  ```json
  {
    "error": "Restaurant not found"
  }
  ```

---

### 模块二: 菜单与菜品 (Menus \u0026 Dishes)

#### 3.3 获取餐厅的菜单列表

- **Endpoint**: `GET /api/restaurants/{id}/menus`
- **描述**: 获取指定餐厅下的所有菜单分类。
- **URL 参数**:
  - `id` (integer, required): 餐厅的唯一ID。
- **请求参数**: 无
- **响应字段**:
  - `menuId` (int) 菜单ID
  - `name` (string) 名称
  - `description` (string) 描述
  - `isPackage` (boolean) 是否套餐（套餐在下单与展示时以菜单维度计算）
- **成功响应 (200 OK)**:
  ```json
  [
    {
      "menuId": 1,
      "name": "午市套餐",
      "description": "周一至周五 11:00-14:00 供应"
    },
    {
      "menuId": 2,
      "name": "招牌单点",
      "description": "全天供应"
    }
  ]
  ```

#### 3.4 获取菜单下的菜品项

- **Endpoint**: `GET /api/menus/{menuId}/items`
- **描述**: 获取指定菜单下的所有菜品项（包含价格）。
- **URL 参数**:
  - `menuId` (integer, required): 菜单的唯一ID。
- **请求参数**: 无
- **响应字段**:
  - `dishId` (int) 菜品ID
  - `name` (string) 菜品名称
  - `imageUrl` (string) 菜品图片URL（可为空）
  - `description` (string) 菜品描述（可为空）
  - `price` (number) 当前菜单项价格（来源于 `menu_items.price`）
  - `sortOrder` (int) 菜单内排序序号
  - `defaultQuantity` (int) 套餐内默认份数（非套餐为 0 或省略）
- **响应头**:
  - `X-Menu-Version`: 菜单版本（签名截断）
  - `X-Menu-Signature`: 菜单签名（基于菜品/数量/排序/价格）
  - `ETag`: `menu-{menuId}-{version}`
- **成功响应 (200 OK)**:
  ```json
  [
    {
      "dishId": 101,
      "name": "红烧肉",
      "imageUrl": "/images/dishes/hongsiaorou.jpg",
      "description": "肥而不腻，入口即化",
      "price": 45.00
    },
    {
      "dishId": 102,
      "name": "清蒸鲈鱼",
      "imageUrl": "/images/dishes/luyu.jpg",
      "description": "鲜嫩可口",
      "price": 68.50
    }
  ]
  ```

---

### 模块三: 订单 (Orders)

#### 3.5 创建订单（下单）

- **Endpoint**: `POST /api/orders`
- **描述**: 消费者提交购物车内容，创建新订单。**需要用户登录**。当前契约以“菜单维度”组织，支持套餐严格校验与计价。
- **请求体 (Request Body)**:
  ```json
  {
    "restaurantId": 1,
    "menus": [
      {
        "menuId": 2,
        "quantity": 1,
        "items": [
          { "dishId": 101, "sortOrder": 1, "quantity": 1 },
          { "dishId": 102, "sortOrder": 2, "quantity": 2 }
        ],
        "menuVersion": "v1",
        "menuSignature": "abc..."
      }
    ]
  }
  ```
- **成功响应 (201 Created)**:
  ```json
  {
    "orderId": 5001,
    "status": "PENDING",
    "totalPrice": 182.00,
    "createdAt": "2025-11-07T14:30:00Z"
  }
  ```
- **错误响应 (400 Bad Request)**:
  - 跨店下单或商品不存在。
  ```json
  {
    "error": "Invalid items in the order. All items must belong to the same restaurant and be available."
  }
  ```
- **校验与规则**:
  - 每个 `item` 必须提供 `dishId`（非套餐还需提供数量）；以 `menu_items` 中该菜单的 `price` 为准。
  - `menuId` 必须属于 `restaurantId`；否则拒绝（`400`）。
  - 套餐校验：`items[]` 必须与服务端菜单项完全一致（`dishId/sortOrder/quantity`），可选校验 `menuSignature`；不一致返回 `409`。参见 `perPackageQuantity/sortOrder` 字段含义。
  - 同一餐厅中允许菜品在多个菜单出现；下单以用户选择菜单的价格计算。
  - `totalPrice` 服务端计算，不接受客户端传入。
  - 原子性：`orders` 与 `order_items` 在同一事务中写入；任一失败回滚。
  - 防越权：会话用户作为订单归属 `user_id`。

#### 3.6 查询订单状态（详情）

- **Endpoint**: `GET /api/orders/{orderId}`
- **描述**: 查询特定订单的状态和详情。**需要用户登录**，且只能查询自己的订单。
- **URL 参数**:
  - `orderId` (integer, required): 订单的唯一ID。
- **请求参数**: 无
- **响应字段**:
  - `orderId` (int) 订单ID
  - `restaurantId` (int) 餐厅ID
  - `restaurantName` (string) 餐厅名称
  - `serialNumber` (int) 当日餐厅内流水号
  - `status` (string) 订单状态（见下方枚举）
  - `totalPrice` (number) 总价
  - `createdAt` (string) 下单时间（服务器时间）
  - `items` (array) 订单明细：`menuId`、`menuName`、`dishId`、`dishName`、`quantity`、`unitPrice`
  - `menus` (array) 分组信息：`menuId/menuName/isPackage/menuQuantity/menuUnitPrice/menuTotalPrice/items[perPackageQuantity/sortOrder]`
- **成功响应 (200 OK)**:
  ```json
  {
    "orderId": 5001,
    "restaurantName": "王记私房菜",
    "status": "CONFIRMED", // PENDING, CONFIRMED, PREPARING, READY_FOR_PICKUP, COMPLETED, CANCELLED
    "totalPrice": 182.00,
    "createdAt": "2025-11-07T14:30:00Z",
    "items": [
      {
        "menuId": 2,
        "menuName": "招牌单点",
        "dishName": "红烧肉",
        "quantity": 1,
        "unitPrice": 45.00
      },
      {
        "menuId": 2,
        "menuName": "招牌单点",
        "dishName": "清蒸鲈鱼",
        "quantity": 2,
        "unitPrice": 68.50
      }
    ]
  }
  ```
 - 历史兼容：旧订单项可能没有 `menuId`（为 `null` 或省略）。
- **错误响应 (401 Unauthorized)**:
  ```json
  { "error": "Unauthorized" }
  ```
- **错误响应 (404 Not Found)**:
  ```json
  {
    "error": "Order not found or you do not have permission to view it."
  }
  ```
- **状态枚举 (Status Enum)**:
  - `PENDING`, `PROCESSING`, `COMPLETED`, `CANCELLED`
 - **curl 示例**:
   ```bash
   curl -s -b "JSESSIONID=..." \
     -H "Accept: application/json" \
     "http://localhost:8080/{context}/api/orders/1"
   ```

---

#### 3.7 查询历史订单（分页）

- **Endpoint**: `GET /api/orders`
- **描述**: 查询当前登录用户的历史订单列表，支持分页与筛选。
- **查询参数**:
  - `page` (int, default 0): 页码（从 0 开始）。
  - `size` (int, default 20): 每页条数。
  - `status` (string, optional): 订单状态过滤。
  - `from` (string, optional): 起始时间（`YYYY-MM-DDTHH:mm:ss`）。
  - `to` (string, optional): 截止时间（`YYYY-MM-DDTHH:mm:ss`）。
- **响应头**:
  - `X-Page`: 当前页码。
  - `X-Size`: 当前页大小。
- **响应字段（列表元素）**:
  - `orderId`, `restaurantId`, `restaurantName`, `serialNumber`, `status`, `totalPrice`, `createdAt`
  - `items[]`: `menuId/menuName/dishId/dishName/quantity/unitPrice`
- **成功响应 (200 OK)**:
  ```json
  [
    {
      "restaurantId": 1,
      "orderId": 5001,
      "restaurantName": "王记私房菜",
      "serialNumber": 12,
      "status": "PENDING",
      "totalPrice": 182.00,
      "createdAt": "2025-11-07T14:30:00Z",
      "items": [
        { "menuId": 2, "menuName": "招牌单点", "dishId": 101, "dishName": "红烧肉", "quantity": 1, "unitPrice": 45.00 }
      ]
    }
  ]
  ```
- **错误响应 (401 Unauthorized)**: 未登录。

#### 3.8 取消订单

- **Endpoint**: `POST /api/orders/{orderId}/cancel`
- **描述**: 取消当前登录用户的指定订单；仅当订单状态为 `PENDING` 时允许。
- **URL 参数**:
  - `orderId` (integer, required)
- **成功响应 (200 OK)**:
  ```json
  { "orderId": 5001, "status": "CANCELLED" }
  ```
- **错误响应**:
  - `404 Not Found`: 订单不存在或无权限。
  - `409 Conflict`: 订单不可取消或已被他人更新。

---

## 4. 实体字段映射 (Entity Field Mapping)
- Restaurant → `restaurants`
  - `restaurant_id` → `restaurantId`
  - `name`, `address`, `phone`, `logo_url` → `logoUrl`, `description`
- Menu → `menus`
  - `menu_id` → `menuId`
  - `name`, `description`
- MenuItem 输出字段来源：
  - `dishes.dish_id` → `dishId`
  - `dishes.name` → `name`
  - `dishes.image_url` → `imageUrl`
  - `dishes.description` → `description`
  - `menu_items.price` → `price`
- Order → `orders`
  - `order_id` → `orderId`
  - `user_id`, `restaurant_id`, `total_price`, `status`, `order_time` → `createdAt`
  - `serialNumber` 为运行时计算字段（按“每日/餐厅”有序号），非持久化列
- OrderItem → `order_items` JOIN `dishes`
  - `dish_name`（来自 `dishes.name`）→ `dishName`
  - `quantity`, `unit_price`
  - 扩展：在订单详情的 `menus[].items[]` 中还包含 `perPackageQuantity` 与 `sortOrder`（用于套餐数量与展示排序）

## 5. 安全与隔离 (Security & Isolation)
- 多租户隔离：所有数据访问在 DAO/SQL 层通过 `restaurant_id` 进行过滤与关联。
- 只读接口：餐厅与菜单为公开数据；订单接口严格绑定会话用户。
- 会话鉴权：通过服务器 Session 管理登录态；未登录访问订单返回 `401`
 - **curl 示例**:
   ```bash
   curl -s \
     -H "Accept: application/json" \
     "http://localhost:8080/{context}/api/restaurants"
   ```
 - **curl 示例**:
   ```bash
   curl -s \
     -H "Accept: application/json" \
     "http://localhost:8080/{context}/api/restaurants/1"
   ```
 - **curl 示例**:
   ```bash
   curl -s \
     -H "Accept: application/json" \
     "http://localhost:8080/{context}/api/restaurants/1/menus"
   ```
 - **curl 示例**:
  ```bash
  curl -s \
    -H "Accept: application/json" \
    "http://localhost:8080/{context}/api/menus/1/items"
  ```

---

## 7. 测试指引 (How to Test)
- 初始化数据：执行 `database/initial-data.sql`（包含两家餐厅、两个消费者、四个订单示例）
- 登录：使用 `testcustomer` 或 `testcustomer2` 登录
- 浏览器测试：打开 `test/api-tests.jsp`
  - 成功下单：使用预填 `restaurantId=1, menuId=1` 的示例 JSON 点击“下单”，返回 `201` 与正确总价
  - 错误用例：
    - 跨餐厅：`restaurantId=1`、`menuId=3`（餐厅2菜单）→ 返回 `400`
    - 无效组合：`menuId=2`、`dishId=3`（菜品不在该菜单）→ 返回 `400`
  - 查询订单：点击“查询订单A/B/C/D”按钮，查看 `menuId/menuName/dishName/quantity/unitPrice` 与总价
- 命令行示例：
  ```bash
  curl -s -X POST \
    -H "Content-Type: application/json" -H "Accept: application/json" \
    -b "JSESSIONID=..." \
    -d '{"restaurantId":1,"items":[{"menuId":1,"dishId":1,"quantity":1},{"menuId":1,"dishId":3,"quantity":2}]}' \
    "http://localhost:8080/{context}/api/orders"
  ```
- 数据库迁移提示：若已有环境需先执行
  ```sql
  ALTER TABLE order_items ADD COLUMN menu_id INT NULL;
  ALTER TABLE order_items ADD CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_id) REFERENCES menus(menu_id);
  ```

---

## 6. curl 综合示例：下单
```bash
curl -s -X POST \
  -H "Content-Type: application/json" -H "Accept: application/json" \
  -b "JSESSIONID=..." \
  -d '{"restaurantId":1,"items":[{"menuId":1,"dishId":1,"quantity":1},{"menuId":1,"dishId":3,"quantity":2}]}' \
  "http://localhost:8080/{context}/api/orders"
```
#### 3.7 查询历史订单列表

- **Endpoint**: `GET /api/orders`
- **描述**: 获取当前用户的订单列表，支持分页与筛选。
- **查询参数**: `page`(默认0), `size`(默认20), `status`(可选), `from`/`to`(时间范围)
- **响应头**: `X-Page`, `X-Size`
- **响应体**: 数组。每项包含 `restaurantId/orderId/restaurantName/serialNumber/status/totalPrice/createdAt/items[]`（items 内含 `dishId/dishName/menuId/menuName/quantity/unitPrice`）。

#### 3.8 取消订单

- **Endpoint**: `POST /api/orders/{orderId}/cancel`
- **描述**: 取消当前用户的某个订单，仅当状态为 `PENDING`。
- **成功响应 (200 OK)**:
  ```json
  { "orderId": 5001, "status": "CANCELLED" }
  ```
- **错误响应**:
  - `404`：订单不存在或无权限
  - `409`：订单已被他人更新，无法取消
## 8. 附：商户端事件推送（SSE）
- 端点：`GET /admin/kitchen/events`
- 事件：`new_order`（新订单创建）、`order_updated`（订单状态变更）
- 客户端：`EventSource` 监听上述事件并刷新厨房面板。