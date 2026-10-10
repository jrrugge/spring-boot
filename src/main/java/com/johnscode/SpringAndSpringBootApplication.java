package com.johnscode;

import com.johnscode.car.Brand;
import com.johnscode.car.Car;
import com.johnscode.car.CarRepository;
import com.johnscode.user.AppUser;
import com.johnscode.user.AppUserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class SpringAndSpringBootApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(
                SpringAndSpringBootApplication.class,
                args
        );

    }

    @Bean
    CommandLineRunner seedData(AppUserRepository appUserRepository,
                               CarRepository carRepository) {
        return args -> {
            if (appUserRepository.count() == 0) {
                AppUser james = new AppUser("James");
                AppUser jamila = new AppUser("Jamila");
                AppUser john = new AppUser("John");
                AppUser jane = new AppUser("Jane");
                appUserRepository.saveAll(List.of(james, jamila, john, jane));
            }

            if (carRepository.count() == 0) {
                Car tesla = new Car("TE-001", new BigDecimal("29.99"), Brand.TESLA, true);
                Car audi = new Car("AU-002", new BigDecimal("24.99"), Brand.AUDI, false);
                Car mercedes = new Car("ME-003", new BigDecimal("34.99"), Brand.MERCEDES, false);
                Car toyota = new Car("TO-004", new BigDecimal("19.99"), Brand.TOYOTA, false);
                Car teslaX = new Car("TE-005", new BigDecimal("39.99"), Brand.TESLA, true);
                Car Lamborghini = new Car("LA-006", new BigDecimal("100.99"), Brand.AUDI, false);
                carRepository.saveAll(List.of(tesla, audi, mercedes, toyota, teslaX, Lamborghini));

            }
        };
    }

}
