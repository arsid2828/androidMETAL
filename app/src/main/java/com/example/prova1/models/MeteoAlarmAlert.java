package com.example.prova1.models;

public class MeteoAlarmAlert {
    private String type;
    private int severity;
    private String title;
    private String summary;
    private String durationText;
    private int color;

    public MeteoAlarmAlert(String type, int severity, String title, String summary, String durationText, int color) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.summary = summary;
        this.durationText = durationText;
        this.color = color;
    }

    public String getType() { return type; }
    public int getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDurationText() { return durationText; }
    public int getColor() { return color; }
}
