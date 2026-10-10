package com.johnscode.booking;

import com.johnscode.car.Brand;
import com.johnscode.car.Car;
import com.johnscode.car.CarService;
import com.johnscode.user.AppUser;
import com.johnscode.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarBookingServiceTest {

    @Mock
    private CarBookingRepository carBookingRepository;

    @Mock
    private CarService carService;

    @Mock
    private UserService userService;

    private CarBookingService underTest;

    private UUID userId;
    private UUID carId;
    private AppUser james;
    private Car tesla;

    @BeforeEach
    void setUp() {
        underTest = new CarBookingService(carBookingRepository, carService, userService);
        userId = UUID.fromString("8ca51d2b-aaaf-4bf2-834a-e02964e10fc3");
        carId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        james = new AppUser(userId, "James");
        tesla = new Car(carId, "TE-001", new BigDecimal("50.00"), Brand.TESLA, true);
    }

    @Test
    void itShouldBookCar() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(4);
        CarBookingRequest request = new CarBookingRequest(userId, carId, startDate, endDate);

        when(userService.getUserById(userId)).thenReturn(james);
        when(carService.getCarById(carId)).thenReturn(tesla);
        when(carBookingRepository.findAll()).thenReturn(List.of());
        when(carBookingRepository.save(any(CarBooking.class))).thenAnswer(invocation -> {
            CarBooking booking = invocation.getArgument(0);
            booking.setId(UUID.randomUUID());
            return booking;
        });

        CarBookingResponse result = underTest.bookCar(request);

        assertEquals("James", result.userName());
        assertEquals("TE-001", result.carRegNumber());
        assertEquals(BookingStatus.ACTIVE, result.status());
        assertEquals(new BigDecimal("150.00"), result.price());
        verify(carBookingRepository).save(any(CarBooking.class));
    }

    @Test
    void itShouldThrowWhenCarIsAlreadyBooked() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(5);
        CarBookingRequest request = new CarBookingRequest(userId, carId, startDate, endDate);

        CarBooking existingBooking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(4),
                new BigDecimal("150.00"), BookingStatus.ACTIVE
        );
        existingBooking.setId(UUID.randomUUID());

        when(userService.getUserById(userId)).thenReturn(james);
        when(carService.getCarById(carId)).thenReturn(tesla);
        when(carBookingRepository.findAll()).thenReturn(List.of(existingBooking));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> underTest.bookCar(request)
        );

        assertEquals("The car is already booked and is not available", exception.getMessage());
        verify(carBookingRepository, never()).save(any(CarBooking.class));
    }

    @Test
    void itShouldGetAllBookings() {
        CarBooking booking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                new BigDecimal("100.00"), BookingStatus.ACTIVE
        );
        booking.setId(UUID.randomUUID());
        when(carBookingRepository.findAll()).thenReturn(List.of(booking));

        List<CarBookingResponse> result = underTest.getAllBookings();

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).id());
        verify(carBookingRepository).findAll();
    }

    @Test
    void itShouldDeleteBooking() {
        UUID bookingId = UUID.randomUUID();
        CarBooking booking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                new BigDecimal("100.00"), BookingStatus.ACTIVE
        );
        booking.setId(bookingId);
        when(carBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        underTest.deleteBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(carBookingRepository).save(booking);
    }
}
