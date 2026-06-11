package com.finsight.repository;

import com.finsight.entity.RecurringPayment;
import com.finsight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {
    List<RecurringPayment> findAllByUser(User user);
    List<RecurringPayment> findAllByIsActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}
