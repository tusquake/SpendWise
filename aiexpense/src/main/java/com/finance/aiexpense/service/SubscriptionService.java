package com.finance.aiexpense.service;

import com.finance.aiexpense.entity.User;
import com.finance.aiexpense.enums.SubscriptionTier;
import com.finance.aiexpense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;

    @Transactional
    public void upgradeSubscription(User user, SubscriptionTier tier, Integer durationMonths) {
        user.setSubscriptionTier(tier);
        user.setSubscriptionStartDate(LocalDateTime.now());
        user.setSubscriptionEndDate(LocalDateTime.now().plusMonths(durationMonths));

        userRepository.save(user);

        log.info("User {} upgraded to {} for {} months",
                user.getEmail(), tier, durationMonths);
    }

    public boolean isSubscriptionActive(User user) {
        return user.getSubscriptionEndDate() != null &&
                user.getSubscriptionEndDate().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void cancelSubscription(User user) {
        user.setSubscriptionTier(SubscriptionTier.FREE);
        user.setSubscriptionEndDate(null);
        userRepository.save(user);

        log.info("Subscription cancelled for user {}", user.getEmail());
    }
}