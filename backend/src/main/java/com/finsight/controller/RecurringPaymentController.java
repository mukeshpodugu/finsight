package com.finsight.controller;

import com.finsight.dto.RecurringPaymentRequest;
import com.finsight.entity.RecurringPayment;
import com.finsight.entity.User;
import com.finsight.service.RecurringPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recurring-payments")
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    public RecurringPaymentController(RecurringPaymentService recurringPaymentService) {
        this.recurringPaymentService = recurringPaymentService;
    }

    @GetMapping
    public ResponseEntity<List<RecurringPayment>> getRecurringPayments(
            @AuthenticationPrincipal User user) {
        List<RecurringPayment> payments = recurringPaymentService.getRecurringPayments(user);
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<RecurringPayment> createRecurringPayment(
            @Valid @RequestBody RecurringPaymentRequest request,
            @AuthenticationPrincipal User user) {
        RecurringPayment payment = recurringPaymentService.createRecurringPayment(request, user);
        return ResponseEntity.ok(payment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRecurringPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        recurringPaymentService.deleteRecurringPayment(id, user);
        return ResponseEntity.ok(Map.of("message", "Recurring payment setup deleted successfully"));
    }
}
