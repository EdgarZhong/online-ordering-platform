package com.platform.ordering.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.platform.ordering.util.DBUtil;

@WebServlet(name = "MenusResourceApiServlet", urlPatterns = "/api/menus/*")
public class MenusResourceApiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        String path = req.getPathInfo();
        PrintWriter out = resp.getWriter();
        if (path == null || path.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Bad request\"}");
            return;
        }
        String[] parts = path.split("/");
        if (parts.length != 3 || !"items".equals(parts[2])) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\":\"Not found\"}");
            return;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            int menuId = Integer.parseInt(parts[1]);
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                    "SELECT d.dish_id, d.name, d.image_url, d.description, mi.price, mi.sort_order, mi.quantity " +
                            "FROM menu_items mi " +
                            "JOIN dishes d ON mi.dish_id = d.dish_id " +
                            "JOIN menus m ON mi.menu_id = m.menu_id " +
                            "WHERE mi.menu_id = ? " +
                            "ORDER BY mi.sort_order, mi.menu_item_id");
            ps.setInt(1, menuId);
            rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            StringBuilder sigInput = new StringBuilder();
            while (rs.next()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('{')
                        .append("\"dishId\":").append(rs.getInt("dish_id")).append(',')
                        .append("\"name\":\"").append(escape(rs.getString("name"))).append('\"').append(',')
                        .append("\"imageUrl\":\"").append(escape(rs.getString("image_url"))).append('\"').append(',')
                        .append("\"description\":\"").append(escape(rs.getString("description"))).append('\"').append(',')
                        .append("\"price\":").append(rs.getBigDecimal("price")).append(',')
                        .append("\"sortOrder\":").append(rs.getInt("sort_order")).append(',')
                        .append("\"defaultQuantity\":").append(rs.getInt("quantity"))
                        .append('}');
                sigInput.append(rs.getInt("dish_id")).append('|')
                        .append(rs.getInt("sort_order")).append('|')
                        .append(rs.getInt("quantity")).append('|')
                        .append(rs.getBigDecimal("price")).append(';');
            }
            sb.append(']');
            String signature = sha256Hex(sigInput.toString());
            String version = signature.length() >= 12 ? signature.substring(0, 12) : signature;
            resp.setHeader("ETag", "menu-" + menuId + "-" + version);
            resp.setHeader("X-Menu-Signature", signature);
            resp.setHeader("X-Menu-Version", version);
            out.write(sb.toString());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid menu id\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"Internal server error\"}");
        } finally {
            DBUtil.close(conn, ps, rs);
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

    private static String sha256Hex(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}