package com.johnscode.booking;

import com.johnscode.car.Brand;
import com.johnscode.car.Car;
import com.johnscode.car.CarService;
import com.johnscode.user.AppUser;
import com.johnscode.exception.BookingAlreadyCancelledException;
import com.johnscode.exception.CarAlreadyBookedException;
import com.johnscode.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        when(carBookingRepository.existsOverlappingActiveBooking(carId, startDate, endDate)).thenReturn(false);
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
        assertNotNull(result.bookedAt());
        verify(carBookingRepository).save(any(CarBooking.class));
    }

    @Test
    void itShouldThrowWhenCarIsAlreadyBooked() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(5);
        CarBookingRequest request = new CarBookingRequest(userId, carId, startDate, endDate);

        when(userService.getUserById(userId)).thenReturn(james);
        when(carService.getCarById(carId)).thenReturn(tesla);
        when(carBookingRepository.existsOverlappingActiveBooking(carId, startDate, endDate)).thenReturn(true);

        CarAlreadyBookedException exception = assertThrows(
                CarAlreadyBookedException.class,
                () -> underTest.bookCar(request)
        );

        assertEquals("This car is already booked", exception.getMessage());
        verify(carBookingRepository, never()).save(any(CarBooking.class));
    }

    @Test
    void itShouldListOnlyActiveBookings() {
        when(carBookingRepository.findByStatus(BookingStatus.ACTIVE)).thenReturn(List.of());

        underTest.getAllBookings();

        verify(carBookingRepository).findByStatus(BookingStatus.ACTIVE);
        verify(carBookingRepository, never()).findAll();
    }

    @Test
    void itShouldGetAllBookings() {
        CarBooking booking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                new BigDecimal("100.00"), BookingStatus.ACTIVE, LocalDateTime.now()
        );
        booking.setId(UUID.randomUUID());
        when(carBookingRepository.findByStatus(BookingStatus.ACTIVE)).thenReturn(List.of(booking));

        List<CarBookingResponse> result = underTest.getAllBookings();

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).id());
        verify(carBookingRepository).findByStatus(BookingStatus.ACTIVE);
    }

    @Test
    void itShouldGetBookingsByUserId() {
        CarBooking booking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                new BigDecimal("100.00"), BookingStatus.ACTIVE, LocalDateTime.now()
        );
        booking.setId(UUID.randomUUID());

        when(userService.getUserById(userId)).thenReturn(james);
        when(carBookingRepository.findByUser_IdAndStatus(userId, BookingStatus.ACTIVE))
                .thenReturn(List.of(booking));

        List<CarBookingResponse> result = underTest.getBookingsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).id());
        assertEquals("James", result.get(0).userName());
        verify(userService).getUserById(userId);
        verify(carBookingRepository).findByUser_IdAndStatus(userId, BookingStatus.ACTIVE);
    }

    @Test
    void itShouldDeleteBooking() {
        UUID bookingId = UUID.randomUUID();
        CarBooking booking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                new BigDecimal("100.00"), BookingStatus.ACTIVE, LocalDateTime.now()
        );
        booking.setId(bookingId);
        when(carBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        underTest.deleteBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(carBookingRepository).save(booking);
    }

    @Test
    void itShouldThrowWhenDeletingAlreadyCancelledBooking() {
        UUID bookingId = UUID.randomUUID();
        CarBooking booking = new CarBooking(
                james, tesla,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                new BigDecimal("100.00"), BookingStatus.CANCELLED, LocalDateTime.now()
        );
        booking.setId(bookingId);
        when(carBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingAlreadyCancelledException exception = assertThrows(
                BookingAlreadyCancelledException.class,
                () -> underTest.deleteBooking(bookingId)
        );
        assertEquals("This booking is already cancelled", exception.getMessage());

        verify(carBookingRepository, never()).save(any(CarBooking.class));
    }
}
