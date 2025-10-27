package com.platform.ordering.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类 (DBUtil)
 * <p>
 * 负责读取配置文件、加载驱动并提供数据库连接。
 * 这是项目中所有DAO实现获取数据库连接的唯一入口。
 * 使用静态代码块在类加载时初始化驱动和配置，保证效率。
 * </p>
 */
public class DBUtil {

    private static Properties props = new Properties();

    // 静态代码块，在类被加载到JVM时执行一次
    static {
        try {
            // 1. 使用类加载器读取配置文件
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (is == null) {
                throw new RuntimeException("数据库配置文件 'db.properties' 未找到!");
            }
            props.load(is);

            // 2. 加载数据库驱动
            Class.forName(props.getProperty("db.driver"));

        } catch (IOException e) {
            // 处理文件读取异常
            System.err.println("读取数据库配置文件失败");
            e.printStackTrace();
            throw new RuntimeException("加载数据库配置时发生IO异常", e);
        } catch (ClassNotFoundException e) {
            // 处理驱动加载异常
            System.err.println("数据库驱动加载失败");
            e.printStackTrace();
            throw new RuntimeException("找不到数据库驱动类", e);
        }
    }

    /**
     * 获取数据库连接
     *
     * @return Connection 数据库连接对象
     * @throws SQLException 如果连接失败
     */
    public static Connection getConnection() throws SQLException {
        // 3. 建立连接并返回
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
        );
    }

    /**
     * 关闭连接的辅助方法 (可重载以关闭Statement, ResultSet等)
     *
     * @param conn 需要关闭的连接
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace(); // 在生产环境中，这里应该使用日志框架记录错误
            }
        }
    }

    /**
     * 关闭数据库资源的重载方法
     * <p>
     * 按照ResultSet -> Statement -> Connection的顺序安全地关闭资源。
     * 这是推荐在DAO中使用的关闭方法。
     * </p>
     *
     * @param conn Connection对象，可以为null
     * @param stmt Statement或PreparedStatement对象，可以为null
     * @param rs   ResultSet对象，可以为null
     */
    public static void close(Connection conn, java.sql.Statement stmt, java.sql.ResultSet rs) {
        // 1. 关闭结果集
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // 2. 关闭语句
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // 3. 关闭连接
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
