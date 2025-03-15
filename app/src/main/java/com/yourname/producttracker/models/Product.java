package com.yourname.producttracker.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "products")
public class Product {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @NonNull
    private String barcode;
    
    private String name;
    
    private String imageUrl;
    
    private Date expiryDate;
    
    private long createdAt;
    
    // Constructor
    public Product(@NonNull String barcode, String name, String imageUrl, Date expiryDate) {
        this.barcode = barcode;
        this.name = name;
        this.imageUrl = imageUrl;
        this.expiryDate = expiryDate;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @NonNull
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(@NonNull String barcode) {
        this.barcode = barcode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.before(new Date());
    }
    
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        
        long diff = expiryDate.getTime() - System.currentTimeMillis();
        return diff / (24 * 60 * 60 * 1000);
    }
}
