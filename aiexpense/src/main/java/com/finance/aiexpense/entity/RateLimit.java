package com.finance.aiexpense.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "limit_type", "date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false)
    private LimitType limitType;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "request_count", nullable = false)
    private Integer requestCount = 0;

    @Column(name = "last_request_time")
    private LocalDateTime lastRequestTime;
}