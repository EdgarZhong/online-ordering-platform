package com.platform.ordering.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.platform.ordering.model.Order;
import com.platform.ordering.model.OrderItem;
import com.platform.ordering.model.User;
import com.platform.ordering.util.DBUtil;
import com.platform.ordering.util.KitchenEventBus;

@WebServlet(name = "OrdersResourceApiServlet", urlPatterns = "/api/orders/*")
public class OrdersResourceApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        PrintWriter out = resp.getWriter();
        String path = req.getPathInfo();
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || user.getUserId() <= 0) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("{\"error\":\"Unauthorized\"}");
            return;
        }
        if (path == null || path.equals("/")) {
            try {
                int page = 0;
                int size = 20;
                String status = req.getParameter("status");
                String from = req.getParameter("from");
                String to = req.getParameter("to");
                try { page = Integer.parseInt(req.getParameter("page")); } catch (Exception ignored) {}
                try { size = Integer.parseInt(req.getParameter("size")); } catch (Exception ignored) {}
                if (size <= 0) size = 20;
                if (page < 0) page = 0;
                java.util.List<Order> orders = fetchOrdersForUserPaged(user.getUserId(), page, size, status, from, to);
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                for (int oi = 0; oi < orders.size(); oi++) {
                    Order o = orders.get(oi);
                    if (oi > 0) sb.append(',');
                    String restaurantName = fetchRestaurantName(o.getRestaurantId());
                    sb.append('{')
                            .append("\"restaurantId\":").append(o.getRestaurantId()).append(',')
                            .append("\"orderId\":").append(o.getOrderId()).append(',')
                            .append("\"restaurantName\":\"").append(escape(restaurantName)).append('\"').append(',')
                            .append("\"serialNumber\":").append(o.getSerialNumber()).append(',')
                            .append("\"status\":\"").append(escape(o.getStatus())).append('\"').append(',')
                            .append("\"totalPrice\":").append(o.getTotalPrice()).append(',')
                            .append("\"createdAt\":\"").append(o.getCreatedAt() == null ? "" : o.getCreatedAt().toString()).append('\"').append(',')
                            .append("\"items\":");
                    sb.append('[');
                    java.util.List<OrderItem> items = o.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        OrderItem it = items.get(i);
                        if (i > 0) sb.append(',');
                        sb.append('{')
                                .append("\"dishId\":").append(it.getDishId()).append(',')
                                .append("\"dishName\":\"").append(escape(it.getDishName())).append('\"').append(',')
                                .append("\"menuId\":").append(it.getMenuId()).append(',')
                                .append("\"menuName\":\"").append(escape(it.getMenuName())).append('\"').append(',')
                                .append("\"quantity\":").append(it.getQuantity()).append(',')
                                .append("\"unitPrice\":").append(it.getUnitPrice())
                                .append('}');
                    }
                    sb.append(']');
                    sb.append('}');
                }
                sb.append(']');
                resp.setHeader("X-Page", String.valueOf(page));
                resp.setHeader("X-Size", String.valueOf(size));
                out.write(sb.toString());
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\":\"Internal server error\"}");
            }
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
            java.util.Map<Integer, java.util.List<OrderItem>> byMenu = new java.util.HashMap<>();
            for (OrderItem it : order.getItems()) {
                byMenu.computeIfAbsent(it.getMenuId(), k -> new java.util.ArrayList<>()).add(it);
            }
            java.util.Map<Integer, Boolean> menuPackageMap = new java.util.HashMap<>();
            java.util.Map<Integer, String> menuNameMap = new java.util.HashMap<>();
            java.util.Map<String, Integer> defaultQtyMap = new java.util.HashMap<>();
            Connection conn2 = null; PreparedStatement ps2 = null; ResultSet rs2 = null;
            try {
                conn2 = DBUtil.getConnection();
                if (!byMenu.isEmpty()) {
                    StringBuilder in = new StringBuilder();
                    java.util.List<Integer> mids = new java.util.ArrayList<>(byMenu.keySet());
                    for (int i = 0; i < mids.size(); i++) { if (i > 0) in.append(','); in.append('?'); }
                    ps2 = conn2.prepareStatement("SELECT menu_id, name, is_package FROM menus WHERE menu_id IN (" + in + ")");
                    for (int i = 0; i < mids.size(); i++) ps2.setInt(i + 1, mids.get(i));
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        int mid = rs2.getInt(1);
                        menuNameMap.put(mid, rs2.getString(2));
                        try { menuPackageMap.put(mid, rs2.getBoolean(3)); } catch (Exception ignored) { menuPackageMap.put(mid, false); }
                    }
                    if (rs2 != null) { try { rs2.close(); } catch (SQLException ignored) {} }
                    if (ps2 != null) { try { ps2.close(); } catch (SQLException ignored) {} }
                    StringBuilder in2 = new StringBuilder();
                    for (int i = 0; i < mids.size(); i++) { if (i > 0) in2.append(','); in2.append('?'); }
                    ps2 = conn2.prepareStatement("SELECT menu_id, dish_id, quantity, sort_order FROM menu_items WHERE menu_id IN (" + in2 + ")");
                    for (int i = 0; i < mids.size(); i++) ps2.setInt(i + 1, mids.get(i));
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        int mid = rs2.getInt(1);
                        int did = rs2.getInt(2);
                        int dq = rs2.getInt(3);
                        int so = rs2.getInt(4);
                        defaultQtyMap.put(mid + ":" + did, dq);
                        // store sort order alongside default quantity
                        defaultQtyMap.put(mid + ":" + did + ":so", so);
                    }
                }
            } catch (SQLException ignored) {
            } finally {
                DBUtil.close(conn2, ps2, rs2);
            }

            StringBuilder sb = new StringBuilder();
            sb.append('{')
                    .append("\"restaurantId\":").append(order.getRestaurantId()).append(',')
                    .append("\"orderId\":").append(order.getOrderId()).append(',')
                    .append("\"restaurantName\":\"").append(escape(restaurantName)).append('\"').append(',')
                    .append("\"serialNumber\":").append(order.getSerialNumber()).append(',')
                    .append("\"status\":\"").append(escape(order.getStatus())).append('\"').append(',')
                    .append("\"totalPrice\":").append(order.getTotalPrice()).append(',')
                    .append("\"createdAt\":\"").append(order.getCreatedAt() == null ? "" : order.getCreatedAt().toString()).append('\"').append(',')
                    .append("\"items\":");
            sb.append('[');
            java.util.List<OrderItem> items = order.getItems();
            for (int i = 0; i < items.size(); i++) {
                OrderItem it = items.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                        .append("\"dishId\":").append(it.getDishId()).append(',')
                        .append("\"dishName\":\"").append(escape(it.getDishName())).append('\"').append(',')
                        .append("\"menuId\":").append(it.getMenuId()).append(',')
                        .append("\"menuName\":\"").append(escape(it.getMenuName())).append('\"').append(',')
                        .append("\"quantity\":").append(it.getQuantity()).append(',')
                        .append("\"unitPrice\":").append(it.getUnitPrice())
                        .append('}');
            }
            sb.append(']');
            sb.append(',');
            sb.append("\"menus\":");
            sb.append('[');
            int mi = 0;
            for (java.util.Map.Entry<Integer, java.util.List<OrderItem>> e : byMenu.entrySet()) {
                int mid = e.getKey();
                java.util.List<OrderItem> list = e.getValue();
                if (mi++ > 0) sb.append(',');
                boolean isPkg = menuPackageMap.getOrDefault(mid, false);
                int menuQty = 0;
                if (isPkg && !list.isEmpty()) {
                    OrderItem first = list.get(0);
                    Integer dq = defaultQtyMap.get(mid + ":" + first.getDishId());
                    if (dq != null && dq > 0) menuQty = first.getQuantity() / dq;
                }
                java.math.BigDecimal menuTotal = java.math.BigDecimal.ZERO;
                java.math.BigDecimal menuUnitPrice = java.math.BigDecimal.ZERO;
                for (OrderItem it : list) {
                    menuTotal = menuTotal.add(it.getUnitPrice().multiply(new java.math.BigDecimal(it.getQuantity())));
                }
                if (isPkg) {
                    java.math.BigDecimal unitSum = java.math.BigDecimal.ZERO;
                    for (OrderItem it : list) {
                        Integer dq = defaultQtyMap.get(mid + ":" + it.getDishId());
                        int perQty = dq == null ? 0 : dq;
                        unitSum = unitSum.add(it.getUnitPrice().multiply(new java.math.BigDecimal(perQty)));
                    }
                    menuUnitPrice = unitSum;
                }
                sb.append('{')
                        .append("\"menuId\":").append(mid).append(',')
                        .append("\"menuName\":\"").append(escape(menuNameMap.getOrDefault(mid, ""))).append('\"').append(',')
                        .append("\"isPackage\":").append(isPkg).append(',')
                        .append("\"menuQuantity\":").append(menuQty).append(',')
                        .append("\"menuTotalPrice\":").append(menuTotal).append(',')
                        .append("\"menuUnitPrice\":").append(menuUnitPrice).append(',')
                        .append("\"items\":");
                // sort items by sort_order when available
                java.util.List<OrderItem> sorted = new java.util.ArrayList<>(list);
                sorted.sort((a,b)->{
                    Integer sa = defaultQtyMap.get(mid + ":" + a.getDishId() + ":so");
                    Integer sb2 = defaultQtyMap.get(mid + ":" + b.getDishId() + ":so");
                    return Integer.compare(sa == null ? 0 : sa, sb2 == null ? 0 : sb2);
                });
                sb.append('[');
                for (int j = 0; j < sorted.size(); j++) {
                    OrderItem it = sorted.get(j);
                    if (j > 0) sb.append(',');
                    Integer dq = defaultQtyMap.get(mid + ":" + it.getDishId());
                    Integer so = defaultQtyMap.get(mid + ":" + it.getDishId() + ":so");
                    sb.append('{')
                            .append("\"dishId\":").append(it.getDishId()).append(',')
                            .append("\"dishName\":\"").append(escape(it.getDishName())).append('\"').append(',')
                            .append("\"unitPrice\":").append(it.getUnitPrice()).append(',')
                            .append("\"quantity\":").append(it.getQuantity()).append(',')
                            .append("\"perPackageQuantity\":").append(dq == null ? 0 : dq).append(',')
                            .append("\"sortOrder\":").append(so == null ? 0 : so)
                            .append('}');
                }
                sb.append(']');
                sb.append('}');
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
        String path = req.getPathInfo();
        if (path != null && path.matches("/\\d+/cancel")) {
            String[] parts = path.split("/");
            int orderId;
            try {
                orderId = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Invalid order id\"}");
                return;
            }
            Connection conn = null; PreparedStatement ps = null; ResultSet rs = null; PreparedStatement ups = null;
            try {
                conn = DBUtil.getConnection();
                ps = conn.prepareStatement("SELECT status FROM orders WHERE order_id = ? AND user_id = ?");
                ps.setInt(1, orderId);
                ps.setInt(2, user.getUserId());
                rs = ps.executeQuery();
                if (!rs.next()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"error\":\"Order not found or permission denied\"}");
                    return;
                }
                String current = rs.getString(1);
                DBUtil.close(null, ps, rs);
                boolean allowed = "PENDING".equals(current);
                if (!allowed) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.write("{\"error\":\"Order cannot be cancelled in current status\"}");
                    return;
                }
                ups = conn.prepareStatement("UPDATE orders SET status = 'CANCELLED' WHERE order_id = ? AND user_id = ? AND status = 'PENDING'");
                ups.setInt(1, orderId);
                ups.setInt(2, user.getUserId());
                int n = ups.executeUpdate();
                if (n <= 0) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.write("{\"error\":\"Order already updated by others\"}");
                    return;
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"orderId\":" + orderId + ",\"status\":\"CANCELLED\"}");
                try { KitchenEventBus.get().publishOrderUpdated(fetchRestaurantIdByOrder(orderId), orderId, "CANCELLED"); } catch (Exception ignored) {}
                return;
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\":\"Internal server error\"}");
                return;
            } finally {
                DBUtil.close(null, ups, null);
                DBUtil.close(conn, ps, rs);
            }
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
        if (orderReq == null || orderReq.restaurantId == null || orderReq.menus == null || orderReq.menus.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Missing restaurantId or menus\"}");
            return;
        }
        int restaurantId = orderReq.restaurantId;

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
            priceStmt = conn.prepareStatement(
                    "SELECT mi.price, mi.sort_order, mi.quantity FROM menu_items mi JOIN menus m ON mi.menu_id = m.menu_id WHERE m.restaurant_id = ? AND mi.menu_id = ? AND mi.dish_id = ?");

            for (MenuReq mreq : orderReq.menus) {
                if (mreq == null || mreq.menuId == null || mreq.quantity == null || mreq.quantity < 0 || mreq.items == null) {
                    conn.rollback();
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"菜单载荷无效\",\"details\":{\"menuId\":" + (mreq == null || mreq.menuId == null ? 0 : mreq.menuId) + "}}" );
                    return;
                }
                boolean isPackage = isPackageMenu(conn, mreq.menuId, restaurantId);
                if (isPackage) {
                    java.util.List<MenuItemSnapshot> dbItems = fetchMenuItemsSnapshot(conn, mreq.menuId);
                    if (dbItems.size() != mreq.items.size()) {
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.write("{\"error\":\"套餐" + mreq.menuId + "不匹配\",\"details\":{\"missingItems\":true}}\n");
                        return;
                    }
                    // optional signature/version check
                    String serverSig = computeMenuSignature(conn, mreq.menuId);
                    boolean versionMismatch = false;
                    if (mreq.menuSignature != null && !mreq.menuSignature.isEmpty()) {
                        versionMismatch = !mreq.menuSignature.equals(serverSig);
                    }
                    java.util.List<java.util.Map<String,Object>> qtyDiffs = new java.util.ArrayList<>();
                    for (int i = 0; i < dbItems.size(); i++) {
                        MenuItemSnapshot dbi = dbItems.get(i);
                        ItemReq cli = mreq.items.get(i);
                        if (cli == null || cli.dishId == null || cli.sortOrder == null || cli.quantity == null) {
                            conn.rollback();
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            out.write("{\"error\":\"套餐" + mreq.menuId + "不匹配\"}");
                            return;
                        }
                        if (dbi.dishId != cli.dishId || dbi.sortOrder != cli.sortOrder || dbi.quantity != cli.quantity) {
                            java.util.Map<String,Object> diff = new java.util.HashMap<>();
                            diff.put("dishId", cli.dishId);
                            diff.put("serverSortOrder", dbi.sortOrder);
                            diff.put("serverQuantity", dbi.quantity);
                            diff.put("clientSortOrder", cli.sortOrder);
                            diff.put("clientQuantity", cli.quantity);
                            qtyDiffs.add(diff);
                        }
                        priceStmt.setInt(1, restaurantId);
                        priceStmt.setInt(2, mreq.menuId);
                        priceStmt.setInt(3, cli.dishId);
                        rs = priceStmt.executeQuery();
                        if (!rs.next()) {
                            conn.rollback();
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.write("{\"error\":\"Invalid items: menuId+dishId not available under restaurant\"}");
                            return;
                        }
                        java.math.BigDecimal unitPrice = rs.getBigDecimal("price");
                        int finalQty = dbi.quantity * mreq.quantity;
                        total = total.add(unitPrice.multiply(new java.math.BigDecimal(finalQty)));
                        if (rs != null) { try { rs.close(); } catch (SQLException ignored) {} }
                    }
                    if (versionMismatch || !qtyDiffs.isEmpty()) {
                        conn.rollback();
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        StringBuilder details = new StringBuilder();
                        details.append('{').append("\"versionMismatch\":").append(versionMismatch);
                        if (!qtyDiffs.isEmpty()) {
                            details.append(',').append("\"quantityDiffs\":[");
                            for (int i = 0; i < qtyDiffs.size(); i++) {
                                if (i > 0) details.append(',');
                                java.util.Map<String,Object> d = qtyDiffs.get(i);
                                details.append('{')
                                        .append("\"dishId\":").append(d.get("dishId"))
                                        .append(',').append("\"serverSortOrder\":").append(d.get("serverSortOrder"))
                                        .append(',').append("\"serverQuantity\":").append(d.get("serverQuantity"))
                                        .append(',').append("\"clientSortOrder\":").append(d.get("clientSortOrder"))
                                        .append(',').append("\"clientQuantity\":").append(d.get("clientQuantity"))
                                        .append('}');
                            }
                            details.append(']');
                        }
                        details.append('}');
                        out.write("{\"error\":\"套餐" + mreq.menuId + "不匹配\",\"details\":" + details.toString() + "}");
                        return;
                    }
                } else {
                    for (ItemReq cli : mreq.items) {
                        if (cli == null || cli.dishId == null || cli.quantity == null || cli.quantity <= 0) {
                            conn.rollback();
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.write("{\\\"error\\\":\\\"数量必须大于0\\\",\\\"details\\\":{\\\"menuId\\\":" + mreq.menuId + ",\\\"dishId\\\":" + (cli == null || cli.dishId == null ? 0 : cli.dishId) + ",\\\"receivedQty\\\":" + (cli == null || cli.quantity == null ? 0 : cli.quantity) + "}}" );
                            return;
                        }
                        priceStmt.setInt(1, restaurantId);
                        priceStmt.setInt(2, mreq.menuId);
                        priceStmt.setInt(3, cli.dishId);
                        rs = priceStmt.executeQuery();
                        if (!rs.next()) {
                            conn.rollback();
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.write("{\"error\":\"菜品已不在该菜单\",\"details\":{\"menuId\":" + mreq.menuId + ",\"dishId\":" + cli.dishId + "}}" );
                            return;
                        }
                        java.math.BigDecimal unitPrice = rs.getBigDecimal("price");
                        total = total.add(unitPrice.multiply(new java.math.BigDecimal(cli.quantity)));
                        if (rs != null) { try { rs.close(); } catch (SQLException ignored) {} }
                    }
                }
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
                    "INSERT INTO order_items (order_id, menu_id, dish_id, quantity, unit_price) VALUES (?, ?, ?, ?, ?)"
            );
            for (MenuReq mreq : orderReq.menus) {
                boolean isPackage = isPackageMenu(conn, mreq.menuId, restaurantId);
                if (isPackage) {
                    java.util.List<MenuItemSnapshot> dbItems = fetchMenuItemsSnapshot(conn, mreq.menuId);
                    for (MenuItemSnapshot dbi : dbItems) {
                        insertItemStmt.setInt(1, orderId);
                        insertItemStmt.setInt(2, mreq.menuId);
                        insertItemStmt.setInt(3, dbi.dishId);
                        insertItemStmt.setInt(4, dbi.quantity * mreq.quantity);
                        java.math.BigDecimal unitPrice = fetchUnitPrice(conn, restaurantId, mreq.menuId, dbi.dishId);
                        insertItemStmt.setBigDecimal(5, unitPrice);
                        insertItemStmt.addBatch();
                    }
                } else {
                    for (ItemReq cli : mreq.items) {
                        insertItemStmt.setInt(1, orderId);
                        insertItemStmt.setInt(2, mreq.menuId);
                        insertItemStmt.setInt(3, cli.dishId);
                        insertItemStmt.setInt(4, cli.quantity);
                        java.math.BigDecimal unitPrice = fetchUnitPrice(conn, restaurantId, mreq.menuId, cli.dishId);
                        insertItemStmt.setBigDecimal(5, unitPrice);
                        insertItemStmt.addBatch();
                    }
                }
            }
            insertItemStmt.executeBatch();

            conn.commit();
            try { KitchenEventBus.get().publishNewOrder(restaurantId, orderId); } catch (Exception ignored) {}

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

    private int fetchRestaurantIdByOrder(int orderId) throws SQLException {
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement("SELECT restaurant_id FROM orders WHERE order_id = ?");
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    static class OrderReq {
        @SerializedName("restaurantId") Integer restaurantId;
        @SerializedName("menus") java.util.List<MenuReq> menus;
    }
    static class MenuReq {
        @SerializedName("menuId") Integer menuId;
        @SerializedName("quantity") Integer quantity;
        @SerializedName("items") java.util.List<ItemReq> items;
        @SerializedName("menuVersion") String menuVersion;
        @SerializedName("menuSignature") String menuSignature;
    }
    static class ItemReq {
        @SerializedName("dishId") Integer dishId;
        @SerializedName("sortOrder") Integer sortOrder;
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
            ostmt = conn.prepareStatement("SELECT order_id, user_id, restaurant_id, total_price, status, order_time, " +
                    "(SELECT COUNT(1) FROM orders o2 WHERE o2.restaurant_id = orders.restaurant_id AND DATE(o2.order_time) = DATE(orders.order_time) " +
                    "AND (o2.order_time < orders.order_time OR (o2.order_time = orders.order_time AND o2.order_id <= orders.order_id))) AS serial_number " +
                    "FROM orders WHERE order_id = ? AND user_id = ?");
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
            try { order.setSerialNumber(ors.getInt("serial_number")); } catch (Exception ignored) {}

            istmt = conn.prepareStatement("SELECT oi.item_id, oi.order_id, oi.menu_id, oi.dish_id, oi.quantity, oi.unit_price, d.name AS dish_name, m.name AS menu_name FROM order_items oi JOIN dishes d ON oi.dish_id = d.dish_id LEFT JOIN menus m ON oi.menu_id = m.menu_id WHERE oi.order_id = ? ORDER BY oi.item_id");
            istmt.setInt(1, orderId);
            irs = istmt.executeQuery();
            List<OrderItem> items = new ArrayList<>();
            while (irs.next()) {
                OrderItem it = new OrderItem();
                it.setItemId(irs.getInt("item_id"));
                it.setOrderId(irs.getInt("order_id"));
                it.setMenuId(irs.getInt("menu_id"));
                it.setDishId(irs.getInt("dish_id"));
                it.setQuantity(irs.getInt("quantity"));
                it.setUnitPrice(irs.getBigDecimal("unit_price"));
                it.setDishName(irs.getString("dish_name"));
                it.setMenuName(irs.getString("menu_name"));
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

    private boolean isPackageMenu(Connection conn, int menuId, int restaurantId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT is_package FROM menus WHERE menu_id = ? AND restaurant_id = ?");
            ps.setInt(1, menuId);
            ps.setInt(2, restaurantId);
            rs = ps.executeQuery();
            if (!rs.next()) return false;
            return rs.getBoolean(1);
        } finally {
            DBUtil.close(null, ps, rs);
        }
    }

    static class MenuItemSnapshot {
        int dishId;
        int sortOrder;
        int quantity;
    }

    private java.util.List<MenuItemSnapshot> fetchMenuItemsSnapshot(Connection conn, int menuId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT dish_id, sort_order, quantity FROM menu_items WHERE menu_id = ? ORDER BY sort_order, menu_item_id");
            ps.setInt(1, menuId);
            rs = ps.executeQuery();
            java.util.List<MenuItemSnapshot> list = new java.util.ArrayList<>();
            while (rs.next()) {
                MenuItemSnapshot s = new MenuItemSnapshot();
                s.dishId = rs.getInt(1);
                s.sortOrder = rs.getInt(2);
                s.quantity = rs.getInt(3);
                list.add(s);
            }
            return list;
        } finally {
            DBUtil.close(null, ps, rs);
        }
    }

    private java.math.BigDecimal fetchUnitPrice(Connection conn, int restaurantId, int menuId, int dishId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT mi.price FROM menu_items mi JOIN menus m ON mi.menu_id = m.menu_id WHERE m.restaurant_id = ? AND mi.menu_id = ? AND mi.dish_id = ?");
            ps.setInt(1, restaurantId);
            ps.setInt(2, menuId);
            ps.setInt(3, dishId);
            rs = ps.executeQuery();
            if (!rs.next()) return java.math.BigDecimal.ZERO;
            return rs.getBigDecimal(1);
        } finally {
            DBUtil.close(null, ps, rs);
        }
    }

    private String computeMenuSignature(Connection conn, int menuId) throws SQLException {
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT dish_id, sort_order, quantity, price FROM menu_items WHERE menu_id = ? ORDER BY sort_order, menu_item_id");
            ps.setInt(1, menuId);
            rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getInt(1)).append('|')
                        .append(rs.getInt(2)).append('|')
                        .append(rs.getInt(3)).append('|')
                        .append(rs.getBigDecimal(4)).append(';');
            }
            return sha256Hex(sb.toString());
        } finally {
            DBUtil.close(null, ps, rs);
        }
    }

    private static String sha256Hex(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    private java.util.List<Order> fetchOrdersForUser(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ostmt = null;
        PreparedStatement istmt = null;
        ResultSet ors = null;
        ResultSet irs = null;
        try {
            conn = DBUtil.getConnection();
            ostmt = conn.prepareStatement("SELECT order_id, user_id, restaurant_id, total_price, status, order_time FROM orders WHERE user_id = ? ORDER BY order_time DESC");
            ostmt.setInt(1, userId);
            ors = ostmt.executeQuery();
            java.util.List<Order> orders = new java.util.ArrayList<>();
            java.util.List<Integer> orderIds = new java.util.ArrayList<>();
            java.util.Map<Integer, Order> map = new java.util.HashMap<>();
            while (ors.next()) {
                Order order = new Order();
                order.setOrderId(ors.getInt("order_id"));
                order.setUserId(ors.getInt("user_id"));
                order.setRestaurantId(ors.getInt("restaurant_id"));
                order.setTotalPrice(ors.getBigDecimal("total_price"));
                order.setStatus(ors.getString("status"));
                try { order.setCreatedAt(ors.getTimestamp("order_time")); } catch (Exception ignored) {}
                orders.add(order);
                orderIds.add(order.getOrderId());
                map.put(order.getOrderId(), order);
            }
            if (orderIds.isEmpty()) return orders;
            StringBuilder in = new StringBuilder();
            for (int i = 0; i < orderIds.size(); i++) { if (i > 0) in.append(','); in.append('?'); }
            istmt = conn.prepareStatement("SELECT oi.item_id, oi.order_id, oi.menu_id, oi.dish_id, oi.quantity, oi.unit_price, d.name AS dish_name, m.name AS menu_name FROM order_items oi JOIN dishes d ON oi.dish_id = d.dish_id LEFT JOIN menus m ON oi.menu_id = m.menu_id WHERE oi.order_id IN (" + in + ") ORDER BY oi.item_id");
            for (int i = 0; i < orderIds.size(); i++) istmt.setInt(i + 1, orderIds.get(i));
            irs = istmt.executeQuery();
            while (irs.next()) {
                OrderItem it = new OrderItem();
                it.setItemId(irs.getInt("item_id"));
                it.setOrderId(irs.getInt("order_id"));
                it.setMenuId(irs.getInt("menu_id"));
                it.setDishId(irs.getInt("dish_id"));
                it.setQuantity(irs.getInt("quantity"));
                it.setUnitPrice(irs.getBigDecimal("unit_price"));
                it.setDishName(irs.getString("dish_name"));
                it.setMenuName(irs.getString("menu_name"));
                Order o = map.get(it.getOrderId());
                if (o != null) {
                    if (o.getItems() == null) o.setItems(new java.util.ArrayList<>());
                    o.getItems().add(it);
                }
            }
            return orders;
        } finally {
            if (irs != null) try { irs.close(); } catch (SQLException ignored) {}
            if (istmt != null) try { istmt.close(); } catch (SQLException ignored) {}
            DBUtil.close(conn, ostmt, ors);
        }
    }

    private java.util.List<Order> fetchOrdersForUserPaged(int userId, int page, int size, String status, String from, String to) throws SQLException {
        Connection conn = null;
        PreparedStatement ostmt = null;
        PreparedStatement istmt = null;
        ResultSet ors = null;
        ResultSet irs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder("SELECT order_id, user_id, restaurant_id, total_price, status, order_time, " +
                    "(SELECT COUNT(1) FROM orders o2 WHERE o2.restaurant_id = orders.restaurant_id AND DATE(o2.order_time) = DATE(orders.order_time) " +
                    "AND (o2.order_time < orders.order_time OR (o2.order_time = orders.order_time AND o2.order_id <= orders.order_id))) AS serial_number " +
                    "FROM orders WHERE user_id = ? ");
            java.util.List<Object> params = new java.util.ArrayList<>();
            params.add(userId);
            if (status != null && !status.isEmpty()) { sql.append(" AND status = ? "); params.add(status); }
            if (from != null && !from.isEmpty()) { sql.append(" AND order_time >= ? "); params.add(java.sql.Timestamp.valueOf(from.replace('T', ' ') )); }
            if (to != null && !to.isEmpty()) { sql.append(" AND order_time <= ? "); params.add(java.sql.Timestamp.valueOf(to.replace('T', ' ') )); }
            sql.append(" ORDER BY order_time DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add(page * size);
            ostmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer) ostmt.setInt(i + 1, (Integer)v);
                else if (v instanceof java.sql.Timestamp) ostmt.setTimestamp(i + 1, (java.sql.Timestamp)v);
                else ostmt.setString(i + 1, String.valueOf(v));
            }
            ors = ostmt.executeQuery();
            java.util.List<Order> orders = new java.util.ArrayList<>();
            java.util.List<Integer> orderIds = new java.util.ArrayList<>();
            java.util.Map<Integer, Order> map = new java.util.HashMap<>();
            while (ors.next()) {
                Order order = new Order();
                order.setOrderId(ors.getInt("order_id"));
                order.setUserId(ors.getInt("user_id"));
                order.setRestaurantId(ors.getInt("restaurant_id"));
                order.setTotalPrice(ors.getBigDecimal("total_price"));
                order.setStatus(ors.getString("status"));
                try { order.setCreatedAt(ors.getTimestamp("order_time")); } catch (Exception ignored) {}
                try { order.setSerialNumber(ors.getInt("serial_number")); } catch (Exception ignored) {}
                orders.add(order);
                orderIds.add(order.getOrderId());
                map.put(order.getOrderId(), order);
            }
            if (orderIds.isEmpty()) return orders;
            StringBuilder in = new StringBuilder();
            for (int i = 0; i < orderIds.size(); i++) { if (i > 0) in.append(','); in.append('?'); }
            istmt = conn.prepareStatement("SELECT oi.item_id, oi.order_id, oi.menu_id, oi.dish_id, oi.quantity, oi.unit_price, d.name AS dish_name, m.name AS menu_name FROM order_items oi JOIN dishes d ON oi.dish_id = d.dish_id LEFT JOIN menus m ON oi.menu_id = m.menu_id WHERE oi.order_id IN (" + in + ") ORDER BY oi.item_id");
            for (int i = 0; i < orderIds.size(); i++) istmt.setInt(i + 1, orderIds.get(i));
            irs = istmt.executeQuery();
            while (irs.next()) {
                OrderItem it = new OrderItem();
                it.setItemId(irs.getInt("item_id"));
                it.setOrderId(irs.getInt("order_id"));
                it.setMenuId(irs.getInt("menu_id"));
                it.setDishId(irs.getInt("dish_id"));
                it.setQuantity(irs.getInt("quantity"));
                it.setUnitPrice(irs.getBigDecimal("unit_price"));
                it.setDishName(irs.getString("dish_name"));
                it.setMenuName(irs.getString("menu_name"));
                Order o = map.get(it.getOrderId());
                if (o != null) {
                    if (o.getItems() == null) o.setItems(new java.util.ArrayList<>());
                    o.getItems().add(it);
                }
            }
            return orders;
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