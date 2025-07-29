package com.example.recipesbook.Model;


public class Users {
    private String userId;
    private String name;
    private String email;
    private String country;
    private String bio;
    private String imageUrl;

    public Users() {
    }

    public Users(String userId, String name, String email, String country, String bio, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.country = country;
        this.bio = bio;
        this.imageUrl = imageUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
