package com.johnscode.car;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/available")
    public List<CarResponse> getAvailableCars() {
        return carService.getAvailableCars();
    }

    @GetMapping("/available/electric")
    public List<CarResponse> getAvailableElectricCars() {
        return carService.getAvailableElectricCars();
    }
}
