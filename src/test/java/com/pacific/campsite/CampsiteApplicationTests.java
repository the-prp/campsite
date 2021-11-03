package com.pacific.campsite;

import com.pacific.campsite.controller.AvailabilityController;
import com.pacific.campsite.controller.ReservationController;
import com.pacific.campsite.error.ReservationException;
import com.pacific.campsite.model.Reservation;
import com.pacific.campsite.repository.ReservationRepository;
import com.pacific.campsite.service.ReservationService;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class CampsiteApplicationTests {

	@Autowired
	ReservationService service;

	@Autowired
	private ReservationController reservationController;

	@Autowired
	private AvailabilityController availabilityController;

	@Autowired
	private ReservationRepository reservationRepo;

	private String guest = "John";

	private String email = "fakeemail@yahoo.ca";

	private Reservation validReservation;

	private Reservation invalidReservation;

	@BeforeEach
	void initialize(){
		// set up the reservations
		validReservation = new Reservation();
		validReservation.setEmail(email);
		validReservation.setGuest(guest);

		Date checkIn = new Date();
		checkIn.setTime(System.currentTimeMillis());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(checkIn);

		// set check in for two days from now
		calendar.add(Calendar.DAY_OF_MONTH, 2);
		validReservation.setCheckIn(checkIn);

		// set check out for 2 days after that (valid case)
		calendar.add(Calendar.DAY_OF_MONTH, 2);
		validReservation.setCheckOut(calendar.getTime());

		// set checkout for next year
		calendar.add(Calendar.YEAR, 1);

		// create an invalid reservation
		invalidReservation = new Reservation();
		invalidReservation.setEmail(email);
		invalidReservation.setGuest(guest);
		invalidReservation.setCheckIn(checkIn);
		invalidReservation.setCheckOut(calendar.getTime());
	}

	@Test
	void contextLoads() {
		assertThat(reservationController).isNotNull();
		assertThat(availabilityController).isNotNull();
	}

	@Test
	void create_valid_reservation() {
		try {
			service.reserve(validReservation);
		} catch (Exception e){
			Fail.fail(e.getMessage());
		}
	}

	@Test
	void create_invalid_reservation(){
		try {
			service.reserve(invalidReservation);
		} catch (Exception e){
			System.out.println("Invalid Reservation was successfully rejected.");
		}
	}

	@Test
	void multiple_request_test() {
		ExecutorService executor = Executors.newFixedThreadPool(20);

		try {
			for (int i = 0; i < 20; i++) {
				Thread t = new Thread(() -> {
					try {
						Long id = service.reserve(validReservation);
						System.out.println(String.format("The ID for the reservation is %d", id));
						System.out.println(service.getAvailability());
					} catch (ReservationException e) {
						e.printStackTrace();
					}
				});
				Runnable worker = t;

				executor.execute(worker);
			}
		} catch (Exception e){
			Fail.fail("Concurrent request test failed.");
		}

	}




}
