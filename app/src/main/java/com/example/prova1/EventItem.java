// Create this file at: C:/Users/ricux/StudioProjects/androidMETAL/app/src/main/java/com/example/prova1/EventItem.java

package com.example.prova1;

import com.google.gson.annotations.SerializedName;

// This class represents a single item within the "features" array of the JSON response.
public class EventItem {

    // The "properties" object in GeoJSON contains the actual data attributes.
    @SerializedName("properties")
    private EventProperties properties;

    public EventProperties getProperties() {
        return properties;
    }

    public void setProperties(EventProperties properties) {
        this.properties = properties;
    }

    // Add other fields from the GeoJSON feature structure if needed, e.g., "geometry", "id", etc.
}
