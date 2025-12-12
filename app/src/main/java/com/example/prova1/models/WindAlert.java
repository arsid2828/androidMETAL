package com.example.prova1.models;

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
}
