package com.platform.ordering.api;

import com.platform.ordering.dao.RestaurantDAOImpl;
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

@WebServlet(name = "RestaurantsApiServlet", urlPatterns = "/api/restaurants")
public class RestaurantsApiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        PrintWriter out = resp.getWriter();
        try {
            List<Restaurant> restaurants = new RestaurantDAOImpl().listAll();
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < restaurants.size(); i++) {
                Restaurant r = restaurants.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                        .append("\"restaurantId\":").append(r.getRestaurantId()).append(',')
                        .append("\"name\":\"").append(escape(r.getName())).append('\"').append(',')
                        .append("\"address\":\"").append(escape(r.getAddress())).append('\"').append(',')
                        .append("\"phone\":\"").append(escape(r.getPhone())).append('\"').append(',')
                        .append("\"logoUrl\":\"").append(escape(r.getLogoUrl())).append('\"').append(',')
                        .append("\"description\":\"").append(escape(r.getDescription())).append('\"')
                        .append('}');
            }
            sb.append(']');
            out.write(sb.toString());
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Internal server error\"}");
        } finally {
            out.flush();
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