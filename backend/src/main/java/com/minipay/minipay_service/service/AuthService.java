package com.minipay.minipay_service.service;

import com.minipay.minipay_service.domain.User;
import com.minipay.minipay_service.dto.request.LoginRequest;
import com.minipay.minipay_service.dto.request.RegisterRequest;
import com.minipay.minipay_service.dto.response.AuthResponse;
import com.minipay.minipay_service.exception.UserAlreadyExistsException;
import com.minipay.minipay_service.repository.UserRepository;
import com.minipay.minipay_service.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        Authentication authentication = authenticateUser(request.getUsername(), request.getPassword());
        return buildAuthResponse(authentication, user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticateUser(request.getUsername(), request.getPassword());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in repository"));

        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(authentication, user.getRole().name());

    }

    private Authentication authenticateUser(String username, String rawPassword) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, rawPassword)
        );
    }

    private AuthResponse buildAuthResponse(Authentication authentication, String role) {
        String token = jwtTokenProvider.generateToken(authentication);
        return AuthResponse.builder()
                .token(token)
                .username(authentication.getName())
                .role(role)
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .build();
    }
}
