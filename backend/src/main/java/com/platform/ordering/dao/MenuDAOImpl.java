package com.platform.ordering.dao;

import com.platform.ordering.model.Menu;
import com.platform.ordering.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAOImpl implements MenuDAO {
    @Override
    public List<Menu> listByRestaurant(int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql_listByRestaurant = "SELECT menu_id, restaurant_id, name, description, is_package, sort_order FROM menus WHERE restaurant_id = ? ORDER BY sort_order, menu_id";
        List<Menu> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_listByRestaurant);
            pstmt.setInt(1, restaurantId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Menu m = new Menu();
                m.setMenuId(rs.getInt("menu_id"));
                m.setRestaurantId(rs.getInt("restaurant_id"));
                m.setName(rs.getString("name"));
                m.setDescription(rs.getString("description"));
                try { m.setPackage(rs.getBoolean("is_package")); } catch (Exception ignored) {}
                try { m.setSortOrder(rs.getInt("sort_order")); } catch (Exception ignored) {}
                list.add(m);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public Menu findById(int menuId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql_findById = "SELECT menu_id, restaurant_id, name, description, is_package, sort_order FROM menus WHERE menu_id = ? AND restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_findById);
            pstmt.setInt(1, menuId);
            pstmt.setInt(2, restaurantId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Menu m = new Menu();
                m.setMenuId(rs.getInt("menu_id"));
                m.setRestaurantId(rs.getInt("restaurant_id"));
                m.setName(rs.getString("name"));
                m.setDescription(rs.getString("description"));
                try { m.setPackage(rs.getBoolean("is_package")); } catch (Exception ignored) {}
                try { m.setSortOrder(rs.getInt("sort_order")); } catch (Exception ignored) {}
                return m;
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public int save(Menu menu) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_save = "INSERT INTO menus (restaurant_id, name, description, is_package, sort_order) VALUES (?, ?, ?, ?, COALESCE((SELECT MAX(sort_order)+1 FROM menus WHERE restaurant_id = ?),0))";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_save);
            pstmt.setInt(1, menu.getRestaurantId());
            pstmt.setString(2, menu.getName());
            pstmt.setString(3, menu.getDescription());
            pstmt.setBoolean(4, menu.isPackage());
            pstmt.setInt(5, menu.getRestaurantId());
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public int saveAndReturnId(Menu menu) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet keys = null;
        String sql = "INSERT INTO menus (restaurant_id, name, description, is_package, sort_order) VALUES (?, ?, ?, ?, COALESCE((SELECT MAX(sort_order)+1 FROM menus WHERE restaurant_id = ?),0))";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, menu.getRestaurantId());
            pstmt.setString(2, menu.getName());
            pstmt.setString(3, menu.getDescription());
            pstmt.setBoolean(4, menu.isPackage());
            pstmt.setInt(5, menu.getRestaurantId());
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return 0;
        } finally {
            if (keys != null) try { keys.close(); } catch (SQLException ignored) {}
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public int update(Menu menu) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_update = "UPDATE menus SET name = ?, description = ?, is_package = ?, sort_order = ? WHERE menu_id = ? AND restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_update);
            pstmt.setString(1, menu.getName());
            pstmt.setString(2, menu.getDescription());
            pstmt.setBoolean(3, menu.isPackage());
            pstmt.setInt(4, menu.getSortOrder());
            pstmt.setInt(5, menu.getMenuId());
            pstmt.setInt(6, menu.getRestaurantId());
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public int deleteById(int menuId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_delete = "DELETE FROM menus WHERE menu_id = ? AND restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_delete);
            pstmt.setInt(1, menuId);
            pstmt.setInt(2, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public int moveUp(int menuId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement selAll = null;
        PreparedStatement upd = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            selAll = conn.prepareStatement("SELECT menu_id FROM menus WHERE restaurant_id = ? ORDER BY sort_order ASC, menu_id ASC");
            selAll.setInt(1, restaurantId);
            ResultSet rs = selAll.executeQuery();
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) ids.add(rs.getInt(1));
            rs.close();
            int idx = ids.indexOf(menuId);
            if (idx <= 0) { conn.rollback(); return 0; }
            java.util.Collections.swap(ids, idx, idx - 1);
            upd = conn.prepareStatement("UPDATE menus SET sort_order = ? WHERE menu_id = ? AND restaurant_id = ?");
            for (int i = 0; i < ids.size(); i++) {
                upd.setInt(1, i);
                upd.setInt(2, ids.get(i));
                upd.setInt(3, restaurantId);
                upd.addBatch();
            }
            upd.executeBatch();
            conn.commit();
            return 1;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            DBUtil.close(null, upd, null);
            DBUtil.close(null, selAll, null);
            DBUtil.close(conn, null, null);
        }
    }

    public int moveDown(int menuId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement selAll = null;
        PreparedStatement upd = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            selAll = conn.prepareStatement("SELECT menu_id FROM menus WHERE restaurant_id = ? ORDER BY sort_order ASC, menu_id ASC");
            selAll.setInt(1, restaurantId);
            ResultSet rs = selAll.executeQuery();
            java.util.List<Integer> ids = new java.util.ArrayList<>();
            while (rs.next()) ids.add(rs.getInt(1));
            rs.close();
            int idx = ids.indexOf(menuId);
            if (idx < 0 || idx >= ids.size() - 1) { conn.rollback(); return 0; }
            java.util.Collections.swap(ids, idx, idx + 1);
            upd = conn.prepareStatement("UPDATE menus SET sort_order = ? WHERE menu_id = ? AND restaurant_id = ?");
            for (int i = 0; i < ids.size(); i++) {
                upd.setInt(1, i);
                upd.setInt(2, ids.get(i));
                upd.setInt(3, restaurantId);
                upd.addBatch();
            }
            upd.executeBatch();
            conn.commit();
            return 1;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            DBUtil.close(null, upd, null);
            DBUtil.close(null, selAll, null);
            DBUtil.close(conn, null, null);
        }
    }

    public int reorder(int restaurantId, java.util.List<Integer> orderedMenuIds) throws SQLException {
        Connection conn = null;
        PreparedStatement upd = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            upd = conn.prepareStatement("UPDATE menus SET sort_order = ? WHERE menu_id = ? AND restaurant_id = ?");
            for (int i = 0; i < orderedMenuIds.size(); i++) {
                upd.setInt(1, i);
                upd.setInt(2, orderedMenuIds.get(i));
                upd.setInt(3, restaurantId);
                upd.addBatch();
            }
            upd.executeBatch();
            conn.commit();
            return 1;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            DBUtil.close(null, upd, null);
            DBUtil.close(conn, null, null);
        }
    }
}