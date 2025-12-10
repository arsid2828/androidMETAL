package com.example.prova1.models;

import com.google.gson.annotations.SerializedName;

public class Current {
    @SerializedName("time")
    private String time;

    @SerializedName("interval")
    private int interval;

    @SerializedName("temperature_2m")
    private double temperature2m;

    @SerializedName("precipitation")
    private double precipitation;

    @SerializedName("wind_speed_10m")
    private double windSpeed10m;

    public String getTime() { return time; }
    public int getInterval() { return interval; }
    public double getTemperature2m() { return temperature2m; }
    public double getPrecipitation() { return precipitation; }
    public double getWindSpeed10m() { return windSpeed10m; }
}
