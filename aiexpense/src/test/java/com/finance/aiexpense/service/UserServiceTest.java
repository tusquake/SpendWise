package com.finance.aiexpense.service;

import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();
    }

    @Test
    void loadUserByUsername_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals("password", result.getPassword());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_userNotFound_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent@example.com"));
        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}

