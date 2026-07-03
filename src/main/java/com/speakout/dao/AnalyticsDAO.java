package com.speakout.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.speakout.util.DBConnection;

/** Aggregate queries behind the admin analytics dashboard (all live GROUP BYs). */
public class AnalyticsDAO {

    public Map<String, Object> kpis() throws SQLException {
        String sql =
            "SELECT (SELECT count(*) FROM cases) AS total_cases, " +
            "(SELECT count(*) FROM cases WHERE date_trunc('month', created_at) = date_trunc('month', NOW())) AS cases_this_month, " +
            "(SELECT count(*) FROM cases WHERE date_trunc('month', created_at) = date_trunc('month', NOW() - INTERVAL '1 month')) AS cases_last_month, " +
            "(SELECT count(*) FROM users WHERE is_active) AS active_users, " +
            "(SELECT count(*) FROM users WHERE is_active AND role = 'Student') AS active_students, " +
            "(SELECT count(*) FROM users WHERE is_active AND role <> 'Student') AS active_staff, " +
            "(SELECT ROUND(100.0 * count(*) FILTER (WHERE status IN ('Resolved', 'Closed')) / NULLIF(count(*), 0)) FROM cases) AS resolution_rate, " +
            "(SELECT ROUND((AVG(EXTRACT(EPOCH FROM (c.created_at - r.submitted_at))) / 3600.0)::numeric, 1) " +
            " FROM cases c JOIN reports r ON r.report_id = c.report_id) AS avg_response_hours";
        try (Connection c = DBConnection.createConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            Map<String, Object> m = new HashMap<>();
            m.put("totalCases", rs.getInt("total_cases"));
            m.put("casesThisMonth", rs.getInt("cases_this_month"));
            m.put("casesLastMonth", rs.getInt("cases_last_month"));
            m.put("activeUsers", rs.getInt("active_users"));
            m.put("activeStudents", rs.getInt("active_students"));
            m.put("activeStaff", rs.getInt("active_staff"));
            m.put("resolutionRate", rs.getInt("resolution_rate"));
            m.put("avgResponseHours", rs.getDouble("avg_response_hours"));
            return m;
        }
    }

    /** Non-draft reports per month, last 6 months. Each row: [label "Feb 2026", count]. */
    public List<Object[]> reportsPerMonth() throws SQLException {
        String sql =
            "SELECT to_char(m.month, 'Mon YYYY') AS label, count(r.report_id) AS n " +
            "FROM generate_series(date_trunc('month', NOW()) - INTERVAL '5 months', date_trunc('month', NOW()), INTERVAL '1 month') AS m(month) " +
            "LEFT JOIN reports r ON date_trunc('month', r.submitted_at) = m.month AND r.status <> 'Draft' " +
            "GROUP BY m.month ORDER BY m.month";
        return rows(sql);
    }

    /** Cases per school, busiest first. Each row: [school name, count]. */
    public List<Object[]> casesPerSchool() throws SQLException {
        String sql =
            "SELECT s.name AS label, count(cs.case_id) AS n " +
            "FROM cases cs JOIN reports r ON r.report_id = cs.report_id " +
            "JOIN school s ON s.school_id = r.school_id " +
            "GROUP BY s.name ORDER BY n DESC, s.name LIMIT 8";
        return rows(sql);
    }

    private List<Object[]> rows(String sql) throws SQLException {
        List<Object[]> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(new Object[] { rs.getString("label"), rs.getInt("n") });
        }
        return out;
    }
}
