# campsite
A campsite reservation system. This is a Web App that reserves time slots to attend at campsite.

## User Guide

The application should can be run using H2 and IntelliJ or Eclipse. To run, you should have a run configuration set up for a web server. The server will run on port 8080. 

#### Creating a reservation
Navigate to https://localhost:8080/reservation for the reservation UI. Fill out all of the fields to have a valid reservation booked. The reservation must be more than 1 day in advance to your check-in and at most 30 days in advance from your check-in.

#### Checking availability
The availability will be listed in days at the following endpoint: http://localhost/availability. As reservations are created, the days will be removed that are taken.

#### Checking your reservations
To check the reservations you have booked, navigate to http://localhost:8080/reservation/{userId} where userId is the name you reserved under. You should see a list of existing reservations.

#### Modifying your reservation
You can update the dates of your existing reservation by setting the date input on in the list of existing reservations. If the reservation is not valid, there will be no modification.


#### Cancelling your reservation
You can cancel your reservation y clicking the cancel button in your existing reservations.


## Limitations
1. On server restart, the data is cleared. The server does not load in information from a persistent storage. The database used is H2.
2. Users are not authenticated. This is a big security no-no, but for the purposes of this example, it was left out. You can edit anybody's reservation if you know their name.
3. Booking ID is also the database ID. This should not be exposed, but for simplicity's sake, it was exposed. 

