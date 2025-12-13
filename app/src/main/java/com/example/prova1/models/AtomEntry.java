package com.example.prova1.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "entry", strict = false)
public class AtomEntry {

    @Element(name = "title")
    private String title;

    @Element(name = "summary", required = false) // <-- Reso il campo opzionale
    private String summary;

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }
}
