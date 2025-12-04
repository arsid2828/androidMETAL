package com.metal.idrogeo.api;

import com.google.gson.annotations.SerializedName;

public class NewsItem {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("publication_date")
    private String publicationDate;

    @SerializedName("link")
    private String link;

    // Costruttore per i dati di test
    public NewsItem(int id, String title, String description, String publicationDate, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publicationDate = publicationDate;
        this.link = link;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPublicationDate() { return publicationDate; }
    public String getLink() { return link; }
}
