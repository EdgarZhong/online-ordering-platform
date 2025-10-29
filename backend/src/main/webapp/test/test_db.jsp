<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.platform.ordering.util.DBUtil" %>
<%@ page import="com.platform.ordering.dao.UserDAO" %>
<%@ page import="com.platform.ordering.dao.UserDAOImpl" %>
<%@ page import="com.platform.ordering.model.User" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.io.PrintWriter" %>
<html>
<head>
    <title>JDBC Connection Test</title>
</head>
<body>
    <h1>Testing Database Connection and DAO...</h1>
    <hr>
    <% 
        try {
            // 1. Test basic connection
            out.println("<h3>Step 1: Attempting to get a connection from DBUtil...</h3>");
            Connection conn = DBUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                out.println("<p style='color:green;'>SUCCESS: Connection object obtained and is open.</p>");
                conn.close(); // Close it immediately after test
                out.println("<p>INFO: Connection closed.</p>");
            } else {
                out.println("<p style='color:red;'>FAILURE: DBUtil.getConnection() returned null or a closed connection.</p>");
            }

            // 2. Test DAO layer
            out.println("<h3>Step 2: Attempting to find user 'testmerchant' via UserDAO...</h3>");
            UserDAO userDAO = new UserDAOImpl();
            User user = userDAO.findByUsername("testmerchant");

            if (user != null) {
                out.println("<p style='color:green;'>SUCCESS: UserDAO found the user.</p>");
                out.println("<pre>" + user.toString() + "</pre>");
            } else {
                out.println("<p style='color:red;'>FAILURE: userDAO.findByUsername(\"testmerchant\") returned null. User not found in database.</p>");
            }

        } catch (Exception e) {
            out.println("<h3 style='color:red;'>AN EXCEPTION OCCURRED:</h3>");
            out.println("<pre>");
            // Print stack trace to the web page for debugging
            PrintWriter pw = new PrintWriter(out);
            e.printStackTrace(pw);
            out.println("</pre>");
        }
    %>
    <hr>
    <h2>Test Complete.</h2>
</body>
</html>