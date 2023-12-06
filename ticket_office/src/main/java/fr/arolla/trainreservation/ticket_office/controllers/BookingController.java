package fr.arolla.trainreservation.ticket_office.controllers;

import fr.arolla.trainreservation.ticket_office.models.BookingRequest;
import fr.arolla.trainreservation.ticket_office.models.BookingResponse;
import fr.arolla.trainreservation.ticket_office.services.BookingService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BookingController {

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @RequestMapping("/reserve")
  BookingResponse reserve(@RequestBody BookingRequest bookingRequest) {
    return bookingService.reserveSeats(bookingRequest.train_id(), bookingRequest.count());
  }
}
