package com.finsight.service;

import com.finsight.dto.RecurringPaymentRequest;
import com.finsight.entity.Category;
import com.finsight.entity.RecurringPayment;
import com.finsight.entity.User;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.RecurringPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final CategoryRepository categoryRepository;

    public RecurringPaymentService(RecurringPaymentRepository recurringPaymentRepository,
                                   CategoryRepository categoryRepository) {
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<RecurringPayment> getRecurringPayments(User user) {
        return recurringPaymentRepository.findAllByUser(user);
    }

    @Transactional
    public RecurringPayment createRecurringPayment(RecurringPaymentRequest request, User user) {
        Category category = categoryRepository.findByIdAndUserOrSystem(request.getCategoryId(), user)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        RecurringPayment payment = RecurringPayment.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .type(request.getType().toUpperCase())
                .description(request.getDescription())
                .frequency(request.getFrequency().toUpperCase())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionDate(request.getStartDate())
                .isActive(true)
                .build();

        return recurringPaymentRepository.save(payment);
    }

    @Transactional
    public void deleteRecurringPayment(Long id, User user) {
        RecurringPayment payment = recurringPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recurring payment setup not found"));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this recurring payment setup");
        }

        recurringPaymentRepository.delete(payment);
    }
}
