package com.platform.ordering.controller;

import com.platform.ordering.dao.RestaurantDAO;
import com.platform.ordering.dao.RestaurantDAOImpl;
import com.platform.ordering.model.Restaurant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/restaurant")
public class RestaurantServlet extends HttpServlet {
    private final RestaurantDAO restaurantDAO = new RestaurantDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer restaurantId = (Integer) req.getAttribute("restaurantId");
        if (restaurantId == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        try {
            Restaurant r = restaurantDAO.findById(restaurantId);
            req.setAttribute("restaurant", r);
            req.getRequestDispatcher("/admin/restaurant-settings.jsp").forward(req, resp);
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
        if ("update".equalsIgnoreCase(action)) {
            String name = req.getParameter("name");
            String address = req.getParameter("address");
            String phone = req.getParameter("phone");
            String description = req.getParameter("description");
            String logoUrl = req.getParameter("logoUrl");
            Restaurant r = new Restaurant();
            r.setRestaurantId(restaurantId);
            r.setName(name);
            r.setAddress(address);
            r.setPhone(phone);
            r.setDescription(description);
            r.setLogoUrl(logoUrl);
            try {
                restaurantDAO.update(r);
                resp.sendRedirect(req.getContextPath() + "/admin/restaurant");
            } catch (SQLException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}