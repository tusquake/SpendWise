package com.finance.aiexpense.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthControllerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private GoogleAuthController googleAuthController;

    @BeforeEach
    void setUp() {
        // No specific setup needed for this controller beyond mocking dependencies
    }

    @Test
    void redirectToGoogle_shouldRedirectToOAuth2AuthorizationGoogle() throws IOException {
        googleAuthController.redirectToGoogle(request, response);
        verify(response).sendRedirect("/oauth2/authorization/google");
    }
}

