package com.platform.ordering.dao;

import com.platform.ordering.model.MenuItem;

import java.sql.SQLException;
import java.util.List;

public interface MenuItemDAO {
    List<MenuItem> listByMenu(int menuId, int restaurantId) throws SQLException;
    int save(MenuItem item, int restaurantId) throws SQLException;
    int updatePrice(int menuItemId, int restaurantId, java.math.BigDecimal price) throws SQLException;
    int deleteById(int menuItemId, int restaurantId) throws SQLException;
}