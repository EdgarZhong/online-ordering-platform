package com.platform.ordering.dao;

import com.platform.ordering.model.Dish;

import java.sql.SQLException;
import java.util.List;

public interface DishDAO {
    List<Dish> listByRestaurant(int restaurantId) throws SQLException;
    Dish findById(int dishId, int restaurantId) throws SQLException;
    int save(Dish dish) throws SQLException;
    int update(Dish dish) throws SQLException;
    int deleteById(int dishId, int restaurantId) throws SQLException;
}