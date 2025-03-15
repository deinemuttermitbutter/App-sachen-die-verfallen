// app/src/main/java/com/example/myapp/DatabaseHelper.java
package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "foodtracker.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table and column names
    private static final String TABLE_FOOD = "food_items";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_EXPIRY_DATE = "expiry_date";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    
    // Create table SQL query
    private static final String CREATE_TABLE_FOOD = 
            "CREATE TABLE " + TABLE_FOOD + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_EXPIRY_DATE + " TEXT,"
            + COLUMN_IMAGE_PATH + " TEXT"
            + ")";
            
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FOOD);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        onCreate(db);
    }
    
    // Insert new food item
    public long insertFoodItem(String title, String expiryDate, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_EXPIRY_DATE, expiryDate);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        
        long id = db.insert(TABLE_FOOD, null, values);
        db.close();
        
        return id;
    }
    
    // Get all food items
    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> foodItems = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_FOOD + " ORDER BY " + COLUMN_EXPIRY_DATE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                FoodItem item = new FoodItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH))
                );
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                
                foodItems.add(item);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return foodItems;
    }
    
    // Update food item
    public int updateFoodItem(FoodItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_EXPIRY_DATE, item.getExpiryDate());
        values.put(COLUMN_IMAGE_PATH, item.getImagePath());
        
        int rowsAffected = db.update(TABLE_FOOD, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        
        db.close();
        return rowsAffected;
    }
    
    // Delete food item
    public void deleteFoodItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOOD, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
