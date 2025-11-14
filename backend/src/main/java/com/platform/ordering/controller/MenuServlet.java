/*
 * @Author: EdgarZhong 18518713412@163.com
 * @Date: 2025-11-14 19:30:47
 * @LastEditors: EdgarZhong 18518713412@163.com
 * @LastEditTime: 2025-11-15 02:43:39
 * @FilePath: \final\online-ordering-platform\backend\src\main\java\com\platform\ordering\controller\MenuServlet.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.platform.ordering.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.platform.ordering.dao.DishDAO;
import com.platform.ordering.dao.DishDAOImpl;
import com.platform.ordering.dao.MenuDAO;
import com.platform.ordering.dao.MenuDAOImpl;
import com.platform.ordering.model.Dish;
import com.platform.ordering.model.Menu;

@WebServlet("/admin/menus")
public class MenuServlet extends HttpServlet {
    private final MenuDAO menuDAO = new MenuDAOImpl();
    private final DishDAO dishDAO = new DishDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer restaurantId = (Integer) req.getAttribute("restaurantId");
        if (restaurantId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        try {
            String action = req.getParameter("action");
            String menuIdStr = req.getParameter("menuId");
            List<Menu> menus = menuDAO.listByRestaurant(restaurantId);
            List<Dish> dishes = dishDAO.listByRestaurant(restaurantId);
            req.setAttribute("menus", menus);
            req.setAttribute("dishes", dishes);
            if ("edit".equalsIgnoreCase(action) && menuIdStr != null) {
                int menuId = Integer.parseInt(menuIdStr);
                Menu m = menuDAO.findById(menuId, restaurantId);
                req.setAttribute("editMenu", m);
                req.setAttribute("menuItems", new com.platform.ordering.dao.MenuItemDAOImpl().listByMenu(menuId, restaurantId));
                req.setAttribute("selectedMenuId", menuId);
                com.platform.ordering.model.DraftMenu draft = new com.platform.ordering.model.DraftMenu();
                draft.setMenuId(menuId);
                draft.setRestaurantId(restaurantId);
                draft.setName(m.getName());
                draft.setDescription(m.getDescription());
                draft.setPackage(m.isPackage());
                java.util.List<com.platform.ordering.model.DraftMenuItem> ditems = new java.util.ArrayList<>();
                for (com.platform.ordering.model.MenuItem mi : new com.platform.ordering.dao.MenuItemDAOImpl().listByMenu(menuId, restaurantId)) {
                    com.platform.ordering.model.DraftMenuItem di = new com.platform.ordering.model.DraftMenuItem();
                    di.setDishId(mi.getDishId());
                    di.setPrice(mi.getPrice());
                    di.setQuantity(mi.getQuantity());
                    di.setSortOrder(ditems.size());
                    ditems.add(di);
                }
                draft.setItems(ditems);
                req.getSession().setAttribute("menuDraft", draft);
            } else if ("expand".equalsIgnoreCase(action) && menuIdStr != null) {
                int menuId = Integer.parseInt(menuIdStr);
                Menu m = menuDAO.findById(menuId, restaurantId);
                req.setAttribute("menuItems", new com.platform.ordering.dao.MenuItemDAOImpl().listByMenu(menuId, restaurantId));
                req.setAttribute("selectedMenuId", menuId);
                req.setAttribute("selectedMenuName", m != null ? m.getName() : "当前菜单");
            } else if ("new".equalsIgnoreCase(action)) {
                com.platform.ordering.model.DraftMenu draft = new com.platform.ordering.model.DraftMenu();
                draft.setRestaurantId(restaurantId);
                draft.setName("");
                draft.setDescription("");
                draft.setPackage(false);
                draft.setMenuId(0);
                req.getSession().setAttribute("menuDraft", draft);
                Menu view = new Menu();
                view.setRestaurantId(restaurantId);
                view.setName("");
                view.setDescription("");
                view.setPackage(false);
                view.setSortOrder(0);
                req.setAttribute("editMenu", view);
                req.setAttribute("menuItems", new java.util.ArrayList<com.platform.ordering.model.MenuItem>());
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
            if ("create".equalsIgnoreCase(action)) {
                String name = req.getParameter("name");
                String description = req.getParameter("description");
                boolean isPackage = "on".equals(req.getParameter("isPackage"));
                Menu m = new Menu();
                m.setRestaurantId(restaurantId);
                m.setName(name);
                m.setDescription(description);
                m.setPackage(isPackage);
                m.setSortOrder(0);
                int newId = menuDAO.saveAndReturnId(m);
                if (newId > 0) {
                    resp.sendRedirect(req.getContextPath() + "/admin/menus?action=edit&menuId=" + newId);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/admin/menus?error=创建失败");
                }
            } else if ("update".equalsIgnoreCase(action)) {
                String menuIdStrParam = req.getParameter("menuId");
                int menuId = (menuIdStrParam != null && !menuIdStrParam.isEmpty()) ? Integer.parseInt(menuIdStrParam) : 0;
                String name = req.getParameter("name");
                String description = req.getParameter("description");
                boolean isPackage = "on".equals(req.getParameter("isPackage"));
                String[] ids = req.getParameterValues("menuItemId");
                String[] prices = req.getParameterValues("price");
                String[] qtys = req.getParameterValues("quantity");
                String[] orders = req.getParameterValues("sortOrder");
                String[] dishIds = req.getParameterValues("dishId");

                if (menuId == 0) {
                    java.sql.Connection conn = null;
                    java.sql.PreparedStatement psMenu = null;
                    java.sql.PreparedStatement psItemIns = null;
                    java.sql.ResultSet keys = null;
                    String sql_insertMenu = "INSERT INTO menus (restaurant_id, name, description, is_package, sort_order) VALUES (?, ?, ?, ?, COALESCE((SELECT MAX(sort_order)+1 FROM menus WHERE restaurant_id = ?),0))";
                    String sql_insertItem = "INSERT INTO menu_items (menu_id, dish_id, price, quantity, sort_order) VALUES (?, ?, ?, ?, ?)";
                    try {
                        conn = com.platform.ordering.util.DBUtil.getConnection();
                        conn.setAutoCommit(false);
                        psMenu = conn.prepareStatement(sql_insertMenu, java.sql.Statement.RETURN_GENERATED_KEYS);
                        psMenu.setInt(1, restaurantId);
                        psMenu.setString(2, name);
                        psMenu.setString(3, description);
                        psMenu.setBoolean(4, isPackage);
                        psMenu.setInt(5, restaurantId);
                        psMenu.executeUpdate();
                        keys = psMenu.getGeneratedKeys();
                        if (keys.next()) {
                            menuId = keys.getInt(1);
                        }
                        java.util.List<com.platform.ordering.model.DraftMenuItem> toPersist = new java.util.ArrayList<>();
                        if (ids != null && dishIds != null) {
                            for (int i = 0; i < ids.length; i++) {
                                com.platform.ordering.model.DraftMenuItem di = new com.platform.ordering.model.DraftMenuItem();
                                di.setDishId((dishIds != null && i < dishIds.length && dishIds[i] != null && !dishIds[i].isEmpty()) ? Integer.parseInt(dishIds[i]) : 0);
                                di.setQuantity((qtys != null && i < qtys.length && qtys[i] != null && !qtys[i].isEmpty()) ? Integer.parseInt(qtys[i]) : 1);
                                di.setPrice((prices != null && i < prices.length && prices[i] != null && !prices[i].isEmpty()) ? new java.math.BigDecimal(prices[i]) : java.math.BigDecimal.ZERO);
                                di.setSortOrder((orders != null && i < orders.length && orders[i] != null && !orders[i].isEmpty()) ? Integer.parseInt(orders[i]) : i);
                                toPersist.add(di);
                            }
                        } else {
                            com.platform.ordering.model.DraftMenu draft = (com.platform.ordering.model.DraftMenu) req.getSession().getAttribute("menuDraft");
                            if (draft != null && draft.getItems() != null) toPersist = draft.getItems();
                        }
                        psItemIns = conn.prepareStatement(sql_insertItem);
                        for (int i = 0; i < toPersist.size(); i++) {
                            com.platform.ordering.model.DraftMenuItem di = toPersist.get(i);
                            psItemIns.setInt(1, menuId);
                            psItemIns.setInt(2, di.getDishId());
                            psItemIns.setBigDecimal(3, di.getPrice());
                            psItemIns.setInt(4, di.getQuantity());
                            psItemIns.setInt(5, di.getSortOrder());
                            psItemIns.addBatch();
                        }
                        if (!toPersist.isEmpty()) psItemIns.executeBatch();
                        conn.commit();
                        resp.sendRedirect(req.getContextPath() + "/admin/menus");
                    } catch (java.sql.SQLException e) {
                        try { if (conn != null) conn.rollback(); } catch (java.sql.SQLException ignored) {}
                        resp.sendRedirect(req.getContextPath() + "/admin/menus?error=保存失败");
                    } finally {
                        try { if (keys != null) keys.close(); } catch (java.sql.SQLException ignored) {}
                        com.platform.ordering.util.DBUtil.close(null, psItemIns, null);
                        com.platform.ordering.util.DBUtil.close(null, psMenu, null);
                        com.platform.ordering.util.DBUtil.close(conn, null, null);
                    }
                } else {
                    Menu current = menuDAO.findById(menuId, restaurantId);
                    com.platform.ordering.model.DraftMenu draft = (com.platform.ordering.model.DraftMenu) req.getSession().getAttribute("menuDraft");
                    Menu m = new Menu();
                    m.setMenuId(menuId);
                    m.setRestaurantId(restaurantId);
                    m.setName(draft != null ? draft.getName() : name);
                    m.setDescription(draft != null ? draft.getDescription() : description);
                    m.setPackage(draft != null ? draft.isPackage() : isPackage);
                    m.setSortOrder(current != null ? current.getSortOrder() : 0);
                    // 事务：删除旧项并按草稿重建
                    java.sql.Connection conn = null;
                    java.sql.PreparedStatement upd = null;
                    java.sql.PreparedStatement del = null;
                    java.sql.PreparedStatement ins = null;
                    try {
                        conn = com.platform.ordering.util.DBUtil.getConnection();
                        conn.setAutoCommit(false);
                        upd = conn.prepareStatement("UPDATE menus SET name=?, description=?, is_package=? WHERE menu_id=? AND restaurant_id=?");
                        upd.setString(1, m.getName());
                        upd.setString(2, m.getDescription());
                        upd.setBoolean(3, m.isPackage());
                        upd.setInt(4, menuId);
                        upd.setInt(5, restaurantId);
                        upd.executeUpdate();
                        del = conn.prepareStatement("DELETE FROM menu_items WHERE menu_id=?");
                        del.setInt(1, menuId);
                        del.executeUpdate();
                        ins = conn.prepareStatement("INSERT INTO menu_items (menu_id, dish_id, price, quantity, sort_order) VALUES (?, ?, ?, ?, ?)");
                        java.util.List<com.platform.ordering.model.DraftMenuItem> toPersist = new java.util.ArrayList<>();
                        if (ids != null && dishIds != null) {
                            for (int i = 0; i < ids.length; i++) {
                                com.platform.ordering.model.DraftMenuItem di = new com.platform.ordering.model.DraftMenuItem();
                                di.setDishId((dishIds != null && i < dishIds.length && dishIds[i] != null && !dishIds[i].isEmpty()) ? Integer.parseInt(dishIds[i]) : 0);
                                di.setQuantity((qtys != null && i < qtys.length && qtys[i] != null && !qtys[i].isEmpty()) ? Integer.parseInt(qtys[i]) : 1);
                                di.setPrice((prices != null && i < prices.length && prices[i] != null && !prices[i].isEmpty()) ? new java.math.BigDecimal(prices[i]) : java.math.BigDecimal.ZERO);
                                di.setSortOrder((orders != null && i < orders.length && orders[i] != null && !orders[i].isEmpty()) ? Integer.parseInt(orders[i]) : i);
                                toPersist.add(di);
                            }
                        } else if (draft != null && draft.getItems() != null) {
                            toPersist = draft.getItems();
                        }
                        for (int i = 0; i < toPersist.size(); i++) {
                            com.platform.ordering.model.DraftMenuItem di = toPersist.get(i);
                            ins.setInt(1, menuId);
                            ins.setInt(2, di.getDishId());
                            ins.setBigDecimal(3, di.getPrice());
                            ins.setInt(4, di.getQuantity());
                            ins.setInt(5, di.getSortOrder());
                            ins.addBatch();
                        }
                        if (!toPersist.isEmpty()) ins.executeBatch();
                        conn.commit();
                    } catch (java.sql.SQLException e) {
                        try { if (conn != null) conn.rollback(); } catch (java.sql.SQLException ignored) {}
                        resp.sendRedirect(req.getContextPath() + "/admin/menus?error=保存失败");
                        return;
                    } finally {
                        com.platform.ordering.util.DBUtil.close(null, ins, null);
                        com.platform.ordering.util.DBUtil.close(null, del, null);
                        com.platform.ordering.util.DBUtil.close(null, upd, null);
                        com.platform.ordering.util.DBUtil.close(conn, null, null);
                        req.getSession().removeAttribute("menuDraft");
                    }
                    resp.sendRedirect(req.getContextPath() + "/admin/menus");
                }
            } else if ("delete".equalsIgnoreCase(action)) {
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                menuDAO.deleteById(menuId, restaurantId);
                resp.sendRedirect(req.getContextPath() + "/admin/menus");
            } else if ("moveUp".equalsIgnoreCase(action)) {
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                menuDAO.moveUp(menuId, restaurantId);
                resp.sendRedirect(req.getContextPath() + "/admin/menus");
            } else if ("moveDown".equalsIgnoreCase(action)) {
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                menuDAO.moveDown(menuId, restaurantId);
                resp.sendRedirect(req.getContextPath() + "/admin/menus");
            } else if ("saveItems".equalsIgnoreCase(action)) {
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                String[] ids = req.getParameterValues("menuItemId");
                String[] prices = req.getParameterValues("price");
                String[] qtys = req.getParameterValues("quantity");
                String[] orders = req.getParameterValues("sortOrder");
                com.platform.ordering.dao.MenuItemDAOImpl mid = new com.platform.ordering.dao.MenuItemDAOImpl();
                if (ids != null) {
                    for (int i = 0; i < ids.length; i++) {
                        int miId = Integer.parseInt(ids[i]);
                        int qty = Integer.parseInt(qtys[i]);
                        if (qty == 0) {
                            mid.deleteById(miId, restaurantId);
                            continue;
                        }
                        java.math.BigDecimal price = new java.math.BigDecimal(prices[i]);
                        int sort = Integer.parseInt(orders[i]);
                        mid.updatePrice(miId, restaurantId, price);
                        mid.updateQuantity(miId, restaurantId, qty);
                        mid.updateSortOrder(miId, restaurantId, sort);
                    }
                }
                resp.sendRedirect(req.getContextPath() + "/admin/menus?action=edit&menuId=" + menuId);
            } else if ("addDish".equalsIgnoreCase(action)) {
                int menuId = Integer.parseInt(req.getParameter("menuId"));
                int dishId = Integer.parseInt(req.getParameter("dishId"));
                String priceStr = req.getParameter("price");
                java.math.BigDecimal price;
                try {
                    price = (priceStr != null && !priceStr.isEmpty()) ? new java.math.BigDecimal(priceStr) : java.math.BigDecimal.ZERO;
                } catch (NumberFormatException nfe) {
                    price = java.math.BigDecimal.ZERO;
                }
                String draftName = req.getParameter("draftName");
                String draftDescription = req.getParameter("draftDescription");
                String draftIsPackage = req.getParameter("draftIsPackage");
                com.platform.ordering.model.DraftMenu draft = (com.platform.ordering.model.DraftMenu) req.getSession().getAttribute("menuDraft");
                if (draft == null) {
                    draft = new com.platform.ordering.model.DraftMenu();
                    draft.setRestaurantId(restaurantId);
                    draft.setMenuId(menuId);
                }
                if (draftName != null) draft.setName(draftName);
                if (draftDescription != null) draft.setDescription(draftDescription);
                draft.setPackage("on".equals(draftIsPackage));
                com.platform.ordering.model.DraftMenuItem di = new com.platform.ordering.model.DraftMenuItem();
                di.setDishId(dishId);
                di.setPrice(price);
                di.setQuantity(1);
                di.setSortOrder(draft.getItems().size());
                draft.getItems().add(di);
                req.getSession().setAttribute("menuDraft", draft);
                // forward 到编辑视图
                req.setAttribute("menus", menuDAO.listByRestaurant(restaurantId));
                req.setAttribute("dishes", dishDAO.listByRestaurant(restaurantId));
                Menu view = new Menu();
                view.setMenuId(draft.getMenuId());
                view.setRestaurantId(restaurantId);
                view.setName(draft.getName());
                view.setDescription(draft.getDescription());
                view.setPackage(draft.isPackage());
                req.setAttribute("editMenu", view);
                req.getRequestDispatcher("/admin/menu-management.jsp").forward(req, resp);
            } else if ("reorder".equalsIgnoreCase(action)) {
                String order = req.getParameter("order");
                if (order == null || order.trim().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                String[] parts = order.split(",");
                java.util.List<Integer> ids = new java.util.ArrayList<>();
                for (String p : parts) {
                    ids.add(Integer.parseInt(p.trim()));
                }
                menuDAO.reorder(restaurantId, ids);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
