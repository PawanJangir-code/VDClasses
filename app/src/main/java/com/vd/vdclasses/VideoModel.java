package com.vd.vdclasses;

public class VideoModel {

    private String title;
    private String subject;
    private String videoUrl;
    private long createdAt;

    // REQUIRED empty constructor for Firestore
    public VideoModel() {
    }

    public VideoModel(String title, String subject, String videoUrl) {
        this.title = title;
        this.subject = subject;
        this.videoUrl = videoUrl;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
