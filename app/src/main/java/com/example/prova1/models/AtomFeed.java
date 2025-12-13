package com.example.prova1.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import java.util.List;

@Root(name = "feed", strict = false)
public class AtomFeed {

    @ElementList(inline = true, name = "entry")
    private List<AtomEntry> entries;

    public List<AtomEntry> getEntries() {
        return entries;
    }
}
