// app/src/main/java/com/example/myapp/FoodItem.java
package com.example.myapp;

import android.graphics.Bitmap;

public class FoodItem {
    private int id;
    private String title;
    private String expiryDate;
    private String imagePath;
    private Bitmap image;

    public FoodItem(String title, String expiryDate, String imagePath) {
        this.title = title;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
    }

    // Load image on demand to save memory
    public Bitmap getImage() {
        if (image == null && imagePath != null) {
            image = ImageUtils.loadImageFromFile(imagePath);
        }
        return image;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        // Reset loaded image when path changes
        this.image = null;
    }
}
