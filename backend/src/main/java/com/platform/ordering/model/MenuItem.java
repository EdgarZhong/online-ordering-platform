package com.platform.ordering.model;

import java.math.BigDecimal;

/**
 * 菜单项实体类 (MenuItem)
 * <p>
 * 对应数据库中的 `menu_items` 中间表。
 * 它将一个菜品(Dish)和一个菜单(Menu)关联起来，并定义了该菜品在此菜单中的特定价格。
 * </p>
 */
public class MenuItem {

    private int menuItemId;
    private int menuId;
    private int dishId;
    private BigDecimal price;

    public MenuItem() {
    }

    // --- Getters and Setters ---

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}