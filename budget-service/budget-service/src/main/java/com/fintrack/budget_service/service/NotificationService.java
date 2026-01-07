package com.fintrack.budget_service.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    public void sendBudgetExceededAlert(Long userId, String category,
                                        Double budgetAmount, Double spentAmount,
                                        Double transactionAmount) {

        Map<String, Object> alert = new HashMap<>();
        alert.put("userId", userId);
        alert.put("type", "BUDGET_EXCEEDED");
        alert.put("category", category);
        alert.put("budgetAmount", budgetAmount);
        alert.put("spentAmount", spentAmount);
        alert.put("transactionAmount", transactionAmount);
        alert.put("exceededBy", spentAmount - budgetAmount);
        alert.put("timestamp", System.currentTimeMillis());

        System.out.println(" BUDGET EXCEEDED ALERT!");
        System.out.println("User ID: " + userId);
        System.out.println("Category: " + category);
        System.out.println("Budget: $" + budgetAmount);
        System.out.println("Spent: $" + spentAmount);
        System.out.println("Exceeded by: $" + (spentAmount - budgetAmount));
    }

    public void sendBudgetWarning(Long userId, String category,
                                  Double budgetAmount, Double spentAmount) {

        Double usagePercentage = (spentAmount / budgetAmount) * 100;

        if (usagePercentage >= 80) {
            Map<String, Object> warning = new HashMap<>();
            warning.put("userId", userId);
            warning.put("type", "BUDGET_WARNING");
            warning.put("category", category);
            warning.put("budgetAmount", budgetAmount);
            warning.put("spentAmount", spentAmount);
            warning.put("usagePercentage", Math.round(usagePercentage * 100.0) / 100.0);
            warning.put("timestamp", System.currentTimeMillis());

            System.out.println(" BUDGET WARNING!");
            System.out.println("User ID: " + userId);
            System.out.println("Category: " + category);
            System.out.println("Budget: $" + budgetAmount);
            System.out.println("Spent: $" + spentAmount);
            System.out.println("Usage: " + Math.round(usagePercentage * 100.0) / 100.0 + "%");
        }
    }

    public void sendBudgetCreatedAlert(Long userId, String category,
                                       Double amount, String period) {

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("type", "BUDGET_CREATED");
        notification.put("category", category);
        notification.put("amount", amount);
        notification.put("period", period);
        notification.put("timestamp", System.currentTimeMillis());

        System.out.println("BUDGET CREATED!");
        System.out.println("User ID: " + userId);
        System.out.println("Category: " + category);
        System.out.println("Amount: $" + amount);
        System.out.println("Period: " + period);
    }
}