package com.metal.idrogeo.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Questa interfaccia definisce gli endpoint dell'API Idrogeo utilizzando Retrofit.
 */
public interface IdrogeoApiService {

    /**
     * Esegue una richiesta GET all'endpoint /api/v1/news per ottenere la lista di notizie.
     *
     * @return Una Call di Retrofit che, quando eseguita, restituirà una lista di NewsItem.
     */
    @GET("api/v1/news")
    Call<List<NewsItem>> getNews();
}
