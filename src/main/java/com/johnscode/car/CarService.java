package com.johnscode.car;

import com.johnscode.booking.CarBookingRepository;
import com.johnscode.exception.BadRequestException;
import com.johnscode.exception.CarNotFoundException;
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
            throw new BadRequestException("Car ID cannot be null");
        }
        return carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("No car was found with ID: " + carId));
    }

    public List<CarResponse> getAvailableCars() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        return carRepository.findAll()
                .stream()
                .filter(car -> !carBookingRepository.existsOverlappingActiveBooking(
                        car.getId(), today, tomorrow))
                .map(this::toResponse)
                .toList();
    }

    public List<CarResponse> getAvailableElectricCars() {
        return getAvailableCars()
                .stream()
                .filter(CarResponse::isElectric)
                .toList();
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
