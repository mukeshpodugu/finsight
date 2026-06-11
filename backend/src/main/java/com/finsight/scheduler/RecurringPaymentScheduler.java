package com.finsight.scheduler;

import com.finsight.entity.Notification;
import com.finsight.entity.RecurringPayment;
import com.finsight.entity.Transaction;
import com.finsight.repository.NotificationRepository;
import com.finsight.repository.RecurringPaymentRepository;
import com.finsight.repository.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecurringPaymentScheduler {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    public RecurringPaymentScheduler(RecurringPaymentRepository recurringPaymentRepository,
                                     TransactionRepository transactionRepository,
                                     NotificationRepository notificationRepository) {
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
    }

    // Run every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processRecurringPayments() {
        LocalDate today = LocalDate.now();
        List<RecurringPayment> duePayments = recurringPaymentRepository
                .findAllByIsActiveTrueAndNextExecutionDateLessThanEqual(today);

        System.out.println("Processing due recurring payments count: " + duePayments.size());

        for (RecurringPayment payment : duePayments) {
            try {
                Transaction transaction = Transaction.builder()
                        .user(payment.getUser())
                        .category(payment.getCategory())
                        .amount(payment.getAmount())
                        .type(payment.getType())
                        .transactionDate(payment.getNextExecutionDate())
                        .description("Recurring: " + payment.getDescription())
                        .isRecurring(true)
                        .recurringPayment(payment)
                        .build();

                transactionRepository.save(transaction);

                LocalDate nextDate = calculateNextDate(payment.getNextExecutionDate(), payment.getFrequency());
                payment.setNextExecutionDate(nextDate);

                if (payment.getEndDate() != null && nextDate.isAfter(payment.getEndDate())) {
                    payment.setActive(false);
                }
                recurringPaymentRepository.save(payment);

                Notification notification = Notification.builder()
                        .user(payment.getUser())
                        .title("Scheduled Payment Processed")
                        .message(String.format("Your scheduled transaction for '%s' of ₹%s has been successfully recorded.",
                                payment.getDescription(), payment.getAmount()))
                        .type("BILL_REMINDER")
                        .build();
                notificationRepository.save(notification);

            } catch (Exception e) {
                System.err.println("Error processing recurring payment ID: " + payment.getId() + ": " + e.getMessage());
            }
        }
    }

    private LocalDate calculateNextDate(LocalDate current, String frequency) {
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> current.plusDays(1);
            case "WEEKLY" -> current.plusWeeks(1);
            case "MONTHLY" -> current.plusMonths(1);
            case "YEARLY" -> current.plusYears(1);
            default -> current.plusMonths(1);
        };
    }
}
