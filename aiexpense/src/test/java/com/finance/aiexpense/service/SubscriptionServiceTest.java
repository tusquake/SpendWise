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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();
    }

    @Test
    void upgradeSubscription_success() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        subscriptionService.upgradeSubscription(user, SubscriptionTier.PREMIUM, 1);

        assertEquals(SubscriptionTier.PREMIUM, user.getSubscriptionTier());
        assertNotNull(user.getSubscriptionStartDate());
        assertNotNull(user.getSubscriptionEndDate());
        verify(userRepository).save(user);
    }

    @Test
    void isSubscriptionActive_activeSubscription() {
        user.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));

        boolean result = subscriptionService.isSubscriptionActive(user);

        assertTrue(result);
    }

    @Test
    void isSubscriptionActive_expiredSubscription() {
        user.setSubscriptionEndDate(LocalDateTime.now().minusMonths(1));

        boolean result = subscriptionService.isSubscriptionActive(user);

        assertFalse(result);
    }

    @Test
    void isSubscriptionActive_noEndDate() {
        user.setSubscriptionEndDate(null);

        boolean result = subscriptionService.isSubscriptionActive(user);

        assertFalse(result);
    }

    @Test
    void cancelSubscription_success() {
        user.setSubscriptionTier(SubscriptionTier.PREMIUM);
        user.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
        when(userRepository.save(any(User.class))).thenReturn(user);

        subscriptionService.cancelSubscription(user);

        assertEquals(SubscriptionTier.FREE, user.getSubscriptionTier());
        assertNull(user.getSubscriptionEndDate());
        verify(userRepository).save(user);
    }
}

