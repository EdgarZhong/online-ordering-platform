package com.platform.ordering.dao;

import com.platform.ordering.model.Restaurant;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 餐厅数据访问对象接口 (RestaurantDAO)
 */
public interface RestaurantDAO {

    /**
     * 保存一个新的餐厅信息 (非事务性)
     * @param restaurant 待保存的餐厅对象
     * @return 保存成功后，包含数据库生成ID的完整餐厅对象
     * @throws SQLException SQL异常
     */
    Restaurant save(Restaurant restaurant) throws SQLException;

    /**
     * 保存一个新的餐厅信息 (在指定的数据库连接上执行，用于事务)
     * @param restaurant 待保存的餐厅对象
     * @param conn 外部传入的数据库连接
     * @return 保存成功后，包含数据库生成ID的完整餐厅对象
     * @throws SQLException SQL异常
     */
    Restaurant save(Restaurant restaurant, Connection conn) throws SQLException;
}