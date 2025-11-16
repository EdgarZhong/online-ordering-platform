package com.platform.ordering.controller;

import com.google.gson.Gson;
import com.platform.ordering.dao.UserDAO;
import com.platform.ordering.dao.UserDAOImpl;
import com.platform.ordering.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = { "/api/auth/login", "/api/account/password" })
public class ApiAuthServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAOImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/api/account/password".equals(servletPath)) {
            handleChangePassword(req, resp);
            return;
        }
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = userDAO.findByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                // 登录成功
                HttpSession session = req.getSession();
                session.setAttribute("user", user);

                // 重要：出于安全考虑，从返回给前端的JSON中移除密码
                user.setPassword(null);

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(user));

            } else {
                // 登录失败
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户名或密码错误");
                resp.getWriter().write(gson.toJson(error));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // 数据库异常
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "服务器内部错误，请稍后再试");
            resp.getWriter().write(gson.toJson(error));
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }
        Object obj = session.getAttribute("user");
        if (!(obj instanceof User)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }
        User user = (User) obj;
        if (!"customer".equals(user.getRole())) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Forbidden\"}");
            return;
        }
        String body = req.getReader().lines().collect(java.util.stream.Collectors.joining());
        java.util.Map<?,?> payload = null;
        try {
            payload = gson.fromJson(body, java.util.Map.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid JSON\"}");
            return;
        }
        String oldPassword = payload == null ? null : String.valueOf(payload.get("oldPassword"));
        String newPassword = payload == null ? null : String.valueOf(payload.get("newPassword"));
        if (oldPassword == null || newPassword == null || oldPassword.isEmpty() || newPassword.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing oldPassword/newPassword\"}");
            return;
        }
        if (!String.valueOf(user.getPassword()).equals(oldPassword)) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("{\"error\":\"Old password mismatch\"}");
            return;
        }
        try {
            int n = userDAO.updatePassword(user.getUserId(), newPassword);
            if (n <= 0) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\":\"Update failed\"}");
                return;
            }
            user.setPassword(newPassword);
            session.setAttribute("user", user);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"success\":true}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }
}