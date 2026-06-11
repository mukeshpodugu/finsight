package com.finsight.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_insights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "insight_text", nullable = false, columnDefinition = "TEXT")
    private String insightText;

    @Column(name = "insight_type", nullable = false, length = 50)
    private String insightType; // 'SPENDING_INSIGHT', 'BUDGET_RECOMMENDATION'

    @Column(name = "health_score")
    private Integer healthScore; // 0 - 100

    @Column(name = "target_month", nullable = false)
    private int targetMonth;

    @Column(name = "target_year", nullable = false)
    private int targetYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
