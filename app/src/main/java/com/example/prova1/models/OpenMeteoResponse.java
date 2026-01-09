package com.example.prova1.models;

import com.google.gson.annotations.SerializedName;

public class OpenMeteoResponse {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("generationtime_ms")
    private double generationtimeMs;

    @SerializedName("utc_offset_seconds")
    private int utcOffsetSeconds;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("timezone_abbreviation")
    private String timezoneAbbreviation;

    @SerializedName("elevation")
    private double elevation;

    @SerializedName("current_units")
    private CurrentUnits currentUnits;

    @SerializedName("current")
    private Current current;

    @SerializedName("daily")
    private Daily daily;

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getGenerationtimeMs() { return generationtimeMs; }
    public int getUtcOffsetSeconds() { return utcOffsetSeconds; }
    public String getTimezone() { return timezone; }
    public String getTimezoneAbbreviation() { return timezoneAbbreviation; }
    public double getElevation() { return elevation; }
    public CurrentUnits getCurrentUnits() { return currentUnits; }
    public Current getCurrent() { return current; }
    public Daily getDaily() { return daily; }
}
