package com.platform.ordering.model;

import java.sql.Timestamp;

/**
 * 用户实体类 (User)
 * <p>
 * 对应数据库中的 `users` 表。
 * 这是一个POJO (Plain Old Java Object)，主要用于数据传输。
 * </p>
 */
public class User {

    private int userId;
    private Integer restaurantId; // 使用Integer，因为它可以为null（消费者）
    private String username;
    private String password;
    private String role;
    private String phone;
    private Timestamp createdAt;

    public User() {
    }

    // --- Getters and Setters ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Integer restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", restaurantId=" + restaurantId +
                ", username='" + username + "'" +
                ", role='" + role + "'" +
                '}';
    }
}
