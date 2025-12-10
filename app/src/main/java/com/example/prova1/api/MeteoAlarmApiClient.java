package com.example.prova1.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MeteoAlarmApiClient {
    // URL BASE CORRETTO DALLA DOCUMENTAZIONE SWAGGER
    private static final String BASE_URL = "https://api.meteoalarm.org/metadata/v1/";
    
    // Chiave API
    private static final String API_KEY = "ca7015d45849e2a94d9d3920749e5f6cdda58eb2abe4fdd755921bf9c725a617";
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            
            // Header "x-api-key" come richiesto da questa documentazione specifica
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("x-api-key", API_KEY)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
