package com.platform.ordering.dao;

import com.platform.ordering.model.Restaurant;
import com.platform.ordering.util.DBUtil;

import java.sql.*;

/**
 * RestaurantDAO的JDBC实现
 */
public class RestaurantDAOImpl implements RestaurantDAO {

    @Override
    public Restaurant save(Restaurant restaurant) throws SQLException {
        // 这是旧方法，用于非事务性调用
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return save(restaurant, conn);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    @Override
    public Restaurant save(Restaurant restaurant, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        String sql_saveRestaurant = "INSERT INTO restaurants (name, address, phone, description) VALUES (?, ?, ?, ?)";

        try {
            // 使用传入的Connection
            pstmt = conn.prepareStatement(sql_saveRestaurant, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, restaurant.getName());
            pstmt.setString(2, restaurant.getAddress());
            pstmt.setString(3, restaurant.getPhone());
            pstmt.setString(4, restaurant.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    restaurant.setRestaurantId(generatedKeys.getInt(1));
                    return restaurant;
                }
            }
        } finally {
            // 在事务中，只关闭PreparedStatement和ResultSet，不关闭Connection
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
        }
        return null;
    }

    @Override
    public Restaurant findById(int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql_findById = "SELECT restaurant_id, name, address, phone, description, logo_url FROM restaurants WHERE restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_findById);
            pstmt.setInt(1, restaurantId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Restaurant r = new Restaurant();
                r.setRestaurantId(rs.getInt("restaurant_id"));
                r.setName(rs.getString("name"));
                r.setAddress(rs.getString("address"));
                r.setPhone(rs.getString("phone"));
                r.setDescription(rs.getString("description"));
                try { r.setLogoUrl(rs.getString("logo_url")); } catch (Exception ignored) {}
                return r;
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public int update(Restaurant restaurant) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_update = "UPDATE restaurants SET name = ?, address = ?, phone = ?, description = ?, logo_url = ? WHERE restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_update);
            pstmt.setString(1, restaurant.getName());
            pstmt.setString(2, restaurant.getAddress());
            pstmt.setString(3, restaurant.getPhone());
            pstmt.setString(4, restaurant.getDescription());
            pstmt.setString(5, restaurant.getLogoUrl());
            pstmt.setInt(6, restaurant.getRestaurantId());
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public int deleteById(int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_delete = "DELETE FROM restaurants WHERE restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_delete);
            pstmt.setInt(1, restaurantId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public java.util.List<Restaurant> listAll() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT restaurant_id, name, address, phone, description, logo_url FROM restaurants ORDER BY restaurant_id ASC";
        java.util.List<Restaurant> list = new java.util.ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Restaurant r = new Restaurant();
                r.setRestaurantId(rs.getInt("restaurant_id"));
                r.setName(rs.getString("name"));
                r.setAddress(rs.getString("address"));
                r.setPhone(rs.getString("phone"));
                r.setDescription(rs.getString("description"));
                try { r.setLogoUrl(rs.getString("logo_url")); } catch (Exception ignored) {}
                list.add(r);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }
}