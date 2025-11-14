package com.platform.ordering.model;

import java.math.BigDecimal;

public class DraftMenuItem {
    private int dishId;
    private BigDecimal price;
    private int quantity;
    private int sortOrder;

    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}