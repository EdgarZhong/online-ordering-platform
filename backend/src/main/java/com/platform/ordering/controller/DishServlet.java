/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-11-14 19:30:55
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 00:03:12
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\controller\DishServlet.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.platform.ordering.controller;

import com.platform.ordering.dao.DishDAO;
import com.platform.ordering.dao.DishDAOImpl;
import com.platform.ordering.dao.MenuDAO;
import com.platform.ordering.dao.MenuDAOImpl;
import com.platform.ordering.model.Dish;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/dishes")
public class DishServlet extends HttpServlet {
    private final DishDAO dishDAO = new DishDAOImpl();
    private final MenuDAO menuDAO = new MenuDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer restaurantId = (Integer) req.getAttribute("restaurantId");
        if (restaurantId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        try {
            String mode = req.getParameter("mode");
            String dishIdStr = req.getParameter("dishId");
            List<Dish> dishes = dishDAO.listByRestaurant(restaurantId);
            System.out.println("[DishServlet#doGet] restaurantId=" + restaurantId + ", dishes.size=" + dishes.size());
            for (int i = 0; i < dishes.size(); i++) {
                Dish di = dishes.get(i);
                System.out.println("[DishServlet#doGet] idx=" + i + ", id=" + di.getDishId() + ", name=" + di.getName() + ", defaultPrice=" + di.getDefaultPrice() + ", createdAt=" + di.getCreatedAt());
            }
            req.setAttribute("dishes", dishes);
            if ("edit".equalsIgnoreCase(mode) && dishIdStr != null) {
                int dishId = Integer.parseInt(dishIdStr);
                Dish d = dishDAO.findById(dishId, restaurantId);
                System.out.println("[DishServlet#doGet] editDish id=" + d.getDishId() + ", defaultPrice=" + d.getDefaultPrice());
                req.setAttribute("editDish", d);
            }
            if ("new".equalsIgnoreCase(mode)) {
                req.setAttribute("newDish", true);
            }
            req.getRequestDispatcher("/admin/dish-management.jsp").forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        Integer restaurantId = (Integer) req.getAttribute("restaurantId");
        if (restaurantId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        try {
            if ("create".equalsIgnoreCase(action)) {
                String name = req.getParameter("name");
                String imageUrl = req.getParameter("imageUrl");
                String description = req.getParameter("description");
                String defaultPriceStr = req.getParameter("defaultPrice");
                System.out.println("[DishServlet#create] params: name=" + name + ", defaultPriceStr=" + defaultPriceStr + ", imageUrl=" + imageUrl + ", description=" + description);
                if (name == null || name.trim().isEmpty() || defaultPriceStr == null || defaultPriceStr.trim().isEmpty()) {
                    req.setAttribute("error", "请填写必填项：名称与默认价格");
                    req.setAttribute("newDish", true);
                    req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
                    req.getRequestDispatcher("/admin/dish-management.jsp").forward(req, resp);
                    return;
                }
                Dish d = new Dish();
                d.setRestaurantId(restaurantId);
                d.setName(name);
                d.setImageUrl(imageUrl);
                d.setDescription(description);
                try {
                    d.setDefaultPrice(new java.math.BigDecimal(defaultPriceStr));
                } catch (NumberFormatException nfe) {
                    System.out.println("[DishServlet#create] invalid defaultPriceStr=" + defaultPriceStr);
                    req.setAttribute("error", "默认价格格式不正确");
                    req.setAttribute("newDish", true);
                    req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
                    req.getRequestDispatcher("/admin/dish-management.jsp").forward(req, resp);
                    return;
                }
                System.out.println("[DishServlet#create] parsed defaultPrice=" + d.getDefaultPrice());
                dishDAO.save(d);
                resp.sendRedirect(req.getContextPath() + "/admin/dishes");
            } else if ("update".equalsIgnoreCase(action)) {
                int dishId = Integer.parseInt(req.getParameter("dishId"));
                String name = req.getParameter("name");
                String imageUrl = req.getParameter("imageUrl");
                String description = req.getParameter("description");
                String defaultPriceStr = req.getParameter("defaultPrice");
                System.out.println("[DishServlet#update] params: id=" + dishId + ", name=" + name + ", defaultPriceStr=" + defaultPriceStr);
                if (name == null || name.trim().isEmpty() || defaultPriceStr == null || defaultPriceStr.trim().isEmpty()) {
                    req.setAttribute("error", "请填写必填项：名称与默认价格");
                    Dish current = dishDAO.findById(dishId, restaurantId);
                    req.setAttribute("editDish", current);
                    req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
                    req.getRequestDispatcher("/admin/dish-management.jsp").forward(req, resp);
                    return;
                }
                Dish d = new Dish();
                d.setDishId(dishId);
                d.setRestaurantId(restaurantId);
                d.setName(name);
                d.setImageUrl(imageUrl);
                d.setDescription(description);
                try {
                    d.setDefaultPrice(new java.math.BigDecimal(defaultPriceStr));
                } catch (NumberFormatException nfe) {
                    System.out.println("[DishServlet#update] invalid defaultPriceStr=" + defaultPriceStr);
                    req.setAttribute("error", "默认价格格式不正确");
                    Dish current = dishDAO.findById(dishId, restaurantId);
                    req.setAttribute("editDish", current);
                    req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
                    req.getRequestDispatcher("/admin/dish-management.jsp").forward(req, resp);
                    return;
                }
                System.out.println("[DishServlet#update] parsed defaultPrice=" + d.getDefaultPrice());
                dishDAO.update(d);
                resp.sendRedirect(req.getContextPath() + "/admin/dishes");
            } else if ("delete".equalsIgnoreCase(action)) {
                int dishId = Integer.parseInt(req.getParameter("dishId"));
                int affected = dishDAO.deleteById(dishId, restaurantId);
                if (affected > 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/dishes");
                } else {
                    req.setAttribute("error", "该菜品存在订单引用，无法删除");
                    List<Dish> dishes = dishDAO.listByRestaurant(restaurantId);
                    req.setAttribute("dishes", dishes);
                    req.getRequestDispatcher("/admin/dish-management.jsp").forward(req, resp);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}