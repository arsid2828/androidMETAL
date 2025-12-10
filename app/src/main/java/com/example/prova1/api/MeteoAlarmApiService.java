package com.example.prova1.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MeteoAlarmApiService {
    @GET("collections?f=json")
    Call<ResponseBody> getCollections();

    // Secondo i metadata, l'endpoint dati è "locations"
    // URL: collections/{collectionId}/locations
    @GET("collections/{collectionId}/locations?f=json")
    Call<ResponseBody> getWarningsByLocations(
            @Path("collectionId") String collectionId, 
            @Query("bbox") String bbox
    );
}
