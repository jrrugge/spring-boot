package com.johnscode.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> getAllBookings() {
        List<CarBookingResponse> bookings = carBookingService.getAllBookings();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bookings", bookings);
        if (bookings.isEmpty()) {
            body.put("message", "No bookings exist yet.");
        }
        return ResponseEntity.ok(body);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUserId(@PathVariable UUID userId) {
        List<CarBookingResponse> bookings = carBookingService.getBookingsByUserId(userId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bookings", bookings);
        if (bookings.isEmpty()) {
            body.put("message", "No bookings exist for this user yet.");
        }
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Map<String, String>> deleteBooking(@PathVariable UUID bookingId) {
        carBookingService.deleteBooking(bookingId);
        return ResponseEntity.ok(Map.of("message", "Booking deletion confirmed"));
    }
}
