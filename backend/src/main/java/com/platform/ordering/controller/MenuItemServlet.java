package com.platform.ordering.controller;

import com.platform.ordering.dao.MenuDAO;
import com.platform.ordering.dao.MenuDAOImpl;
import com.platform.ordering.dao.DishDAO;
import com.platform.ordering.dao.DishDAOImpl;
import com.platform.ordering.dao.MenuItemDAO;
import com.platform.ordering.dao.MenuItemDAOImpl;
import com.platform.ordering.model.MenuItem;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet("/admin/menu-items")
public class MenuItemServlet extends HttpServlet {
    private final MenuDAO menuDAO = new MenuDAOImpl();
    private final DishDAO dishDAO = new DishDAOImpl();
    private final MenuItemDAO menuItemDAO = new MenuItemDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer restaurantId = (Integer) req.getAttribute("restaurantId");
        if (restaurantId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        try {
            String menuIdStr = req.getParameter("menuId");
            req.setAttribute("menus", menuDAO.listByRestaurant(restaurantId));
            req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
            if (menuIdStr != null && !menuIdStr.isEmpty()) {
                int menuId = Integer.parseInt(menuIdStr);
                req.setAttribute("menuItems", menuItemDAO.listByMenu(menuId, restaurantId));
                req.setAttribute("selectedMenuId", menuId);
            }
            req.getRequestDispatcher("/admin/menu-management.jsp").forward(req, resp);
        } catch (SQLException e) {
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
            if ("bind".equalsIgnoreCase(action)) {
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                int dishId = Integer.parseInt(req.getParameter("dishId"));
                BigDecimal price = new BigDecimal(req.getParameter("price"));
                MenuItem mi = new MenuItem();
                mi.setMenuId(menuId);
                mi.setDishId(dishId);
                mi.setPrice(price);
                menuItemDAO.save(mi, restaurantId);
                resp.sendRedirect(req.getContextPath() + "/admin/menu-items?menuId=" + menuId);
            } else if ("updatePrice".equalsIgnoreCase(action)) {
                int menuItemId = Integer.parseInt(req.getParameter("menuItemId"));
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                BigDecimal price = new BigDecimal(req.getParameter("price"));
                menuItemDAO.updatePrice(menuItemId, restaurantId, price);
                resp.sendRedirect(req.getContextPath() + "/admin/menu-items?menuId=" + menuId);
            } else if ("delete".equalsIgnoreCase(action)) {
                int menuItemId = Integer.parseInt(req.getParameter("menuItemId"));
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                menuItemDAO.deleteById(menuItemId, restaurantId);
                resp.sendRedirect(req.getContextPath() + "/admin/menu-items?menuId=" + menuId);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            String menuIdStr = req.getParameter("menuId");
            req.setAttribute("error", "绑定或更新失败，可能存在重复或数据不合法");
            try {
                req.setAttribute("menus", menuDAO.listByRestaurant(restaurantId));
                req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
                if (menuIdStr != null && !menuIdStr.isEmpty()) {
                    int menuId = Integer.parseInt(menuIdStr);
                    req.setAttribute("menuItems", menuItemDAO.listByMenu(menuId, restaurantId));
                    req.setAttribute("selectedMenuId", menuId);
                }
            } catch (SQLException ignored) {}
            req.getRequestDispatcher("/admin/menu-management.jsp").forward(req, resp);
        }
    }
}