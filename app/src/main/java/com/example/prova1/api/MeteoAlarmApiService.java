package com.example.prova1.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MeteoAlarmApiService {
    // Endpoint corretto secondo Swagger: GET /warnings/location
    @GET("warnings/location")
    Call<ResponseBody> getWarningsByLocation(
            @Query("lat") double lat,
            @Query("lon") double lon
    );
}
