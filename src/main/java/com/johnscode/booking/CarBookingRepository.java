package com.johnscode.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CarBookingRepository extends JpaRepository<CarBooking, UUID> {

    List<CarBooking> findByUser_Id(UUID userId);

    List<CarBooking> findByStatus(BookingStatus status);

    List<CarBooking> findByUser_IdAndStatus(UUID userId, BookingStatus status);

    // One overlap rule for bookCar + getAvailableCars (same dates logic as CLI isCarBooked)
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM CarBooking b
            WHERE b.car.id = :carId
              AND b.status = com.johnscode.booking.BookingStatus.ACTIVE
              AND b.startDate < :endDate
              AND :startDate < b.endDate
            """)
    boolean existsOverlappingActiveBooking(
            @Param("carId") UUID carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

