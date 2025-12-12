//File per ora inutile, da modificare per usare con meteoalarm se funzionerà


package com.example.prova1;

import com.google.gson.annotations.SerializedName;

public class EventProperties {

    // Campi per MeteoAlarm (API Europea)

    @SerializedName("event") // Esempio: "Thunderstorms"
    private String event;

    @SerializedName("severity") // Esempio: "Severe" (Arancione) o "Extreme" (Rosso)
    private String severity;

    @SerializedName("headline") // Esempio: "Storm warning for Venice..."
    private String headline;

    @SerializedName("onset") // Data di inizio, es: "2025-12-12T10:00:00"
    private String onset;

    // --- Getter methods ---

    public String getEvent() { return event; }
    public String getSeverity() { return severity; }
    public String getHeadline() { return headline; }
    public String getOnset() { return onset; }
}