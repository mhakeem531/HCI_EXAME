package com.example.hakeem.log.helper;

/**
 * Created by hakeem on 3/31/18.
 */

public class User {
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public byte[] getProfileImage() {
        return profileImage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }

    private String name;
    private String email;
    private String id;
    private String createdAt;
    private byte[] profileImage;
}
