package com.example.prova1;

import com.google.gson.annotations.SerializedName;

public class DisasterFeature {
    @SerializedName("properties")
    private DisasterProperties properties;

    public DisasterProperties getProperties() {
        return properties;
    }
}
