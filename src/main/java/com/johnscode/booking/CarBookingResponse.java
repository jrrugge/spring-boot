package com.johnscode.booking;
import com.johnscode.car.Brand;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CarBookingResponse(
        UUID id,
        String userName,
        String carRegNumber,
        Brand carBrand,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        BookingStatus status
) {}
