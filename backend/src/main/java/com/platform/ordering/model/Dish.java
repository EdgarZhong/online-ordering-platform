package com.platform.ordering.model;

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
}