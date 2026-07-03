package com.speakout.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.speakout.bean.SchoolBean;
import com.speakout.util.DBConnection;

public class SchoolDAO {

    public SchoolBean findByCode(String code) throws SQLException {
        String sql = "SELECT school_id, code, name, address FROM school WHERE upper(code) = upper(?)";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code == null ? "" : code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public SchoolBean findById(String schoolId) throws SQLException {
        String sql = "SELECT school_id, code, name, address FROM school WHERE school_id = ?";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, schoolId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private SchoolBean map(ResultSet rs) throws SQLException {
        SchoolBean s = new SchoolBean();
        s.setSchoolId(rs.getString("school_id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setAddress(rs.getString("address"));
        return s;
    }
}
