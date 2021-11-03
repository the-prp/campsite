package com.pacific.campsite.controller;

import com.pacific.campsite.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AvailabilityController {

    @Autowired
    ReservationService service;

    @GetMapping("/availability")
    @ResponseBody
    public String getAvailability(){

        StringBuilder sb = new StringBuilder();
        sb.append("Upcoming available days: \n");

        for (Integer i : service.getAvailability()){
            sb.append(i);
            sb.append("\n");
        }

        return sb.toString();
    }
}
