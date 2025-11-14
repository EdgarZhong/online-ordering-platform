package com.platform.ordering.model;

import java.sql.Timestamp;

/**
 * 餐厅实体类 (Restaurant)
 * <p>
 * 对应数据库中的 `restaurants` 表。
 * </p>
 */
public class Restaurant {

    private int restaurantId;
    private String name;
    private String address;
    private String phone;
    private String logoUrl;
    private String description;
    private Timestamp createdAt;

    public Restaurant() {
    }

    // --- Getters and Setters ---

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}