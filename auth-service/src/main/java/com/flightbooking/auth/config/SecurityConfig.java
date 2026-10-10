package com.flightbooking.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.flightbooking.auth.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {
	  private final JwtAuthenticationFilter jwtAuthenticationFilter;
	    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
	        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	    }

	    @Bean
	    public PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }

	    @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	        http.csrf(csrf -> csrf.disable())
	            .sessionManagement(session ->
	                    session.sessionCreationPolicy(
	                            org.springframework.security.config.http
	                                    .SessionCreationPolicy.STATELESS
	                    )
	            )

	            .exceptionHandling(exception ->
	                    exception.authenticationEntryPoint(
	                            new HttpStatusEntryPoint(
	                                    HttpStatus.UNAUTHORIZED
	                            )
	                    )
	            )

	            .authorizeHttpRequests(auth -> auth
	                    .requestMatchers(
	                            "/api/auth/register",
	                            "/api/auth/login",
	                            "/api/auth/refresh",
	                            "/api/auth/logout",
	                            "/actuator/health",
	                            "/api/auth/password/forgot",
	                            "/api/auth/password/reset"
	                    ).permitAll()
	                    .anyRequest().authenticated()
	            ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	        return http.build();
	    }
}
