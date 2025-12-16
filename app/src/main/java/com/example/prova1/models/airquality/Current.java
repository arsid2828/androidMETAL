package com.example.prova1.models.airquality;

import com.google.gson.annotations.SerializedName;

public class Current {
    @SerializedName("time")
    private String time;

    @SerializedName("pm2_5")
    private double pm2_5; // Use pm2_5 to match the JSON

    public String getTime() {
        return time;
    }

    public double getPm2_5() { // The getter method
        return pm2_5;
    }
}
