package com.platform.ordering.dao;

import com.platform.ordering.model.User;
import com.platform.ordering.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问对象的JDBC实现 (UserDAOImpl)
 * <p>
 * 实现了UserDAO接口，提供了具体的数据库操作逻辑。
 * </p>
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public User findByUsername(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        // 根据新规范命名SQL变量
        String sql_findByUsername = "SELECT user_id, restaurant_id, username, password, role, phone, created_at FROM users WHERE username = ?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql_findByUsername);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                // restaurant_id可能为NULL，需要特殊处理
                int restaurantId = rs.getInt("restaurant_id");
                if (!rs.wasNull()) {
                    user.setRestaurantId(restaurantId);
                }
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setPhone(rs.getString("phone"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
            }
        } finally {
            // 使用重载的close方法统一关闭所有资源
            DBUtil.close(conn, pstmt, rs);
        }

        return user;
    }

    @Override
    public int save(User user) throws SQLException {
        // 这是旧方法，用于非事务性调用
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return save(user, conn);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    @Override
    public int save(User user, Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        String sql_saveUser = "INSERT INTO users (restaurant_id, username, password, role, phone) VALUES (?, ?, ?, ?, ?)";

        try {
            // 使用传入的Connection，而不是自己获取
            pstmt = conn.prepareStatement(sql_saveUser);

            if (user.getRestaurantId() != null) {
                pstmt.setInt(1, user.getRestaurantId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getPhone());

            return pstmt.executeUpdate();

        } finally {
            // 在事务中，只关闭PreparedStatement，不关闭Connection
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }
}