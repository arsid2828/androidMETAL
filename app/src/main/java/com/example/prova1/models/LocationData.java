package com.example.prova1.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class LocationData implements Parcelable {
    private String name;
    private double latitude;
    private double longitude;
    private transient String weatherInfo = "Caricamento...";
    private transient String alertInfo = "";
    private transient int alertSeverity = 0;
    private transient String airQualityInfo = "";
    private boolean isFavorite = false;
    private boolean isCurrentLocation = false;

    private transient double temperature;
    private transient int humidity;
    private transient double windSpeed;
    private transient double precipitation;
    private transient double pm25;
    private transient int cloudCover;
    private transient double apparentTemperature;
    private transient double uvIndex;

    // This field is now initialized at declaration and is not transient.
    // This ensures it's handled by both Gson and Parcelable.
    private Map<String, Integer> alertTypeSeverity = new HashMap<>();

    public LocationData(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Parcelable implementation
    protected LocationData(Parcel in) {
        name = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        isFavorite = in.readByte() != 0;
        isCurrentLocation = in.readByte() != 0;
        
        // Use readSerializable for the map; it's safer than readMap for complex data.
        alertTypeSeverity = (Map<String, Integer>) in.readSerializable();
        if (alertTypeSeverity == null) {
            alertTypeSeverity = new HashMap<>();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeByte((byte) (isCurrentLocation ? 1 : 0));
        
        // Use writeSerializable for the map. We create a new HashMap to ensure it's serializable.
        dest.writeSerializable(new HashMap<>(getAlertTypeSeverity()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationData> CREATOR = new Creator<LocationData>() {
        @Override
        public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        @Override
        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };

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
    public double getApparentTemperature() { return apparentTemperature; }
    public double getUvIndex() { return uvIndex; }
    
    // Defensive getter for the alert severity map
    public Map<String, Integer> getAlertTypeSeverity() { 
        if (alertTypeSeverity == null) {
            alertTypeSeverity = new HashMap<>();
        }
        return alertTypeSeverity; 
    }

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
    public void setApparentTemperature(double apparentTemperature) { this.apparentTemperature = apparentTemperature; }
    public void setUvIndex(double uvIndex) { this.uvIndex = uvIndex; }
    public void setAlertTypeSeverity(Map<String, Integer> alertTypeSeverity) { this.alertTypeSeverity = alertTypeSeverity; }
}
