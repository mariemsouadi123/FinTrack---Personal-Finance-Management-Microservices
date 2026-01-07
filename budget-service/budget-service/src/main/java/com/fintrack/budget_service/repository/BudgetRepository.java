package com.fintrack.budget_service.repository;

import com.fintrack.budget_service.entities.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Find budgets by user ID
    List<Budget> findByUserId(Long userId);

    // Find budgets by user ID and category
    List<Budget> findByUserIdAndCategory(Long userId, String category);

    // Find active budgets for a user
    List<Budget> findByUserIdAndStatus(Long userId, String status);

    // Find active budgets within date range
    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.startDate <= :currentDate AND b.endDate >= :currentDate AND b.status = 'ACTIVE'")
    List<Budget> findActiveBudgets(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);

    // Find budget for specific category and period
    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.category = :category AND b.startDate <= :date AND b.endDate >= :date")
    Optional<Budget> findCurrentBudgetForCategory(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("date") LocalDate date);

    // Find exceeded budgets
    @Query("SELECT b FROM Budget b WHERE b.userId = :userId AND b.spent > b.amount")
    List<Budget> findExceededBudgets(@Param("userId") Long userId);
}