package com.example.cloudticketreservationwk.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryStore {

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_CANCELED = "Canceled";

    public static class EventItem {
        public String id;
        public String title;
        public String date;
        public String location;
        public String category;
        public String description;
        public int capacity;
        public String status;

        public EventItem(String title, String date, String location, String category, String description) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.date = date;
            this.location = location;
            this.category = category;
            this.description = description;
            this.capacity = 0;
            this.status = STATUS_ACTIVE;
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

        public void setCanceled() {
            this.status = STATUS_CANCELED;
        }

        public void setActive() {
            this.status = STATUS_ACTIVE;
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
            this.status = STATUS_ACTIVE;
        }

        public void setCanceled() {
            this.status = STATUS_CANCELED;
        }
    }

    public static final List<EventItem> EVENTS = new ArrayList<>();
    public static final List<ReservationItem> MY_RESERVATIONS = new ArrayList<>();

    static {
        EVENTS.add(new EventItem("Comedy Night", "2026-03-10", "Downtown", "Comedy", "A fun comedy show.", 100));
        EVENTS.add(new EventItem("Tech Meetup", "2026-03-12", "Campus Hall", "Tech", "Talks + networking.", 50));
        EVENTS.add(new EventItem("Live Concert", "2026-03-20", "Main Arena", "Music", "Live performance night.", 500));
        EVENTS.add(new EventItem("Food Festival", "2026-03-25", "Old Port", "Food", "Local food + vendors.", 200));

        EVENTS.get(2).setCanceled();

        MY_RESERVATIONS.add(new ReservationItem(EVENTS.get(0), 2));
        MY_RESERVATIONS.add(new ReservationItem(EVENTS.get(1), 1));
    }

    public static EventItem findEventById(String id) {
        if (id == null) return null;

        for (EventItem event : EVENTS) {
            if (event.id.equals(id)) return event;
        }
        return null;
    }

    public static void addReservation(String eventId, int tickets) {
        EventItem event = findEventById(eventId);
        if (event == null) return;

        MY_RESERVATIONS.add(new ReservationItem(event, tickets));
    }

    public static ReservationItem findReservationById(String id) {
        if (id == null) return null;

        for (ReservationItem reservation : MY_RESERVATIONS) {
            if (reservation.id.equals(id)) return reservation;
        }
        return null;
    }

    public static void cancelReservation(String reservationId) {
        ReservationItem reservation = findReservationById(reservationId);
        if (reservation != null) {
            reservation.setCanceled();
        }
    }
}