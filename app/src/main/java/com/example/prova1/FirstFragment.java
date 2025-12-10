package com.example.prova1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prova1.api.MeteoAlarmApiClient;
import com.example.prova1.api.MeteoAlarmApiService;
import com.example.prova1.api.WeatherApiClient;
import com.example.prova1.api.WeatherApiService;
import com.example.prova1.models.OpenMeteoResponse;
import com.example.prova1.models.WeatherItem;
import com.example.prova1.ui.WeatherAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WeatherAdapter weatherAdapter;
    private List<WeatherItem> weatherList = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

    private double currentLat = 45.44;
    private double currentLon = 12.33;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocationAndFetchData();
                } else {
                    Toast.makeText(getContext(), "Permesso GPS negato. Uso Venezia come default.", Toast.LENGTH_LONG).show();
                    startDataFetch();
                }
            });

    private int pendingRequests = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.news_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        weatherAdapter = new WeatherAdapter(weatherList);
        recyclerView.setAdapter(weatherAdapter);

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        refreshData();
    }

    private void refreshData() {
        if (!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);
        weatherList.clear();
        weatherAdapter.notifyDataSetChanged();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocationAndFetchData();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndFetchData() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLon = location.getLongitude();
                        Toast.makeText(getContext(), "Posizione: " + currentLat + ", " + currentLon, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Posizione non trovata. Uso default.", Toast.LENGTH_SHORT).show();
                    }
                    startDataFetch();
                });
    }

    private void startDataFetch() {
        pendingRequests = 2; 
        fetchOpenMeteoData();
        fetchMeteoAlarmData();
    }

    private synchronized void checkRequestsFinished() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    // --- OPEN METEO (INTATTO) ---
    private void fetchOpenMeteoData() {
        WeatherApiService apiService = WeatherApiClient.getClient().create(WeatherApiService.class);
        String currentParams = "temperature_2m,precipitation,wind_speed_10m";

        Call<OpenMeteoResponse> call = apiService.getForecast(currentLat, currentLon, currentParams);
        call.enqueue(new Callback<OpenMeteoResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OpenMeteoResponse data = response.body();
                    if (data.getCurrent() != null) {
                        String desc = String.format("Temp: %s%s\nVento: %s%s\nPrecipitazioni: %s%s",
                                data.getCurrent().getTemperature2m(), data.getCurrentUnits().getTemperature2m(),
                                data.getCurrent().getWindSpeed10m(), data.getCurrentUnits().getWindSpeed10m(),
                                data.getCurrent().getPrecipitation(), data.getCurrentUnits().getPrecipitation());
                        addWeatherItem(new WeatherItem("☁️ Meteo: Tua Posizione", desc, "Oggi"));
                    }
                }
                checkRequestsFinished();
            }
            @Override
            public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                checkRequestsFinished();
            }
        });
    }

    // --- METEOALARM (LOGICA DINAMICA) ---
    private void fetchMeteoAlarmData() {
        MeteoAlarmApiService apiService = MeteoAlarmApiClient.getClient().create(MeteoAlarmApiService.class);
        
        Log.d("METEOALARM_DEBUG", "Step 1: Cerco le collections disponibili...");
        
        // Prima chiamata: Scopriamo come si chiama la collection
        apiService.getCollections().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        // Log.d("METEOALARM_DEBUG", "Collections JSON: " + json);
                        
                        String collectionId = parseFirstCollectionId(json);
                        Log.d("METEOALARM_DEBUG", "Collection trovata: " + collectionId);
                        
                        if (collectionId != null) {
                            fetchWarningsWithCorrectCollection(apiService, collectionId);
                        } else {
                             addWeatherItem(new WeatherItem("MeteoAlarm", "Errore: Nessuna collection trovata.", "Oggi"));
                             checkRequestsFinished();
                        }
                    } catch (Exception e) {
                        Log.e("METEOALARM_DEBUG", "Errore lettura collections: " + e.getMessage());
                        checkRequestsFinished();
                    }
                } else {
                    Log.e("METEOALARM_DEBUG", "Errore Collections: " + response.code());
                    addWeatherItem(new WeatherItem("MeteoAlarm", "Server non raggiungibile (Code " + response.code() + ")", "Oggi"));
                    checkRequestsFinished();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("METEOALARM_DEBUG", "Fail Collections: " + t.getMessage());
                checkRequestsFinished();
            }
        });
    }
    
    // Seconda chiamata: Usiamo l'ID corretto per cercare gli allarmi
    private void fetchWarningsWithCorrectCollection(MeteoAlarmApiService apiService, String collectionId) {
        double delta = 0.05; 
        String bbox = (currentLon - delta) + "," + (currentLat - delta) + "," +
                      (currentLon + delta) + "," + (currentLat + delta);
        
        Log.d("METEOALARM_DEBUG", "Step 2: Chiedo LOCATIONS per " + collectionId + " con BBOX " + bbox);

        // USIAMO L'ENDPOINT LOCATIONS
        apiService.getWarningsByLocations(collectionId, bbox).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Log.d("METEOALARM_DEBUG", "Risposta Finale: " + json);
                        
                        if (!isFeatureCollectionEmpty(json)) {
                            addWeatherItem(new WeatherItem("⚠️ Allerte Attive", parseMeteoAlarmJson(json), "Oggi"));
                        } else {
                             addWeatherItem(new WeatherItem("MeteoAlarm", "✅ Nessuna allerta attiva.", "Oggi"));
                        }
                    } catch (Exception e) {
                         Log.e("METEOALARM_DEBUG", "Errore parsing finale: " + e.getMessage());
                    }
                } else {
                    Log.e("METEOALARM_DEBUG", "Errore Locations: " + response.code());
                    addWeatherItem(new WeatherItem("MeteoAlarm", "Errore query (" + response.code() + ")", "Oggi"));
                }
                checkRequestsFinished();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                checkRequestsFinished();
            }
        });
    }

    private String parseFirstCollectionId(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            if (root.has("collections")) {
                JSONArray collections = root.getJSONArray("collections");
                if (collections.length() > 0) {
                    return collections.getJSONObject(0).getString("id");
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private boolean isFeatureCollectionEmpty(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            if (root.has("features")) {
                return root.getJSONArray("features").length() == 0;
            }
        } catch (Exception e) {}
        return true;
    }

    private String parseMeteoAlarmJson(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            if (root.has("features")) {
                JSONArray features = root.getJSONArray("features");
                if (features.length() == 0) return "Nessuna allerta.";
                
                StringBuilder sb = new StringBuilder();
                int limit = Math.min(features.length(), 10);
                
                for (int i = 0; i < limit; i++) {
                    JSONObject feature = features.getJSONObject(i);
                    JSONObject properties = feature.optJSONObject("properties");
                    if (properties != null) {
                        String event = properties.optString("event", "Avviso Generico");
                        String severity = properties.optString("severity", "N/A"); 
                        String awarenessLevel = properties.optString("awareness_level", ""); 
                        String onset = properties.optString("onset", "");
                        
                        String colorEmoji = "⚠️";
                        if (awarenessLevel.contains("Yellow") || severity.contains("Moderate")) colorEmoji = "🟡";
                        if (awarenessLevel.contains("Orange") || severity.contains("Severe")) colorEmoji = "🟠";
                        if (awarenessLevel.contains("Red") || severity.contains("Extreme")) colorEmoji = "🔴";

                        sb.append(colorEmoji).append(" ").append(event)
                          .append("\nLivello: ").append(severity).append(" / ").append(awarenessLevel)
                          .append("\nInizio: ").append(onset).append("\n\n");
                    }
                }
                return sb.toString();
            }
        } catch (Exception e) {
            return "Errore lettura dati.";
        }
        return "Dati non leggibili.";
    }

    private synchronized void addWeatherItem(WeatherItem item) {
        weatherList.add(item);
        weatherAdapter.notifyItemInserted(weatherList.size() - 1);
    }
}
