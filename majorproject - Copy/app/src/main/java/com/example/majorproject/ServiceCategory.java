package com.example.majorproject;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ServiceCategory {
    private String id;
    private String name;
    private int imageResId;
    private long createdAt;
    private boolean isActive;


    public ServiceCategory() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
        this.imageResId = R.drawable.ic_services;
    }


    public ServiceCategory(String name) {
        this();
        this.name = name;
    }


    public ServiceCategory(String id, String name, int imageResId) {
        this(name);
        this.id = id;
        this.imageResId = imageResId;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "ServiceCategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageResId=" + imageResId +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
}