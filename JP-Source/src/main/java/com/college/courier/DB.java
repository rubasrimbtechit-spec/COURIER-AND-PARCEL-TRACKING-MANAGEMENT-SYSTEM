package com.college.courier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {
    private DB() {}
    public static Connection getConnection() throws SQLException {
        String url = required("DB_URL");
        String user = required("DB_USER");
        String password = required("DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }
    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is not set: " + name);
        }
        return value;
    }
}
