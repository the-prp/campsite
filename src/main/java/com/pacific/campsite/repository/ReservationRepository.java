package com.pacific.campsite.repository;

import com.pacific.campsite.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(value = "SELECT * FROM RESERVATION WHERE RESERVATION.GUEST = ?1", nativeQuery = true)
    Set<Reservation> getUserReservations(String guest);
}
