// Create this file at: C:/Users/ricux/StudioProjects/androidMETAL/app/src/main/java/com/example/prova1/EventProperties.java

package com.example.prova1;

import com.google.gson.annotations.SerializedName;

// This class maps to the "properties" object within each feature.
public class EventProperties {

    // Example fields. Replace these with the actual keys from your API's JSON response.
    @SerializedName("id_evento")
    private String eventId;

    @SerializedName("tipologia")
    private String type;

    @SerializedName("data_inizio")
    private String startDate;

    // --- Getter and Setter methods for the fields ---

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}

