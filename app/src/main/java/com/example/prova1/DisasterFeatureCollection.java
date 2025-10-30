package com.example.prova1;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DisasterFeatureCollection {
    @SerializedName("features")
    private List<DisasterFeature> features;

    public List<DisasterFeature> getFeatures() {
        return features;
    }
}
