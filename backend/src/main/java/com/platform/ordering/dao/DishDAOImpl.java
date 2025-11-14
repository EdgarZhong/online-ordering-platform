package com.platform.ordering.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.platform.ordering.model.Dish;
import com.platform.ordering.util.DBUtil;

public class DishDAOImpl implements DishDAO {
    @Override
    public List<Dish> listByRestaurant(int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql_listByRestaurant = "SELECT dish_id, restaurant_id, name, image_url, description, default_price, created_at FROM dishes WHERE restaurant_id = ? ORDER BY created_at ASC";
        List<Dish> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_listByRestaurant);
            pstmt.setInt(1, restaurantId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Dish d = new Dish();
                d.setDishId(rs.getInt("dish_id"));
                d.setRestaurantId(rs.getInt("restaurant_id"));
                d.setName(rs.getString("name"));
                d.setImageUrl(rs.getString("image_url"));
                d.setDescription(rs.getString("description"));
                d.setDefaultPrice(rs.getBigDecimal("default_price"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(d);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public Dish findById(int dishId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql_findById = "SELECT dish_id, restaurant_id, name, image_url, description, default_price, created_at FROM dishes WHERE dish_id = ? AND restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_findById);
            pstmt.setInt(1, dishId);
            pstmt.setInt(2, restaurantId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Dish d = new Dish();
                d.setDishId(rs.getInt("dish_id"));
                d.setRestaurantId(rs.getInt("restaurant_id"));
                d.setName(rs.getString("name"));
                d.setImageUrl(rs.getString("image_url"));
                d.setDescription(rs.getString("description"));
                d.setDefaultPrice(rs.getBigDecimal("default_price"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                return d;
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public int save(Dish dish) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_save = "INSERT INTO dishes (restaurant_id, name, image_url, description, default_price) VALUES (?, ?, ?, ?, ?)";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_save);
            pstmt.setInt(1, dish.getRestaurantId());
            pstmt.setString(2, dish.getName());
            pstmt.setString(3, dish.getImageUrl());
            pstmt.setString(4, dish.getDescription());
            System.out.println("[DishDAOImpl#save] sql_save=" + sql_save);
            System.out.println("[DishDAOImpl#save] params: restaurantId=" + dish.getRestaurantId() + ", name=" + dish.getName() + ", imageUrl=" + dish.getImageUrl() + ", description=" + dish.getDescription() + ", defaultPrice=" + dish.getDefaultPrice());
            pstmt.setBigDecimal(5, dish.getDefaultPrice());
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public int update(Dish dish) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_update = "UPDATE dishes SET name = ?, image_url = ?, description = ?, default_price = ? WHERE dish_id = ? AND restaurant_id = ?";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_update);
            pstmt.setString(1, dish.getName());
            pstmt.setString(2, dish.getImageUrl());
            pstmt.setString(3, dish.getDescription());
            System.out.println("[DishDAOImpl#update] sql_update=" + sql_update);
            System.out.println("[DishDAOImpl#update] params: name=" + dish.getName() + ", imageUrl=" + dish.getImageUrl() + ", description=" + dish.getDescription() + ", defaultPrice=" + dish.getDefaultPrice() + ", dishId=" + dish.getDishId() + ", restaurantId=" + dish.getRestaurantId());
            pstmt.setBigDecimal(4, dish.getDefaultPrice());
            pstmt.setInt(5, dish.getDishId());
            pstmt.setInt(6, dish.getRestaurantId());
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public int deleteById(int dishId, int restaurantId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql_deleteSafe = "DELETE FROM dishes WHERE dish_id = ? AND restaurant_id = ? AND NOT EXISTS (SELECT 1 FROM order_items WHERE dish_id = ?)";
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_deleteSafe);
            pstmt.setInt(1, dishId);
            pstmt.setInt(2, restaurantId);
            pstmt.setInt(3, dishId);
            return pstmt.executeUpdate();
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }
}