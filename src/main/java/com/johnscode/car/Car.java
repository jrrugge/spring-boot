package com.johnscode.car;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String regNumber;

    @Column(nullable = false)
    private BigDecimal rentalPricePerDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Brand brand;

    @Column(nullable = false)
    private boolean electric;

    protected Car() {
    }

    public Car(String regNumber, BigDecimal rentalPricePerDay, Brand brand, boolean electric) {
        this.regNumber = regNumber;
        this.rentalPricePerDay = rentalPricePerDay;
        this.brand = brand;
        this.electric = electric;
    }

    public Car(UUID id, String regNumber, BigDecimal rentalPricePerDay, Brand brand, boolean electric) {
        this.id = id;
        this.regNumber = regNumber;
        this.rentalPricePerDay = rentalPricePerDay;
        this.brand = brand;
        this.electric = electric;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public BigDecimal getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public void setRentalPricePerDay(BigDecimal rentalPricePerDay) {
        this.rentalPricePerDay = rentalPricePerDay;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public boolean isElectric() {
        return electric;
    }

    public void setElectric(boolean electric) {
        this.electric = electric;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Car car = (Car) o;
        return electric == car.electric
                && Objects.equals(id, car.id)
                && Objects.equals(regNumber, car.regNumber)
                && Objects.equals(rentalPricePerDay, car.rentalPricePerDay)
                && brand == car.brand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, regNumber, rentalPricePerDay, brand, electric);
    }
}
