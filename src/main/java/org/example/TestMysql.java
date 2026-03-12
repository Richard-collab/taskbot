package org.example;

import org.example.chat.utils.MysqlClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用户实体类
 */
class User {
    private Integer id;
    private String name;
    private Integer age;
    private String email;

    // 构造方法、getter、setter省略
    public User() {}

    public User(Integer id, String name, Integer age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    // getter和setter方法...
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', age=" + age + ", email='" + email + "'}";
    }
}

/**
 * 使用示例
 */
public class TestMysql {

    public static void main(String[] args) {
        try {
            // 1. 创建用户表（如果不存在）
            createUserTable();

            // 2. 插入数据
            insertUser();

            // 3. 查询数据
            queryUsers();

            // 4. 更新数据
            updateUser();

            // 5. 删除数据
            deleteUser();

        } finally {
            // 关闭连接池
            MysqlClient.shutdown();
        }
    }

    private static void createUserTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL, " +
                "age INT, " +
                "email VARCHAR(100))";
        MysqlClient.executeUpdate(sql);
        System.out.println("用户表创建成功");
    }

    private static void insertUser() {
        // 插入单条数据
        String sql = "INSERT INTO users (name, age, email) VALUES (?, ?, ?)";
        int affectedRows = MysqlClient.executeUpdate(sql, "张三", 25, "zhangsan@example.com");
        System.out.println("插入数据成功，影响行数: " + affectedRows);

        // 批量插入
        List<Object[]> batchParams = new ArrayList<>();
        batchParams.add(new Object[]{"李四", 30, "lisi@example.com"});
        batchParams.add(new Object[]{"王五", 28, "wangwu@example.com"});
        batchParams.add(new Object[]{"赵六", 35, "zhaoliu@example.com"});

        int[] results = MysqlClient.executeBatch(sql, batchParams);
        System.out.println("批量插入完成，影响行数: " + Arrays.toString(results));
    }

    private static void queryUsers() {
        // 查询所有用户
        String sql = "SELECT * FROM users";
        List<User> users = MysqlClient.queryList(sql, rs -> new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("email")
        ));

        System.out.println("所有用户:");
        for (User user : users) {
            System.out.println(user);
        }

        // 查询单个用户
        String sql2 = "SELECT * FROM users WHERE id = ?";
        User user = MysqlClient.querySingleObject(sql2, rs -> new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("email")
        ), 1);

        System.out.println("ID为1的用户: " + user);

        // 查询单个值
        String sql3 = "SELECT COUNT(*) FROM users";
        Long count = (Long) MysqlClient.querySingleValue(sql3);
        System.out.println("用户总数: " + count);
    }

    private static void updateUser() {
        String sql = "UPDATE users SET age = ? WHERE name = ?";
        int affectedRows = MysqlClient.executeUpdate(sql, 26, "张三");
        System.out.println("更新数据成功，影响行数: " + affectedRows);
    }

    private static void deleteUser() {
        String sql = "DELETE FROM users WHERE name = ?";
        int affectedRows = MysqlClient.executeUpdate(sql, "赵六");
        System.out.println("删除数据成功，影响行数: " + affectedRows);
    }
}
