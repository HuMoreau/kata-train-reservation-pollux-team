package fr.arolla.trainreservation.ticket_office.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.trainreservation.ticket_office.models.Seat;
import fr.arolla.trainreservation.ticket_office.models.BookingResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final RestTemplate restTemplate;

    BookingService() {
        restTemplate = new RestTemplate();
    }

    public BookingResponse reserveSeats(String trainId, int seatCount){

        // Step 1: Get a booking reference
        var bookingReference = getBookingReference();

        // Step 2: Retrieve train data for the given train ID
        var seats = getTrainSeats(trainId);

        // Step 3: find available seats (hard-code coach 'A' for now)
        var availableSeats = seats.stream().filter(seat -> seat.bookingReference() == null);

        if(isBookingSeatNumberTooMuch(seatCount, availableSeats.toList().size(), getMaximumBookingCapacity(seats))){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too much seats requested");
        }

        var freeSeatsPerCoach = getAvailableSeatsNumberByCoach(seats);

        // Step 4: call the '/reserve' end point
        var toReserve = availableSeats.limit(seatCount);
        var ids = toReserve.map(seat -> seat.number() + seat.coach()).toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("train_id", trainId);
        payload.put("seats", ids);
        payload.put("booking_reference", bookingReference);
        restTemplate.postForObject("http://127.0.0.1:8081/reserve", payload, String.class);

        // Step 5: return reference and booked seats
        return new BookingResponse(trainId, bookingReference, ids);
    }

    private String getBookingReference(){
        return restTemplate.getForObject("http://127.0.0.1:8082/booking_reference", String.class);
    }

    private ArrayList<Seat> getTrainSeats(String trainId){
        var json = restTemplate.getForObject("http://127.0.0.1:8081/data_for_train/" + trainId, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Seat> seats = new ArrayList<>();
        try {
            var tree = objectMapper.readTree(json);
            var seatsNode = tree.get("seats");
            for (JsonNode node : seatsNode) {
                String coach = node.get("coach").asText();
                String seatNumber = node.get("seat_number").asText();
                var jsonBookingReference = node.get("booking_reference").asText();
                if (jsonBookingReference.isEmpty()) {
                    var seat = new Seat(seatNumber, coach, null);
                    seats.add(seat);
                } else {
                    var seat = new Seat(seatNumber, coach, jsonBookingReference);
                    seats.add(seat);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return seats;
    }

    protected int getMaximumBookingCapacity(ArrayList<Seat> seats){
        return (int) (seats.size()*0.7);
    }

    protected boolean isBookingSeatNumberTooMuch(int pendingBookingSeatCount, int freeSeatsCount, int maximumBookingCapacity){
        return (maximumBookingCapacity - freeSeatsCount) + pendingBookingSeatCount > maximumBookingCapacity;
    }

    private Map<String,Long> getAvailableSeatsNumberByCoach(ArrayList<Seat> availableSeats){
        return availableSeats.stream()
                .collect(Collectors.groupingBy(Seat::coach, Collectors.mapping(Seat::number, Collectors.counting())));
    }

    private Map<String,Long> getMaximumSeatsNumberByCoach(ArrayList<Seat> seats){
        return seats.stream()
                .collect(Collectors.groupingBy(Seat::coach, Collectors.mapping(Seat::number, Collectors.counting())));
    }

    private String getBestCoachToBookInto(Map<String, Long> freeSeatsPerCoach){
        return "";
        //getMaximumBookingCapacity()
    }
}
