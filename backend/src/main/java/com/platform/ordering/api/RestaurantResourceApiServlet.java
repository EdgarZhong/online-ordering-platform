package com.platform.ordering.api;

import com.platform.ordering.dao.MenuDAOImpl;
import com.platform.ordering.dao.RestaurantDAOImpl;
import com.platform.ordering.model.Menu;
import com.platform.ordering.model.Restaurant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "RestaurantResourceApiServlet", urlPatterns = "/api/restaurants/*")
public class RestaurantResourceApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String path = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Bad request\"}");
            return;
        }

        String[] parts = path.split("/");
        // Expected: /{id} or /{id}/menus
        if (parts.length < 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Bad request\"}");
            return;
        }

        try {
            int restaurantId = Integer.parseInt(parts[1]);
            if (parts.length == 2) {
                Restaurant r = new RestaurantDAOImpl().findById(restaurantId);
                if (r == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"error\":\"Restaurant not found\"}");
                    return;
                }
                out.write('{'
                        + "\"restaurantId\":" + r.getRestaurantId() + ','
                        + "\"name\":\"" + escape(r.getName()) + "\"," 
                        + "\"address\":\"" + escape(r.getAddress()) + "\"," 
                        + "\"phone\":\"" + escape(r.getPhone()) + "\"," 
                        + "\"logoUrl\":\"" + escape(r.getLogoUrl()) + "\"," 
                        + "\"description\":\"" + escape(r.getDescription()) + "\""
                        + '}');
                return;
            }

            if (parts.length == 3 && "menus".equals(parts[2])) {
                List<Menu> menus = new MenuDAOImpl().listByRestaurant(restaurantId);
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                for (int i = 0; i < menus.size(); i++) {
                    Menu m = menus.get(i);
                    if (i > 0) sb.append(',');
                    sb.append('{')
                            .append("\"menuId\":").append(m.getMenuId()).append(',')
                            .append("\"name\":\"").append(escape(m.getName())).append('\"').append(',')
                            .append("\"description\":\"").append(escape(m.getDescription())).append('\"')
                            .append('}');
                }
                sb.append(']');
                out.write(sb.toString());
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\":\"Not found\"}");
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid restaurant id\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Internal server error\"}");
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = hex.length(); j < 4; j++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}