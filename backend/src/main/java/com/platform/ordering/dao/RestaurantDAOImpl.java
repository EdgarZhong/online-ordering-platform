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
}