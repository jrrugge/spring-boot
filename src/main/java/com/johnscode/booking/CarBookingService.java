package com.johnscode.booking;
import com.johnscode.car.Car;
import com.johnscode.car.CarService;
import com.johnscode.user.AppUser;
import com.johnscode.exception.BadRequestException;
import com.johnscode.exception.BookingAlreadyCancelledException;
import com.johnscode.exception.BookingNotFoundException;
import com.johnscode.exception.CarAlreadyBookedException;
import com.johnscode.user.UserService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class CarBookingService {
    private final CarBookingRepository carBookingRepository;
    private final CarService carService;
    private final UserService userService;

    public CarBookingService(
            CarBookingRepository carBookingRepository,
            CarService carService,
            UserService userService
    ) {
        this.carBookingRepository = carBookingRepository;
        this.carService = carService;
        this.userService = userService;
    }

    public CarBookingResponse bookCar(CarBookingRequest request) {
        UUID userId = request.userId();
        UUID carId = request.carId();
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();

        if (userId == null) {
            throw new BadRequestException("User ID cannot be null");
        }
        if (carId == null) {
            throw new BadRequestException("Car ID cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }

        AppUser user = userService.getUserById(userId);
        Car car = carService.getCarById(carId);

        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("The start date cannot be in the past");
        }
        if (!endDate.isAfter(startDate)) {
            throw new BadRequestException("The end date must be after the start date");
        }
        if (carBookingRepository.existsOverlappingActiveBooking(carId, startDate, endDate)) {
            throw new CarAlreadyBookedException("This car is already booked");
        }

        long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal totalPrice = car.getRentalPricePerDay().multiply(BigDecimal.valueOf(numberOfDays));

        // Server timestamp — not sent by the client (same as CLI bookCar)
        CarBooking booking = new CarBooking(
                user, car, startDate, endDate, totalPrice, BookingStatus.ACTIVE, LocalDateTime.now()
        );
        CarBooking saved = carBookingRepository.save(booking);

        return toResponse(saved);
    }

    // Lists show ACTIVE only; CANCELLED rows stay in DB for history
    public List<CarBookingResponse> getAllBookings() {
        return carBookingRepository.findByStatus(BookingStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CarBookingResponse> getBookingsByUserId(UUID userId) {
        userService.getUserById(userId);
        return carBookingRepository.findByUser_IdAndStatus(userId, BookingStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Soft delete (same as CLI Phase 5): row kept for history; GET lists use ACTIVE only
    public void deleteBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new BadRequestException("Booking ID cannot be null");
        }
        CarBooking booking = carBookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("No booking was found with ID: " + bookingId));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException("This booking is already cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        carBookingRepository.save(booking);
    }
    // Entity → API (includes bookedAt from the row we saved in bookCar)
    private CarBookingResponse toResponse(CarBooking booking) {
        return new CarBookingResponse(
                booking.getId(),
                booking.getUser().getName(),
                booking.getCar().getRegNumber(),
                booking.getCar().getBrand(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getPrice(),
                booking.getStatus(),
                booking.getBookedAt()
        );
    }
}
