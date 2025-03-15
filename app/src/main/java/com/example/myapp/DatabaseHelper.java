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
    private static final String DATABASE_NAME = "food_items.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_FOOD_ITEMS = "food_items";
    
    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";
    public static final String COLUMN_IMAGE_PATH = "image_path";

    // Create table SQL query
    private static final String CREATE_TABLE_FOOD_ITEMS = "CREATE TABLE " + TABLE_FOOD_ITEMS + "("
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
        db.execSQL(CREATE_TABLE_FOOD_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD_ITEMS);
        onCreate(db);
    }

    // Insert a new food item
    public long insertFoodItem(String title, String expiryDate, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_EXPIRY_DATE, expiryDate);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        
        long id = db.insert(TABLE_FOOD_ITEMS, null, values);
        db.close();
        return id;
    }

    // Get all food items
    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> foodItems = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FOOD_ITEMS;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                
                FoodItem foodItem = new FoodItem(id, title, expiryDate, imagePath);
                foodItems.add(foodItem);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return foodItems;
    }

    // Update a food item
    public int updateFoodItem(FoodItem foodItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, foodItem.getTitle());
        values.put(COLUMN_EXPIRY_DATE, foodItem.getExpiryDate());
        values.put(COLUMN_IMAGE_PATH, foodItem.getImagePath());
        
        // Update row
        int result = db.update(TABLE_FOOD_ITEMS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(foodItem.getId())});
        db.close();
        return result;
    }

    // Delete a food item
    public void deleteFoodItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FOOD_ITEMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
