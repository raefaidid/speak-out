package com.speakout.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserBean implements Serializable {

    private String userId;
    private String schoolId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private String classForm;
    private boolean active;
    private Timestamp createdAt;

    // joined
    private String schoolName;
    private String schoolCode;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getClassForm() { return classForm; }
    public void setClassForm(String classForm) { this.classForm = classForm; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getSchoolCode() { return schoolCode; }
    public void setSchoolCode(String schoolCode) { this.schoolCode = schoolCode; }

    public boolean isStudent() { return "Student".equals(role); }
    public boolean isTeacher() { return "Teacher".equals(role); }
    public boolean isAdmin()   { return "Admin".equals(role); }
    public boolean isStaff()   { return isTeacher() || isAdmin(); }

    /** Initials for the avatar circle, e.g. "Aiman bin Zulkifli" -> "AZ". */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
