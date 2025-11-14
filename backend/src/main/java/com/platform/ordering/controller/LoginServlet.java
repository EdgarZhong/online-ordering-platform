package com.platform.ordering.controller;

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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = userDAO.findByUsername(username);

            // 验证用户是否存在，以及密码是否匹配
            if (user != null && user.getPassword().equals(password)) {
                // 登录成功
                // 1. 创建Session
                HttpSession session = req.getSession();
                // 2. 将完整的用户信息存入Session，以便后续使用
                session.setAttribute("user", user);

                // 3. 根据角色重定向到不同页面
                if ("merchant".equals(user.getRole()) || "superadmin".equals(user.getRole())) {
                    // 商户或超级管理员跳转到后台仪表盘
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard.jsp");
                } else {
                    // 消费者跳转到前端首页 (此处暂定为项目根路径)
                    resp.sendRedirect(req.getContextPath() + "/");
                }
            } else {
                // 登录失败，重定向回登录页并附带错误提示
                resp.sendRedirect(req.getContextPath() + "/login.jsp?error=invalidCredentials");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // 数据库异常，同样视为登录失败
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=invalidCredentials");
        }
    }
}