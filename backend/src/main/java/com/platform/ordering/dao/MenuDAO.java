/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-11-14 19:29:48
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 02:25:24
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\dao\MenuDAO.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.platform.ordering.dao;

import com.platform.ordering.model.Menu;

import java.sql.SQLException;
import java.util.List;

public interface MenuDAO {
    List<Menu> listByRestaurant(int restaurantId) throws SQLException;
    Menu findById(int menuId, int restaurantId) throws SQLException;
    int save(Menu menu) throws SQLException;
    int saveAndReturnId(Menu menu) throws SQLException;
    int update(Menu menu) throws SQLException;
    int deleteById(int menuId, int restaurantId) throws SQLException;
    int moveUp(int menuId, int restaurantId) throws SQLException;
    int moveDown(int menuId, int restaurantId) throws SQLException;
    int reorder(int restaurantId, java.util.List<Integer> orderedMenuIds) throws SQLException;
}