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
import androidx.annotation.Nullable;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

    private static final String WIND_CHANNEL_ID = "wind_notification_channel";
    private static final String FEED_CHANNEL_ID = "feed_notification_channel";
    private static final int WIND_NOTIFICATION_ID = 1;
    private static final int FEED_NOTIFICATION_ID = 2;
    private static final double WIND_SPEED_THRESHOLD = 30.0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocationAndFetchData();
                } else {
                    Toast.makeText(getContext(), "Permesso GPS negato.", Toast.LENGTH_LONG).show();
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
        createNotificationChannels();
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
            fetchOpenMeteoData(location);
            fetchFeedData(location);
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
        fetchOpenMeteoData(locationData);
        fetchFeedData(locationData);
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
                .getForecast(locationData.getLatitude(), locationData.getLongitude(), "temperature_2m,precipitation,wind_speed_10m")
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
                                locationData.setWeatherInfo(desc);

                                if (data.getCurrent().getWindSpeed10m() >= WIND_SPEED_THRESHOLD) {
                                    sendWindNotification(data.getCurrent().getWindSpeed10m(), locationData);
                                }
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

    private void fetchFeedData(final LocationData locationData) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(locationData.getLatitude(), locationData.getLongitude(), 1);
            if (addresses == null || addresses.isEmpty()) {
                locationData.setAlertInfo("");
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
                            }
                            locationAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onFailure(@NonNull Call<AtomFeed> call, @NonNull Throwable t) {
                            Log.e("FEED_ERROR", "Errore di rete feed", t);
                            locationData.setAlertInfo("");
                            locationAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });

        } catch (IOException e) {
            Log.e("GEOCODER_ERROR", "Errore Geocoder: " + e.getMessage());
            locationData.setAlertInfo("");
            locationAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void findAlertForRegion(AtomFeed feed, String region, LocationData locationData) {
        if (feed == null || feed.getEntries() == null || region == null) {
            locationData.setAlertInfo("");
            return;
        }

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

                int color = Color.GRAY;
                String lowerCaseTitle = title.toLowerCase();
                if (lowerCaseTitle.contains("red")) {
                    color = Color.RED;
                } else if (lowerCaseTitle.contains("orange")) {
                    color = Color.rgb(255, 165, 0);
                } else if (lowerCaseTitle.contains("yellow")) {
                    color = Color.YELLOW;
                }

                locationData.setAlertInfo(summary);
                sendFeedNotification(title, summary, durationText, color, locationData);
                return;
            }
        }

        locationData.setAlertInfo("");
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);

            NotificationChannel windChannel = new NotificationChannel(WIND_CHANNEL_ID, "Allerte Vento", NotificationManager.IMPORTANCE_HIGH);
            windChannel.setDescription("Notifiche per allerte di vento forte.");
            notificationManager.createNotificationChannel(windChannel);

            NotificationChannel feedChannel = new NotificationChannel(FEED_CHANNEL_ID, "Allarmi Feed", NotificationManager.IMPORTANCE_HIGH);
            feedChannel.setDescription("Notifiche da feed esterni.");
            notificationManager.createNotificationChannel(feedChannel);
        }
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendWindNotification(double windSpeed, LocationData locationData) {
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

        String notificationTitle = title + " a " + locationData.getName();
        if (isNotificationActive(WIND_NOTIFICATION_ID, notificationTitle)) {
            Log.d("WIND_NOTIFICATION", "Skipping duplicate notification.");
            return;
        }

        String contentText = String.format("Velocità del vento: %.1f km/h", windSpeed);
        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), locationData.getName(), title, contentText, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), WIND_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(contentText)
                .setColor(color)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(requireContext()).notify(WIND_NOTIFICATION_ID, builder.build());
        alertViewModel.addWindAlert(newAlert);
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void sendFeedNotification(String title, String summary, String durationText, int color, LocationData locationData) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        String notificationTitle = title + " (" + locationData.getName() + ")";
        if (isNotificationActive(FEED_NOTIFICATION_ID, notificationTitle)) {
            Log.d("FEED_NOTIFICATION", "Skipping duplicate notification.");
            return;
        }

        WindAlert newAlert = new WindAlert(System.currentTimeMillis(), locationData.getName(), title, summary, color);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), FEED_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(durationText)
                .setColor(color)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(summary))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat.from(requireContext()).notify(FEED_NOTIFICATION_ID, builder.build());
        alertViewModel.addWindAlert(newAlert);
    }

    @SuppressLint("NewApi")
    private boolean isNotificationActive(int notificationId, String title) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification sbn : activeNotifications) {
            if (sbn.getId() == notificationId) {
                String existingTitle = sbn.getNotification().extras.getString(Notification.EXTRA_TITLE);
                if (title.equals(existingTitle)) {
                    return true;
                }
            }
        }
        return false;
    }
}
