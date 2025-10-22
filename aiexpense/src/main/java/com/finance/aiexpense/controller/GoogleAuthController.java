package com.finance.aiexpense.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GoogleAuthController {

    /**
     * Frontend can call /auth/google to start Google OAuth in one click.
     * This will redirect to Spring's /oauth2/authorization/google which
     * constructs the proper Google URL including state and redirect_uri.
     */
    @GetMapping("/auth/google")
    public void redirectToGoogle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // If your Spring mapping includes a prefix (e.g. /api), change below accordingly.
        response.sendRedirect("/oauth2/authorization/google");
    }
}
