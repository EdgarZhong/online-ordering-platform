package com.platform.ordering.controller;

import com.platform.ordering.dao.RestaurantDAO;
import com.platform.ordering.dao.RestaurantDAOImpl;
import com.platform.ordering.dao.UserDAO;
import com.platform.ordering.dao.UserDAOImpl;
import com.platform.ordering.model.Restaurant;
import com.platform.ordering.model.User;
import com.platform.ordering.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/register")
public class RegistrationServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAOImpl();
    private RestaurantDAO restaurantDAO = new RestaurantDAOImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = req.getParameter("role");

        if ("customer".equals(role)) {
            handleCustomerRegistration(req, resp);
        } else if ("merchant".equals(role)) {
            handleMerchantRegistration(req, resp);
        } else {
            // 如果角色未知，则重定向到错误页面
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=invalidRole");
        }
    }

    private void handleCustomerRegistration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User customer = new User();
            customer.setUsername(req.getParameter("username"));
            customer.setPassword(req.getParameter("password"));
            customer.setPhone(req.getParameter("phone"));
            customer.setRole("customer");
            userDAO.save(customer); // 使用独立的非事务性保存
            resp.sendRedirect(req.getContextPath() + "/login.jsp?registration=success");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=registrationFailed");
        }
    }

    private void handleMerchantRegistration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Connection conn = null;
        try {
            // --- 开启事务 ---
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 1. 关闭自动提交

            // 2. 创建餐厅
            Restaurant restaurant = new Restaurant();
            restaurant.setName(req.getParameter("restaurantName"));
            restaurant.setAddress(req.getParameter("address"));
            restaurant.setPhone(req.getParameter("restaurantPhone"));
            restaurant.setDescription(req.getParameter("description"));
            Restaurant savedRestaurant = restaurantDAO.save(restaurant, conn); // 在事务连接上执行

            if (savedRestaurant == null) {
                throw new SQLException("Failed to create restaurant record, rolling back.");
            }

            // 3. 创建用户
            User merchant = new User();
            merchant.setUsername(req.getParameter("username"));
            merchant.setPassword(req.getParameter("password"));
            merchant.setPhone(req.getParameter("phone"));
            merchant.setRole("merchant");
            merchant.setRestaurantId(savedRestaurant.getRestaurantId());
            userDAO.save(merchant, conn); // 在同一个事务连接上执行

            // --- 提交事务 ---
            conn.commit(); // 4. 所有操作成功，提交

            resp.sendRedirect(req.getContextPath() + "/login.jsp?registration=success");

        } catch (SQLException e) {
            e.printStackTrace();
            // --- 回滚事务 ---
            if (conn != null) {
                try {
                    conn.rollback(); // 5. 发生任何SQL异常，回滚
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            resp.sendRedirect(req.getContextPath() + "/login.jsp?error=registrationFailed");
        } finally {
            // --- 清理资源 ---
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复连接的默认状态
                    conn.close(); // 关闭连接
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}