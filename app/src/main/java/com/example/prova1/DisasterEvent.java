// Create this file at: C:/Users/ricux/StudioProjects/androidMETAL/app/src/main/java/com/example/prova1/DisasterEvent.java

package com.example.prova1;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// This class represents the top-level object of the JSON response.
public class DisasterEvent {

    // The @SerializedName annotation links this field to the "features" key in the JSON.
    // The GeoJSON standard often uses "features" for a list of items.
    @SerializedName("features")
    private List<EventItem> events;
    private DisasterFeature[] features;

    public List<EventItem> getEvents() {
        return events;
    }

    public void setEvents(List<EventItem> events) {
        this.events = events;
    }

    public DisasterFeature[] getFeatures() {
        return features;
    }

    public void setFeatures(DisasterFeature[] features) {
        this.features = features;
    }
}
