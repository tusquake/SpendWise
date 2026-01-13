package com.finance.aiexpense.service;

import com.finance.aiexpense.dto.AuthRequest;
import com.finance.aiexpense.dto.AuthResponse;
import com.finance.aiexpense.dto.RegisterRequest;
import com.finance.aiexpense.dto.UserDTO;
import com.finance.aiexpense.entity.Role;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.exception.ResourceConflictException;
import com.finance.aiexpense.repository.UserRepository;
import com.finance.aiexpense.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        authRequest = new AuthRequest();
        authRequest.setEmail("john@example.com");
        authRequest.setPassword("password123");

        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertNotNull(response.getUser());
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> authService.register(registerRequest));
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john@example.com");
        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }

    @Test
    void login_userNotFound_throwsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(authRequest));
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void refreshToken_success() {
        when(jwtService.extractUsername(anyString())).thenReturn("john@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("newJwtToken");

        AuthResponse response = authService.refreshToken("refreshToken");

        assertNotNull(response);
        assertEquals("newJwtToken", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(jwtService).extractUsername("refreshToken");
        verify(jwtService).isTokenValid("refreshToken", user);
        verify(jwtService).generateToken(user);
    }

    @Test
    void refreshToken_invalidToken_throwsException() {
        when(jwtService.extractUsername(anyString())).thenReturn("john@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(anyString(), any(User.class))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.refreshToken("invalidToken"));
        verify(jwtService).isTokenValid("invalidToken", user);
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void refreshToken_userNotFound_throwsException() {
        when(jwtService.extractUsername(anyString())).thenReturn("john@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.refreshToken("refreshToken"));
        verify(userRepository).findByEmail("john@example.com");
    }
}

