package com.example.cloudticketreservationwk.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryStore {

    public static class EventItem {
        public String id;
        public String title;
        public String date;
        public String location;
        public String category;
        public String description;
        public int capacity;
        public String status; // "Active" or "Canceled"

        public EventItem(String title, String date, String location, String category, String description) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.date = date;
            this.location = location;
            this.category = category;
            this.description = description;
            this.capacity = 0;
            this.status = "Active";
        }

        public EventItem(String title, String date, String location, String category, String description, int capacity) {
            this(title, date, location, category, description);
            this.capacity = capacity;
        }

        public void updateFrom(EventItem other) {
            this.title = other.title;
            this.date = other.date;
            this.location = other.location;
            this.category = other.category;
            this.description = other.description;
            this.capacity = other.capacity;
        }
    }

    public static class ReservationItem {
        public String id;
        public EventItem event;
        public int tickets;
        public String status;

        public ReservationItem(EventItem event, int tickets) {
            this.id = UUID.randomUUID().toString();
            this.event = event;
            this.tickets = tickets;
            this.status = "Active";
        }
    }

    public static final List<EventItem> EVENTS = new ArrayList<>();
    public static final List<ReservationItem> MY_RESERVATIONS = new ArrayList<>();

    static {
        EVENTS.add(new EventItem("Comedy Night", "2026-03-10", "Downtown", "Comedy", "A fun comedy show.", 100));
        EVENTS.add(new EventItem("Tech Meetup", "2026-03-12", "Campus Hall", "Tech", "Talks + networking.", 50));
        EVENTS.add(new EventItem("Live Concert", "2026-03-20", "Main Arena", "Music", "Live performance night.", 500));
        EVENTS.add(new EventItem("Food Festival", "2026-03-25", "Old Port", "Food", "Local food + vendors.", 200));
    }

    public static EventItem findEventById(String id) {
        if (id == null) return null;
        for (EventItem event : EVENTS) {
            if (event.id.equals(id)) return event;
        }
        return null;
    }
}