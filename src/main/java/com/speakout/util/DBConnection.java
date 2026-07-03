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
 * credentials, they are resolved once at startup from DB_URL/DB_USER/DB_PASSWORD
 * environment variables, falling back to src/main/resources/db.properties when
 * those aren't set — so each group member can still point at their own local
 * PostgreSQL without editing Java code, while a deployed server can be
 * configured purely through env vars. DAOs must use try-with-resources so
 * connections always close.
 */
public final class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }
        URL = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url"));
        USER = System.getenv().getOrDefault("DB_USER", props.getProperty("db.user"));
        PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password", ""));
        if (URL == null || USER == null) {
            throw new IllegalStateException(
                "Database not configured: set DB_URL/DB_USER env vars or provide db.properties");
        }
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
