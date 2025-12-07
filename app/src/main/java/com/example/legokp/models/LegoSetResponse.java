package com.example.legokp.models;

import java.util.List;

public class LegoSetResponse {
    private int count;
    private String next;
    private String previous;
    private List<LegoSet> results;

    // Getters and Setters
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public List<LegoSet> getResults() { return results; }
    public void setResults(List<LegoSet> results) { this.results = results; }
}

