package com.platform.ordering.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.platform.ordering.util.DBUtil;

@WebServlet(name = "DashboardServlet", urlPatterns = { "/admin/dashboard" })
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object ridObj = req.getAttribute("restaurantId");
        if (ridObj == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        int restaurantId = (Integer) ridObj;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            LocalDate today = LocalDate.now();
            Timestamp start = Timestamp.valueOf(today.atStartOfDay());
            Timestamp end = Timestamp.valueOf(today.plusDays(1).atStartOfDay());

            String sql_ordersToday = "SELECT COUNT(1) AS cnt FROM orders WHERE restaurant_id=? AND order_time>=? AND order_time<?";
            ps = conn.prepareStatement(sql_ordersToday);
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, start);
            ps.setTimestamp(3, end);
            rs = ps.executeQuery();
            int ordersTodayCount = 0;
            if (rs.next()) ordersTodayCount = rs.getInt("cnt");
            DBUtil.close(null, ps, rs);

            String sql_revenueToday = "SELECT COALESCE(SUM(total_price),0) AS amt FROM orders WHERE restaurant_id=? AND status='COMPLETED' AND order_time>=? AND order_time<?";
            ps = conn.prepareStatement(sql_revenueToday);
            ps.setInt(1, restaurantId);
            ps.setTimestamp(2, start);
            ps.setTimestamp(3, end);
            rs = ps.executeQuery();
            BigDecimal revenueToday = BigDecimal.ZERO;
            if (rs.next()) revenueToday = rs.getBigDecimal("amt");
            DBUtil.close(null, ps, rs);

            String sql_pendingCnt = "SELECT COUNT(1) AS cnt FROM orders WHERE restaurant_id=? AND status='PENDING'";
            ps = conn.prepareStatement(sql_pendingCnt);
            ps.setInt(1, restaurantId);
            rs = ps.executeQuery();
            int pendingCount = 0;
            if (rs.next()) pendingCount = rs.getInt("cnt");
            DBUtil.close(null, ps, rs);

            String sql_processingCnt = "SELECT COUNT(1) AS cnt FROM orders WHERE restaurant_id=? AND status='PROCESSING'";
            ps = conn.prepareStatement(sql_processingCnt);
            ps.setInt(1, restaurantId);
            rs = ps.executeQuery();
            int processingCount = 0;
            if (rs.next()) processingCount = rs.getInt("cnt");

            req.setAttribute("ordersTodayCount", ordersTodayCount);
            req.setAttribute("revenueToday", revenueToday);
            req.setAttribute("pendingCount", pendingCount);
            req.setAttribute("processingCount", processingCount);
            req.setAttribute("todayStr", today.toString());
            req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("仪表盘数据加载失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
}