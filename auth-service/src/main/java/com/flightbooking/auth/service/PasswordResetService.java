
package com.flightbooking.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flightbooking.auth.dto.ForgotPasswordRequest;
import com.flightbooking.auth.dto.ResetPasswordRequest;
import com.flightbooking.auth.entity.PasswordResetToken;
import com.flightbooking.auth.entity.User;
import com.flightbooking.auth.repository.PasswordResetTokenRepository;
import com.flightbooking.auth.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;
    private final String fromEmail;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.frontend-base-url}") String frontendBaseUrl,
            @Value("${spring.mail.username}") String fromEmail) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.fromEmail = fromEmail;
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();

        // Always return the same response to the caller.
        var userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(sha256(rawToken));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        String resetUrl = frontendBaseUrl
                + "/reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Reset your SkyGo password");
        message.setText(
                "We received a request to reset your SkyGo password.\n\n"
                + "Open this link within 15 minutes to choose a new password:\n"
                + resetUrl + "\n\n"
                + "If you did not request this, you can ignore this email."
        );

        mailSender.send(message);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = sha256(request.token());

        PasswordResetToken resetToken =
                tokenRepository.findByTokenHashAndUsedFalse(tokenHash)
                    .filter(token -> token.getExpiresAt()
                            .isAfter(LocalDateTime.now()))
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Reset link is invalid or has expired"));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash reset token", ex);
        }
    }
}
