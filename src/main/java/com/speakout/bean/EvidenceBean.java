package com.speakout.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class EvidenceBean implements Serializable {

    private String evidenceId;
    private String reportId;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private Timestamp uploadedAt;

    public String getEvidenceId() { return evidenceId; }
    public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getFileName() {
        if (fileUrl == null) return "";
        int i = fileUrl.lastIndexOf('/');
        return i >= 0 ? fileUrl.substring(i + 1) : fileUrl;
    }

    public String getSizeDisplay() {
        if (fileSize >= 1048576) return String.format("%.1f MB", fileSize / 1048576.0);
        return String.format("%.0f KB", Math.max(1, fileSize / 1024.0));
    }
}
