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

        String sql = "SELECT user_id, restaurant_id, username, password, role, phone, created_at FROM users WHERE username = ?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
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
        Connection conn = null;
        PreparedStatement pstmt = null;

        // 注意：这里没有插入user_id，因为它是由数据库自增的
        String sql = "INSERT INTO users (restaurant_id, username, password, role, phone) VALUES (?, ?, ?, ?, ?)";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            // restaurant_id可能为null
            if (user.getRestaurantId() != null) {
                pstmt.setInt(1, user.getRestaurantId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword()); // 实际项目应加密
            pstmt.setString(4, user.getRole());
            pstmt.setString(5, user.getPhone());

            return pstmt.executeUpdate();

        } finally {
            // 统一关闭资源（即使ResultSet为null）
            DBUtil.close(conn, pstmt, null);
        }
    }
}
