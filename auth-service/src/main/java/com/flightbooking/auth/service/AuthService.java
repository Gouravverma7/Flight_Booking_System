package com.flightbooking.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightbooking.auth.dto.AuthResponse;
import com.flightbooking.auth.dto.RegisterRequest;
import com.flightbooking.auth.entity.RefreshToken;
import com.flightbooking.auth.entity.Role;
import com.flightbooking.auth.entity.User;
import com.flightbooking.auth.exception.ConflictException;
import com.flightbooking.auth.exception.UnauthorizedException;
import com.flightbooking.auth.repository.RefreshTokenRepository;
import com.flightbooking.auth.repository.UserRepository;
import com.flightbooking.auth.security.JwtService;
import com.flightbooking.auth.dto.LoginRequest;
import com.flightbooking.auth.dto.LoginResponse;
import com.flightbooking.auth.security.TokenHashUtil;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenRepository refreshTokenRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public AuthResponse register(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase();
		if(userRepository.existsByEmail(email)) {
			throw new ConflictException("Email is already Registered");
		}

		User user = new User();
		user.setName(request.getName().trim());
		user.setEmail(email);

		String encodedPassword = passwordEncoder.encode(request.getPassword());
		user.setPassword(encodedPassword);
		user.setRole(Role.USER);
		user.setCreatedAt(LocalDateTime.now());

		User savedUser = userRepository.save(user);

		return new AuthResponse(
				"User Registered Successfully",
				savedUser.getId(),
				savedUser.getName(),
				savedUser.getEmail());
	}

	public LoginResponse login(LoginRequest request) {

	    String email =
	            request.getEmail()
	                    .trim()
	                    .toLowerCase();

	    User user =
	            userRepository.findByEmail(email)
	                    .orElseThrow(() ->
	                            new UnauthorizedException(
	                                    "Invalid email or password"
	                            )
	                    );

	    if (!passwordEncoder.matches(
	            request.getPassword(),
	            user.getPassword())) {

	        throw new UnauthorizedException(
	                "Invalid email or password"
	        );
	    }

	    String accessToken =
	            jwtService.generateAccessToken(
	                    user.getId(),
	                    user.getEmail(),
	                    user.getRole().name()
	            );

	    String refreshToken =
	            jwtService.generateRefreshToken(
	                    user.getId(),
	                    user.getEmail()
	            );

	    saveRefreshToken(
	            user,
	            refreshToken
	    );

	    return new LoginResponse(
	            accessToken,
	            refreshToken,
	            user.getId(),
	            user.getName(),
	            user.getEmail(),
	            user.getRole().name()
	    );
	}

	private void saveRefreshToken(
	        User user,
	        String rawToken) {

	    RefreshToken refreshToken =
	            new RefreshToken();

	    refreshToken.setTokenHash(
	            TokenHashUtil.sha256(rawToken)
	    );

	    refreshToken.setUser(user);

	    refreshToken.setExpireAt(
	            LocalDateTime.now()
	                    .plusSeconds(
	                            jwtService
	                                    .getRefreshTokenExpiration()
	                                    / 1000
	                    )
	    );

	    refreshToken.setRevoked(false);

	    refreshTokenRepository.save(
	            refreshToken
	    );
	}

	public LoginResponse refreshToken(String rawRefreshToken) {
		if(!jwtService.isTokenValid(rawRefreshToken)) {
			throw new UnauthorizedException("Refresh token is expired or invalid");
		}

		String tokenType = jwtService.extractTokenType(rawRefreshToken);
		if(!"REFRESH".equals(tokenType)) {
			throw new UnauthorizedException("Invalid token type");
		}

		String tokenHash = TokenHashUtil.sha256(rawRefreshToken);

		RefreshToken storedToken = refreshTokenRepository
										.findByTokenHashAndRevokedFalse(
												tokenHash
										).orElseThrow(() -> new UnauthorizedException("Refresh token is revoked or invalid"));

		if(storedToken.getExpireAt().isBefore(java.time.LocalDateTime.now())) {
			storedToken.setRevoked(true);
			refreshTokenRepository.save(storedToken);
//			throw new RuntimeException("Refresh token is expired");
			throw new UnauthorizedException("Refresh token is expired");
		}

		User user = storedToken.getUser();

		storedToken.setRevoked(true);

		refreshTokenRepository.save(storedToken);

		String newAccessToken = jwtService.generateAccessToken(
										user.getId(),
										user.getEmail(),
										user.getRole().name());

		String newRefreshToken = jwtService.generateRefreshToken(
										user.getId(),
										user.getEmail());

		saveRefreshToken(user, newRefreshToken);

		return new LoginResponse(
				newAccessToken,
				newRefreshToken,
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole().name());
	}

	public void logout(String rawRefreshToken) {
		String tokenHash = TokenHashUtil.sha256(rawRefreshToken);
		refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
							.ifPresent(token -> {
								token.setRevoked(true);
								refreshTokenRepository.save(token);
							});
	}




}
