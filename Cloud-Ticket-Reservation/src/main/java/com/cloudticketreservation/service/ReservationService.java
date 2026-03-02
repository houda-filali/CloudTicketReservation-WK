package com.cloudticketreservation.service;

import com.cloudticketreservation.model.User;
import com.cloudticketreservation.model.Event;

public class ReservationService {
    public boolean createReservation(User user, Event event) {

        if (event.getAvailableSeats() > 0) {
            event.setAvailableSeats(event.getAvailableSeats() - 1);
            return true;
        }

        return false;
    }
}
