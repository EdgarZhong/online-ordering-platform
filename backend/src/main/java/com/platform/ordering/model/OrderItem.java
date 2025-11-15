package com.platform.ordering.model;

import java.math.BigDecimal;

public class OrderItem {
    private int itemId;
    private int orderId;
    private int dishId;
    private int quantity;
    private BigDecimal unitPrice;
    private String dishName;

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
}