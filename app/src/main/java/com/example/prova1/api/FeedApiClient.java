package com.example.prova1.api;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class FeedApiClient {

    // L'URL base non è fisso, verrà specificato dinamicamente nella chiamata
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://feeds.meteoalarm.org/") // URL base generico
                    .addConverterFactory(SimpleXmlConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
