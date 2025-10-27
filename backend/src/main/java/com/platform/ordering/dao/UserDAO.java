package com.platform.ordering.dao;

import com.platform.ordering.model.User;

import java.sql.SQLException;

/**
 * 用户数据访问对象接口 (UserDAO)
 * <p>
 * 定义了所有与用户数据相关的数据库操作的标准。
 * 所有具体的UserDAO实现都必须实现这个接口。
 * </p>
 */
public interface UserDAO {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 如果找到，返回User对象；否则返回null
     */
    User findByUsername(String username) throws SQLException;

    /**
     * 新增一个用户
     *
     * @param user 待保存的用户对象
     * @return 返回影响的行数，通常是1表示成功
     */
    int save(User user) throws SQLException;

    // 未来可以根据需要添加更多方法，例如：
    // User findById(int userId);
    // List<User> findAll();
    // int update(User user);
    // int delete(int userId);
}
