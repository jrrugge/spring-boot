package com.johnscode.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface CarBookingRepository extends JpaRepository<CarBooking, UUID> {

    List<CarBooking> findByUser_Id(UUID userId);

}

