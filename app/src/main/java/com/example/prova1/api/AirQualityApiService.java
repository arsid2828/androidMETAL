package com.example.prova1.api;

import com.example.prova1.models.OpenMeteoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirQualityApiService {
    @GET("air-quality")
    Call<OpenMeteoResponse> getAirQuality(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("current") String current
    );
}
