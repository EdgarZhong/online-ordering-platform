/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-11-14 19:30:27
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 01:55:23
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\dao\MenuItemDAOImpl.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.platform.ordering.dao;

import com.platform.ordering.model.MenuItem;
import com.platform.ordering.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAOImpl implements MenuItemDAO {
    @Override
    public List<MenuItem> listByMenu(int menuId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql_listByMenu = "SELECT mi.menu_item_id, mi.menu_id, mi.dish_id, mi.price, mi.quantity, mi.sort_order FROM menu_items mi JOIN menus m ON mi.menu_id = m.menu_id WHERE mi.menu_id = ? AND m.restaurant_id = ? ORDER BY mi.sort_order, mi.menu_item_id";
        List<MenuItem> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_listByMenu);
            pstmt.setInt(1, menuId);
            pstmt.setInt(2, restaurantId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                MenuItem mi = new MenuItem();
                mi.setMenuItemId(rs.getInt("menu_item_id"));
                mi.setMenuId(rs.getInt("menu_id"));
                mi.setDishId(rs.getInt("dish_id"));
                mi.setPrice(rs.getBigDecimal("price"));
                try { mi.setQuantity(rs.getInt("quantity")); } catch (Exception ignored) {}
                try { mi.setSortOrder(rs.getInt("sort_order")); } catch (Exception ignored) {}
                list.add(mi);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public int save(MenuItem item, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_save = "INSERT INTO menu_items (menu_id, dish_id, price, quantity, sort_order) SELECT ?, ?, ?, 1, COALESCE((SELECT MAX(sort_order)+1 FROM menu_items WHERE menu_id = ?),0) WHERE EXISTS (SELECT 1 FROM menus WHERE menu_id = ? AND restaurant_id = ?)";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_save);
            pstmt.setInt(1, item.getMenuId());
            pstmt.setInt(2, item.getDishId());
            pstmt.setBigDecimal(3, item.getPrice());
            pstmt.setInt(4, item.getMenuId());
            pstmt.setInt(5, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public int updatePrice(int menuItemId, int restaurantId, java.math.BigDecimal price) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_updatePrice = "UPDATE menu_items mi SET price = ? FROM menus m WHERE mi.menu_item_id = ? AND mi.menu_id = m.menu_id AND m.restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_updatePrice);
            pstmt.setBigDecimal(1, price);
            pstmt.setInt(2, menuItemId);
            pstmt.setInt(3, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public int updateQuantity(int menuItemId, int restaurantId, int quantity) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_updateQty = "UPDATE menu_items mi SET quantity = ? FROM menus m WHERE mi.menu_item_id = ? AND mi.menu_id = m.menu_id AND m.restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_updateQty);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, menuItemId);
            pstmt.setInt(3, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public int updateSortOrder(int menuItemId, int restaurantId, int sortOrder) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_updateSort = "UPDATE menu_items mi SET sort_order = ? FROM menus m WHERE mi.menu_item_id = ? AND mi.menu_id = m.menu_id AND m.restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_updateSort);
            pstmt.setInt(1, sortOrder);
            pstmt.setInt(2, menuItemId);
            pstmt.setInt(3, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
    @Override
    public int deleteById(int menuItemId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_delete = "DELETE FROM menu_items USING menus WHERE menu_items.menu_item_id = ? AND menu_items.menu_id = menus.menu_id AND menus.restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_delete);
            pstmt.setInt(1, menuItemId);
            pstmt.setInt(2, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    public int upsert(MenuItem item, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement upd = null;
        PreparedStatement ins = null;
        try {
            conn = DBUtil.getConnection();
            upd = conn.prepareStatement("UPDATE menu_items mi SET price = ?, quantity = COALESCE(quantity,1) FROM menus m WHERE mi.menu_id = ? AND mi.dish_id = ? AND mi.menu_id = m.menu_id AND m.restaurant_id = ?");
            upd.setBigDecimal(1, item.getPrice());
            upd.setInt(2, item.getMenuId());
            upd.setInt(3, item.getDishId());
            upd.setInt(4, restaurantId);
            int affected = upd.executeUpdate();
            if (affected > 0) return affected;
            ins = conn.prepareStatement("INSERT INTO menu_items (menu_id, dish_id, price, quantity, sort_order) SELECT ?, ?, ?, 1, COALESCE((SELECT MAX(sort_order)+1 FROM menu_items WHERE menu_id = ?),0) WHERE EXISTS (SELECT 1 FROM menus WHERE menu_id = ? AND restaurant_id = ?)");
            ins.setInt(1, item.getMenuId());
            ins.setInt(2, item.getDishId());
            ins.setBigDecimal(3, item.getPrice());
            ins.setInt(4, item.getMenuId());
            ins.setInt(5, item.getMenuId());
            ins.setInt(6, restaurantId);
            return ins.executeUpdate();
        } finally {
            DBUtil.close(null, ins, null);
            DBUtil.close(null, upd, null);
            DBUtil.close(conn, null, null);
        }
    }
}