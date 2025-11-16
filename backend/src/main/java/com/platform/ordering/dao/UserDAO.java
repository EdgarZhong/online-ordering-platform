package com.platform.ordering.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.platform.ordering.model.User;

/**
 * 用户数据访问对象接口 (UserDAO)
 * <p>
 * 定义了所有与用户数据相关的数据库操作的标准。
 * 所有具体的UserDAO实现都必须实现这个接口。
 * </p>
 */
public interface UserDAO {

    User findByUsername(String username) throws SQLException;

    /**
     * 新增一个用户 (非事务性，方法内部自己管理连接)
     * @param user 待保存的用户对象
     * @return 返回影响的行数
     */
    int save(User user) throws SQLException;

    /**
     * 新增一个用户 (在指定的数据库连接上执行，用于事务)
     * @param user 待保存的用户对象
     * @param conn 外部传入的数据库连接
     * @return 返回影响的行数
     */
    int save(User user, Connection conn) throws SQLException;

    int updatePassword(int userId, String newPassword) throws SQLException;
}
