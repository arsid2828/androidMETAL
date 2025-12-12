package com.example.prova1.models;

import java.util.Objects;

public class WindAlert {
    private final String title;
    private final String content;
    private final int color;

    public WindAlert(String title, String content, int color) {
        this.title = title;
        this.content = content;
        this.color = color;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WindAlert windAlert = (WindAlert) o;
        return Objects.equals(title, windAlert.title) &&
               Objects.equals(content, windAlert.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, content);
    }
}
