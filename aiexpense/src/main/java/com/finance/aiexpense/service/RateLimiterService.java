package com.finance.aiexpense.service;

import com.finance.aiexpense.entity.LimitType;
import com.finance.aiexpense.entity.RateLimit;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.exception.RateLimitExceededException;
import com.finance.aiexpense.repository.RateLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RateLimitRepository rateLimitRepository;

    @Transactional
    public void checkAndIncrementAIChatLimit(User user) {
        LocalDate today = LocalDate.now();

        RateLimit rateLimit = rateLimitRepository
                .findByUserAndLimitTypeAndDate(user, LimitType.AI_CHAT, today)
                .orElseGet(() -> createNewRateLimit(user, LimitType.AI_CHAT, today));

        SubscriptionTier tier = user.getSubscriptionTier();
        int limit = tier.getDailyAIChatLimit();

        if (rateLimit.getRequestCount() >= limit) {
            log.warn("Rate limit exceeded for user: {} ({})", user.getEmail(), tier);
            throw new RateLimitExceededException(
                    String.format("Daily AI chat limit exceeded. You have used %d/%d requests. " +
                                    "Upgrade to Premium for %d requests per day!",
                            rateLimit.getRequestCount(), limit,
                            SubscriptionTier.PREMIUM.getDailyAIChatLimit())
            );
        }

        rateLimit.setRequestCount(rateLimit.getRequestCount() + 1);
        rateLimit.setLastRequestTime(LocalDateTime.now());
        rateLimitRepository.save(rateLimit);

        log.info("AI chat request allowed. User: {}, Count: {}/{}",
                user.getEmail(), rateLimit.getRequestCount(), limit);
    }

    public int getRemainingAIChats(User user) {
        LocalDate today = LocalDate.now();
        RateLimit rateLimit = rateLimitRepository
                .findByUserAndLimitTypeAndDate(user, LimitType.AI_CHAT, today)
                .orElse(null);

        int limit = user.getSubscriptionTier().getDailyAIChatLimit();
        int used = (rateLimit != null) ? rateLimit.getRequestCount() : 0;

        return Math.max(0, limit - used);
    }

    private RateLimit createNewRateLimit(User user, LimitType limitType, LocalDate date) {
        return RateLimit.builder()
                .user(user)
                .limitType(limitType)
                .date(date)
                .requestCount(0)
                .build();
    }
}