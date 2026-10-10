package com.johnscode.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/bookings")
public class CarBookingController {

    private final CarBookingService carBookingService;

    public CarBookingController(CarBookingService carBookingService) {
        this.carBookingService = carBookingService;
    }

    @PostMapping
    public ResponseEntity<CarBookingResponse> bookCar(@RequestBody CarBookingRequest request) {
        CarBookingResponse response = carBookingService.bookCar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CarBookingResponse> getAllBookings() {
        return carBookingService.getAllBookings();
    }

    @GetMapping("/user/{userId}")
    public List<CarBookingResponse> getBookingsByUserId(@PathVariable UUID userId) {
        return carBookingService.getBookingsByUserId(userId);
    }

    @DeleteMapping("/{bookingId}")
    public void deleteBooking(@PathVariable UUID bookingId) {
        carBookingService.deleteBooking(bookingId);
    }
}
