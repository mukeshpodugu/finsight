package com.finsight.controller;

import com.finsight.dto.BudgetRequest;
import com.finsight.dto.BudgetUtilizationResponse;
import com.finsight.entity.Budget;
import com.finsight.entity.User;
import com.finsight.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User user) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        
        List<Budget> budgets = budgetService.getBudgets(user, m, y);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    public ResponseEntity<Budget> setBudget(
            @Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal User user) {
        Budget budget = budgetService.setBudget(request, user);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/utilization")
    public ResponseEntity<List<BudgetUtilizationResponse>> getUtilization(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal User user) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();

        List<BudgetUtilizationResponse> utilization = budgetService.getBudgetUtilization(user, m, y);
        return ResponseEntity.ok(utilization);
    }
}
