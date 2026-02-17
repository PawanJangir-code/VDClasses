package com.vd.vdclasses;

public class AttendanceModel {

    private String email;
    private String date;
    private long timestamp;

    // REQUIRED empty constructor for Firestore
    public AttendanceModel() {
    }

    public AttendanceModel(String email, String date, long timestamp) {
        this.email = email;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
