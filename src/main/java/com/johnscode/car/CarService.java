package com.johnscode.car;
import com.johnscode.booking.BookingStatus;
import com.johnscode.booking.CarBooking;
import com.johnscode.booking.CarBookingRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final CarBookingRepository carBookingRepository;
    public CarService(CarRepository carRepository, CarBookingRepository carBookingRepository) {
        this.carRepository = carRepository;
        this.carBookingRepository = carBookingRepository;
    }

    public List<CarResponse> getAllCars() {
        return carRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Car getCarById(UUID carId) {
        if (carId == null) {
            throw new IllegalArgumentException("Car ID cannot be null");
        }
        return carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("No car was found with ID: " + carId));
    }

    public List<CarResponse> getAvailableCars() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return carRepository.findAll()
                .stream()
                .filter(car -> !isCarBooked(car.getId(), today, tomorrow))
                .map(this::toResponse)
                .toList();
    }
    public List<CarResponse> getAvailableElectricCars() {
        return getAvailableCars()
                .stream()
                .filter(CarResponse::isElectric)
                .toList();
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
    private CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getRegNumber(),
                car.getRentalPricePerDay(),
                car.getBrand(),
                car.isElectric()
        );
    }
}