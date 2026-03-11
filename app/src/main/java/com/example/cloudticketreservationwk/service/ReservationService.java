package com.example.cloudticketreservationwk.service;

import com.example.cloudticketreservationwk.model.User;
import com.example.cloudticketreservationwk.model.Event;

public class ReservationService {
    public boolean createReservation(User user, Event event) {

        if (event.getAvailableSeats() > 0) {
            event.setAvailableSeats(event.getAvailableSeats() - 1);
            return true;
        }

        return false;
    }
}
