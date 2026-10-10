package com.johnscode.booking;
import com.johnscode.car.Car;
import com.johnscode.car.CarService;
import com.johnscode.user.AppUser;
import com.johnscode.user.UserService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
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
            UserService userService)
    {
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
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (carId == null) {
            throw new IllegalArgumentException("Car ID cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        AppUser user = userService.getUserById(userId);
        Car car = carService.getCarById(carId);

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The start date cannot be in the past");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("The end date must be after the start date");
        }
        if (isCarBooked(carId, startDate, endDate)) {
            throw new IllegalStateException("The car is already booked and is not available");
        }

        long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        BigDecimal totalPrice = car.getRentalPricePerDay().multiply(BigDecimal.valueOf(numberOfDays));

        CarBooking booking = new CarBooking(user, car, startDate, endDate, totalPrice, BookingStatus.ACTIVE);
        CarBooking saved = carBookingRepository.save(booking);

        return toResponse(saved);
    }

    private boolean isCarBooked(UUID carId, LocalDate startDate, LocalDate endDate) {
        return carBookingRepository.findAll().stream()
                .anyMatch(booking ->
                        booking.getCar().getId().equals(carId)
                                && booking.getStatus() == BookingStatus.ACTIVE
                                && booking.getStartDate().isBefore(endDate)
                                && startDate.isBefore(booking.getEndDate())
                );
    }

    public List<CarBookingResponse> getAllBookings() {
        return carBookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
//List bookings by user id
    public List<CarBookingResponse> getBookingsByUserId(UUID userId) {
        userService.getUserById(userId);
        return carBookingRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        CarBooking booking = carBookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("No booking was found with ID: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        carBookingRepository.save(booking);
    }
//Map Entity/DB to DTO
    private CarBookingResponse toResponse(CarBooking booking) {
        return new CarBookingResponse(
                booking.getId(),
                booking.getUser().getName(),
                booking.getCar().getRegNumber(),
                booking.getCar().getBrand(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getPrice(),
                booking.getStatus()
        );
    }
}
