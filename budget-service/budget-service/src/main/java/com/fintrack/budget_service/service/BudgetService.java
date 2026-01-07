package com.fintrack.budget_service.service;

import com.fintrack.budget_service.entities.Budget;
import com.fintrack.budget_service.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationService notificationService;  // ADD THIS

    // Create a new budget
    @Transactional
    public Budget createBudget(Long userId, String category, Double amount,
                               String period, LocalDate startDate, String description) {

        // Check if user already has a budget for this category in the current period
        Optional<Budget> existingBudget = budgetRepository
                .findCurrentBudgetForCategory(userId, category, LocalDate.now());

        if (existingBudget.isPresent()) {
            throw new RuntimeException("You already have an active budget for " + category);
        }

        // Calculate end date based on period
        LocalDate endDate = calculateEndDate(startDate, period);

        // Create new budget
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategory(category);
        budget.setAmount(amount);
        budget.setPeriod(period.toUpperCase());
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setDescription(description);
        budget.setStatus("ACTIVE");

        Budget savedBudget = budgetRepository.save(budget);

        // Send notification
        if (notificationService != null) {
            notificationService.sendBudgetCreatedAlert(userId, category, amount, period);
        }

        return savedBudget;
    }

    // Get all budgets for a user
    public List<Budget> getUserBudgets(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    // Get active budgets for a user
    public List<Budget> getActiveBudgets(Long userId) {
        return budgetRepository.findActiveBudgets(userId, LocalDate.now());
    }

    // Get budget by ID (with user validation)
    public Budget getBudgetById(Long budgetId, Long userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to access this budget");
        }

        return budget;
    }

    // Update budget
    @Transactional
    public Budget updateBudget(Long budgetId, Long userId, String category,
                               Double amount, String period, String description) {

        Budget budget = getBudgetById(budgetId, userId);

        // Update fields
        if (category != null) budget.setCategory(category);
        if (amount != null) budget.setAmount(amount);
        if (period != null) {
            budget.setPeriod(period.toUpperCase());
            budget.setEndDate(calculateEndDate(budget.getStartDate(), period));
        }
        if (description != null) budget.setDescription(description);

        // Update status
        updateBudgetStatus(budget);

        return budgetRepository.save(budget);
    }

    // Add spent amount to budget
    @Transactional
    public Budget addSpending(Long userId, String category, Double amount) {
        // Find active budget for this user and category
        Optional<Budget> optionalBudget = budgetRepository
                .findCurrentBudgetForCategory(userId, category, LocalDate.now());

        if (!optionalBudget.isPresent()) {
            // No budget found for this category
            return null;
        }

        Budget budget = optionalBudget.get();
        return processSpending(budget, amount);
    }

    // Helper method
    private Budget processSpending(Budget budget, Double amount) {
        budget.setSpent(budget.getSpent() + amount);

        // Update status if exceeded
        updateBudgetStatus(budget);

        Budget updatedBudget = budgetRepository.save(budget);

        // Send warnings/alerts if notification service exists
        if (notificationService != null) {
            if (updatedBudget.isExceeded()) {
                notificationService.sendBudgetExceededAlert(
                        budget.getUserId(),
                        budget.getCategory(),
                        budget.getAmount(),
                        budget.getSpent(),
                        amount
                );
            } else {
                // Send warning if usage is high (80% or more)
                notificationService.sendBudgetWarning(
                        budget.getUserId(),
                        budget.getCategory(),
                        budget.getAmount(),
                        budget.getSpent()
                );
            }
        }

        return updatedBudget;
    }

    // Delete (deactivate) budget
    @Transactional
    public void deleteBudget(Long budgetId, Long userId) {
        Budget budget = getBudgetById(budgetId, userId);
        budget.setStatus("INACTIVE");
        budgetRepository.save(budget);
    }

    // Get budget summary for user
    public Map<String, Object> getBudgetSummary(Long userId) {
        List<Budget> allBudgets = budgetRepository.findByUserId(userId);
        List<Budget> activeBudgets = budgetRepository.findActiveBudgets(userId, LocalDate.now());
        List<Budget> exceededBudgets = budgetRepository.findExceededBudgets(userId);

        double totalBudget = 0;
        double totalSpent = 0;
        double totalRemaining = 0;
        Map<String, Double> categorySummary = new HashMap<>();
        Map<String, Double> categorySpent = new HashMap<>();

        for (Budget budget : allBudgets) {
            totalBudget += budget.getAmount();
            totalSpent += budget.getSpent();
            totalRemaining += budget.getRemaining();

            // Category summary
            categorySummary.merge(budget.getCategory(), budget.getAmount(), Double::sum);
            categorySpent.merge(budget.getCategory(), budget.getSpent(), Double::sum);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", userId);
        summary.put("totalBudgets", allBudgets.size());
        summary.put("activeBudgets", activeBudgets.size());
        summary.put("exceededBudgets", exceededBudgets.size());
        summary.put("totalBudgetAmount", totalBudget);
        summary.put("totalSpent", totalSpent);
        summary.put("totalRemaining", totalRemaining);
        summary.put("overallUsagePercentage", totalBudget > 0 ? (totalSpent / totalBudget) * 100 : 0);
        summary.put("categoryBudgets", categorySummary);
        summary.put("categorySpending", categorySpent);
        summary.put("date", LocalDate.now().toString());

        // Add warnings
        List<Map<String, Object>> warnings = new ArrayList<>();
        for (Budget budget : activeBudgets) {
            Double usage = budget.getUsagePercentage();
            if (usage >= 80) {
                Map<String, Object> warning = new HashMap<>();
                warning.put("category", budget.getCategory());
                warning.put("budgetId", budget.getId());
                warning.put("budgetAmount", budget.getAmount());
                warning.put("spentAmount", budget.getSpent());
                warning.put("remaining", budget.getRemaining());
                warning.put("usagePercentage", Math.round(usage * 100.0) / 100.0);
                warning.put("status", usage >= 100 ? "EXCEEDED" : "WARNING");
                warnings.add(warning);
            }
        }
        summary.put("warnings", warnings);

        return summary;
    }

    // Get budgets by category
    public List<Budget> getBudgetsByCategory(Long userId, String category) {
        return budgetRepository.findByUserIdAndCategory(userId, category);
    }

    // ADD THIS METHOD: Check transaction against budget
    public Map<String, Object> checkTransactionAgainstBudget(Long userId, String category, Double amount) {
        Optional<Budget> optionalBudget = budgetRepository
                .findCurrentBudgetForCategory(userId, category, LocalDate.now());

        Map<String, Object> result = new HashMap<>();
        result.put("hasBudget", false);
        result.put("userId", userId);
        result.put("category", category);
        result.put("transactionAmount", amount);

        if (!optionalBudget.isPresent()) {
            result.put("message", "No budget found for category: " + category);
            return result;
        }

        Budget budget = optionalBudget.get();
        Double newSpent = budget.getSpent() + amount;
        Double remaining = budget.getRemaining();
        Double remainingAfterTransaction = remaining - amount;

        result.put("hasBudget", true);
        result.put("budgetId", budget.getId());
        result.put("budgetAmount", budget.getAmount());
        result.put("currentSpent", budget.getSpent());
        result.put("currentRemaining", remaining);
        result.put("remainingAfterTransaction", remainingAfterTransaction);
        result.put("willExceed", newSpent > budget.getAmount());
        result.put("exceedAmount", Math.max(0, newSpent - budget.getAmount()));
        result.put("usagePercentageBefore", budget.getUsagePercentage());
        result.put("usagePercentageAfter", (newSpent / budget.getAmount()) * 100);
        result.put("budgetStatus", budget.getStatus());

        // Add warning levels
        if (remainingAfterTransaction < 0) {
            result.put("alertLevel", "CRITICAL");
            result.put("message", "This transaction will exceed your budget by $" +
                    Math.abs(remainingAfterTransaction));
        } else if (remainingAfterTransaction < (budget.getAmount() * 0.2)) {
            result.put("alertLevel", "WARNING");
            result.put("message", "This transaction will leave less than 20% of your budget");
        } else if (remainingAfterTransaction < (budget.getAmount() * 0.5)) {
            result.put("alertLevel", "INFO");
            result.put("message", "This transaction will use a significant portion of your budget");
        } else {
            result.put("alertLevel", "SAFE");
            result.put("message", "Budget has sufficient funds for this transaction");
        }

        return result;
    }

    // Helper method to calculate end date
    private LocalDate calculateEndDate(LocalDate startDate, String period) {
        switch (period.toUpperCase()) {
            case "WEEKLY":
                return startDate.plusWeeks(1);
            case "MONTHLY":
                return startDate.plusMonths(1);
            case "YEARLY":
                return startDate.plusYears(1);
            default:
                return startDate.plusMonths(1); // Default to monthly
        }
    }

    // Helper method to update budget status
    private void updateBudgetStatus(Budget budget) {
        if (budget.getSpent() > budget.getAmount()) {
            budget.setStatus("EXCEEDED");
        } else if (LocalDate.now().isAfter(budget.getEndDate())) {
            budget.setStatus("COMPLETED");
        } else {
            budget.setStatus("ACTIVE");
        }
    }
}