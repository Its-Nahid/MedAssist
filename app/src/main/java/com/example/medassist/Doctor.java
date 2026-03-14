package com.example.medassist;

public class Doctor {
    private String name;
    private String specialty;
    private String phone;
    private String rating;
    private String reviews;
    private String imageUrl; // Field to store the image URL from Firebase

    public Doctor() {} // Empty constructor for Firebase

    public Doctor(String name, String specialty, String phone, String rating, String reviews, String imageUrl) {
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.rating = rating;
        this.reviews = reviews;
        this.imageUrl = imageUrl; // Initialize imageUrl
    }

    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getPhone() { return phone; }
    public String getRating() { return rating; }
    public String getReviews() { return reviews; }
    public String getImageUrl() { return imageUrl; } // Getter for the image URL
}
