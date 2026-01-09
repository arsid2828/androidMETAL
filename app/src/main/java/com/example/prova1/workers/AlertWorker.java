package com.example.prova1.workers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.prova1.MainApplication;
import com.example.prova1.R;
import com.example.prova1.api.FeedApiClient;
import com.example.prova1.api.FeedApiService;
import com.example.prova1.api.WeatherApiClient;
import com.example.prova1.api.WeatherApiService;
import com.example.prova1.data.LocationRepository;
import com.example.prova1.models.AtomEntry;
import com.example.prova1.models.AtomFeed;
import com.example.prova1.models.LocationData;
import com.example.prova1.models.OpenMeteoResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class AlertWorker extends Worker {

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationRepository locationRepository;
    private final SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "alert_prefs";
    private static final String TAG = "AlertWorker";

    public AlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationRepository = new LocationRepository(context);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker starting...");

        // First, check saved locations, which don't require location permission.
        List<LocationData> savedLocations = locationRepository.getSavedLocations();
        Log.d(TAG, "Checking alerts for " + savedLocations.size() + " saved locations.");
        for (LocationData savedLocation : savedLocations) {
            checkAlerts(savedLocation);
        }

        // Now, check for background location permission to get the current location.
        boolean hasPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            hasPermission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        if (!hasPermission) {
            Log.w(TAG, "Background location permission not granted. Skipping current location check.");
            return Result.success(); // Still a success because we checked saved locations.
        }

        try {
            @SuppressLint("MissingPermission") // Permission is checked above.
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            Location location = Tasks.await(locationTask);
            if (location != null) {
                LocationData currentLocation = new LocationData("Posizione Attuale", location.getLatitude(), location.getLongitude());
                currentLocation.setCurrentLocation(true);
                Log.d(TAG, "Checking alerts for current location.");
                checkAlerts(currentLocation);
            } else {
                Log.w(TAG, "Current location is null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current location", e);
        }

        Log.d(TAG, "Worker finished.");
        return Result.success();
    }

    private void checkAlerts(LocationData locationData) {
        checkOpenMeteoAlerts(locationData);
        checkFeedAlerts(locationData);
    }

    private void checkOpenMeteoAlerts(LocationData locationData) {
        try {
            Response<OpenMeteoResponse> response = WeatherApiClient.getClient().create(WeatherApiService.class)
                    .getForecast(locationData.getLatitude(), locationData.getLongitude(), "temperature_2m", "wind_speed_10m", "visibility")
                    .execute();
            if (response.isSuccessful() && response.body() != null) {
                OpenMeteoResponse data = response.body();
                if (data.getCurrent() != null) {
                    double windSpeed = data.getCurrent().getWindSpeed10m();
                    int windSeverity = getWindSeverity(windSpeed);
                    compareAndNotify(locationData, "wind", windSeverity, String.format(Locale.getDefault(), "Vento a %.1f km/h", windSpeed), MainApplication.WIND_CHANNEL_ID);

                    double temperature = data.getCurrent().getTemperature2m();
                    int tempSeverity = getTempSeverity(temperature);
                    compareAndNotify(locationData, "temp", tempSeverity, String.format(Locale.getDefault(), "Temperatura di %.1f°C", temperature), MainApplication.TEMP_CHANNEL_ID);

                    double visibility = data.getCurrent().getVisibility();
                    int visibilitySeverity = getVisibilitySeverity(visibility);
                    compareAndNotify(locationData, "visibility", visibilitySeverity, String.format(Locale.getDefault(), "Visibilità di %.1f km", visibility / 1000), MainApplication.VISIBILITY_CHANNEL_ID);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching OpenMeteo data for " + locationData.getName(), e);
        }
    }

    private void checkFeedAlerts(LocationData locationData) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(locationData.getLatitude(), locationData.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String countryName = address.getCountryName();
                String region = address.getAdminArea();
                String feedUrl = getFeedUrlForCountry(countryName);

                if (feedUrl != null) {
                    Response<AtomFeed> response = FeedApiClient.getClient().create(FeedApiService.class).getFeed(feedUrl).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        AtomFeed feed = response.body();
                        if (feed.getEntries() != null) {
                            for (AtomEntry entry : feed.getEntries()) {
                                if (entry.getTitle() != null && region != null && entry.getTitle().toLowerCase().contains(region.toLowerCase())) {
                                    int severity = getFeedSeverity(entry.getTitle());
                                    String alertType = extractAlertType(entry.getSummary());
                                    compareAndNotify(locationData, "feed_" + alertType, severity, entry.getSummary(), MainApplication.FEED_CHANNEL_ID);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching feed data for " + locationData.getName(), e);
        }
    }

    private void compareAndNotify(LocationData locationData, String alertType, int currentSeverity, String description, String channelId) {
        String preferenceKey = getPreferenceKey(locationData, alertType);
        int previousSeverity = sharedPreferences.getInt(preferenceKey, 0);

        if (currentSeverity > previousSeverity) {
            Log.d(TAG, "Severity increased for " + alertType + " at " + locationData.getName() + ". New: " + currentSeverity + ", Old: " + previousSeverity);
            sendNotification(locationData, alertType, description, channelId);
            sharedPreferences.edit().putInt(preferenceKey, currentSeverity).apply();
        } else if (currentSeverity == 0 && previousSeverity > 0) {
            Log.d(TAG, "Alert ended for " + alertType + " at " + locationData.getName());
            sharedPreferences.edit().remove(preferenceKey).apply();
        }
    }

    @SuppressLint("MissingPermission")
    private void sendNotification(LocationData locationData, String alertType, String description, String channelId) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted.");
            return;
        }

        String title = getNotificationTitle(locationData, alertType);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setOnlyAlertOnce(true);

        int notificationId = (locationData.getName() + alertType).hashCode();
        Log.d(TAG, "Sending notification for " + alertType + " to " + locationData.getName() + " with ID: " + notificationId);
        NotificationManagerCompat.from(getApplicationContext()).notify(notificationId, builder.build());
    }

    private String getNotificationTitle(LocationData locationData, String alertType) {
        String readableAlertType;
        if (alertType.equals("wind")) {
            readableAlertType = "Vento";
        } else if (alertType.equals("temp")) {
            readableAlertType = "Temperatura";
        } else if (alertType.equals("visibility")) {
            readableAlertType = "Visibilità";
        } else if (alertType.startsWith("feed_")) {
            String feedType = alertType.substring(5);
            if (feedType.equals("Altro") || feedType.equals("Sconosciuto")) {
                return "Nuova allerta meteo per " + locationData.getName();
            }
            readableAlertType = "Allarme Meteo (" + feedType + ")";
        } else {
            readableAlertType = "Nuova";
        }
        return "Allerta " + readableAlertType + " per " + locationData.getName();
    }

    private String getPreferenceKey(LocationData locationData, String alertType) {
        return locationData.getName() + "_" + alertType;
    }

    private String getFeedUrlForCountry(String countryName) {
        if (countryName == null) return null;
        if ("italy".equalsIgnoreCase(countryName)) {
            return "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-italy";
        } else if ("hungary".equalsIgnoreCase(countryName)) {
            return "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-hungary";
        } else if ("spain".equalsIgnoreCase(countryName)) {
            return "https://feeds.meteoalarm.org/feeds/meteoalarm-legacy-atom-spain";
        }
        return null;
    }

    private int getWindSeverity(double windSpeed) {
        if (windSpeed >= 150) return 4;
        if (windSpeed >= 70) return 3;
        if (windSpeed >= 45) return 2;
        if (windSpeed >= 30) return 1;
        return 0;
    }

    private int getTempSeverity(double temp) {
        if (temp >= 38) return 3;
        if (temp >= 34) return 2;
        if (temp <= -5) return 2;
        if (temp <= 2) return 1;
        return 0;
    }

    private int getVisibilitySeverity(double visibilityInMeters) {
        if (visibilityInMeters < 100) return 4; // Dense fog, very dangerous
        if (visibilityInMeters < 500) return 3; // Thick fog
        if (visibilityInMeters < 1000) return 2; // Moderate fog
        if (visibilityInMeters < 5000) return 1; // Mist or poor visibility
        return 0; // Good visibility
    }

    private int getFeedSeverity(String title) {
        if (title.toLowerCase().contains("red")) return 3;
        if (title.toLowerCase().contains("orange")) return 2;
        if (title.toLowerCase().contains("yellow")) return 1;
        return 0;
    }

    private String extractAlertType(String summary) {
        if (summary == null) return "Sconosciuto";
        summary = summary.toLowerCase();
        if (summary.contains("wind")) return "Vento";
        if (summary.contains("rain")) return "Pioggia";
        if (summary.contains("snow") || summary.contains("ice")) return "Neve/Ghiaccio";
        if (summary.contains("thunderstorms")) return "Temporali";
        if (summary.contains("fog")) return "Nebbia";
        if (summary.contains("temperature")) return "Temperatura";
        if (summary.contains("coastal") || summary.contains("sea")) return "Costiero";
        if (summary.contains("forest fire")) return "Incendi";
        return "Altro";
    }
}
