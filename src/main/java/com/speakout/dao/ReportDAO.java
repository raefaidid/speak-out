package com.speakout.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.speakout.bean.ReportBean;
import com.speakout.util.DBConnection;

public class ReportDAO {

    private static final String BASE =
        "SELECT r.report_id, r.school_id, r.reporter_id, r.category_id, r.title, r.description, " +
        "       r.location, r.incident_date, r.severity, r.anonymity_flag, r.status, " +
        "       r.submitted_at, r.updated_at, " +
        "       c.name AS category_name, u.full_name AS reporter_name, u.class_form AS reporter_class, " +
        "       s.name AS school_name, cs.case_id " +
        "FROM reports r " +
        "JOIN category c ON c.category_id = r.category_id " +
        "JOIN users u    ON u.user_id = r.reporter_id " +
        "JOIN school s   ON s.school_id = r.school_id " +
        "LEFT JOIN cases cs ON cs.report_id = r.report_id ";

    /** Inserts and returns the generated report id (e.g. SO-0413). */
    public String insert(ReportBean r) throws SQLException {
        String sql = "INSERT INTO reports (report_id, school_id, reporter_id, category_id, title, description, " +
                     "location, incident_date, severity, anonymity_flag, status) " +
                     "VALUES ('SO-' || lpad(nextval('report_seq')::text, 4, '0'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "RETURNING report_id";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getSchoolId());
            ps.setString(2, r.getReporterId());
            ps.setString(3, r.getCategoryId());
            ps.setString(4, r.getTitle());
            ps.setString(5, r.getDescription());
            ps.setString(6, r.getLocation());
            ps.setDate(7, r.getIncidentDate());
            ps.setString(8, r.getSeverity());
            ps.setBoolean(9, r.isAnonymityFlag());
            ps.setString(10, r.getStatus());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    public void update(ReportBean r) throws SQLException {
        String sql = "UPDATE reports SET category_id = ?, title = ?, description = ?, location = ?, " +
                     "incident_date = ?, severity = ?, status = ?, updated_at = NOW() WHERE report_id = ?";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getCategoryId());
            ps.setString(2, r.getTitle());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getLocation());
            ps.setDate(5, r.getIncidentDate());
            ps.setString(6, r.getSeverity());
            ps.setString(7, r.getStatus());
            ps.setString(8, r.getReportId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(String reportId, String status) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE reports SET status = ?, updated_at = NOW() WHERE report_id = ?")) {
            ps.setString(1, status);
            ps.setString(2, reportId);
            ps.executeUpdate();
        }
    }

    /** Deletes a report (evidence and case cascade). Returns rows deleted. */
    public int delete(String reportId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM reports WHERE report_id = ?")) {
            ps.setString(1, reportId);
            return ps.executeUpdate();
        }
    }

    public ReportBean findById(String reportId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE r.report_id = ?")) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<ReportBean> listByReporter(String reporterId, String q, String categoryId, String status)
            throws SQLException {
        StringBuilder sql = new StringBuilder(BASE + "WHERE r.reporter_id = ? ");
        List<Object> params = new ArrayList<>();
        params.add(reporterId);
        if (q != null && !q.isBlank()) {
            sql.append("AND (lower(r.title) LIKE ? OR lower(r.report_id) LIKE ?) ");
            String like = "%" + q.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            sql.append("AND r.category_id = ? ");
            params.add(categoryId);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND r.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY r.updated_at DESC");
        List<ReportBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public List<ReportBean> listRecentByReporter(String reporterId, int limit) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(
                 BASE + "WHERE r.reporter_id = ? ORDER BY r.updated_at DESC LIMIT ?")) {
            ps.setString(1, reporterId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<ReportBean> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    /** Student dashboard tiles: total, awaiting (Submitted), lastWeek, drafts. */
    public Map<String, Integer> studentStats(String reporterId) throws SQLException {
        String sql = "SELECT count(*) AS total, " +
                     "count(*) FILTER (WHERE status = 'Submitted') AS awaiting, " +
                     "count(*) FILTER (WHERE submitted_at > NOW() - INTERVAL '7 days' AND status <> 'Draft') AS last_week, " +
                     "count(*) FILTER (WHERE status = 'Draft') AS drafts " +
                     "FROM reports WHERE reporter_id = ?";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Map<String, Integer> m = new HashMap<>();
                m.put("total", rs.getInt("total"));
                m.put("awaiting", rs.getInt("awaiting"));
                m.put("lastWeek", rs.getInt("last_week"));
                m.put("drafts", rs.getInt("drafts"));
                return m;
            }
        }
    }

    private ReportBean map(ResultSet rs) throws SQLException {
        ReportBean r = new ReportBean();
        r.setReportId(rs.getString("report_id"));
        r.setSchoolId(rs.getString("school_id"));
        r.setReporterId(rs.getString("reporter_id"));
        r.setCategoryId(rs.getString("category_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setLocation(rs.getString("location"));
        Date d = rs.getDate("incident_date");
        r.setIncidentDate(d);
        r.setSeverity(rs.getString("severity"));
        r.setAnonymityFlag(rs.getBoolean("anonymity_flag"));
        r.setStatus(rs.getString("status"));
        r.setSubmittedAt(rs.getTimestamp("submitted_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        r.setCategoryName(rs.getString("category_name"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setReporterClass(rs.getString("reporter_class"));
        r.setSchoolName(rs.getString("school_name"));
        r.setCaseId(rs.getString("case_id"));
        return r;
    }
}
