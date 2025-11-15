package com.platform.ordering.api;

import com.platform.ordering.model.Order;
import com.platform.ordering.model.OrderItem;
import com.platform.ordering.model.User;
import com.platform.ordering.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

@WebServlet(name = "OrdersResourceApiServlet", urlPatterns = "/api/orders/*")
public class OrdersResourceApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Bad request\"}");
            return;
        }

        User user = (User) req.getSession().getAttribute("user");
        if (user == null || user.getUserId() <= 0) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        String[] parts = path.split("/");
        if (parts.length != 2) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\":\"Not found\"}");
            return;
        }
        try {
            int orderId = Integer.parseInt(parts[1]);
            Order order = fetchOrderDetail(orderId, user.getUserId());
            if (order == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"Order not found or permission denied\"}");
                return;
            }
            String restaurantName = fetchRestaurantName(order.getRestaurantId());
            StringBuilder sb = new StringBuilder();
            sb.append('{')
                    .append("\"orderId\":").append(order.getOrderId()).append(',')
                    .append("\"restaurantName\":\"").append(escape(restaurantName)).append('\"').append(',')
                    .append("\"status\":\"").append(escape(order.getStatus())).append('\"').append(',')
                    .append("\"totalPrice\":").append(order.getTotalPrice()).append(',')
                    .append("\"createdAt\":\"").append(order.getCreatedAt() == null ? "" : order.getCreatedAt().toString()).append('\"').append(',')
                    .append("\"items\":");
            sb.append('[');
            List<OrderItem> items = order.getItems();
            for (int i = 0; i < items.size(); i++) {
                OrderItem it = items.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                        .append("\"dishName\":\"").append(escape(it.getDishName())).append('\"').append(',')
                        .append("\"quantity\":").append(it.getQuantity()).append(',')
                        .append("\"unitPrice\":").append(it.getUnitPrice())
                        .append('}');
            }
            sb.append(']');
            sb.append('}');
            out.write(sb.toString());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid order id\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Internal server error\"}");
        }
    }

    private String fetchRestaurantName(int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement("SELECT name FROM restaurants WHERE restaurant_id = ?");
            ps.setInt(1, restaurantId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
            return "";
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        PrintWriter out = resp.getWriter();
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || user.getUserId() <= 0) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }

        String body = req.getReader().lines().collect(Collectors.joining());
        Gson gson = new Gson();
        OrderReq orderReq;
        try {
            orderReq = gson.fromJson(body, OrderReq.class);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid JSON\"}");
            return;
        }
        if (orderReq == null || orderReq.restaurantId == null || orderReq.items == null || orderReq.items.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Missing restaurantId or items\"}");
            return;
        }
        int restaurantId = orderReq.restaurantId;
        for (OrderItemReq it : orderReq.items) {
            if (it == null || it.dishId == null || it.quantity == null || it.quantity <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid item\"}");
                return;
            }
        }

        Connection conn = null;
        PreparedStatement priceStmt = null;
        PreparedStatement insertOrderStmt = null;
        PreparedStatement insertItemStmt = null;
        PreparedStatement updateTotalStmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            java.util.Map<Integer, java.math.BigDecimal> unitPriceMap = new java.util.HashMap<>();
            priceStmt = conn.prepareStatement(
                    "SELECT MIN(mi.price) AS price FROM menu_items mi " +
                            "JOIN menus m ON mi.menu_id = m.menu_id " +
                            "WHERE m.restaurant_id = ? AND mi.dish_id = ?");
            for (OrderItemReq it : orderReq.items) {
                priceStmt.setInt(1, restaurantId);
                priceStmt.setInt(2, it.dishId);
                rs = priceStmt.executeQuery();
                if (!rs.next() || rs.getBigDecimal(1) == null) {
                    conn.rollback();
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Invalid items in the order. All items must belong to the same restaurant and be available.\"}");
                    return;
                }
                java.math.BigDecimal unitPrice = rs.getBigDecimal(1);
                unitPriceMap.put(it.dishId, unitPrice);
                total = total.add(unitPrice.multiply(new java.math.BigDecimal(it.quantity)));
                if (rs != null) { try { rs.close(); } catch (SQLException ignored) {} }
            }

            insertOrderStmt = conn.prepareStatement(
                    "INSERT INTO orders (user_id, restaurant_id, total_price, status, order_time) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING order_id");
            insertOrderStmt.setInt(1, user.getUserId());
            insertOrderStmt.setInt(2, restaurantId);
            insertOrderStmt.setBigDecimal(3, total);
            insertOrderStmt.setString(4, "PENDING");
            rs = insertOrderStmt.executeQuery();
            if (!rs.next()) { conn.rollback(); throw new SQLException("Order id not returned"); }
            int orderId = rs.getInt(1);
            if (rs != null) { try { rs.close(); } catch (SQLException ignored) {} }

            insertItemStmt = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, dish_id, quantity, unit_price) VALUES (?, ?, ?, ?)");
            for (OrderItemReq it : orderReq.items) {
                insertItemStmt.setInt(1, orderId);
                insertItemStmt.setInt(2, it.dishId);
                insertItemStmt.setInt(3, it.quantity);
                insertItemStmt.setBigDecimal(4, unitPriceMap.get(it.dishId));
                insertItemStmt.addBatch();
            }
            insertItemStmt.executeBatch();

            conn.commit();

            StringBuilder sb = new StringBuilder();
            sb.append('{')
                    .append("\"orderId\":").append(orderId).append(',')
                    .append("\"status\":\"PENDING\",")
                    .append("\"totalPrice\":").append(total).append(',')
                    .append("\"createdAt\":\"").append(new java.sql.Timestamp(System.currentTimeMillis()).toString()).append('\"')
                    .append('}');
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.write(sb.toString());
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Internal server error\"}");
        } finally {
            DBUtil.close(null, updateTotalStmt, null);
            DBUtil.close(null, insertItemStmt, null);
            DBUtil.close(null, insertOrderStmt, null);
            DBUtil.close(conn, priceStmt, null);
        }
    }

    static class OrderReq {
        @SerializedName("restaurantId") Integer restaurantId;
        @SerializedName("items") java.util.List<OrderItemReq> items;
    }
    static class OrderItemReq {
        @SerializedName("dishId") Integer dishId;
        @SerializedName("quantity") Integer quantity;
    }

    private Order fetchOrderDetail(int orderId, int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ostmt = null;
        PreparedStatement istmt = null;
        ResultSet ors = null;
        ResultSet irs = null;
        try {
            conn = DBUtil.getConnection();
            ostmt = conn.prepareStatement("SELECT order_id, user_id, restaurant_id, total_price, status, order_time FROM orders WHERE order_id = ? AND user_id = ?");
            ostmt.setInt(1, orderId);
            ostmt.setInt(2, userId);
            ors = ostmt.executeQuery();
            if (!ors.next()) return null;
            Order order = new Order();
            order.setOrderId(ors.getInt("order_id"));
            order.setUserId(ors.getInt("user_id"));
            order.setRestaurantId(ors.getInt("restaurant_id"));
            order.setTotalPrice(ors.getBigDecimal("total_price"));
            order.setStatus(ors.getString("status"));
            try { order.setCreatedAt(ors.getTimestamp("order_time")); } catch (Exception ignored) {}

            istmt = conn.prepareStatement("SELECT oi.item_id, oi.order_id, oi.dish_id, oi.quantity, oi.unit_price, d.name AS dish_name FROM order_items oi JOIN dishes d ON oi.dish_id = d.dish_id WHERE oi.order_id = ? ORDER BY oi.item_id");
            istmt.setInt(1, orderId);
            irs = istmt.executeQuery();
            List<OrderItem> items = new ArrayList<>();
            while (irs.next()) {
                OrderItem it = new OrderItem();
                it.setItemId(irs.getInt("item_id"));
                it.setOrderId(irs.getInt("order_id"));
                it.setDishId(irs.getInt("dish_id"));
                it.setQuantity(irs.getInt("quantity"));
                it.setUnitPrice(irs.getBigDecimal("unit_price"));
                it.setDishName(irs.getString("dish_name"));
                items.add(it);
            }
            order.setItems(items);
            return order;
        } finally {
            if (irs != null) try { irs.close(); } catch (SQLException ignored) {}
            if (istmt != null) try { istmt.close(); } catch (SQLException ignored) {}
            DBUtil.close(conn, ostmt, ors);
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = hex.length(); j < 4; j++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}