package com.example.prova1.models;

public class LocationData {
    private String name;
    private double latitude;
    private double longitude;
    private String weatherInfo = "Caricamento...";
    private String alertInfo = "";
    private boolean isFavorite = false;
    private boolean isCurrentLocation = false;

    public LocationData(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getWeatherInfo() {
        return weatherInfo;
    }

    public String getAlertInfo() {
        return alertInfo;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isCurrentLocation() {
        return isCurrentLocation;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setWeatherInfo(String weatherInfo) {
        this.weatherInfo = weatherInfo;
    }

    public void setAlertInfo(String alertInfo) {
        this.alertInfo = alertInfo;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setCurrentLocation(boolean currentLocation) {
        isCurrentLocation = currentLocation;
    }
}
