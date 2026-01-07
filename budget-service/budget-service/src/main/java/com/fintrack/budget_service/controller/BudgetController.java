package com.fintrack.budget_service.controller;

import com.fintrack.budget_service.entities.Budget;
import com.fintrack.budget_service.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    // Health check
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "budget-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // SIMPLIFIED: Create budget - userId comes from request body
    @PostMapping
    public ResponseEntity<?> createBudget(@RequestBody Map<String, Object> request) {
        try {
            // Extract userId from request body (REQUIRED)
            Long userId = null;
            if (request.containsKey("userId")) {
                userId = Long.valueOf(request.get("userId").toString());
            }

            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId is required in request body"));
            }

            // Extract request data
            String category = (String) request.get("category");
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Category is required"));
            }

            Double amount = null;
            if (request.containsKey("amount")) {
                try {
                    amount = Double.valueOf(request.get("amount").toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Amount must be a valid number"));
                }
            }
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid amount greater than 0 is required"));
            }

            String period = (String) request.get("period");
            if (period == null || period.trim().isEmpty()) {
                period = "MONTHLY"; // Default value
            }

            String description = (String) request.get("description");

            // Parse start date or use current date
            LocalDate startDate = LocalDate.now();
            if (request.containsKey("startDate")) {
                try {
                    startDate = LocalDate.parse((String) request.get("startDate"));
                } catch (Exception e) {
                    // If parsing fails, use today
                }
            }

            // Create budget
            Budget budget = budgetService.createBudget(
                    userId, category, amount, period, startDate, description
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Budget created successfully");
            response.put("budgetId", budget.getId());
            response.put("budget", budget);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    // SIMPLIFIED: Get all budgets for user - userId comes from request param
    @GetMapping
    public ResponseEntity<?> getUserBudgets(@RequestParam Long userId) {
        try {
            List<Budget> budgets = budgetService.getUserBudgets(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SIMPLIFIED: Get active budgets - userId from request param
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBudgets(@RequestParam Long userId) {
        try {
            List<Budget> budgets = budgetService.getActiveBudgets(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SIMPLIFIED: Get budget by ID - userId from request param
    @GetMapping("/{id}")
    public ResponseEntity<?> getBudgetById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            Budget budget = budgetService.getBudgetById(id, userId);
            return ResponseEntity.ok(budget);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SIMPLIFIED: Update budget
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {
            // Get userId from request body
            Long userId = null;
            if (request.containsKey("userId")) {
                userId = Long.valueOf(request.get("userId").toString());
            }

            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "userId is required"));
            }

            String category = (String) request.get("category");
            Double amount = request.containsKey("amount")
                    ? Double.valueOf(request.get("amount").toString())
                    : null;
            String period = (String) request.get("period");
            String description = (String) request.get("description");

            Budget budget = budgetService.updateBudget(id, userId, category, amount, period, description);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Budget updated successfully");
            response.put("budget", budget);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SIMPLIFIED: Delete budget
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            budgetService.deleteBudget(id, userId);
            return ResponseEntity.ok(Map.of("message", "Budget deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SIMPLIFIED: Get budget summary
    @GetMapping("/summary")
    public ResponseEntity<?> getBudgetSummary(@RequestParam Long userId) {
        try {
            Map<String, Object> summary = budgetService.getBudgetSummary(userId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // SIMPLIFIED: Get budgets by category
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getBudgetsByCategory(
            @PathVariable String category,
            @RequestParam Long userId) {

        try {
            List<Budget> budgets = budgetService.getBudgetsByCategory(userId, category);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add-spending")
    public ResponseEntity<?> addSpendingFromTransaction(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String category = (String) request.get("category");
            Double amount = Double.valueOf(request.get("amount").toString());

            Budget updatedBudget = budgetService.addSpending(userId, category, amount);

            Map<String, Object> response = new HashMap<>();
            if (updatedBudget != null) {
                response.put("success", true);
                response.put("budgetId", updatedBudget.getId());
                response.put("category", updatedBudget.getCategory());
                response.put("newSpent", updatedBudget.getSpent());
                response.put("remaining", updatedBudget.getRemaining());
                response.put("status", updatedBudget.getStatus());
                response.put("isExceeded", updatedBudget.isExceeded());
            } else {
                response.put("success", false);
                response.put("message", "No active budget found for this category");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint for transaction service to check transaction
    @PostMapping("/check-transaction")
    public ResponseEntity<?> checkTransactionFromService(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String category = (String) request.get("category");
            Double amount = Double.valueOf(request.get("amount").toString());

            Map<String, Object> checkResult = budgetService.checkTransactionAgainstBudget(
                    userId, category, amount);

            return ResponseEntity.ok(checkResult);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}