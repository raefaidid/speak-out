package com.speakout.bean;

import java.io.Serializable;

public class SchoolBean implements Serializable {

    private String schoolId;
    private String code;
    private String name;
    private String address;

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
