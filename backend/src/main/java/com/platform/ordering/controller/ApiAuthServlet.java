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

@WebServlet("/api/auth/login")
public class ApiAuthServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAOImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
}