package com.johnscode.user;

import com.johnscode.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private UserService underTest;

    private UUID userId;
    private AppUser james;

    @BeforeEach
    void setUp() {
        underTest = new UserService(appUserRepository);
        userId = UUID.fromString("8ca51d2b-aaaf-4bf2-834a-e02964e10fc3");
        james = new AppUser(userId, "James");
    }

    @Test
    void itShouldGetAllUsers() {
        when(appUserRepository.findAll()).thenReturn(List.of(james));

        List<UserResponse> result = underTest.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("James", result.get(0).name());
        verify(appUserRepository).findAll();
    }

    @Test
    void itShouldGetUserById() {
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(james));

        AppUser result = underTest.getUserById(userId);

        assertEquals("James", result.getName());
        verify(appUserRepository).findById(userId);
    }

    @Test
    void itShouldThrowWhenUserIdIsNull() {
        assertThrows(BadRequestException.class, () -> underTest.getUserById(null));
    }
}
