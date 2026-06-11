package com.finsight.service;

import com.finsight.dto.SavingsGoalRequest;
import com.finsight.entity.Notification;
import com.finsight.entity.SavingsGoal;
import com.finsight.entity.User;
import com.finsight.repository.NotificationRepository;
import com.finsight.repository.SavingsGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final NotificationRepository notificationRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                              NotificationRepository notificationRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<SavingsGoal> getGoals(User user) {
        return savingsGoalRepository.findAllByUser(user);
    }

    @Transactional
    public SavingsGoal createGoal(SavingsGoalRequest request, User user) {
        SavingsGoal goal = SavingsGoal.builder()
                .user(user)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .deadline(request.getDeadline())
                .status("IN_PROGRESS")
                .build();
        return savingsGoalRepository.save(goal);
    }

    @Transactional
    public SavingsGoal contributeToGoal(Long id, BigDecimal amount, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to savings goal");
        }

        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);

        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus("COMPLETED");

            Notification notification = Notification.builder()
                    .user(user)
                    .title("Savings Goal Achieved! 🏆")
                    .message(String.format("Congratulations! You have successfully reached your target savings goal of ₹%s for: %s.",
                            goal.getTargetAmount(), goal.getName()))
                    .type("GOAL_REMINDER")
                    .build();
            notificationRepository.save(notification);
        }

        return savingsGoalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(Long id, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this savings goal");
        }

        savingsGoalRepository.delete(goal);
    }
}
