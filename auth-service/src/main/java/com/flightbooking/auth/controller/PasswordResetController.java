
package com.flightbooking.auth.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.flightbooking.auth.dto.ForgotPasswordRequest;
import com.flightbooking.auth.dto.ResetPasswordRequest;
import com.flightbooking.auth.service.PasswordResetService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(Map.of("message",
                "If an account exists for this email, a reset link will be sent."
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
    }
}
