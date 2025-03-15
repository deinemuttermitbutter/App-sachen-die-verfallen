// app/src/main/java/com/example/myapp/ImageUtils.java
package com.example.myapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.camera.core.ImageProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {
    
    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        
        // Fix rotation issue by always rotating 90 degrees clockwise
        return rotateImage(bitmap, 90);
    }
    
    public static String saveBitmapToFile(Context context, Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        
        File directory = new File(context.getFilesDir(), "food_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        File file = new File(directory, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static boolean deleteImage(String path) {
        if (path == null) return false;
        
        File file = new File(path);
        return file.exists() && file.delete();
    }
    
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap loadImageFromFile(String path) {
    if (path == null) return null;
    
    File file = new File(path);
    if (!file.exists()) return null;
    
    return BitmapFactory.decodeFile(path);
    }
}
