package com.speakout.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class CaseBean implements Serializable {

    private String caseId;
    private String reportId;
    private String assignedTo;
    private String status;
    private String priority;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // joined
    private String assignedName;
    private ReportBean report;

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getAssignedName() { return assignedName; }
    public void setAssignedName(String assignedName) { this.assignedName = assignedName; }
    public ReportBean getReport() { return report; }
    public void setReport(ReportBean report) { this.report = report; }

    public String getStatusPill() {
        return switch (status == null ? "" : status) {
            case "New" -> "submitted";
            case "Under Investigation" -> "review";
            case "Resolved" -> "resolved";
            case "Closed" -> "draft";
            default -> "draft";
        };
    }
}
