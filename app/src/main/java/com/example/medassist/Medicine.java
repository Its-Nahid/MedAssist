package com.example.medassist;

public class Medicine {
    private String name;
    private String description;
    private String shortUse;
    private String sideEffects;
    private String precautions;
    private String price; // This is now a String
    private String imageUrl;
    private String medicineId;

    // No-argument constructor is essential for Firestore
    public Medicine() {}

    public Medicine(String name, String description, String shortUse,
                    String sideEffects, String precautions,
                    String price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.shortUse = shortUse;
        this.sideEffects = sideEffects;
        this.precautions = precautions;
        this.price = price; // Changed to String
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getShortUse() { return shortUse; }
    public String getSideEffects() { return sideEffects; }
    public String getPrecautions() { return precautions; }
    public String getPrice() { return price; } // Returns String
    public String getImageUrl() { return imageUrl; }
    public String getMedicineId() { return medicineId; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setShortUse(String shortUse) { this.shortUse = shortUse; }
    public void setSideEffects(String sideEffects) { this.sideEffects = sideEffects; }
    public void setPrecautions(String precautions) { this.precautions = precautions; }
    public void setPrice(String price) { this.price = price; } // Accepts String
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }
}
