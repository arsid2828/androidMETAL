package com.example.prova1.models.airquality;

import com.google.gson.annotations.SerializedName;

public class CurrentUnits {
    @SerializedName("pm2_5")
    private String pm2_5;

    public String getPm2_5() {
        return pm2_5;
    }
}
