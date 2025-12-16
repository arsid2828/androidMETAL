package com.example.prova1.models.airquality;

import com.google.gson.annotations.SerializedName;

public class AirQualityResponse {
    @SerializedName("current")
    private Current current;

    @SerializedName("current_units")
    private CurrentUnits currentUnits;

    public Current getCurrent() {
        return current;
    }

    public CurrentUnits getCurrentUnits() {
        return currentUnits;
    }
}
