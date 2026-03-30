package com.example.majorproject;

public class Worker {
    private String workerId, name, email, category, city, experience, contact, rating, profileUrl, availability;

    public Worker() {
    }

    public Worker(String workerId, String name, String email, String category, String city, String experience, String contact, String rating, String profileUrl, String availability) {
        this.workerId = workerId;
        this.name = name;
        this.email = email;
        this.category = category;
        this.city = city;
        this.experience = experience;
        this.contact = contact;
        this.rating = rating;
        this.profileUrl = profileUrl;
        this.availability = availability;
    }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}
