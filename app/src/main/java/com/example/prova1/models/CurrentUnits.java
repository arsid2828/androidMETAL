package com.example.prova1.models;

import com.google.gson.annotations.SerializedName;

public class CurrentUnits {
    @SerializedName("time")
    private String time;

    @SerializedName("interval")
    private String interval;

    @SerializedName("temperature_2m")
    private String temperature2m;

    @SerializedName("precipitation")
    private String precipitation;

    @SerializedName("wind_speed_10m")
    private String windSpeed10m;

    public String getTime() { return time; }
    public String getInterval() { return interval; }
    public String getTemperature2m() { return temperature2m; }
    public String getPrecipitation() { return precipitation; }
    public String getWindSpeed10m() { return windSpeed10m; }
}
