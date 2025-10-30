package com.example.prova1;

import com.google.gson.annotations.SerializedName;

public class DisasterProperties {
    @SerializedName("class_diss")
    private String disasterClass;

    @SerializedName("tipo_diss")
    private String disasterType;

    public String getDisasterClass() {
        return disasterClass;
    }

    public String getDisasterType() {
        return disasterType;
    }

    // The API response contains many other fields.
    // We are only mapping these two for now.
}
