/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-10-30 01:00:41
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 00:01:06
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\model\Menu.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
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
    private boolean isPackage;
    private int sortOrder;

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

    public boolean isPackage() {
        return isPackage;
    }

    public void setPackage(boolean aPackage) {
        isPackage = aPackage;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}