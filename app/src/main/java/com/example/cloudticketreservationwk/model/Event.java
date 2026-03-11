package com.example.cloudticketreservationwk.model;

public class Event {
    private String id;
    private String name;
    private int availableSeats;
    private int totalSeats;
    private String date;
    private String location;
    private String category;
    private boolean isCancelled;

    // Default constructor for Firestore
    public Event() {}
    public Event(String name, String date, String location, String category, int availableSeats, int totalSeats) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.category = category;
        this.availableSeats = availableSeats;
        this.totalSeats = totalSeats;
        this.isCancelled = false;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDate() {
        return this.date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getLocation() {
        return this.location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getCategory() {
        return this.category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public int getAvailableSeats() {
        return availableSeats;
    }
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }
    public int getTotalSeats() {
        return this.totalSeats;
    }
    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
    public boolean getIsCancelled() {
        return this.isCancelled;
    }
    public void setIsCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    // Helper methods
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }

    public boolean bookSeats(int numberOfSeats) {
        if (availableSeats >= numberOfSeats && !isCancelled) {
            availableSeats -= numberOfSeats;
            return true;
        }
        return false;
    }

}
