
package com.flightbooking.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flightbooking.auth.dto.ChangePasswordRequest;
import com.flightbooking.auth.dto.UpdateProfileRequest;
import com.flightbooking.auth.dto.UserProfileResponse;
import com.flightbooking.auth.entity.User;
import com.flightbooking.auth.exception.ConflictException;
import com.flightbooking.auth.exception.UnauthorizedException;
import com.flightbooking.auth.repository.UserRepository;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String authenticatedEmail) {
        User user = findUser(authenticatedEmail);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(
            String authenticatedEmail,
            UpdateProfileRequest request) {

        User user = findUser(authenticatedEmail);

        String newEmail = request.email().trim().toLowerCase();

        if (!user.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("Email is already registered");
        }

        user.setName(request.name().trim());
        user.setEmail(newEmail);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    @Transactional
    public void changePassword(
            String authenticatedEmail,
            ChangePasswordRequest request) {

        User user = findUser(authenticatedEmail);

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException("Authenticated user not found"));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
