package com.example.prova1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prova1.api.AirQualityApiClient;
import com.example.prova1.api.AirQualityApiService;
import com.example.prova1.api.FeedApiClient;
import com.example.prova1.api.FeedApiService;
import com.example.prova1.api.WeatherApiClient;
import com.example.prova1.api.WeatherApiService;
import com.example.prova1.data.LocationRepository;
import com.example.prova1.models.AlertViewModel;
import com.example.prova1.models.AtomEntry;
import com.example.prova1.models.AtomFeed;
import com.example.prova1.models.LocationData;
import com.example.prova1.models.OpenMeteoResponse;
import com.example.prova1.models.WindAlert;
import com.example.prova1.ui.AddLocationDialogFragment;
import com.example.prova1.ui.LocationAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements AddLocationDialogFragment.AddLocationDialogListener, LocationAdapter.OnItemInteractionListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private AlertViewModel alertViewModel;
    private LocationAdapter locationAdapter;
    private List<LocationData> locations = new ArrayList<>();
    private LocationRepository locationRepository;

    private static final int WIND_NOTIFICATION_ID = 1;
    private static final int AIR_QUALITY_NOTIFICATION_ID = 3;
    private static final double WIND_SPEED_THRESHOLD = 30.0;
    private static final double PM25_THRESHOLD = 25.0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    requestBackgroundLocationPermission();
                } else {
                    Toast.makeText(getContext(), "Permesso GPS negato.", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<String> requestBackgroundLocationLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocationAndFetchData();
                } else {
                    showBackgroundLocationDialog();
                }
            });

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(getContext(), "Permesso notifiche negato.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationRepository = new LocationRepository(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        alertViewModel = new ViewModelProvider(requireActivity()).get(AlertViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupUI(view);
        refreshData();
    }

    private void setupUI(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        RecyclerView recyclerView = view.findViewById(R.id.locations_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        locationAdapter = new LocationAdapter();
        locationAdapter.setOnItemInteractionListener(this);
        recyclerView.setAdapter(locationAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorSecondary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        if (!swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(true);

        boolean isCurrentLocationFavorite = false;
        for (LocationData loc : locations) {
            if (loc.isCurrentLocation() && loc.isFavorite()) {
                isCurrentLocationFavorite = true;
                break;
            }
        }

        locations.clear();
        locations.addAll(locationRepository.getSavedLocations());

        for(LocationData location : locations) {
            fetchAllDataForLocation(location);
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocationAndFetchData(isCurrentLocationFavorite);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            } else {
                getCurrentLocationAndFetchData();
            }
        } else {
            getCurrentLocationAndFetchData();
        }
    }

    private void showBackgroundLocationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Permesso di Posizione in Background")
                .setMessage("Per ricevere notifiche di allerte meteo anche quando l'app è chiusa, è necessario concedere il permesso di accedere alla posizione in background. Si prega di selezionare 'Consenti sempre' nelle impostazioni.")
                .setPositiveButton("Apri Impostazioni", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void fetchAllDataForLocation(LocationData locationData) {
        fetchOpenMeteoData(locationData);
        fetchAirQualityData(locationData);
        fetchFeedData(locationData);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndFetchData(boolean isFavorite) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LocationData currentLocation = new LocationData("Posizione Attuale", location.getLatitude(), location.getLongitude());
                        currentLocation.setCurrentLocation(true);
                        currentLocation.setFavorite(isFavorite);
                        startDataFetch(currentLocation);
                    } else {
                        Toast.makeText(getContext(), "Posizione non trovata.", Toast.LENGTH_SHORT).show();
                        sortAndDisplayLocations();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Errore GPS.", Toast.LENGTH_SHORT).show();
                    sortAndDisplayLocations();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndFetchData() {
        getCurrentLocationAndFetchData(false);
    }

    private void startDataFetch(LocationData locationData) {
        if (locationData.isCurrentLocation()) {
            Iterator<LocationData> iterator = locations.iterator();
            while (iterator.hasNext()) {
                LocationData loc = iterator.next();
                if (loc.isCurrentLocation()) {
                    iterator.remove();
                }
            }
        }

        locations.add(locationData);
        sortAndDisplayLocations();
        locationRepository.saveLocations(locations);
        fetchAllDataForLocation(locationData);
    }

    public void showAddLocationDialog() {
        AddLocationDialogFragment dialog = new AddLocationDialogFragment();
        dialog.show(getChildFragmentManager(), "AddLocationDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(String cityName) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LocationData newLocation = new LocationData(address.getLocality(), address.getLatitude(), address.getLongitude());
                startDataFetch(newLocation);
            } else {
                Toast.makeText(getContext(), "Città non trovata.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Errore di rete. Impossibile aggiungere la città.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        locations.remove(position);
        locationAdapter.setLocations(locations);
        locationRepository.saveLocations(locations);
    }

    @Override
    public void onFavoriteClick(int position) {
        LocationData location = locations.get(position);
        location.setFavorite(!location.isFavorite());
        sortAndDisplayLocations();
        locationRepository.saveLocations(locations);
    }

    private void sortAndDisplayLocations() {
        Collections.sort(locations, (o1, o2) -> Boolean.compare(o2.isFavorite(), o1.isFavorite()));
        locationAdapter.setLocations(locations);
    }

    private void fetchOpenMeteoData(final LocationData locationData) {
        WeatherApiClient.getClient().create(WeatherApiService.class)
                .getForecast(locationData.getLatitude(), locationData.getLongitude(), "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,cloud_cover")
                .enqueue(new Callback<OpenMeteoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenMeteoResponse data = response.body();
                            if (data.getCurrent() != null) {
                                locationData.setTemperature(data.getCurrent().getTemperature2m());
                                locationData.setHumidity(data.getCurrent().getRelativeHumidity2m());
                                locationData.setWindSpeed(data.getCurrent().getWindSpeed10m());
                                locationData.setPrecipitation(data.getCurrent().getPrecipitation());
                                locationData.setCloudCover(data.getCurrent().getCloudCover());
                                locationData.setWeatherInfo(""); // Clear old weather info

                                if (data.getCurrent().getWindSpeed10m() >= WIND_SPEED_THRESHOLD) {
                                    sendWindNotification(data.getCurrent().getWindSpeed10m(), locationData);
                                }

                                double currentTemp = data.getCurrent().getTemperature2m();
                                sendTemperatureNotification(currentTemp, locationData);
                            }
                        } else {
                            locationData.setWeatherInfo("Dati meteo non disponibili");
                        }
                        locationAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                        locationData.setWeatherInfo("Errore di rete");
                        locationAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void fetchAirQualityData(final LocationData locationData) {
        AirQualityApiClient.getClient().create(AirQualityApiService.class)
                .getAirQuality(locationData.getLatitude(), locationData.getLongitude(), "pm2_5")
                .enqueue(new Callback<OpenMeteoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<OpenMeteoResponse> call, @NonNull Response<OpenMeteoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenMeteoResponse data = response.body();
                            if (data.getCurrent() != null && data.getCurrent().getPm25() > 0) {
                                locationData.setPm25(data.getCurrent().getPm25());
                                locationData.setAirQualityInfo(""); // Clear old air quality info

                                if (data.getCurrent().getPm25() >= PM25_THRESHOLD) {
                                    sendAirQualityNotification(data.getCurrent().getPm25(), locationData);
                                }
                            }
                        } else {
                            locationData.setAirQualityInfo("");
                        }
                        locationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(@NonNull Call<OpenMeteoResponse> call, @NonNull Throwable t) {
                        locationData.setAirQualityInfo("");
                        locationAdapter.notifyDataSetChanged();
                        Log.e("AirQuality", "Failure: " + t.getMessage());
                    }
                });
    }

    private void fetchFeedData(final LocationData locationData) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(locationData.getLatitude(), locationData.getLongitude(), 1);
            if (addresses == null || addresses.isEmpty()) {
                locationData.setAlertInfo("");
                locationData.setAlertSeverity(0);
                locationAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            Address address = addresses.get(0);
            if (locationData.isCurrentLocation()) {
                locationData.setName(address.getLocality() != null ? address.getLocality() : "Posizione Sconosciuta");
            }

            String countryName = address.getCountryName();
            String region = address.getAdminArea();

            String feedUrl;
            if ("italy".equalsIgnoreCase(countryName)) {
                feedUrl = "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-italy";
            } else if ("hungary".equalsIgnoreCase(countryName)) {
                feedUrl = "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-hungary";
            } else if ("spain".equalsIgnoreCase(countryName)) {
                feedUrl = "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-spain";
            } else {
                locationData.setAlertInfo("");
                locationData.setAlertSeverity(0);
                locationAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            FeedApiClient.getClient().create(FeedApiService.class).getFeed(feedUrl)
                    .enqueue(new Callback<AtomFeed>() {
                        @Override
                        public void onResponse(@NonNull Call<AtomFeed> call, @NonNull Response<AtomFeed> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                AtomFeed feed = response.body();
                                findAlertForRegion(feed, region, locationData);
                            } else {
                                locationData.setAlertInfo("");
                                locationData.setAlertSeverity(0);
                            }
                            locationAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onFailure(@NonNull Call<AtomFeed> call, @NonNull Throwable t) {
                            Log.e("FEED_ERROR", "Errore di rete feed", t);
                            locationData.setAlertInfo("");
                            locationData.setAlertSeverity(0);
                            locationAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });

        } catch (IOException e) {
            Log.e("GEOCODER_ERROR", "Errore Geocoder: " + e.getMessage());
        }
    }

    private static class MeteoAlarmAlert {
        String type;
        int severity;
        String title;
        String summary;
        String durationText;
        int color;

        MeteoAlarmAlert(String type, int severity, String title, String summary, String durationText, int color) {
            this.type = type;
            this.severity = severity;
            this.title = title;
            this.summary = summary;
            this.durationText = durationText;
            this.color = color;
        }
    }

    private void findAlertForRegion(AtomFeed feed, String region, LocationData locationData) {
        if (feed == null || feed.getEntries() == null || region == null) {
            locationData.setAlertInfo("✅ Nessuna allerta attiva.");
            locationData.setAlertSeverity(0);
            return;
        }

        Map<String, Integer> previousAlertTypeSeverity = new HashMap<>(locationData.getAlertTypeSeverity());
        locationData.getAlertTypeSeverity().clear();

        List<MeteoAlarmAlert> allAlerts = new ArrayList<>();
        for (AtomEntry entry : feed.getEntries()) {
            if (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(region.toLowerCase())) {
                String title = entry.getTitle();
                String summary = entry.getSummary() != null ? entry.getSummary() : title;

                String durationText = title;
                String[] sentences = summary.split("\\. ");
                for (String sentence : sentences) {
                    if (sentence.toLowerCase().startsWith("valid to") || sentence.toLowerCase().startsWith("valido fino al")) {
                        durationText = sentence;
                        break;
                    }
                }

                int severity = 0;
                int color = Color.GRAY;
                if (title.toLowerCase().contains("red")) {
                    severity = 3;
                    color = Color.RED;
                } else if (title.toLowerCase().contains("orange")) {
                    severity = 2;
                    color = Color.rgb(255, 165, 0);
                } else if (title.toLowerCase().contains("yellow")) {
                    severity = 1;
                    color = Color.YELLOW;
                }

                String alertType = extractAlertType(summary);
                allAlerts.add(new MeteoAlarmAlert(alertType, severity, title, summary, durationText, color));
            }
        }

        if (allAlerts.isEmpty()) {
            locationData.setAlertInfo("✅ Nessuna allerta attiva per la tua regione.");
            locationData.setAlertSeverity(0);
            return;
        }

        Map<String, MeteoAlarmAlert> mostSevereAlerts = new HashMap<>();
        int maxSeverity = 0;
        for (MeteoAlarmAlert alert : allAlerts) {
            if (!mostSevereAlerts.containsKey(alert.type) || alert.severity > mostSevereAlerts.get(alert.type).severity) {
                mostSevereAlerts.put(alert.type, alert);
            }
            if (alert.severity > maxSeverity) {
                maxSeverity = alert.severity;
            }
        }

        StringBuilder alertsBuilder = new StringBuilder();
        for (MeteoAlarmAlert alert : mostSevereAlerts.values()) {
            alertsBuilder.append("⚠️ ").append(alert.summary).append(" | ");
            Integer previousSeverity = previousAlertTypeSeverity.get(alert.type);
            if (previousSeverity == null || alert.severity > previousSeverity) {
                int notificationId = (locationData.getName() + alert.title + alert.summary).hashCode();
                sendFeedNotification(alert.title, alert.summary, alert.durationText, alert.color, locationData, notificationId);
            }
            locationData.getAlertTypeSeverity().put(alert.type, alert.severity);
        }

        String finalAlertInfo = alertsBuilder.toString().trim();
        if (finalAlertInfo.endsWith("|")) {
            finalAlertInfo = finalAlertInfo.substring(0, finalAlertInfo.length() - 2);
        }
        locationData.setAlertInfo(finalAlertInfo);
        locationData.setAlertSeverity(maxSeverity);
    }

    private String extractAlertType(String summary) {
        if (summary == null) {
            return "Unknown";
        }
        if (summary.toLowerCase().contains("wind")) {
            return "Wind";
        }
        if (summary.toLowerCase().contains("rain")) {
            return "Rain";
        }
        if (summary.toLowerCase().contains("snow") || summary.toLowerCase().contains("ice")) {
            return "Snow/Ice";
        }
        if (summary.toLowerCase().contains("thunderstorms")) {
            return "Thunderstorms";
        }
        if (summary.toLowerCase().contains("fog")) {
            return "Fog";
        }
        if (summary.toLowerCase().contains("temperature")) {
            return "Temperature";
        }
        if (summary.toLowerCase().contains("coastal") || summary.toLowerCase().contains("sea")) {
            return "Coastal Event";
        }
        if (summary.toLowerCase().contains("forest fire")) {
            return "Forest Fire";
        }
        return "Other";
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendWindNotification(double windSpeed, LocationData location) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        String title;
        int color;
        if (windSpeed >= 150) {
            title = "In questo momento a " + location.getName() + " Pericolo Uragani";
            color = Color.MAGENTA;
        } else if (windSpeed >= 70) {
            title = "In questo momento a " + location.getName() + " Vento Molto Forte";
            color = Color.RED;
        } else if (windSpeed >= 45) {
            title = "In questo momento a " + location.getName() + " Vento Forte";
            color = Color.rgb(255, 165, 0);
        } else {
            title = "In questo momento a " + location.getName() + " Vento Moderato";
            color = Color.YELLOW;
        }

        String contentText = String.format("Vento a %.1f km/h a %s", windSpeed, location.getName());
        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), location.getName(), title, contentText, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), MainApplication.WIND_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(requireContext()).notify(WIND_NOTIFICATION_ID, builder.build());
        alertViewModel.addWindAlert(newAlert);
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendFeedNotification(String title, String summary, String durationText, int color, LocationData location, int notificationId) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), location.getName(), title, summary, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), MainApplication.FEED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(durationText)
                .setColor(color)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(summary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(requireContext()).notify(notificationId, builder.build());
        alertViewModel.addWindAlert(newAlert);
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendTemperatureNotification(double temp, LocationData location) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        String titleSuffix = null;
        int color = 0;

        if (temp >= 38) {
            titleSuffix = "Caldo Estremo";
            color = Color.RED;
        } else if (temp >= 34) {
            titleSuffix = "Caldo";
            color = Color.rgb(255, 165, 0);
        } else if (temp <= -5) {
            titleSuffix = "Gelo";
            color = Color.BLUE;
        } else if (temp <= 2) {
            titleSuffix = "Freddo";
            color = Color.CYAN;
        } else {
            return;
        }

        String notificationTitle = "In questo momento a " + location.getName() + " " + titleSuffix;

        String contentText = String.format("Temperatura: %.1f°C a %s", temp, location.getName());
        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), location.getName(), notificationTitle, contentText, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), MainApplication.TEMP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(requireContext()).notify(location.getName().hashCode(), builder.build());
        alertViewModel.addWindAlert(newAlert);
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendAirQualityNotification(double pm25, LocationData location) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        String title;
        int color;
        if (pm25 > 75) {
            title = "Qualità dell'Aria Nociva";
            color = Color.MAGENTA;
        } else if (pm25 > 50) {
            title = "Qualità dell'Aria Molto Scarsa";
            color = Color.RED;
        } else if (pm25 > 25) {
            title = "Qualità dell'Aria Scarsa";
            color = Color.rgb(255, 165, 0);
        } else {
            return;
        }

        String contentText = String.format("Qualità dell'aria in PM2.5: %.1f µg/m³ a %s", pm25, location.getName());
        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), location.getName(), title, contentText, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), MainApplication.AIR_QUALITY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(requireContext()).notify(AIR_QUALITY_NOTIFICATION_ID, builder.build());
        alertViewModel.addWindAlert(newAlert);
    }

    @SuppressLint("NewApi")
    private boolean isNotificationActive(int notificationId, String title) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        android.service.notification.StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for (android.service.notification.StatusBarNotification sbn : activeNotifications) {
            if (sbn.getId() == notificationId) {
                String existingTitle = sbn.getNotification().extras.getString(android.app.Notification.EXTRA_TITLE);
                if (title.equals(existingTitle)) {
                    return true;
                }
            }
        }
        return false;
    }
}
