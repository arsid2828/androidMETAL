//HOME

package com.example.prova1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prova1.api.FeedApiClient;
import com.example.prova1.api.FeedApiService;
import com.example.prova1.api.WeatherApiClient;
import com.example.prova1.api.WeatherApiService;
import com.example.prova1.models.AlertViewModel;
import com.example.prova1.models.AtomEntry;
import com.example.prova1.models.AtomFeed;
import com.example.prova1.models.OpenMeteoResponse;
import com.example.prova1.models.WeatherItem;
import com.example.prova1.models.WindAlert;
import com.example.prova1.ui.WeatherAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WeatherAdapter weatherAdapter;
    private List<WeatherItem> weatherList = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private AlertViewModel alertViewModel;

    private double currentLat = 45.44; // Default: Venezia
    private double currentLon = 12.33;

    private static final String CHANNEL_ID = "wind_notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final double WIND_SPEED_THRESHOLD = 30.0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocationAndFetchData();
                } else {
                    Toast.makeText(getContext(), "Permesso GPS negato. Uso Venezia come default.", Toast.LENGTH_LONG).show();
                    startDataFetch();
                }
            });

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(getContext(), "Permesso notifiche negato.", Toast.LENGTH_SHORT).show();
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

        alertViewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupUI(view);
        createNotificationChannel();
        refreshData();
    }

    private void setupUI(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.news_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        weatherAdapter = new WeatherAdapter(weatherList);
        recyclerView.setAdapter(weatherAdapter);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        if (!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);
        weatherList.clear();
        weatherAdapter.notifyDataSetChanged();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocationAndFetchData();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndFetchData() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLon = location.getLongitude();
                    } else {
                        Toast.makeText(getContext(), "Posizione non trovata. Uso default.", Toast.LENGTH_SHORT).show();
                    }
                    startDataFetch();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Errore GPS. Uso default.", Toast.LENGTH_SHORT).show();
                    startDataFetch();
                });
    }

    private void startDataFetch() {
        pendingRequests = 2;
        fetchOpenMeteoData();
        fetchFeedData();
    }

    private synchronized void checkRequestsFinished() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fetchOpenMeteoData() {
        WeatherApiClient.getClient().create(WeatherApiService.class)
            .getForecast(currentLat, currentLon, "temperature_2m,precipitation,wind_speed_10m")
            .enqueue(new Callback<OpenMeteoResponse>() {
                @Override
                public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        OpenMeteoResponse data = response.body();
                        if (data.getCurrent() != null) {
                            String desc = String.format("Temp: %.1f%s, Vento: %.1f%s, Precip: %.1f%s",
                                data.getCurrent().getTemperature2m(), data.getCurrentUnits().getTemperature2m(),
                                data.getCurrent().getWindSpeed10m(), data.getCurrentUnits().getWindSpeed10m(),
                                data.getCurrent().getPrecipitation(), data.getCurrentUnits().getPrecipitation());
                            addWeatherItem(new WeatherItem("☁️ Meteo Attuale", desc, "Ora"));

                            if (data.getCurrent().getWindSpeed10m() >= WIND_SPEED_THRESHOLD) {
                                sendWindNotification(data.getCurrent().getWindSpeed10m());
                            }
                        }
                    } else {
                         addWeatherItem(new WeatherItem("☁️ Meteo Attuale", "Dati non disponibili", ""));
                    }
                    checkRequestsFinished();
                }

                @Override
                public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                    addWeatherItem(new WeatherItem("☁️ Meteo Attuale", "Errore di rete", ""));
                    checkRequestsFinished();
                }
            });
    }

    private void fetchFeedData() {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLat, currentLon, 1);
            if (addresses == null || addresses.isEmpty()) {
                addWeatherItem(new WeatherItem("Feed Allarmi", "Impossibile determinare la posizione.", ""));
                checkRequestsFinished();
                return;
            }

            Address address = addresses.get(0);
            String countryName = address.getCountryName();
            String region = address.getAdminArea(); // Es. "Veneto"

            String feedUrl;
            if ("italy".equalsIgnoreCase(countryName)) {
                feedUrl = "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-italy";
            } else if ("hungary".equalsIgnoreCase(countryName)) {
                feedUrl = "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-hungary";
            } else if ("spain".equalsIgnoreCase(countryName)) {
                feedUrl = "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-spain";
            } else {
                addWeatherItem(new WeatherItem("Feed Allarmi", "Feed non disponibile per questo luogo.", ""));
                checkRequestsFinished();
                return;
            }

            FeedApiClient.getClient().create(FeedApiService.class).getFeed(feedUrl)
                .enqueue(new Callback<AtomFeed>() {
                    @Override
                    public void onResponse(@NonNull Call<AtomFeed> call, @NonNull Response<AtomFeed> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AtomFeed feed = response.body();
                            String alertSummary = findAlertForRegion(feed, region);
                            addWeatherItem(new WeatherItem("Feed Allarmi", alertSummary, "Oggi"));
                        } else {
                            addWeatherItem(new WeatherItem("Feed Allarmi", "Errore nel caricamento del feed.", ""));
                        }
                        checkRequestsFinished();
                    }

                    @Override
                    public void onFailure(@NonNull Call<AtomFeed> call, @NonNull Throwable t) {
                        Log.e("FEED_ERROR", "Errore di rete feed", t);
                        addWeatherItem(new WeatherItem("Feed Allarmi", "Errore di rete.", ""));
                        checkRequestsFinished();
                    }
                });

        } catch (IOException e) {
            Log.e("GEOCODER_ERROR", "Errore Geocoder: " + e.getMessage());
            addWeatherItem(new WeatherItem("Feed Allarmi", "Errore di geolocalizzazione.", ""));
            checkRequestsFinished();
        }
    }

    private String findAlertForRegion(AtomFeed feed, String region) {
        if (feed == null || feed.getEntries() == null || region == null) {
            return "✅ Nessuna allerta attiva.";
        }

        for (AtomEntry entry : feed.getEntries()) {
            if (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(region.toLowerCase())) {
                String message = entry.getSummary();
                if (message == null || message.trim().isEmpty()) {
                    message = entry.getTitle();
                }
                return "⚠️ " + message;
            }
        }

        return "✅ Nessuna allerta attiva per la tua regione.";
    }


    private synchronized void addWeatherItem(WeatherItem item) {
        weatherList.add(item);
        weatherAdapter.notifyItemInserted(weatherList.size() - 1);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Allerte Vento", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifiche per allerte di vento forte.");
            requireActivity().getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendWindNotification(double windSpeed) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        String title;
        int color;
        if (windSpeed >= 90) {
            title = "Pericolo Uragani";
            color = Color.MAGENTA;
        } else if (windSpeed >= 60) {
            title = "Vento Molto Forte";
            color = Color.RED;
        } else if (windSpeed >= 40) {
            title = "Vento Forte";
            color = Color.rgb(255, 165, 0);
        } else {
            title = "Vento Moderato";
            color = Color.YELLOW;
        }

        // Controlla se una notifica identica è già attiva
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification sbn : activeNotifications) {
            if (sbn.getId() == NOTIFICATION_ID) {
                String existingTitle = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
                if (title.equals(existingTitle)) {
                    Log.d("WIND_NOTIFICATION", "Skipping notification, an identical one is already active.");
                    return; // Esce senza inviare una notifica duplicata
                }
                break; 
            }
        }

        String contentText = String.format("Velocità del vento: %.1f km/h", windSpeed);
        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), "Posizione Attuale", title, contentText, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true); // <-- AGGIUNTO PER SILENZIARE GLI AGGIORNAMENTI

        NotificationManagerCompat.from(requireContext()).notify(NOTIFICATION_ID, builder.build());

        // Aggiunge l'allerta alla UI interna dell'app (es. NotificationsFragment)
        alertViewModel.addWindAlert(newAlert);
    }
}
