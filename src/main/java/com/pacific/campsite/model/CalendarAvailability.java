package com.pacific.campsite.model;

import com.pacific.campsite.error.ReservationException;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class CalendarAvailability {

    /**
     * Map of calendar dates marked by availability.
     * The boolean value is true if it is available.
     */
    private static Map<Integer, Boolean> availableDays;

    /**
     * 31 days ahead is far enough
     */
    private static int DAYS_IN_MONTH = 31;

    private final Object lock = new Object();

    public CalendarAvailability(){

        availableDays = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();

        for (int i=0; i<DAYS_IN_MONTH; i++) {
            // increment the day
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            availableDays.put(calendar.get(Calendar.DAY_OF_MONTH), true);
        }
    }

    public boolean isAvailable(Reservation reservation) {

        synchronized (lock) {

            Calendar checkIn = Calendar.getInstance();
            checkIn.setTime(reservation.getCheckIn());
            Integer checkInDay = checkIn.get(Calendar.DAY_OF_MONTH);

            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(reservation.getCheckOut());
            Integer checkOutDay = checkOut.get(Calendar.DAY_OF_MONTH);

            List<Integer> days = new LinkedList(availableDays.keySet());

            List<Integer> daysToBook = new LinkedList<>();

            Iterator<Integer> iterator = days.iterator();

            boolean available = false;

            while (iterator.hasNext()) {
                Integer day = iterator.next();

                // start looking if it is available on the check in day
                // or the day after check in (middle day)
                if (day == checkInDay || (day == (checkInDay + 1) && day != checkOutDay)) {
                    available = availableDays.get(day);
                    if (available) {
                        // look at the next day
                        daysToBook.add(day);
                        if (checkInDay == checkOutDay) {
                            // single day edge case
                            availableDays.replace(day, false);
                            return true;
                        }
                        continue;
                    } else {
                        // this day is unavailable. stop looking
                        break;
                    }
                } else if (day == checkOutDay) {
                    available = availableDays.get(day);

                    if (available) {
                        // book the days off in the memory model
                        daysToBook.add(day);
                        for (Integer bookedDay : daysToBook) {
                            // change the flag
                            availableDays.replace(bookedDay, false);
                        }
                    }
                    return available;
                } else {
                    // keep looking
                    continue;
                }
            }

            return available;
        }
    }

    public Map<Integer, Boolean> getAvailableDays() {
        return availableDays;
    }

    public void cancel(Reservation reservation) {

        synchronized (lock) {

            Calendar checkIn = Calendar.getInstance();
            checkIn.setTime(reservation.getCheckIn());
            Integer checkInDay = checkIn.get(Calendar.DAY_OF_MONTH);

            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(reservation.getCheckOut());
            Integer checkOutDay = checkOut.get(Calendar.DAY_OF_MONTH);

            for (int i = checkInDay; i <= checkOutDay; i++) {
                // set the days to available since the reservation was cancelled
                availableDays.replace(i, true);
            }
        }
    }


    public boolean modify(Reservation oldReservation, Reservation newReservation) {
        synchronized (lock) {
            Calendar checkIn = Calendar.getInstance();
            checkIn.setTime(oldReservation.getCheckIn());
            Integer checkInDay = checkIn.get(Calendar.DAY_OF_MONTH);

            Calendar checkOut = Calendar.getInstance();
            checkOut.setTime(oldReservation.getCheckOut());
            Integer checkOutDay = checkOut.get(Calendar.DAY_OF_MONTH);

            for (int i = checkInDay; i <= checkOutDay; i++) {
                // set the days to available since the reservation was cancelled
                availableDays.replace(i, true);
            }

            boolean available = isAvailable(newReservation);

            // revert
            if (!available) {
                for (int i = checkInDay; i <= checkOutDay; i++) {
                    // set the days to available since the reservation was cancelled
                    availableDays.replace(i, false);
                }
            }

            return available;
        }
    }

}
