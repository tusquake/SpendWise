package com.finance.aiexpense.service;

import com.finance.aiexpense.entity.LimitType;
import com.finance.aiexpense.entity.RateLimit;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.exception.RateLimitExceededException;
import com.finance.aiexpense.repository.RateLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RateLimitRepository rateLimitRepository;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    private User freeUser;
    private User premiumUser;
    private RateLimit rateLimit;

    @BeforeEach
    void setUp() {
        freeUser = User.builder()
                .id(1L)
                .email("free@example.com")
                .subscriptionTier(SubscriptionTier.FREE)
                .build();

        premiumUser = User.builder()
                .id(2L)
                .email("premium@example.com")
                .subscriptionTier(SubscriptionTier.PREMIUM)
                .build();

        rateLimit = RateLimit.builder()
                .id(1L)
                .user(freeUser)
                .limitType(LimitType.AI_CHAT)
                .date(LocalDate.now())
                .requestCount(0)
                .build();
    }

    @Test
    void checkAndIncrementAIChatLimit_success() {
        when(rateLimitRepository.findByUserAndLimitTypeAndDate(any(User.class), any(LimitType.class), any(LocalDate.class)))
                .thenReturn(Optional.of(rateLimit));
        when(rateLimitRepository.save(any(RateLimit.class))).thenReturn(rateLimit);

        assertDoesNotThrow(() -> rateLimiterService.checkAndIncrementAIChatLimit(freeUser));
        verify(rateLimitRepository).save(any(RateLimit.class));
    }

    @Test
    void checkAndIncrementAIChatLimit_createsNewRateLimit() {
        when(rateLimitRepository.findByUserAndLimitTypeAndDate(any(User.class), any(LimitType.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(rateLimitRepository.save(any(RateLimit.class))).thenReturn(rateLimit);

        assertDoesNotThrow(() -> rateLimiterService.checkAndIncrementAIChatLimit(freeUser));
        verify(rateLimitRepository).save(any(RateLimit.class));
    }

    @Test
    void checkAndIncrementAIChatLimit_exceedsLimit_throwsException() {
        rateLimit.setRequestCount(SubscriptionTier.FREE.getDailyAIChatLimit());
        when(rateLimitRepository.findByUserAndLimitTypeAndDate(any(User.class), any(LimitType.class), any(LocalDate.class)))
                .thenReturn(Optional.of(rateLimit));

        assertThrows(RateLimitExceededException.class, () -> rateLimiterService.checkAndIncrementAIChatLimit(freeUser));
        verify(rateLimitRepository, never()).save(any(RateLimit.class));
    }

    @Test
    void getRemainingAIChats_withExistingRateLimit() {
        rateLimit.setRequestCount(1);
        when(rateLimitRepository.findByUserAndLimitTypeAndDate(any(User.class), any(LimitType.class), any(LocalDate.class)))
                .thenReturn(Optional.of(rateLimit));

        int remaining = rateLimiterService.getRemainingAIChats(freeUser);

        int expected = SubscriptionTier.FREE.getDailyAIChatLimit() - 1;
        assertEquals(expected, remaining);
    }

    @Test
    void getRemainingAIChats_withoutRateLimit() {
        when(rateLimitRepository.findByUserAndLimitTypeAndDate(any(User.class), any(LimitType.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        int remaining = rateLimiterService.getRemainingAIChats(freeUser);

        assertEquals(SubscriptionTier.FREE.getDailyAIChatLimit(), remaining);
    }

    @Test
    void getRemainingAIChats_premiumUser() {
        when(rateLimitRepository.findByUserAndLimitTypeAndDate(any(User.class), any(LimitType.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        int remaining = rateLimiterService.getRemainingAIChats(premiumUser);

        assertEquals(SubscriptionTier.PREMIUM.getDailyAIChatLimit(), remaining);
    }
}

