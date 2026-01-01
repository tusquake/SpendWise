package com.finance.aiexpense.controller;

import com.finance.aiexpense.dto.ApiResponse;
import com.finance.aiexpense.dto.AuthRequest;
import com.finance.aiexpense.dto.AuthResponse;
import com.finance.aiexpense.dto.RegisterRequest;
import com.finance.aiexpense.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password");

        authRequest = new AuthRequest();
        authRequest.setEmail("john.doe@example.com");
        authRequest.setPassword("password");

        authResponse = new AuthResponse("mockJwtToken", "mockRefreshToken");
    }

    @Test
    void registerUser_success() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authController.register(registerRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("User registered successfully", responseEntity.getBody().getMessage());
        assertEquals(authResponse, responseEntity.getBody().getData());
    }

    @Test
    void loginUser_success() {
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authController.login(authRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Login successful", responseEntity.getBody().getMessage());
        assertEquals(authResponse, responseEntity.getBody().getData());
    }

    @Test
    void refreshToken_success() {
        when(authService.refreshToken(any(String.class))).thenReturn(authResponse);

        ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authController.refreshToken("Bearer mockRefreshToken");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("Token refreshed", responseEntity.getBody().getMessage());
        assertEquals(authResponse, responseEntity.getBody().getData());
    }
}

