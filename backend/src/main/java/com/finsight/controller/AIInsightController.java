package com.finsight.controller;

import com.finsight.entity.User;
import com.finsight.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AIInsightController {

    private final AIService aiService;

    public AIInsightController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/spending-insights")
    public ResponseEntity<Map<String, String>> getSpendingInsights(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User user) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        String insights = aiService.getSpendingInsights(user, m, y);
        return ResponseEntity.ok(Map.of("insights", insights));
    }

    @GetMapping("/budget-recommendations")
    public ResponseEntity<Map<String, String>> getBudgetRecommendations(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User user) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        String recommendations = aiService.getBudgetRecommendations(user, m, y);
        return ResponseEntity.ok(Map.of("recommendations", recommendations));
    }

    @GetMapping("/financial-health-score")
    public ResponseEntity<Map<String, Object>> getFinancialHealthScore(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User user) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        Map<String, Object> scoreDetails = aiService.getFinancialHealthScore(user, m, y);
        return ResponseEntity.ok(scoreDetails);
    }
}
