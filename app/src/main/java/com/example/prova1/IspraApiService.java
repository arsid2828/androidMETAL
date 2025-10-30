package com.example.prova1;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IspraApiService {
    @GET("collections/eventi-di-dissesto/items")
    Call<DisasterEvent> getDisasterEvents();
}
