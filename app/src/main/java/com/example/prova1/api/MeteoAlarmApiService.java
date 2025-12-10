package com.example.prova1.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MeteoAlarmApiService {
    // Per ora scarichiamo il raw JSON per vedere la struttura o usare un parser generico se necessario
    @GET("collections")
    Call<ResponseBody> getCollections();

    // Esempio query warning per posizione (da verificare la sintassi esatta EDR)
    // Spesso EDR usa WKT per le coordinate, es: coords=POINT(lon lat)
    @GET("collections/warnings/position")
    Call<ResponseBody> getWarningsByPosition(@Query("coords") String wktCoords);
}
