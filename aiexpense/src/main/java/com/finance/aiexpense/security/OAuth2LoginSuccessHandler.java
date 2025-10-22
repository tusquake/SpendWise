package com.finance.aiexpense.security;

import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        log.info("GitHub OAuth2 attributes: {}", oAuth2User.getAttributes());

        if (email == null) {
            // Attempt to extract from "emails" attribute if exists
            Object emailsObj = oAuth2User.getAttribute("emails");
            if (emailsObj instanceof List<?> emailsList && !emailsList.isEmpty()) {
                Map<?, ?> firstEmail = (Map<?, ?>) emailsList.get(0);
                email = (String) firstEmail.get("email");
            }
        }

        // Fallback: create a dummy email using GitHub login
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        log.info("=== OAuth2 Login Success ===");
        log.info("Email: {}", email);
        log.info("Redirect URI: {}", redirectUri);

        try {
            // Find user in database
            String finalEmail = email;
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login: " + finalEmail));

            log.info("User found in database: {}", user.getEmail());

            // Generate JWT token
            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("JWT tokens generated successfully");

            // Redirect to frontend with tokens
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            log.info("Redirecting to: {}", targetUrl);

            // Clear the authentication to ensure redirect works
            clearAuthenticationAttributes(request);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

            log.info("Redirect executed successfully");

        } catch (Exception e) {
            log.error("Error during OAuth2 success handler: ", e);
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "authentication_failed")
                    .build().toUriString();
            response.sendRedirect(errorUrl);
        }
    }
}