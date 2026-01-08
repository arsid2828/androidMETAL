package com.example.prova1.models;

public class LocationData {
    private String name;
    private double latitude;
    private double longitude;
    private transient String weatherInfo = "Caricamento...";
    private transient String alertInfo = "";
    private transient int alertSeverity = 0; // 0: None, 1: Yellow, 2: Orange, 3: Red
    private transient String airQualityInfo = "";
    private boolean isFavorite = false;
    private boolean isCurrentLocation = false;

    private transient double temperature;
    private transient int humidity;
    private transient double windSpeed;
    private transient double precipitation;
    private transient double pm25;
    private transient int cloudCover;

    public LocationData(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getName() { return name != null ? name : ""; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getWeatherInfo() { return weatherInfo != null ? weatherInfo : ""; }
    public String getAlertInfo() { return alertInfo != null ? alertInfo : ""; }
    public int getAlertSeverity() { return alertSeverity; }
    public String getAirQualityInfo() { return airQualityInfo != null ? airQualityInfo : ""; }
    public boolean isFavorite() { return isFavorite; }
    public boolean isCurrentLocation() { return isCurrentLocation; }
    public double getTemperature() { return temperature; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public double getPrecipitation() { return precipitation; }
    public double getPm25() { return pm25; }
    public int getCloudCover() { return cloudCover; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setWeatherInfo(String weatherInfo) { this.weatherInfo = weatherInfo; }
    public void setAlertInfo(String alertInfo) { this.alertInfo = alertInfo; }
    public void setAlertSeverity(int alertSeverity) { this.alertSeverity = alertSeverity; }
    public void setAirQualityInfo(String airQualityInfo) { this.airQualityInfo = airQualityInfo; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setCurrentLocation(boolean currentLocation) { isCurrentLocation = currentLocation; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }
    public void setPm25(double pm25) { this.pm25 = pm25; }
    public void setCloudCover(int cloudCover) { this.cloudCover = cloudCover; }
}
