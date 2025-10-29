package com.platform.ordering.model;

/**
 * 菜单实体类 (Menu)
 * <p>
 * 对应数据库中的 `menus` 表。
 * 代表一个菜单或一个套餐。
 * </p>
 */
public class Menu {

    private int menuId;
    private int restaurantId;
    private String name;
    private String description;

    public Menu() {
    }

    // --- Getters and Setters ---

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}