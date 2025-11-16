# 路径与环境变量配置教程（统一前后端配置）

本文详细说明本项目（`online-ordering-platform`）的路径与环境变量如何统一配置，并给出开发/生产两套完整示例与校验清单。

**统一入口**：仅需修改两处配置文件即可完成环境切换。
- 前端：`frontend/.env`
- 后端：`backend/src/main/webapp/WEB-INF/web.xml`

---

## 1. 前端（Frontend）

前端使用 Vite 的环境变量系统。所有对后端的地址均通过 `.env` 提供，代码中不应出现硬编码域名/端口。

### 1.1 变量说明（`.env`）
- `VITE_BACKEND_BASE`：后端完整基地址，用于浏览器跳转登录/注销等页面（示例：`http://localhost:8080/online_ordering_backend_war_exploded`）。
- `VITE_API_BASE`：后端 API 基地址（示例：`http://localhost:8080/online_ordering_backend_war_exploded/api`）。若不配置，默认为 `${VITE_BACKEND_BASE}/api`。
- `VITE_BACKEND_CONTEXT`：后端上下文路径，仅供开发代理与构造相对地址（示例：`/online_ordering_backend_war_exploded`）。
- `VITE_BACKEND_TARGET`：开发代理目标主机（示例：`http://localhost:8080`）。
- `VITE_DEV_PORT`：Vite 开发服务器端口（示例：`5173`）。

> 代码读取位置：
- Axios 实例与登录重定向：`frontend/src/api/index.js:3-6, 19-21`
- 路由守卫重定向：`frontend/src/router/index.js:20-35`
- 开发代理与端口：`frontend/vite.config.js:1-20`

### 1.2 开发环境示例（本地 IDE 调试）
在 `frontend/.env` 写入：
```env
VITE_BACKEND_BASE=http://localhost:8080/online_ordering_backend_war_exploded
VITE_BACKEND_CONTEXT=/online_ordering_backend_war_exploded
VITE_API_BASE=http://localhost:8080/online_ordering_backend_war_exploded/api
VITE_BACKEND_TARGET=http://localhost:8080
VITE_DEV_PORT=5173
```

> 说明：`online_ordering_backend_war_exploded` 为 IntelliJ IDEA 部署到 Tomcat 的默认 exploded 上下文名，需与 IDE 实际部署名称保持一致。

### 1.3 生产环境示例
- 方案A（前后端不同域，推荐）：
```env
VITE_BACKEND_BASE=https://api.example.com/online_ordering
VITE_API_BASE=https://api.example.com/online_ordering/api
```
- 方案B（同域同上下文，前端经反向代理挂载到同域）：
```env
VITE_BACKEND_BASE=/online_ordering
VITE_API_BASE=/online_ordering/api
```

> 构建时（`npm run build`），仅 `VITE_` 前缀变量参与替换。`VITE_BACKEND_TARGET/VITE_BACKEND_CONTEXT/VITE_DEV_PORT` 为开发期变量，不影响生产包。

---

## 2. 后端（Backend）

后端通过 `web.xml` 提供跨域白名单与前端默认重定向地址。

### 2.1 `WEB-INF/web.xml` 关键参数
```xml
<context-param>
    <param-name>consumerDefaultRedirect</param-name>
    <param-value>http://localhost:5173/</param-value>
</context-param>
<context-param>
    <param-name>corsAllowedOrigin</param-name>
    <param-value>http://localhost:5173</param-value>
</context-param>
```

- `consumerDefaultRedirect`：登录成功后消费者默认回跳的前端根地址（`LoginServlet` 读取，参见 `backend/src/main/java/com/platform/ordering/controller/LoginServlet.java:27, 69-70`）。
- `corsAllowedOrigin`：允许跨域的来源（`CorsFilter` 读取，参见 `backend/src/main/java/com/platform/ordering/filter/CorsFilter.java:14, 22-28`）。

### 2.2 开发/生产取值
- 开发环境：
  - `consumerDefaultRedirect=http://localhost:5173/`
  - `corsAllowedOrigin=http://localhost:5173`
- 生产环境：
  - `consumerDefaultRedirect=https://app.example.com/`
  - `corsAllowedOrigin=https://app.example.com`

---

## 3. 启动与校验

### 3.1 开发环境步骤
- 在 IDE 中启动后端至 `http://localhost:8080/online_ordering_backend_war_exploded`。
- 启动前端开发服务至 `http://localhost:5173/`。
- 访问前端受保护页面，未登录会被重定向至 `${VITE_BACKEND_BASE}/login.jsp?redirect=...`（路由守卫），登录成功后根据 `redirect` 或 `consumerDefaultRedirect` 返回前端。

### 3.2 生产环境切换
- 修改两处：
  - 后端 `web.xml`：`consumerDefaultRedirect`、`corsAllowedOrigin` 指向前端实际域。
  - 前端 `.env`：`VITE_BACKEND_BASE` 与 `VITE_API_BASE` 指向后端域（或同域相对路径）。
- 若采用同域部署（反向代理），`corsAllowedOrigin` 可与后端同域，甚至不再需要跨域配置。

### 3.3 校验清单
- 前端 `src` 中无硬编码 `http://localhost`（已统一从 `.env` 读取）。
- 后端仅通过 `web.xml` 暴露外部地址参数；过滤器/控制器无域名硬编码。
- 登录后 API 请求携带会话，跨域时 `CorsFilter` 正确返回 `Access-Control-Allow-Origin` 与 `Access-Control-Allow-Credentials`。

---

## 4. 常见问题与排查

- 后端上下文名不一致：确保 `.env` 的 `VITE_BACKEND_CONTEXT/VITE_BACKEND_BASE` 与 IDE 部署的实际上下文一致。
- 跨域 Cookie 未携带：确保前端与后端域名/端口与 `corsAllowedOrigin` 完全匹配；登录后由服务端设置会话 Cookie，并允许跨域凭据（`withCredentials: true`）。
- 重定向循环：`LoginServlet` 仅允许回跳到 `consumerDefaultRedirect` 前缀或本应用内路径，避免开放重定向；前端路由守卫的未登录跳转应指向 `${VITE_BACKEND_BASE}/login.jsp`。