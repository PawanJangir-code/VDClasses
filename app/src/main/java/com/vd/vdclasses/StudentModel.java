package com.vd.vdclasses;

public class StudentModel {

    private String email;
    private long createdAt;

    // REQUIRED empty constructor for Firestore
    public StudentModel() {
    }

    public StudentModel(String email) {
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    public String getEmail() {
        return email;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
