package com.example.prova1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;
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
import java.util.StringJoiner;

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
    private static final int VISIBILITY_NOTIFICATION_ID = 5;
    private static final int UV_INDEX_NOTIFICATION_ID = 6;
    private static final double WIND_SPEED_THRESHOLD = 30.0;
    private static final double PM25_THRESHOLD = 25.0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    requestBackgroundLocationPermission();
                } else {
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Permesso GPS negato.", Toast.LENGTH_LONG).show();
                    }
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
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Permesso notifiche negato.", Toast.LENGTH_SHORT).show();
                    }
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

    @Override
    public void onResume() {
        super.onResume();
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

    public void refreshData() {
        Context context = getContext();
        if (context == null) return;

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

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocationAndFetchData(isCurrentLocationFavorite);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void requestBackgroundLocationPermission() {
        Context context = getContext();
        if (context == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            } else {
                getCurrentLocationAndFetchData();
            }
        } else {
            getCurrentLocationAndFetchData();
        }
    }

    private void showBackgroundLocationDialog() {
        Context context = getContext();
        if (context == null) return;

        new AlertDialog.Builder(context)
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
                    Context context = getContext();
                    if (location != null) {
                        LocationData currentLocation = new LocationData("Posizione Attuale", location.getLatitude(), location.getLongitude());
                        currentLocation.setCurrentLocation(true);
                        currentLocation.setFavorite(isFavorite);
                        startDataFetch(currentLocation);
                    } else if (context != null) {
                        Toast.makeText(context, "Posizione non trovata.", Toast.LENGTH_SHORT).show();
                        sortAndDisplayLocations();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Errore GPS.", Toast.LENGTH_SHORT).show();
                    }
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
        Context context = getContext();
        if (context == null) return;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(cityName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LocationData newLocation = new LocationData(address.getLocality(), address.getLatitude(), address.getLongitude());
                startDataFetch(newLocation);
            } else {
                Toast.makeText(context, "Città non trovata.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(context, "Errore di rete. Impossibile aggiungere la città.", Toast.LENGTH_SHORT).show();
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        StringJoiner currentJoiner = new StringJoiner(",");
        currentJoiner.add("temperature_2m");
        currentJoiner.add("relative_humidity_2m");
        currentJoiner.add("precipitation");
        currentJoiner.add("wind_speed_10m");
        currentJoiner.add("pressure_msl");

        if (prefs.getBoolean("cloud_cover", false)) {
            currentJoiner.add("cloud_cover");
        }
        if (prefs.getBoolean("apparent_temperature", false)) {
            currentJoiner.add("apparent_temperature");
        }
        if (prefs.getBoolean("uv_index", false)) {
            currentJoiner.add("uv_index");
        }
        if (prefs.getBoolean("visibility", false)) {
            currentJoiner.add("visibility");
        }

        String current = currentJoiner.toString();
        String daily = "";
        if (prefs.getBoolean("sunrise_sunset", false)) {
            daily = "sunrise,sunset";
        }

        WeatherApiClient.getClient().create(WeatherApiService.class)
                .getForecast(locationData.getLatitude(), locationData.getLongitude(), current, daily, "auto")
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
                                locationData.setPressureMsl(data.getCurrent().getPressureMsl());
                                if (prefs.getBoolean("cloud_cover", false)) {
                                    locationData.setCloudCover(data.getCurrent().getCloudCover());
                                }
                                if (prefs.getBoolean("apparent_temperature", false)) {
                                    locationData.setApparentTemperature(data.getCurrent().getApparentTemperature());
                                }
                                if (prefs.getBoolean("uv_index", false)) {
                                    locationData.setUvIndex(data.getCurrent().getUvIndex());
                                    sendUvIndexNotification(data.getCurrent().getUvIndex(), locationData);
                                }
                                if (prefs.getBoolean("visibility", false)) {
                                    locationData.setVisibility(data.getCurrent().getVisibility());
                                }

                                locationData.setWeatherInfo(""); // Clear old weather info

                                if (data.getCurrent().getWindSpeed10m() >= WIND_SPEED_THRESHOLD) {
                                    sendWindNotification(data.getCurrent().getWindSpeed10m(), locationData);
                                }

                                double currentTemp = data.getCurrent().getTemperature2m();
                                sendTemperatureNotification(currentTemp, locationData);

                                if (prefs.getBoolean("visibility", false)) {
                                    double currentVisibility = data.getCurrent().getVisibility();
                                    sendVisibilityNotification(currentVisibility, locationData);
                                }
                            }
                            if (data.getDaily() != null && data.getDaily().getSunrise() != null && !data.getDaily().getSunrise().isEmpty()) {
                                locationData.setSunrise(data.getDaily().getSunrise().get(0));
                                locationData.setSunset(data.getDaily().getSunset().get(0));
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        if (!prefs.getBoolean("pm25", false)) {
            locationData.setAirQualityInfo("disabled");
            locationAdapter.notifyDataSetChanged();
            return;
        }
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
        Context context = getContext();
        if (context == null) return;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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
            
            WindAlert newAlert = new WindAlert(System.currentTimeMillis(), locationData.getName(), alert.title, alert.summary, alert.color);
            alertViewModel.addWindAlert(newAlert);

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
        alertViewModel.addWindAlert(newAlert);

        Context context = getContext();
        if (context == null) return;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.WIND_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context).notify(WIND_NOTIFICATION_ID, builder.build());
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendFeedNotification(String title, String summary, String durationText, int color, LocationData location, int notificationId) {
        Context context = getContext();
        if (context == null) return;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.FEED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(durationText)
                .setColor(color)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(summary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendTemperatureNotification(double temp, LocationData location) {
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
        alertViewModel.addWindAlert(newAlert);

        Context context = getContext();
        if (context == null) return;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.TEMP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context).notify(location.getName().hashCode(), builder.build());
    }
    
    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendUvIndexNotification(double uvIndex, LocationData location) {
        String title;
        String contentText;
        int color;

        if (uvIndex >= 11) {
            title = "Indice UV Estremo a " + location.getName();
            contentText = String.format("Indice UV: %.1f. Evitare l'esposizione al sole.", uvIndex);
            color = Color.MAGENTA;
        } else if (uvIndex >= 8) {
            title = "Indice UV Molto Alto a " + location.getName();
            contentText = String.format("Indice UV: %.1f. Protezione solare molto alta richiesta.", uvIndex);
            color = Color.RED;
        } else if (uvIndex >= 6) {
            title = "Indice UV Alto a " + location.getName();
            contentText = String.format("Indice UV: %.1f. Usare protezione solare.", uvIndex);
            color = Color.rgb(255, 165, 0); // Orange
        } else if (uvIndex >= 3) {
            title = "Indice UV Moderato a " + location.getName();
            contentText = String.format("Indice UV: %.1f. Consigliata protezione solare.", uvIndex);
            color = Color.YELLOW;
        } else {
            return; 
        }

        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), location.getName(), title, contentText, color);
        alertViewModel.addWindAlert(newAlert);

        Context context = getContext();
        if (context == null) return;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.UV_INDEX_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context).notify(UV_INDEX_NOTIFICATION_ID, builder.build());
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendVisibilityNotification(double visibility, LocationData location) {
        String titleSuffix = null;
        int color = 0;
        if (visibility < 100) {
            titleSuffix = "Visibilità Molto Bassa";
            color = Color.RED;
        } else if (visibility < 500) {
            titleSuffix = "Visibilità Bassa";
            color = Color.rgb(255, 165, 0);
        } else if (visibility < 1000) {
            titleSuffix = "Visibilità Ridotta";
            color = Color.YELLOW;
        } else {
            return;
        }
        String notificationTitle = "In questo momento a " + location.getName() + " " + titleSuffix;
        String contentText = String.format("Visibilità: %.1f km a %s", visibility / 1000, location.getName());
        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), location.getName(), notificationTitle, contentText, color);
        alertViewModel.addWindAlert(newAlert);

        Context context = getContext();
        if (context == null) return;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.VISIBILITY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context).notify(VISIBILITY_NOTIFICATION_ID, builder.build());
    }


    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendAirQualityNotification(double pm25, LocationData location) {
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
        alertViewModel.addWindAlert(newAlert);

        Context context = getContext();
        if (context == null) return;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.AIR_QUALITY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(context).notify(AIR_QUALITY_NOTIFICATION_ID, builder.build());
    }
}
