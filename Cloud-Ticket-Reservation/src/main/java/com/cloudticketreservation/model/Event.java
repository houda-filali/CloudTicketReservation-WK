package com.cloudticketreservation.model;

public class Event {
    private String id;
    private int availableSeats;


    public Event(String id, int availableSeats) {
        this.id = id;
        this.availableSeats = availableSeats;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getAvailableSeats() {
        return availableSeats;
    }
    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

}
