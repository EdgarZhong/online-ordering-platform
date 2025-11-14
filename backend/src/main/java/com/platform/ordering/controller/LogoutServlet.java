package com.platform.ordering.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // 获取现有session，不创建新的
        if (session != null) {
            session.invalidate(); // 销毁session
        }
        // 重定向到登录页面，并附带退出成功提示
        resp.sendRedirect(req.getContextPath() + "/login.jsp?logout=success");
    }
}