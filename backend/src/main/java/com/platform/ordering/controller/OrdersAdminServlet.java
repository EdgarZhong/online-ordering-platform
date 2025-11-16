package com.platform.ordering.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.platform.ordering.dao.OrderAdminDAO;
import com.platform.ordering.dao.OrderAdminDAOImpl;
import com.platform.ordering.model.Order;
import com.platform.ordering.model.User;
import com.platform.ordering.util.KitchenEventBus;

/**
 * 商家端订单管理控制器
 * 路由：/admin/orders/*
 * 支持：GET 列表与详情；POST 状态更新
 * 注意：严格依赖 AuthFilter 注入的 requestScope.restaurantId 进行数据隔离
 */
@WebServlet(name = "OrdersAdminServlet", urlPatterns = { "/admin/orders", "/admin/orders/*" })
public class OrdersAdminServlet extends HttpServlet {
    private final OrderAdminDAO orderAdminDAO = new OrderAdminDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Object ridObj = req.getAttribute("restaurantId");
        if (ridObj == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        int restaurantId = (Integer) ridObj;
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            handleList(req, resp, restaurantId);
        } else {
            // 详情：/admin/orders/{id}
            String[] parts = path.split("/");
            if (parts.length >= 2) {
                try {
                    int orderId = Integer.parseInt(parts[1]);
                    handleDetail(req, resp, restaurantId, orderId);
                } catch (NumberFormatException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "非法订单编号");
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp, int restaurantId) throws ServletException, IOException {
        String status = param(req, "status");
        String fromStr = param(req, "from");
        String toStr = param(req, "to");
        String keyword = param(req, "keyword");
        int page = intParam(req, "page", 1);
        int size = intParam(req, "size", 10);

        Timestamp from = null;
        Timestamp to = null;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (fromStr != null && !fromStr.isEmpty()) {
                from = Timestamp.valueOf(LocalDate.parse(fromStr, df).atStartOfDay());
            }
            if (toStr != null && !toStr.isEmpty()) {
                to = Timestamp.valueOf(LocalDate.parse(toStr, df).plusDays(1).atStartOfDay());
            }
        } catch (Exception ignored) {
        }

        try {
            List<Order> orders = orderAdminDAO.listOrders(restaurantId, status, from, to, keyword, page, size);
            int total = orderAdminDAO.countOrders(restaurantId, status, from, to, keyword);
            int pages = Math.max(1, (total + size - 1) / size);
            req.setAttribute("orders", orders);
            req.setAttribute("total", total);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("pages", pages);
            req.setAttribute("status", status);
            req.setAttribute("from", fromStr);
            req.setAttribute("to", toStr);
            req.setAttribute("keyword", keyword);
            req.getRequestDispatcher("/admin/orders.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("订单列表查询失败", e);
        }
    }

    private void handleDetail(HttpServletRequest req, HttpServletResponse resp, int restaurantId, int orderId) throws ServletException, IOException {
        try {
            Order order = orderAdminDAO.getOrderDetail(orderId, restaurantId);
            if (order == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "订单不存在或不属于当前餐厅");
                return;
            }
            java.util.LinkedHashMap<String, java.util.List<com.platform.ordering.model.OrderItem>> itemGroups = new java.util.LinkedHashMap<>();
            java.util.LinkedHashMap<String, java.math.BigDecimal> groupTotals = new java.util.LinkedHashMap<>();
            java.util.LinkedHashMap<String, java.lang.Boolean> groupIsPackage = new java.util.LinkedHashMap<>();
            if (order.getItems() != null) {
                for (com.platform.ordering.model.OrderItem it : order.getItems()) {
                    String key = it.getMenuName() == null ? "未分组" : it.getMenuName();
                    itemGroups.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(it);
                    java.math.BigDecimal subtotal = it.getUnitPrice().multiply(new java.math.BigDecimal(it.getQuantity()));
                    groupTotals.put(key, groupTotals.getOrDefault(key, java.math.BigDecimal.ZERO).add(subtotal));
                    if (!groupIsPackage.containsKey(key)) {
                        groupIsPackage.put(key, it.isPackage());
                    }
                }
            }
            req.setAttribute("order", order);
            req.setAttribute("itemGroups", itemGroups);
            req.setAttribute("groupTotals", groupTotals);
            req.setAttribute("groupIsPackage", groupIsPackage);
            req.getRequestDispatcher("/admin/order-detail.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("订单详情查询失败", e);
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
                resp.sendRedirect(req.getContextPath() + "/admin/orders/" + orderId);
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
}