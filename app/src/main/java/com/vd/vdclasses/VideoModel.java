package com.vd.vdclasses;

public class VideoModel {

    private String title;
    private String subject;
    private String youtubeUrl;
    private long createdAt;

    // REQUIRED empty constructor for Firestore
    public VideoModel() {
    }

    public VideoModel(String title, String subject, String youtubeUrl) {
        this.title = title;
        this.subject = subject;
        this.youtubeUrl = youtubeUrl;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
