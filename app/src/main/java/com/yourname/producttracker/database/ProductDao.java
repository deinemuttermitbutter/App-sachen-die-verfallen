package com.yourname.producttracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.yourname.producttracker.models.Product;

import java.util.List;

@Dao
public interface ProductDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Product product);
    
    @Update
    void update(Product product);
    
    @Delete
    void delete(Product product);
    
    @Query("DELETE FROM products WHERE id = :id")
    void deleteById(int id);
    
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    LiveData<List<Product>> getAllProducts();
    
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    Product getProductByBarcode(String barcode);
    
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    Product getProductById(int id);
    
    @Query("SELECT * FROM products WHERE expiryDate <= :currentDate ORDER BY expiryDate ASC")
    LiveData<List<Product>> getExpiredProducts(long currentDate);
    
    @Query("SELECT * FROM products WHERE expiryDate > :currentDate ORDER BY expiryDate ASC")
    LiveData<List<Product>> getNonExpiredProducts(long currentDate);
}
