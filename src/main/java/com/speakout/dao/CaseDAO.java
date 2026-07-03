package com.speakout.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.speakout.bean.CaseBean;
import com.speakout.bean.ReportBean;
import com.speakout.bean.UserBean;
import com.speakout.util.DBConnection;

public class CaseDAO {

    private static final String BASE =
        "SELECT cs.case_id, cs.report_id, cs.assigned_to, cs.status, cs.priority, cs.created_at, cs.updated_at, " +
        "       a.full_name AS assigned_name, " +
        "       r.school_id, r.reporter_id, r.category_id, r.title, r.description, r.location, r.incident_date, " +
        "       r.severity, r.anonymity_flag, r.status AS report_status, r.submitted_at, r.updated_at AS report_updated_at, " +
        "       cat.name AS category_name, u.full_name AS reporter_name, u.class_form AS reporter_class, " +
        "       s.name AS school_name " +
        "FROM cases cs " +
        "LEFT JOIN users a ON a.user_id = cs.assigned_to " +
        "JOIN reports r    ON r.report_id = cs.report_id " +
        "JOIN category cat ON cat.category_id = r.category_id " +
        "JOIN users u      ON u.user_id = r.reporter_id " +
        "JOIN school s     ON s.school_id = r.school_id ";

    /** Creates a case for a newly submitted report; returns the case id. */
    public String createForReport(String reportId, String assignedTo, String priority) throws SQLException {
        String sql = "INSERT INTO cases (case_id, report_id, assigned_to, status, priority) " +
                     "VALUES ('CS-' || lpad(nextval('case_seq')::text, 4, '0'), ?, ?, 'New', ?) RETURNING case_id";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reportId);
            ps.setString(2, assignedTo);
            ps.setString(3, priority);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    public CaseBean findById(String caseId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE cs.case_id = ?")) {
            ps.setString(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public CaseBean findByReportId(String reportId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE cs.report_id = ?")) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /**
     * Cases visible to a staff member: teachers see cases they lead or co-handle
     * (their own school by construction); admins see every case.
     */
    public List<CaseBean> listForStaff(UserBean staff, String q, String categoryId, String status)
            throws SQLException {
        StringBuilder sql = new StringBuilder(BASE);
        List<Object> params = new ArrayList<>();
        if (staff.isAdmin()) {
            sql.append("WHERE 1=1 ");
        } else {
            sql.append("WHERE (cs.assigned_to = ? OR EXISTS " +
                       "(SELECT 1 FROM case_handlers ch WHERE ch.case_id = cs.case_id AND ch.user_id = ?)) ");
            params.add(staff.getUserId());
            params.add(staff.getUserId());
        }
        if (q != null && !q.isBlank()) {
            sql.append("AND (lower(r.title) LIKE ? OR lower(cs.case_id) LIKE ? OR lower(u.full_name) LIKE ?) ");
            String like = "%" + q.trim().toLowerCase() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (categoryId != null && !categoryId.isBlank()) {
            sql.append("AND r.category_id = ? ");
            params.add(categoryId);
        }
        if (status != null && !status.isBlank()) {
            sql.append("AND cs.status = ? ");
            params.add(status);
        }
        sql.append("ORDER BY cs.updated_at DESC");
        List<CaseBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    /** Teacher tiles: open, needsReview, resolvedThisMonth, avgResponseHours. Admin gets all cases. */
    public Map<String, Object> staffStats(UserBean staff) throws SQLException {
        String scope = staff.isAdmin() ? "" :
            "AND (cs.assigned_to = ? OR EXISTS (SELECT 1 FROM case_handlers ch WHERE ch.case_id = cs.case_id AND ch.user_id = ?)) ";
        String sql = "SELECT count(*) FILTER (WHERE cs.status IN ('New', 'Under Investigation')) AS open, " +
                     "count(*) FILTER (WHERE cs.status = 'New') AS needs_review, " +
                     "count(*) FILTER (WHERE cs.status IN ('Resolved', 'Closed') " +
                     "                 AND date_trunc('month', cs.updated_at) = date_trunc('month', NOW())) AS resolved_month, " +
                     "ROUND((AVG(EXTRACT(EPOCH FROM (cs.created_at - r.submitted_at))) / 3600.0)::numeric, 1) AS avg_hours " +
                     "FROM cases cs JOIN reports r ON r.report_id = cs.report_id WHERE 1=1 " + scope;
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (!staff.isAdmin()) {
                ps.setString(1, staff.getUserId());
                ps.setString(2, staff.getUserId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Map<String, Object> m = new HashMap<>();
                m.put("open", rs.getInt("open"));
                m.put("needsReview", rs.getInt("needs_review"));
                m.put("resolvedMonth", rs.getInt("resolved_month"));
                double h = rs.getDouble("avg_hours");
                m.put("avgHours", rs.wasNull() ? "–" : String.valueOf(h));
                return m;
            }
        }
    }

    public void updateStatus(String caseId, String status) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE cases SET status = ?, updated_at = NOW() WHERE case_id = ?")) {
            ps.setString(1, status);
            ps.setString(2, caseId);
            ps.executeUpdate();
        }
    }

    public int delete(String caseId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM cases WHERE case_id = ?")) {
            ps.setString(1, caseId);
            return ps.executeUpdate();
        }
    }

    public List<UserBean> listHandlers(String caseId) throws SQLException {
        String sql = "SELECT u.user_id, u.full_name, u.role FROM case_handlers ch " +
                     "JOIN users u ON u.user_id = ch.user_id WHERE ch.case_id = ? ORDER BY ch.added_at";
        List<UserBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserBean u = new UserBean();
                    u.setUserId(rs.getString("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setRole(rs.getString("role"));
                    out.add(u);
                }
            }
        }
        return out;
    }

    public void addHandler(String caseId, String userId) throws SQLException {
        try (Connection c = DBConnection.createConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO case_handlers (case_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
            ps.setString(1, caseId);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
    }

    private CaseBean map(ResultSet rs) throws SQLException {
        CaseBean cr = new CaseBean();
        cr.setCaseId(rs.getString("case_id"));
        cr.setReportId(rs.getString("report_id"));
        cr.setAssignedTo(rs.getString("assigned_to"));
        cr.setStatus(rs.getString("status"));
        cr.setPriority(rs.getString("priority"));
        cr.setCreatedAt(rs.getTimestamp("created_at"));
        cr.setUpdatedAt(rs.getTimestamp("updated_at"));
        cr.setAssignedName(rs.getString("assigned_name"));

        ReportBean r = new ReportBean();
        r.setReportId(rs.getString("report_id"));
        r.setSchoolId(rs.getString("school_id"));
        r.setReporterId(rs.getString("reporter_id"));
        r.setCategoryId(rs.getString("category_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setLocation(rs.getString("location"));
        r.setIncidentDate(rs.getDate("incident_date"));
        r.setSeverity(rs.getString("severity"));
        r.setAnonymityFlag(rs.getBoolean("anonymity_flag"));
        r.setStatus(rs.getString("report_status"));
        r.setSubmittedAt(rs.getTimestamp("submitted_at"));
        r.setUpdatedAt(rs.getTimestamp("report_updated_at"));
        r.setCategoryName(rs.getString("category_name"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setReporterClass(rs.getString("reporter_class"));
        r.setSchoolName(rs.getString("school_name"));
        cr.setReport(r);
        return cr;
    }
}
