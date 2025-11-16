package com.platform.ordering.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.time.LocalDate;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.platform.ordering.dao.OrderAdminDAO;
import com.platform.ordering.dao.OrderAdminDAOImpl;
import com.platform.ordering.util.KitchenEventBus;
import com.platform.ordering.model.Order;
import com.platform.ordering.model.User;

@WebServlet(name = "KitchenBoardServlet", urlPatterns = { "/admin/kitchen", "/admin/kitchen/*" })
public class KitchenBoardServlet extends HttpServlet {
    private final OrderAdminDAO orderAdminDAO = new OrderAdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object ridObj = req.getAttribute("restaurantId");
        if (ridObj == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        int restaurantId = (Integer) ridObj;
        try {
            List<Order> pending = orderAdminDAO.listOrders(restaurantId, "PENDING", null, null, null, 1, 50);
            List<Order> processing = orderAdminDAO.listOrders(restaurantId, "PROCESSING", null, null, null, 1, 50);
            Collections.reverse(pending);
            Collections.reverse(processing);
            req.setAttribute("pendingGroups", groupByDate(pending));
            req.setAttribute("processingGroups", groupByDate(processing));
            req.getRequestDispatcher("/admin/kitchen.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("厨房面板加载失败", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object ridObj = req.getAttribute("restaurantId");
        if (ridObj == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        int restaurantId = (Integer) ridObj;
        String action = param(req, "action");
        if ("updateStatus".equals(action)) {
            int orderId = intParam(req, "orderId", -1);
            String newStatus = param(req, "newStatus");
            String reason = param(req, "reason");
            HttpSession session = req.getSession(false);
            int updatedBy = 0;
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    updatedBy = user.getUserId();
                }
            }
            if (orderId <= 0 || newStatus == null || newStatus.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少必要参数");
                return;
            }
            try {
                boolean ok = orderAdminDAO.updateOrderStatus(orderId, restaurantId, newStatus, updatedBy, reason);
                if (!ok) {
                    resp.sendError(HttpServletResponse.SC_CONFLICT, "状态流转不合法或订单不可更新");
                    return;
                }
                try { KitchenEventBus.get().publishOrderUpdated(restaurantId, orderId, newStatus); } catch (Exception ignored) {}
                resp.sendRedirect(req.getContextPath() + "/admin/kitchen");
            } catch (Exception e) {
                throw new ServletException("订单状态更新失败", e);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "未知操作");
        }
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    private int intParam(HttpServletRequest req, String name, int def) {
        try {
            String v = req.getParameter(name);
            if (v == null || v.trim().isEmpty()) return def;
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private Map<String, List<Order>> groupByDate(List<Order> orders) {
        LinkedHashMap<String, List<Order>> groups = new LinkedHashMap<>();
        for (Order o : orders) {
            LocalDate d = o.getCreatedAt().toLocalDateTime().toLocalDate();
            String key = d.toString();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(o);
        }
        return groups;
    }
}