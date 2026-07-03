package com.speakout.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.speakout.bean.UserBean;
import com.speakout.util.DBConnection;

public class UserDAO {

    private static final String BASE =
        "SELECT u.user_id, u.school_id, u.full_name, u.email, u.password_hash, u.role, " +
        "       u.class_form, u.is_active, u.created_at, s.name AS school_name, s.code AS school_code " +
        "FROM users u JOIN school s ON s.school_id = u.school_id ";

    public UserBean findByEmail(String email) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE lower(u.email) = lower(?)")) {
            ps.setString(1, email == null ? "" : email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public UserBean findById(String userId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE u.user_id = ?")) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE lower(email) = lower(?)")) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new user and returns the generated user id (e.g. USR-1001). */
    public String insert(UserBean u) throws SQLException {
        String sql = "INSERT INTO users (user_id, school_id, full_name, email, password_hash, role, class_form) " +
                     "VALUES ('USR-' || nextval('user_seq'), ?, ?, ?, ?, ?, ?) RETURNING user_id";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getSchoolId());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getEmail().trim().toLowerCase());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getRole());
            ps.setString(6, u.getClassForm());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    public void updateProfile(String userId, String fullName, String classForm) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE users SET full_name = ?, class_form = ? WHERE user_id = ?")) {
            ps.setString(1, fullName);
            ps.setString(2, classForm);
            ps.setString(3, userId);
            ps.executeUpdate();
        }
    }

    /** Active teachers of a school, least-loaded (fewest open cases) first. */
    public List<UserBean> teachersOfSchool(String schoolId) throws SQLException {
        String sql = BASE +
            "LEFT JOIN cases cs ON cs.assigned_to = u.user_id AND cs.status IN ('New', 'Under Investigation') " +
            "WHERE u.school_id = ? AND u.role = 'Teacher' AND u.is_active " +
            "GROUP BY u.user_id, u.school_id, u.full_name, u.email, u.password_hash, u.role, " +
            "         u.class_form, u.is_active, u.created_at, s.name, s.code " +
            "ORDER BY count(cs.case_id), u.full_name";
        List<UserBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, schoolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    private UserBean map(ResultSet rs) throws SQLException {
        UserBean u = new UserBean();
        u.setUserId(rs.getString("user_id"));
        u.setSchoolId(rs.getString("school_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setClassForm(rs.getString("class_form"));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setSchoolName(rs.getString("school_name"));
        u.setSchoolCode(rs.getString("school_code"));
        return u;
    }
}
