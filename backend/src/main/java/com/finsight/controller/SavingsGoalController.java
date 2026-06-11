package com.finsight.controller;

import com.finsight.dto.SavingsGoalRequest;
import com.finsight.entity.SavingsGoal;
import com.finsight.entity.User;
import com.finsight.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getGoals(@AuthenticationPrincipal User user) {
        List<SavingsGoal> goals = savingsGoalService.getGoals(user);
        return ResponseEntity.ok(goals);
    }

    @PostMapping
    public ResponseEntity<SavingsGoal> createGoal(
            @Valid @RequestBody SavingsGoalRequest request,
            @AuthenticationPrincipal User user) {
        SavingsGoal goal = savingsGoalService.createGoal(request, user);
        return ResponseEntity.ok(goal);
    }

    @PutMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoal> contribute(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal User user) {
        SavingsGoal goal = savingsGoalService.contributeToGoal(id, amount, user);
        return ResponseEntity.ok(goal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        savingsGoalService.deleteGoal(id, user);
        return ResponseEntity.ok(Map.of("message", "Savings goal deleted successfully"));
    }
}
