/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-10-30 01:01:08
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 00:00:50
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\model\Dish.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.platform.ordering.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
/**
 * 菜品实体类 (Dish)
 * <p>
 * 对应数据库中的 `dishes` 表。
 * 代表一个基础菜品，是菜品库中的一个“原料”，本身不包含价格。
 * </p>
 */
public class Dish {

    private int dishId;
    private int restaurantId;
    private String name;
    private String imageUrl;
    private String description;
    private BigDecimal defaultPrice;
    private Timestamp createdAt;

    public Dish() {
    }

    // --- Getters and Setters ---

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}