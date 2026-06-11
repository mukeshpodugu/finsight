package com.finsight.service;

import com.finsight.dto.TransactionRequest;
import com.finsight.entity.Budget;
import com.finsight.entity.Category;
import com.finsight.entity.Notification;
import com.finsight.entity.Transaction;
import com.finsight.entity.User;
import com.finsight.repository.BudgetRepository;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.NotificationRepository;
import com.finsight.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;

    @Value("${finsight.upload.dir}")
    private String uploadDir;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              BudgetRepository budgetRepository,
                              NotificationRepository notificationRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
        this.notificationRepository = notificationRepository;
    }

    public Page<Transaction> getTransactions(User user, String type, Long categoryId,
                                             LocalDate startDate, LocalDate endDate,
                                             String search, Pageable pageable) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findByIdAndUserOrSystem(categoryId, user)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }
        return transactionRepository.findFiltered(user, type, category, startDate, endDate, search, pageable);
    }

    public Transaction getTransactionById(Long id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to transaction");
        }
        return transaction;
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request, MultipartFile file, User user) {
        Category category = categoryRepository.findByIdAndUserOrSystem(request.getCategoryId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        String receiptUrl = null;
        if (file != null && !file.isEmpty()) {
            receiptUrl = saveReceipt(file);
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType().toUpperCase())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .receiptUrl(receiptUrl)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        if ("EXPENSE".equals(savedTransaction.getType())) {
            checkBudgetUtil(user, category, savedTransaction.getTransactionDate());
        }

        return savedTransaction;
    }

    @Transactional
    public Transaction updateTransaction(Long id, TransactionRequest request, MultipartFile file, User user) {
        Transaction transaction = getTransactionById(id, user);
        Category category = categoryRepository.findByIdAndUserOrSystem(request.getCategoryId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        transaction.setCategory(category);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType().toUpperCase());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());

        if (file != null && !file.isEmpty()) {
            String receiptUrl = saveReceipt(file);
            transaction.setReceiptUrl(receiptUrl);
        }

        Transaction updated = transactionRepository.save(transaction);

        if ("EXPENSE".equals(updated.getType())) {
            checkBudgetUtil(user, category, updated.getTransactionDate());
        }

        return updated;
    }

    @Transactional
    public void deleteTransaction(Long id, User user) {
        Transaction transaction = getTransactionById(id, user);
        transactionRepository.delete(transaction);
    }

    private String saveReceipt(MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filepath = Paths.get(uploadDir, filename).toString();
            file.transferTo(new File(filepath));
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save receipt file: " + e.getMessage());
        }
    }

    private void checkBudgetUtil(User user, Category category, LocalDate date) {
        int month = date.getMonthValue();
        int year = date.getYear();

        Optional<Budget> budgetOpt = budgetRepository.findByUserAndCategoryAndMonthAndYear(user, category, month, year);
        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            BigDecimal totalSpent = transactionRepository.sumExpenseByCategoryAndDateBetween(user, category, start, end);
            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }

            BigDecimal limit = budget.getAmount();
            BigDecimal threshold80 = limit.multiply(new BigDecimal("0.8"));

            if (totalSpent.compareTo(limit) > 0) {
                Notification notification = Notification.builder()
                        .user(user)
                        .title("Budget Exceeded: " + category.getName())
                        .message(String.format("Alert! You have spent ₹%s, which exceeds your monthly budget limit of ₹%s for %s.",
                                totalSpent, limit, category.getName()))
                        .type("BUDGET_ALERT")
                        .build();
                notificationRepository.save(notification);
            } else if (totalSpent.compareTo(threshold80) >= 0) {
                Notification notification = Notification.builder()
                        .user(user)
                        .title("Budget Warning: " + category.getName())
                        .message(String.format("Warning: You have utilized 80%% or more of your budget limit (Spent ₹%s of ₹%s) for %s.",
                                totalSpent, limit, category.getName()))
                        .type("BUDGET_ALERT")
                        .build();
                notificationRepository.save(notification);
            }
        }
    }
}
