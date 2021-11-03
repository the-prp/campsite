package com.pacific.campsite.service;

import com.pacific.campsite.error.ReservationException;
import com.pacific.campsite.model.CalendarAvailability;
import com.pacific.campsite.model.Reservation;
import com.pacific.campsite.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class ReservationService {

    Logger logger = Logger.getLogger(ReservationService.class.getName());

    CalendarAvailability calendarAvailability;

    @Autowired
    ReservationRepository repo;

    @PostConstruct
    public void init() {
        calendarAvailability = new CalendarAvailability();
    }

    public Set<Integer> getAvailability() {
        Set<Integer> availableDays = new LinkedHashSet<>();

        for (Map.Entry<Integer, Boolean> day : calendarAvailability.getAvailableDays().entrySet()){
            if (day.getValue()){
                availableDays.add(day.getKey());
            }
        }
        return availableDays;
    }

    public long reserve(Reservation reservation) throws ReservationException {

        verifyReservation(reservation);

        // check the calendar availability
        if (!calendarAvailability.isAvailable(reservation)){
            throw new ReservationException("Booking could not be completed. " +
                    "One or more of the days requested are already booked.");
        }

        // save reservation
        repo.save(reservation);

        return reservation.getId();
    }

    public void cancel(Long reservationId) throws ReservationException {

        logger.info(String.format("Cancelling reservation with ID=%s", reservationId));

        try {
            Reservation reservation = repo.getById(reservationId);
            repo.deleteById(reservationId);
            // update the calendar model
            calendarAvailability.cancel(reservation);
        } catch (Exception e) {
            throw new ReservationException("Could not cancel reservation", e);
        }
    }

    public void modify(Reservation reservation) throws ReservationException {

        logger.info(String.format("Modifying reservation with ID=%s", reservation.getId()));

        try {
            Reservation entity = repo.getById(reservation.getId());
            reservation.setGuest(entity.getGuest());
            reservation.setEmail(entity.getEmail());

            // update the calendar model
            boolean modified = calendarAvailability.modify(entity, reservation);

            if (modified) {
                entity.setCheckIn(reservation.getCheckIn());
                entity.setCheckOut(reservation.getCheckOut());

                repo.save(entity);
            }
        } catch (Exception e) {
            throw new ReservationException("Reservation could not be modified.");
        }
    }

    private void verifyReservation(Reservation reservation) throws ReservationException {

        logger.info(String.format("Verifying reservation: %s", reservation.toString()));

        // check that the fields are populated
        if (reservation == null ||
        reservation.getGuest() == null ||
        reservation.getEmail() == null ||
        reservation.getCheckIn() == null ||
        reservation.getCheckOut() == null){
            throw new ReservationException(("One or more reservation fields are empty"));
        }

        Date now = new Date(System.currentTimeMillis());
        Date checkIn = reservation.getCheckIn();
        Date checkOut = reservation.getCheckOut();

        // checkout out must be at a later date
        if (checkIn.getTime() > checkOut.getTime()) {
            throw new ReservationException("Check in date must be before check out date.");
        }

        // block bookings less than 1 day in advance and more than a month
        long difference = Math.abs(now.getTime() - checkOut.getTime());
        long advance = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);

        if (advance < 1 || advance > 31) {
            throw new ReservationException("Bookings must be made one day in advance or up to one month.");
        }

        // check if the range of the check in and out is less than 3 days
        difference = Math.abs(checkOut.getTime() - checkIn.getTime());
        long reservationLength = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);

        // booking is longer than 3 days
        if (reservationLength > 3) {
            throw new ReservationException("Booking may only be within 1 to 3 days.");
        }
    }

    public Set<Reservation> getUserReservations(String guest) {
        return repo.getUserReservations(guest);
    }

}
