package org.example.chat.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * MySQL数据库操作工具类（使用HikariCP连接池）
 */
public class MysqlClient {

    private static HikariDataSource dataSource;

    // 静态代码块初始化连接池
    static {
        try {
            // 加载配置文件
            Properties props = new Properties();
            InputStream inputStream = MysqlClient.class.getClassLoader()
                    .getResourceAsStream("db.properties");
            if (inputStream == null) {
                throw new RuntimeException("找不到数据库配置文件db.properties");
            }
            props.load(inputStream);

            // 创建HikariCP配置
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(props.getProperty("db.driver"));
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));

            // 连接池配置
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("pool.minimumIdle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("pool.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("pool.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("pool.maxLifetime", "1800000")));

            // 创建数据源
            dataSource = new HikariDataSource(config);

        } catch (IOException e) {
            throw new RuntimeException("初始化数据库连接池失败", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭连接（实际是返还给连接池）
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭PreparedStatement
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭ResultSet
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行更新操作（INSERT、UPDATE、DELETE）
     * @param sql SQL语句
     * @param params 参数列表
     * @return 受影响的行数
     */
    public static int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            // 设置参数
            setParameters(pstmt, params);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("执行更新操作失败", e);
        } finally {
            closeStatement(pstmt);
            closeConnection(conn);
        }
    }

    /**
     * 执行查询操作
     * @param sql SQL语句
     * @param params 参数列表
     * @return 结果集
     */
    public static ResultSet executeQuery(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            // 设置参数
            setParameters(pstmt, params);

            rs = pstmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            // 关闭资源
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
            throw new RuntimeException("执行查询操作失败", e);
        }
        // 注意：调用者需要负责关闭ResultSet、Statement和Connection
    }

    /**
     * 查询单个值
     * @param sql SQL语句
     * @param params 参数列表
     * @return 查询结果
     */
    public static Object querySingleValue(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            setParameters(pstmt, params);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("查询单个值失败", e);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
    }

    /**
     * 查询列表（使用RowMapper将结果集映射为对象）
     * @param sql SQL语句
     * @param mapper 行映射器
     * @param params 参数列表
     * @return 对象列表
     */
    public static <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<>();

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            setParameters(pstmt, params);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                T obj = mapper.mapRow(rs);
                list.add(obj);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("查询列表失败", e);
        } finally {
            closeResultSet(rs);
            closeStatement(pstmt);
            closeConnection(conn);
        }
    }

    /**
     * 查询单个对象
     * @param sql SQL语句
     * @param mapper 行映射器
     * @param params 参数列表
     * @return 单个对象
     */
    public static <T> T querySingleObject(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = queryList(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 批量更新操作
     * @param sql SQL语句
     * @param batchParams 批量参数
     * @return 受影响的行数数组
     */
    public static int[] executeBatch(String sql, List<Object[]> batchParams) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            // 设置批量参数
            for (Object[] params : batchParams) {
                setParameters(pstmt, params);
                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("批量更新操作失败", e);
        } finally {
            closeStatement(pstmt);
            closeConnection(conn);
        }
    }

    /**
     * 设置PreparedStatement参数
     */
    private static void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 行映射器接口（用于将ResultSet映射为对象）
     */
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }

    /**
     * 关闭连接池
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}