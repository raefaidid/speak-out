package com.speakout.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.speakout.bean.CaseNoteBean;
import com.speakout.util.DBConnection;

public class CaseNoteDAO {

    public String insert(String caseId, String authorId, String body, String visibility) throws SQLException {
        String sql = "INSERT INTO case_notes (note_id, case_id, author_id, body, visibility) " +
                     "VALUES ('NT-' || lpad(nextval('note_seq')::text, 4, '0'), ?, ?, ?, ?) RETURNING note_id";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, caseId);
            ps.setString(2, authorId);
            ps.setString(3, body);
            ps.setString(4, visibility);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    /** Notes on a case, oldest first. Students only get reporter-visible ones. */
    public List<CaseNoteBean> listByCase(String caseId, boolean includeInternal) throws SQLException {
        String sql = "SELECT n.note_id, n.case_id, n.author_id, n.body, n.visibility, n.created_at, " +
                     "       u.full_name AS author_name, u.role AS author_role " +
                     "FROM case_notes n JOIN users u ON u.user_id = n.author_id " +
                     "WHERE n.case_id = ? " + (includeInternal ? "" : "AND n.visibility = 'reporter-visible' ") +
                     "ORDER BY n.created_at";
        List<CaseNoteBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CaseNoteBean n = new CaseNoteBean();
                    n.setNoteId(rs.getString("note_id"));
                    n.setCaseId(rs.getString("case_id"));
                    n.setAuthorId(rs.getString("author_id"));
                    n.setBody(rs.getString("body"));
                    n.setVisibility(rs.getString("visibility"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    n.setAuthorName(rs.getString("author_name"));
                    n.setAuthorRole(rs.getString("author_role"));
                    out.add(n);
                }
            }
        }
        return out;
    }
}
