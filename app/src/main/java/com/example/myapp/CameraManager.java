// app/src/main/java/com/example/myapp/CameraManager.java
package com.example.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraManager {
    private MainActivity activity;
    private ImageCapture imageCapture;
    private Bitmap capturedImage;
    private String currentCameraMode;
    private String currentImagePath;
    private AlertDialog cameraDialog;
    
    // Callback interfaces
    public interface OnImageCapturedCallback {
        void onImageCaptured(Bitmap image, String imagePath);
    }
    
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private OnImageCapturedCallback captureCallback;
    
    public CameraManager(MainActivity activity) {
        this.activity = activity;
        
        // Setup permission launcher
        setupPermissionLauncher();
    }
    
    private void setupPermissionLauncher() {
        requestPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted, proceed with camera
                    openCamera(currentCameraMode);
                } else {
                    Toast.makeText(activity, "Camera permission is required to use this feature", 
                            Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    public boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            // Always request permission when not granted
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            return false;
        }
        return true;
    }
    
    public void openCamera(String mode, OnImageCapturedCallback callback) {
        currentCameraMode = mode;
        this.captureCallback = callback;
        
        if (checkCameraPermission()) {
            openCamera(mode);
        }
    }

    private void openCamera(String mode) {
        // Close existing camera dialog if any
        if (cameraDialog != null && cameraDialog.isShowing()) {
            cameraDialog.dismiss();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View cameraView = activity.getLayoutInflater().inflate(R.layout.dialog_camera, null);
        PreviewView previewView = cameraView.findViewById(R.id.preview_view);
        
        builder.setView(cameraView);
        cameraDialog = builder.create();
        
        // Set up camera
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(activity);
                
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();
                
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageCapture);
                
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(activity, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(activity));
        
        cameraView.findViewById(R.id.capture_button).setOnClickListener(v -> {
            captureImage();
        });
        
        cameraDialog.show();
    }
    
    private void captureImage() {
        if (imageCapture == null) return;
        
        imageCapture.takePicture(ContextCompat.getMainExecutor(activity),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        // Convert ImageProxy to Bitmap (with rotation fix in ImageUtils)
                        capturedImage = ImageUtils.imageProxyToBitmap(image);
                        
                        // Save bitmap to file
                        currentImagePath = ImageUtils.saveBitmapToFile(activity, capturedImage);
                        
                        image.close();
                        cameraDialog.dismiss();
                        
                        if (captureCallback != null) {
                            captureCallback.onImageCaptured(capturedImage, currentImagePath);
                        }
                    }
                    
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(activity, "Error capturing image", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    // Getters
    public String getCurrentImagePath() {
        return currentImagePath;
    }
    
    public Bitmap getCapturedImage() {
        return capturedImage;
    }
}
