package com.platform.ordering.dao;

import java.sql.Timestamp;
import java.util.List;

import com.platform.ordering.model.Order;

/**
 * 商家端订单管理DAO接口
 * 说明：所有方法必须基于 restaurantId 做数据隔离；
 * 支持订单分页查询、详情查询、状态更新。
 */
public interface OrderAdminDAO {

    /**
     * 分页查询订单摘要列表（按下单时间倒序），支持状态/时间范围/关键词筛选。
     * 关键词同时在订单编号和顾客用户名上进行匹配。
     */
    List<Order> listOrders(int restaurantId, String status, Timestamp from, Timestamp to, String keyword, int page, int size) throws Exception;

    /**
     * 统计满足条件的订单总数，用于分页。
     */
    int countOrders(int restaurantId, String status, Timestamp from, Timestamp to, String keyword) throws Exception;

    /**
     * 查询订单详情（包含订单项）。
     */
    Order getOrderDetail(int orderId, int restaurantId) throws Exception;

    /**
     * 更新订单状态（遵循有限状态流转规则），返回是否更新成功。
     * 现有数据库状态取值：PENDING、PROCESSING、COMPLETED、CANCELLED。
     */
    boolean updateOrderStatus(int orderId, int restaurantId, String newStatus, int updatedByUserId, String reason) throws Exception;
}