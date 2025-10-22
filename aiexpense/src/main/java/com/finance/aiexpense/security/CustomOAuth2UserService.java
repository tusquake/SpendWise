package com.finance.aiexpense.security;

import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.AuthProvider;
import com.finance.aiexpense.entity.Role;
import com.finance.aiexpense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 user loaded from provider: {}", registrationId);

        processOAuth2User(registrationId, oAuth2User);

        return oAuth2User;
    }

    private void processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        String email;
        String name;
        String providerId;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub");
        } else if ("github".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("id").toString();

            // GitHub might not provide email if it's private
            if (email == null) {
                email = oAuth2User.getAttribute("login") + "@github.com";
            }
            if (name == null) {
                name = oAuth2User.getAttribute("login");
            }
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isEmpty()) {
            // Create new user
            user = User.builder()
                    .email(email)
                    .name(name)
                    .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                    .providerId(providerId)
                    .role(Role.USER)
                    .build();

            userRepository.save(user);
            log.info("Created new user from OAuth2: {}", email);
        } else {
            user = userOptional.get();

            // Update user info if needed
            if (user.getProvider() == null || user.getProviderId() == null) {
                user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
                user.setProviderId(providerId);
                userRepository.save(user);
                log.info("Updated user with OAuth2 info: {}", email);
            }
        }
    }
}