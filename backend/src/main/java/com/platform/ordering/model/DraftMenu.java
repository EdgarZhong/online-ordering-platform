package com.platform.ordering.model;

import java.util.ArrayList;
import java.util.List;

public class DraftMenu {
    private int menuId;
    private int restaurantId;
    private String name;
    private String description;
    private boolean isPackage;
    private List<DraftMenuItem> items = new ArrayList<>();

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }
    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPackage() { return isPackage; }
    public void setPackage(boolean aPackage) { isPackage = aPackage; }
    public List<DraftMenuItem> getItems() { return items; }
    public void setItems(List<DraftMenuItem> items) { this.items = items; }
}