package com.fintrack.transaction_service.controller;

import com.fintrack.transaction_service.entities.*;
import com.fintrack.transaction_service.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Health check
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "transaction-service"
        ));
    }

    // Create transaction
    @PostMapping
    public ResponseEntity<?> createTransaction(
            @RequestBody TransactionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token from "Bearer <token>"
            String token = extractToken(authHeader);

            TransactionResponse response = transactionService.createTransaction(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    // Get all transactions for user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTransactionsByUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<Transaction> transactions = transactionService.getTransactionsByUser(userId, token);
            return ResponseEntity.ok(transactions);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get transactions by user and category
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<?> getTransactionsByCategory(
            @PathVariable Long userId,
            @PathVariable String category,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<Transaction> transactions = transactionService.getTransactionsByUserAndCategory(userId, category, token);
            return ResponseEntity.ok(transactions);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Transaction transaction = transactionService.getTransactionById(id, token);
            return ResponseEntity.ok(transaction);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get user balance
    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<?> getUserBalance(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Double balance = transactionService.getUserBalance(userId, token);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("balance", balance);
            response.put("currency", "TND");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Get transaction summary
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getTransactionSummary(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<Transaction> transactions = transactionService.getTransactionsByUser(userId, token);

            Map<String, Double> summary = new HashMap<>();
            for (Transaction t : transactions) {
                summary.merge(t.getCategory(), t.getAmount(), Double::sum);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalTransactions", transactions.size());
            response.put("categorySummary", summary);
            response.put("balance", transactions.stream()
                    .mapToDouble(t -> "INCOME".equals(t.getCategory()) ? t.getAmount() : -t.getAmount())
                    .sum());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Helper method to extract token from Authorization header
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header format");
        }
        return authHeader.substring(7);
    }
}
