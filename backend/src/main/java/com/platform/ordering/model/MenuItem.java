/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-10-30 01:01:14
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 03:01:09
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\model\MenuItem.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
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
    private int quantity;
    private int sortOrder;

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}