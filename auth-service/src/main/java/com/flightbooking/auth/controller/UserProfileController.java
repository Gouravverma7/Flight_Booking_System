
package com.flightbooking.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.flightbooking.auth.dto.ChangePasswordRequest;
import com.flightbooking.auth.dto.UpdateProfileRequest;
import com.flightbooking.auth.dto.UserProfileResponse;
import com.flightbooking.auth.service.UserProfileService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {
    private final UserProfileService userProfileService;
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getProfile(getEmail(authentication))
        );
    }

    @PatchMapping
    public ResponseEntity<UserProfileResponse> updateProfile(Authentication authentication,
            											@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(getEmail(authentication), request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
            											@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(getEmail(authentication), request);
        return ResponseEntity.noContent().build();
    }

    private String getEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }

        Object details = authentication.getDetails();
        if (details instanceof String email && email.contains("@")) {
            return email;
        }

        String principal = authentication.getName();
        if (principal != null && principal.contains("@")) {
            return principal;
        }

        throw new IllegalStateException("Could not resolve authenticated user's email");
    }
}
