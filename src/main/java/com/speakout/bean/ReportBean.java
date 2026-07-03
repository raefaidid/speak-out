package com.speakout.bean;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

public class ReportBean implements Serializable {

    private String reportId;
    private String schoolId;
    private String reporterId;
    private String categoryId;
    private String title;
    private String description;
    private String location;
    private Date incidentDate;
    private String severity;
    private boolean anonymityFlag;
    private String status;
    private Timestamp submittedAt;
    private Timestamp updatedAt;

    // joined
    private String categoryName;
    private String reporterName;
    private String reporterClass;
    private String schoolName;
    private String caseId;

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Date getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Date incidentDate) { this.incidentDate = incidentDate; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public boolean isAnonymityFlag() { return anonymityFlag; }
    public void setAnonymityFlag(boolean anonymityFlag) { this.anonymityFlag = anonymityFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public String getReporterClass() { return reporterClass; }
    public void setReporterClass(String reporterClass) { this.reporterClass = reporterClass; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public boolean isEditable() { return "Draft".equals(status) || "Submitted".equals(status); }
    public boolean isDeletable() { return "Draft".equals(status); }

    /** CSS suffix for the status pill, matching assets/style.css. */
    public String getStatusPill() {
        return switch (status == null ? "" : status) {
            case "Draft" -> "draft";
            case "Submitted" -> "submitted";
            case "In review" -> "review";
            case "Resolved" -> "resolved";
            default -> "draft";
        };
    }

    public String getCategoryPill() {
        return categoryName == null ? "" : "cat-" + categoryName.toLowerCase();
    }
}
