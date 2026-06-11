package com.finsight.repository;

import com.finsight.entity.Category;
import com.finsight.entity.Transaction;
import com.finsight.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Transaction> findFiltered(
            @Param("user") User user,
            @Param("type") String type,
            @Param("category") Category category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable);

    List<Transaction> findAllByUserAndTransactionDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    BigDecimal sumAmountByUserAndType(@Param("user") User user, @Param("type") String type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = 'EXPENSE' " +
           "AND t.category = :category AND t.transactionDate >= :start AND t.transactionDate <= :end")
    BigDecimal sumExpenseByCategoryAndDateBetween(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type " +
           "AND t.transactionDate >= :start AND t.transactionDate <= :end GROUP BY t.category.name")
    List<Object[]> sumAmountByCategoryBetween(
            @Param("user") User user,
            @Param("type") String type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
