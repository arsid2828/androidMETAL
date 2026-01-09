package com.example.prova1.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Daily {
    @SerializedName("time")
    private List<String> time;

    @SerializedName("sunrise")
    private List<String> sunrise;

    @SerializedName("sunset")
    private List<String> sunset;

    public List<String> getTime() { return time; }
    public List<String> getSunrise() { return sunrise; }
    public List<String> getSunset() { return sunset; }
}
