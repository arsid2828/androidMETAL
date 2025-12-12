package com.example.prova1.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class WindAlert {
    private final long timestamp;
    private final String location;
    private final String title;
    private final String content;
    private final int color;

    public WindAlert(long timestamp, String location, String title, String content, int color) {
        this.timestamp = timestamp;
        this.location = location;
        this.title = title;
        this.content = content;
        this.color = color;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getColor() {
        return color;
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(timestamp));
    }

    public String getFormattedTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    // Due alert sono uguali se hanno la stessa data e lo stesso contenuto (velocità del vento)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WindAlert windAlert = (WindAlert) o;
        return Objects.equals(getFormattedDate(), windAlert.getFormattedDate()) &&
               Objects.equals(getContent(), windAlert.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormattedDate(), getContent());
    }
}
