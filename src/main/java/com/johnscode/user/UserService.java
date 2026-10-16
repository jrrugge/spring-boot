package com.johnscode.user;

import com.johnscode.exception.BadRequestException;
import com.johnscode.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

    }

    public AppUser getUserById(UUID userId) {
        if (userId == null) {
            throw new BadRequestException("User ID cannot be null");
        }
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No user was found with ID: " + userId));
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getName());
    }
}

