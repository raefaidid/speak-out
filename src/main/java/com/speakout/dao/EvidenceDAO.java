package com.speakout.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.speakout.bean.EvidenceBean;
import com.speakout.util.DBConnection;

public class EvidenceDAO {

    public String insert(String reportId, String fileUrl, String fileType, long fileSize) throws SQLException {
        String sql = "INSERT INTO evidence (evidence_id, report_id, file_url, file_type, file_size) " +
                     "VALUES ('EV-' || lpad(nextval('evidence_seq')::text, 4, '0'), ?, ?, ?, ?) RETURNING evidence_id";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reportId);
            ps.setString(2, fileUrl);
            ps.setString(3, fileType);
            ps.setLong(4, fileSize);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    public List<EvidenceBean> listByReport(String reportId) throws SQLException {
        String sql = "SELECT evidence_id, report_id, file_url, file_type, file_size, uploaded_at " +
                     "FROM evidence WHERE report_id = ? ORDER BY uploaded_at";
        List<EvidenceBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public EvidenceBean findById(String evidenceId) throws SQLException {
        String sql = "SELECT evidence_id, report_id, file_url, file_type, file_size, uploaded_at " +
                     "FROM evidence WHERE evidence_id = ?";
        try (Connection c = DBConnection.createConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, evidenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private EvidenceBean map(ResultSet rs) throws SQLException {
        EvidenceBean e = new EvidenceBean();
        e.setEvidenceId(rs.getString("evidence_id"));
        e.setReportId(rs.getString("report_id"));
        e.setFileUrl(rs.getString("file_url"));
        e.setFileType(rs.getString("file_type"));
        e.setFileSize(rs.getLong("file_size"));
        e.setUploadedAt(rs.getTimestamp("uploaded_at"));
        return e;
    }
}
