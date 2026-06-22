package com.minipay.minipay_service.service;

import com.minipay.minipay_service.domain.User;
import com.minipay.minipay_service.domain.enums.Role;
import com.minipay.minipay_service.dto.request.LoginRequest;
import com.minipay.minipay_service.dto.request.RegisterRequest;
import com.minipay.minipay_service.dto.response.AuthResponse;
import com.minipay.minipay_service.exception.UserAlreadyExistsException;
import com.minipay.minipay_service.repository.UserRepository;
import com.minipay.minipay_service.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("ginah");
        registerRequest.setEmail("ginah@test.com");
        registerRequest.setPassword("testpass123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("ginah");
        loginRequest.setPassword("testpass123");

        savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("ginah")
                .email("ginah@test.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_shouldCreateUserAndReturnToken_whenUsernameAndEmailAreUnique() {
        // Arrange
        when(userRepository.existsByUsername("ginah")).thenReturn(false);
        when(userRepository.existsByEmail("ginah@test.com")).thenReturn(false);
        when(passwordEncoder.encode("testpass123")).thenReturn("encoded-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("ginah");
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("fake-jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("ginah");
        assertThat(response.getRole()).isEqualTo("USER");

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("testpass123");
    }

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("ginah")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("ginah")).thenReturn(false);
        when(userRepository.existsByEmail("ginah@test.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("ginah");
        when(userRepository.findByUsername("ginah")).thenReturn(Optional.of(savedUser));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("fake-jwt-token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(86400000L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getUsername()).isEqualTo("ginah");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void login_shouldPropagateException_whenAuthenticationManagerRejectsCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);

        verify(userRepository, never()).findByUsername(any());
    }
}