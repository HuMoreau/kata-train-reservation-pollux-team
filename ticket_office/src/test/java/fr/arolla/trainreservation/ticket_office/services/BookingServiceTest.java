package fr.arolla.trainreservation.ticket_office.services;

import fr.arolla.trainreservation.ticket_office.models.Seat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BookingServiceTest {

    @Autowired
    BookingService bookingService;

    @Test
    void should_return_true_when_booking_seat_number_is_bigger_than_available_bookable_seats() {
        // Given
        int pendingBookingSeatCount = 5;
        int freeSeatsCount = 3;
        int maximumBookingCapacity = 4; // 70% of train capacity

        // When
        boolean result = bookingService.isBookingSeatNumberTooMuch(pendingBookingSeatCount, freeSeatsCount, maximumBookingCapacity);

        // Then
        assertTrue(result);
    }

    @Test
    void should_return_false_when_booking_seat_number_is_lower_than_available_bookable_seats() {
        // Given
        int pendingBookingSeatCount = 1;
        int freeSeatsCount = 3;
        int maximumBookingCapacity = 4; // 70% of train capacity

        // When
        boolean result = bookingService.isBookingSeatNumberTooMuch(pendingBookingSeatCount, freeSeatsCount, maximumBookingCapacity);

        // Then
        assertFalse(result);
    }


}