package com.fintrack.transaction_service.service;

import com.fintrack.transaction_service.client.AuthServiceClient;
import com.fintrack.transaction_service.client.BudgetServiceClient;
import com.fintrack.transaction_service.entities.*;
import com.fintrack.transaction_service.entities.transaction_service.entities.ValidationRequest;
import com.fintrack.transaction_service.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private BudgetServiceClient budgetServiceClient;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, String authToken) {
        // 1. Validate token with auth service
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setToken(authToken);

        ValidationResponse validationResponse = authServiceClient.validateToken(validationRequest);

        if (!validationResponse.getValid()) {
            throw new RuntimeException("Invalid or expired token: " + validationResponse.getError());
        }

        // 2. Check transaction against budget BEFORE creating (with fallback handling)
        Map<String, Object> budgetCheck = checkBudgetBeforeTransaction(request);
        boolean hasBudget = (boolean) budgetCheck.getOrDefault("hasBudget", false);
        boolean willExceed = hasBudget && (boolean) budgetCheck.getOrDefault("willExceed", false);
        String budgetMessage = (String) budgetCheck.getOrDefault("message", "Budget check not available");
        boolean isFallback = (boolean) budgetCheck.getOrDefault("fallback", false);

        // 3. Create transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(request.getCategory());
        transaction.setUserId(request.getUserId());
        transaction.setStatus("COMPLETED");
        transaction.setDate(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 4. Update budget with spending (if budget exists and not fallback)
        if (hasBudget && !isFallback) {
            updateBudgetAfterTransaction(request);
        }

        // 5. Prepare response with budget info
        TransactionResponse response = new TransactionResponse();
        response.setId(savedTransaction.getId());
        response.setAmount(savedTransaction.getAmount());
        response.setDescription(savedTransaction.getDescription());
        response.setCategory(savedTransaction.getCategory());
        response.setDate(savedTransaction.getDate());
        response.setUserId(savedTransaction.getUserId());
        response.setStatus(savedTransaction.getStatus());
        response.setMessage("Transaction created successfully" +
                (isFallback ? " (budget service unavailable)" : ""));

        // Add budget information to response
        Map<String, Object> budgetInfo = new HashMap<>();
        budgetInfo.put("hasBudget", hasBudget);
        budgetInfo.put("budgetCheck", budgetCheck);
        budgetInfo.put("budgetMessage", budgetMessage);
        budgetInfo.put("willExceedBudget", willExceed);
        budgetInfo.put("isFallback", isFallback);
        response.setBudgetInfo(budgetInfo);

        return response;
    }

    // Check budget before making transaction
    private Map<String, Object> checkBudgetBeforeTransaction(TransactionRequest request) {
        try {
            Map<String, Object> checkRequest = new HashMap<>();
            checkRequest.put("userId", request.getUserId());
            checkRequest.put("category", request.getCategory());
            checkRequest.put("amount", request.getAmount());

            return budgetServiceClient.checkTransaction(checkRequest);

        } catch (Exception e) {
            // If budget service is down, return fallback response
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("hasBudget", false);
            fallback.put("fallback", true);
            fallback.put("message", "Budget service unavailable - proceeding without budget check");
            fallback.put("error", e.getMessage());
            fallback.put("userId", request.getUserId());
            fallback.put("category", request.getCategory());
            fallback.put("transactionAmount", request.getAmount());
            fallback.put("alertLevel", "UNKNOWN");
            return fallback;
        }
    }

    // Update budget after transaction
    private void updateBudgetAfterTransaction(TransactionRequest request) {
        try {
            Map<String, Object> spendingRequest = new HashMap<>();
            spendingRequest.put("userId", request.getUserId());
            spendingRequest.put("category", request.getCategory());
            spendingRequest.put("amount", request.getAmount());

            Map<String, Object> result = budgetServiceClient.addSpending(spendingRequest);

            boolean success = (boolean) result.getOrDefault("success", false);
            if (!success) {
                System.err.println("Failed to update budget: " + result.get("message"));
            }

        } catch (Exception e) {
            System.err.println("Error updating budget: " + e.getMessage());
            // Don't fail the transaction if budget update fails
        }
    }

    public List<Transaction> getTransactionsByUser(Long userId, String authToken) {
        // Validate token first
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setToken(authToken);

        ValidationResponse validationResponse = authServiceClient.validateToken(validationRequest);

        if (!validationResponse.getValid()) {
            throw new RuntimeException("Invalid or expired token");
        }

        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByUserAndCategory(Long userId, String category, String authToken) {
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setToken(authToken);

        ValidationResponse validationResponse = authServiceClient.validateToken(validationRequest);

        if (!validationResponse.getValid()) {
            throw new RuntimeException("Invalid or expired token");
        }

        return transactionRepository.findByUserIdAndCategory(userId, category);
    }

    public Transaction getTransactionById(Long id, String authToken) {
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setToken(authToken);

        ValidationResponse validationResponse = authServiceClient.validateToken(validationRequest);

        if (!validationResponse.getValid()) {
            throw new RuntimeException("Invalid or expired token");
        }

        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
    }

    public Double getUserBalance(Long userId, String authToken) {
        ValidationRequest validationRequest = new ValidationRequest();
        validationRequest.setToken(authToken);

        ValidationResponse validationResponse = authServiceClient.validateToken(validationRequest);

        if (!validationResponse.getValid()) {
            throw new RuntimeException("Invalid or expired token");
        }

        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        return transactions.stream()
                .mapToDouble(t -> {
                    if ("INCOME".equals(t.getCategory())) {
                        return t.getAmount();
                    } else {
                        return -t.getAmount();
                    }
                })
                .sum();
    }
}