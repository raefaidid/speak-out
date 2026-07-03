package com.speakout.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection utility (Chapter 7-2 pattern: static createConnection()
 * using Class.forName + DriverManager).
 *
 * One deviation from the lecture example: instead of hardcoding the URL and
 * credentials, they are read once from src/main/resources/db.properties so
 * each group member can point at their own local PostgreSQL without editing
 * Java code. DAOs must use try-with-resources so connections always close.
 */
public final class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password", "");
    }

    private DBConnection() {
    }

    public static Connection createConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");   // step 1: load the JDBC driver
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL driver not on classpath", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);   // step 2: establish the connection
    }
}
