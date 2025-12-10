package com.example.prova1.models;

public class WeatherItem {
    private String title;       // e.g. "Roma"
    private String description; // e.g. "Temp: 20°C, Wind: 10km/h"
    private String date;        // e.g. "2024-05-24T12:00"

    public WeatherItem(String title, String description, String date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
}
