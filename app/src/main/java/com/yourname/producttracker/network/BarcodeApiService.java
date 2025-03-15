package com.yourname.producttracker.network;

import com.yourname.producttracker.models.ProductResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BarcodeApiService {
    
    // This is an example, replace with actual barcode API endpoint
    @GET("api/products/{barcode}")
    Call<ProductResponse> getProductInfo(@Path("barcode") String barcode);
    
    // Factory for creating the service
    class Factory {
        private static final String BASE_URL = "https://api.example.com/";
        
        public static BarcodeApiService create() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            
            return retrofit.create(BarcodeApiService.class);
        }
    }
}
