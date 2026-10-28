package com.johnscode.car;

import com.johnscode.booking.CarBookingRepository;
import com.johnscode.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarBookingRepository carBookingRepository;

    private CarService underTest;

    private UUID carId;
    private Car tesla;

    @BeforeEach
    void setUp() {
        underTest = new CarService(carRepository, carBookingRepository);
        carId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        tesla = new Car(carId, "TE-001", new BigDecimal("29.99"), Brand.TESLA, true);
    }

    @Test
    void itShouldGetAllCars() {
        when(carRepository.findAll()).thenReturn(List.of(tesla));

        List<CarResponse> result = underTest.getAllCars();

        assertEquals(1, result.size());
        assertEquals("TE-001", result.get(0).regNumber());
        verify(carRepository).findAll();
    }

    @Test
    void itShouldGetCarById() {
        when(carRepository.findById(carId)).thenReturn(Optional.of(tesla));

        Car result = underTest.getCarById(carId);

        assertEquals("TE-001", result.getRegNumber());
        verify(carRepository).findById(carId);
    }

    @Test
    void itShouldThrowWhenCarIdIsNull() {
        assertThrows(BadRequestException.class, () -> underTest.getCarById(null));
    }

    @Test
    void itShouldGetAvailableCars() {
        Car audi = new Car(UUID.randomUUID(), "AU-002", new BigDecimal("24.99"), Brand.AUDI, false);

        when(carRepository.findAll()).thenReturn(List.of(tesla, audi));
        when(carBookingRepository.existsOverlappingActiveBooking(eq(carId), any(), any())).thenReturn(true);
        when(carBookingRepository.existsOverlappingActiveBooking(eq(audi.getId()), any(), any())).thenReturn(false);

        List<CarResponse> result = underTest.getAvailableCars();

        assertEquals(1, result.size());
        assertEquals("AU-002", result.get(0).regNumber());
    }

    @Test
    void itShouldGetAvailableElectricCars() {
        Car audi = new Car(UUID.randomUUID(), "AU-002", new BigDecimal("24.99"), Brand.AUDI, false);

        when(carRepository.findAll()).thenReturn(List.of(tesla, audi));
        when(carBookingRepository.existsOverlappingActiveBooking(eq(carId), any(), any())).thenReturn(true);
        when(carBookingRepository.existsOverlappingActiveBooking(eq(audi.getId()), any(), any())).thenReturn(false);

        List<CarResponse> result = underTest.getAvailableElectricCars();

        assertEquals(0, result.size());
    }
}
