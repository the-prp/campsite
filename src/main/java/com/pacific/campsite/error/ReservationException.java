package com.pacific.campsite.error;

public class ReservationException extends Exception {

    public ReservationException(String msg, ReservationException e){
        super(msg, e);
    }

    public ReservationException(String msg){
        super(msg);
    }

    public ReservationException(String msg, Exception e){
        super(msg, e);
    }
}
