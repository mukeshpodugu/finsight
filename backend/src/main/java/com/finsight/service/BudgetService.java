package com.finsight.service;

import com.finsight.dto.BudgetRequest;
import com.finsight.dto.BudgetUtilizationResponse;
import com.finsight.entity.Budget;
import com.finsight.entity.Category;
import com.finsight.entity.User;
import com.finsight.repository.BudgetRepository;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         CategoryRepository categoryRepository,
                         TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Budget> getBudgets(User user, int month, int year) {
        return budgetRepository.findAllByUserAndMonthAndYear(user, month, year);
    }

    @Transactional
    public Budget setBudget(BudgetRequest request, User user) {
        Category category = categoryRepository.findByIdAndUserOrSystem(request.getCategoryId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!"EXPENSE".equals(category.getType())) {
            throw new RuntimeException("Budgets can only be set on expense categories");
        }

        Optional<Budget> existingOpt = budgetRepository.findByUserAndCategoryAndMonthAndYear(
                user, category, request.getMonth(), request.getYear());

        Budget budget;
        if (existingOpt.isPresent()) {
            budget = existingOpt.get();
            budget.setAmount(request.getAmount());
        } else {
            budget = Budget.builder()
                    .user(user)
                    .category(category)
                    .amount(request.getAmount())
                    .month(request.getMonth())
                    .year(request.getYear())
                    .build();
        }

        return budgetRepository.save(budget);
    }

    public List<BudgetUtilizationResponse> getBudgetUtilization(User user, int month, int year) {
        List<Budget> budgets = budgetRepository.findAllByUserAndMonthAndYear(user, month, year);
        List<BudgetUtilizationResponse> utilizationList = new ArrayList<>();

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        for (Budget budget : budgets) {
            Category category = budget.getCategory();
            BigDecimal spent = transactionRepository.sumExpenseByCategoryAndDateBetween(user, category, start, end);
            if (spent == null) {
                spent = BigDecimal.ZERO;
            }

            BigDecimal limit = budget.getAmount();
            BigDecimal remaining = limit.subtract(spent);
            
            double percentageUsed = 0.0;
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = spent.multiply(new BigDecimal("100"))
                        .divide(limit, 2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            utilizationList.add(BudgetUtilizationResponse.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .colorCode(category.getColorCode())
                    .budgetAmount(limit)
                    .spentAmount(spent)
                    .remainingAmount(remaining)
                    .percentageUsed(percentageUsed)
                    .build());
        }

        return utilizationList;
    }
}
