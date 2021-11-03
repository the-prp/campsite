package com.pacific.campsite.controller;

import com.pacific.campsite.error.ReservationException;
import com.pacific.campsite.model.Reservation;
import com.pacific.campsite.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReservationController {

    @Autowired
    ReservationService service;

    @GetMapping("/reservation")
    public String reservationForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        return "reservation";
    }

    @PostMapping("/reservation")
    public String createReservation(@ModelAttribute Reservation reservation, Model model) {
        model.addAttribute("reservation", reservation);
        try {
            service.reserve(reservation);
        } catch (ReservationException e){
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "reservation";
    }

    @PostMapping("/reservation/modify/{id}")
    public String modifyReservation(@ModelAttribute Reservation reservation, Model model) {
        try {
            model.addAttribute("reservation", reservation);
            service.modify(reservation);
        } catch (ReservationException e){
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "confirmation";
    }

    @PostMapping("/reservation/cancel/{id}")
    public String cancelReservation(@PathVariable String id, Model model) {
        try {
            service.cancel(Long.valueOf(id));
        } catch (ReservationException e){
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "confirmation";
    }

    @GetMapping("/reservation/{guest}")
    public String getReservations(@PathVariable String guest, @ModelAttribute Reservation reservation, Model model) {
        model.addAttribute("reservations", service.getUserReservations(guest));
        model.addAttribute("reservation", reservation);

        return "myreservations";
    }
}