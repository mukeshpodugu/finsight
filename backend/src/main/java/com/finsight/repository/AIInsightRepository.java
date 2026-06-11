package com.finsight.repository;

import com.finsight.entity.AIInsight;
import com.finsight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AIInsightRepository extends JpaRepository<AIInsight, Long> {
    Optional<AIInsight> findByUserAndInsightTypeAndTargetMonthAndTargetYear(
            User user, String insightType, int month, int year);
}
