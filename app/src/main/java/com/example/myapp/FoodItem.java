// app/src/main/java/com/example/myapp/FoodItem.java
package com.example.myapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FoodItem {
    private int id;
    private String title;
    private String expiryDate;
    private String imagePath;
    private transient Bitmap cachedImage;
    
    // Constructor for new items (without ID)
    public FoodItem(String title, String expiryDate, String imagePath) {
        this.title = title;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
    }
    
    // Constructor for items from database
    public FoodItem(int id, String title, String expiryDate, String imagePath) {
        this.id = id;
        this.title = title;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
    }
    
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
        // Clear cached image when path changes
        this.cachedImage = null;
    }
    
    public Bitmap getImage() {
        if (cachedImage == null && imagePath != null) {
            cachedImage = BitmapFactory.decodeFile(imagePath);
        }
        return cachedImage;
    }
    
    public void setCachedImage(Bitmap bitmap) {
        this.cachedImage = bitmap;
    }
}
