package com.finance.aiexpense.repository;

import com.finance.aiexpense.entity.LimitType;
import com.finance.aiexpense.entity.RateLimit;
import com.finance.aiexpense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RateLimitRepository extends JpaRepository<RateLimit, Long> {

    Optional<RateLimit> findByUserAndLimitTypeAndDate(
            User user, LimitType limitType, LocalDate date);
}
