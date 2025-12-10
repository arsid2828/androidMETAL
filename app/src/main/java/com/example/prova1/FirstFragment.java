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

import org.json.JSONArray;
import org.json.JSONObject;

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

    // Coordinate di test (Venezia)
    private static final double LAT = 45.44;
    private static final double LON = 12.33;

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

        // Reset e caricamento dati
        weatherList.clear();
        weatherAdapter.notifyDataSetChanged();
        
        Toast.makeText(getContext(), "Scaricamento dati meteo...", Toast.LENGTH_SHORT).show();
        
        fetchOpenMeteoData();
        fetchMeteoAlarmData();
    }

    private void fetchOpenMeteoData() {
        WeatherApiService apiService = WeatherApiClient.getClient().create(WeatherApiService.class);
        
        String currentParams = "temperature_2m,precipitation,wind_speed_10m";

        Call<OpenMeteoResponse> call = apiService.getForecast(LAT, LON, currentParams);

        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OpenMeteoResponse data = response.body();
                    
                    if (data.getCurrent() != null) {
                        String title = "☁️ Meteo Attuale (Venezia)";
                        
                        String tempUnit = data.getCurrentUnits().getTemperature2m();
                        String speedUnit = data.getCurrentUnits().getWindSpeed10m();
                        String precipUnit = data.getCurrentUnits().getPrecipitation();

                        String desc = String.format("Temperatura: %s%s\nVento: %s%s\nPrecipitazioni: %s%s",
                                data.getCurrent().getTemperature2m(), tempUnit,
                                data.getCurrent().getWindSpeed10m(), speedUnit,
                                data.getCurrent().getPrecipitation(), precipUnit);
                        
                        String date = "Aggiornato: " + data.getCurrent().getTime().replace("T", " ");

                        addWeatherItem(new WeatherItem(title, desc, date));
                    }
                } else {
                    Log.e("FirstFragment", "Errore OpenMeteo: " + response.code());
                    addWeatherItem(new WeatherItem("Errore Meteo", "Non sono riuscito a scaricare il meteo.", "N/A"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                Log.e("FirstFragment", "Errore call OpenMeteo", t);
                addWeatherItem(new WeatherItem("Errore Connessione", "Impossibile contattare Open-Meteo.", "N/A"));
            }
        });
    }

    private void fetchMeteoAlarmData() {
        MeteoAlarmApiService apiService = MeteoAlarmApiClient.getClient().create(MeteoAlarmApiService.class);

        // Query standard OGC EDR per posizione WKT
        String wkt = "POINT(" + LON + " " + LAT + ")";
        
        Call<ResponseBody> call = apiService.getWarningsByPosition(wkt);
        
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String rawJson = response.body().string();
                        
                        // Tentativo di parsing base per rendere l'output leggibile
                        // La struttura EDR di solito restituisce una FeatureCollection
                        String parsedInfo = parseMeteoAlarmJson(rawJson);
                        
                        addWeatherItem(new WeatherItem("⚠️ Allerte MeteoAlarm (Europa)", parsedInfo, "Oggi"));
                        
                    } catch (Exception e) {
                        Log.e("FirstFragment", "Errore parsing MeteoAlarm", e);
                        addWeatherItem(new WeatherItem("MeteoAlarm", "Dati ricevuti ma formato non riconosciuto.", "Oggi"));
                    }
                } else {
                     Log.w("FirstFragment", "MeteoAlarm position failed: " + response.code());
                     // Se 404 o altro, probabilmente non ci sono allerte o l'endpoint richiede params diversi
                     addWeatherItem(new WeatherItem("MeteoAlarm", "Nessuna allerta attiva o servizio momentaneamente non disponibile (" + response.code() + ")", "Oggi"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("FirstFragment", "Errore call MeteoAlarm", t);
            }
        });
    }

    /**
     * Cerca di estrarre informazioni utili dal JSON EDR GeoJSON.
     */
    private String parseMeteoAlarmJson(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            
            // Verifica se è una FeatureCollection
            if (root.has("type") && "FeatureCollection".equalsIgnoreCase(root.getString("type"))) {
                JSONArray features = root.getJSONArray("features");
                
                if (features.length() == 0) {
                    return "Nessuna allerta segnalata per questa zona.";
                }
                
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < features.length(); i++) {
                    JSONObject feature = features.getJSONObject(i);
                    JSONObject properties = feature.optJSONObject("properties");
                    if (properties != null) {
                        String event = properties.optString("event", "Evento sconosciuto");
                        String severity = properties.optString("severity", "N/A");
                        String headline = properties.optString("headline", "");
                        
                        sb.append("• ").append(event)
                          .append(" (Gravità: ").append(severity).append(")\n");
                        if (!headline.isEmpty()) {
                            sb.append("  ").append(headline).append("\n");
                        }
                    }
                }
                return sb.toString();
            } else {
                return "Formato risposta non standard (non FeatureCollection).";
            }
        } catch (Exception e) {
            return "Impossibile leggere i dettagli dell'allerta.";
        }
    }

    private synchronized void addWeatherItem(WeatherItem item) {
        // Aggiungiamo sempre in cima alla lista o in fondo? 
        // In fondo va bene per ora.
        weatherList.add(item);
        // Notifica specifica per l'inserimento
        weatherAdapter.notifyItemInserted(weatherList.size() - 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
