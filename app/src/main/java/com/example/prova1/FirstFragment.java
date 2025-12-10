package com.example.prova1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prova1.api.MeteoAlarmApiClient;
import com.example.prova1.api.MeteoAlarmApiService;
import com.example.prova1.api.WeatherApiClient;
import com.example.prova1.api.WeatherApiService;
import com.example.prova1.models.OpenMeteoResponse;
import com.example.prova1.models.WeatherItem;
import com.example.prova1.ui.WeatherAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private RecyclerView recyclerView;
    private WeatherAdapter weatherAdapter;
    private List<WeatherItem> weatherList = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.news_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        weatherAdapter = new WeatherAdapter(weatherList);
        recyclerView.setAdapter(weatherAdapter);

        // Pulisci la lista e scarica i dati
        weatherList.clear();
        fetchOpenMeteoData();
        fetchMeteoAlarmData();
    }

    private void fetchOpenMeteoData() {
        WeatherApiService apiService = WeatherApiClient.getClient().create(WeatherApiService.class);
        
        // Esempio: Venezia
        double lat = 45.44;
        double lon = 12.33;
        String currentParams = "temperature_2m,precipitation,wind_speed_10m";

        Call<OpenMeteoResponse> call = apiService.getForecast(lat, lon, currentParams);

        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OpenMeteoResponse data = response.body();
                    
                    if (data.getCurrent() != null) {
                        String title = "Open-Meteo: Venezia";
                        String desc = "Temp: " + data.getCurrent().getTemperature2m() + data.getCurrentUnits().getTemperature2m() +
                                      "\nPrecip: " + data.getCurrent().getPrecipitation() + data.getCurrentUnits().getPrecipitation() +
                                      "\nVento: " + data.getCurrent().getWindSpeed10m() + data.getCurrentUnits().getWindSpeed10m();
                        String date = data.getCurrent().getTime();

                        addWeatherItem(new WeatherItem(title, desc, date));
                    }
                } else {
                    Log.e("FirstFragment", "Errore OpenMeteo: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                Log.e("FirstFragment", "Errore call OpenMeteo", t);
            }
        });
    }

    private void fetchMeteoAlarmData() {
        MeteoAlarmApiService apiService = MeteoAlarmApiClient.getClient().create(MeteoAlarmApiService.class);

        // Proviamo a cercare warning per la stessa posizione (Venezia approx).
        // WKT: POINT(lon lat) -> POINT(12.33 45.44)
        String wkt = "POINT(12.33 45.44)";
        
        Call<ResponseBody> call = apiService.getWarningsByPosition(wkt);
        
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Per ora prendiamo il JSON grezzo, poi si potrà parsare meglio
                        String rawJson = response.body().string();
                        // Tronchiamo per evitare stringhe troppo lunghe nella UI di test
                        String preview = rawJson.length() > 200 ? rawJson.substring(0, 200) + "..." : rawJson;
                        
                        addWeatherItem(new WeatherItem("MeteoAlarm (Raw Data)", preview, "Oggi"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Se fallisce la ricerca per posizione, proviamo a listare le collection per debug
                     Log.w("FirstFragment", "MeteoAlarm position failed: " + response.code());
                     addWeatherItem(new WeatherItem("MeteoAlarm", "Nessun allarme o errore API (" + response.code() + ")", "Oggi"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("FirstFragment", "Errore call MeteoAlarm", t);
                 addWeatherItem(new WeatherItem("MeteoAlarm", "Errore connessione: " + t.getMessage(), "Oggi"));
            }
        });
    }

    private synchronized void addWeatherItem(WeatherItem item) {
        weatherList.add(item);
        weatherAdapter.notifyItemInserted(weatherList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
