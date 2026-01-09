package com.example.prova1.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.prova1.models.LocationData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationRepository {

    private static final String PREFS_NAME = "locations_prefs";
    private static final String LOCATIONS_KEY = "saved_locations";

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public LocationRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLocations(List<LocationData> locations) {
        List<LocationData> savableLocations = new ArrayList<>();
        for (LocationData location : locations) {
            // Don't save the current location, as it's dynamic.
            if (!location.isCurrentLocation()) {
                savableLocations.add(location);
            }
        }
        String json = gson.toJson(savableLocations);
        sharedPreferences.edit().putString(LOCATIONS_KEY, json).apply();
    }

    public List<LocationData> getSavedLocations() {
        String json = sharedPreferences.getString(LOCATIONS_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<LocationData>>() {}.getType();
        List<LocationData> locations = gson.fromJson(json, type);

        if (locations != null) {
            for (LocationData location : locations) {
                if (location.getAlertTypeSeverity() == null) {
                    location.setAlertTypeSeverity(new HashMap<>());
                }
            }
        } else {
            return new ArrayList<>();
        }

        return locations;
    }
}
