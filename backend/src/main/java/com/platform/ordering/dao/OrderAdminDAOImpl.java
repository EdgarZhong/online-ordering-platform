package com.platform.ordering.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.platform.ordering.model.Order;
import com.platform.ordering.model.OrderItem;
import com.platform.ordering.util.DBUtil;

/**
 * 商家端订单管理DAO实现
 * 说明：所有查询与更新均以 restaurant_id 为强制过滤条件，确保多租户数据隔离。
 */
public class OrderAdminDAOImpl implements OrderAdminDAO {

    @Override
    public List<Order> listOrders(int restaurantId, String status, Timestamp from, Timestamp to, String keyword, int page, int size) throws Exception {
        List<Order> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sb = new StringBuilder();
            String sql_list = "SELECT order_id, user_id, restaurant_id, total_price, status, order_time, " +
                    "(SELECT COUNT(1) FROM orders o2 WHERE o2.restaurant_id = orders.restaurant_id AND DATE(o2.order_time) = DATE(orders.order_time) " +
                    "AND (o2.order_time < orders.order_time OR (o2.order_time = orders.order_time AND o2.order_id <= orders.order_id))) AS serial_number " +
                    "FROM orders WHERE restaurant_id = ?";
            sb.append(sql_list);
            if (status != null && !status.isEmpty()) {
                sb.append(" AND status = ?");
            }
            if (from != null) {
                sb.append(" AND order_time >= ?");
            }
            if (to != null) {
                sb.append(" AND order_time <= ?");
            }
            if (keyword != null && !keyword.isEmpty()) {
                sb.append(" AND (CAST(order_id AS TEXT) LIKE ? OR EXISTS (SELECT 1 FROM users u WHERE u.user_id = orders.user_id AND u.username ILIKE ?))");
            }
            sb.append(" ORDER BY order_time DESC LIMIT ? OFFSET ?");

            ps = conn.prepareStatement(sb.toString());
            int idx = 1;
            ps.setInt(idx++, restaurantId);
            if (status != null && !status.isEmpty()) {
                ps.setString(idx++, status);
            }
            if (from != null) {
                ps.setTimestamp(idx++, from);
            }
            if (to != null) {
                ps.setTimestamp(idx++, to);
            }
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            ps.setInt(idx++, size);
            ps.setInt(idx, Math.max(0, (page - 1) * size));

            rs = ps.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setUserId(rs.getInt("user_id"));
                o.setRestaurantId(rs.getInt("restaurant_id"));
                o.setTotalPrice(rs.getBigDecimal("total_price"));
                o.setStatus(rs.getString("status"));
                o.setCreatedAt(rs.getTimestamp("order_time"));
                o.setSerialNumber(rs.getInt("serial_number"));
                list.add(o);
            }
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    @Override
    public int countOrders(int restaurantId, String status, Timestamp from, Timestamp to, String keyword) throws Exception {
        int total = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sb = new StringBuilder();
            String sql_count = "SELECT COUNT(1) AS cnt FROM orders WHERE restaurant_id = ?";
            sb.append(sql_count);
            if (status != null && !status.isEmpty()) {
                sb.append(" AND status = ?");
            }
            if (from != null) {
                sb.append(" AND order_time >= ?");
            }
            if (to != null) {
                sb.append(" AND order_time <= ?");
            }
            if (keyword != null && !keyword.isEmpty()) {
                sb.append(" AND (CAST(order_id AS TEXT) LIKE ? OR EXISTS (SELECT 1 FROM users u WHERE u.user_id = orders.user_id AND u.username ILIKE ?))");
            }
            ps = conn.prepareStatement(sb.toString());
            int idx = 1;
            ps.setInt(idx++, restaurantId);
            if (status != null && !status.isEmpty()) {
                ps.setString(idx++, status);
            }
            if (from != null) {
                ps.setTimestamp(idx++, from);
            }
            if (to != null) {
                ps.setTimestamp(idx++, to);
            }
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("cnt");
            }
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return total;
    }

    @Override
    public Order getOrderDetail(int orderId, int restaurantId) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Order o = null;
        try {
            conn = DBUtil.getConnection();
            String sql_order = "SELECT order_id, user_id, restaurant_id, total_price, status, order_time, " +
                    "(SELECT COUNT(1) FROM orders o2 WHERE o2.restaurant_id = orders.restaurant_id AND DATE(o2.order_time) = DATE(orders.order_time) " +
                    "AND (o2.order_time < orders.order_time OR (o2.order_time = orders.order_time AND o2.order_id <= orders.order_id))) AS serial_number " +
                    "FROM orders WHERE order_id = ? AND restaurant_id = ?";
            ps = conn.prepareStatement(sql_order);
            ps.setInt(1, orderId);
            ps.setInt(2, restaurantId);
            rs = ps.executeQuery();
            if (rs.next()) {
                o = new Order();
                o.setOrderId(rs.getInt("order_id"));
                o.setUserId(rs.getInt("user_id"));
                o.setRestaurantId(rs.getInt("restaurant_id"));
                o.setTotalPrice(rs.getBigDecimal("total_price"));
                o.setStatus(rs.getString("status"));
                o.setCreatedAt(rs.getTimestamp("order_time"));
                o.setSerialNumber(rs.getInt("serial_number"));
            }
            DBUtil.close(null, ps, rs);
            if (o == null) {
                return null;
            }
            String sql_items = "SELECT oi.item_id, oi.order_id, oi.menu_id, oi.dish_id, oi.quantity, oi.unit_price, d.name AS dish_name, m.name AS menu_name, COALESCE(m.is_package, FALSE) AS is_package FROM order_items oi LEFT JOIN dishes d ON oi.dish_id = d.dish_id LEFT JOIN menus m ON oi.menu_id = m.menu_id WHERE oi.order_id = ? ORDER BY oi.item_id";
            ps = conn.prepareStatement(sql_items);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            List<OrderItem> items = new ArrayList<>();
            while (rs.next()) {
                OrderItem it = new OrderItem();
                it.setItemId(rs.getInt("item_id"));
                it.setOrderId(rs.getInt("order_id"));
                it.setMenuId(rs.getInt("menu_id"));
                it.setDishId(rs.getInt("dish_id"));
                it.setQuantity(rs.getInt("quantity"));
                it.setUnitPrice(rs.getBigDecimal("unit_price"));
                it.setDishName(rs.getString("dish_name"));
                it.setMenuName(rs.getString("menu_name"));
                it.setIsPackage(rs.getBoolean("is_package"));
                items.add(it);
            }
            o.setItems(items);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return o;
    }

    @Override
    public boolean updateOrderStatus(int orderId, int restaurantId, String newStatus, int updatedByUserId, String reason) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 读取当前状态
            String sql_current = "SELECT status FROM orders WHERE order_id = ? AND restaurant_id = ?";
            ps = conn.prepareStatement(sql_current);
            ps.setInt(1, orderId);
            ps.setInt(2, restaurantId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            }
            String current = rs.getString("status");
            DBUtil.close(null, ps, rs);

            // 简化的合法流转：PENDING->PROCESSING->COMPLETED；任意阶段可->CANCELLED；完成/取消不可再变更
            boolean allowed = false;
            if ("CANCELLED".equals(newStatus)) {
                allowed = !"COMPLETED".equals(current) && !"CANCELLED".equals(current);
            } else if ("PROCESSING".equals(newStatus)) {
                allowed = "PENDING".equals(current);
            } else if ("COMPLETED".equals(newStatus)) {
                allowed = "PROCESSING".equals(current);
            }
            if (!allowed) {
                return false;
            }

            String sql_update = "UPDATE orders SET status = ?, order_time = order_time WHERE order_id = ? AND restaurant_id = ?";
            ps = conn.prepareStatement(sql_update);
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            ps.setInt(3, restaurantId);
            int n = ps.executeUpdate();
            return n > 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }
}