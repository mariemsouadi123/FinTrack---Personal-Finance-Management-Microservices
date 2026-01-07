package com.fintrack.transaction_service.client;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BudgetServiceFallback implements BudgetServiceClient {

    @Override
    public Map<String, Object> addSpending(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Budget service unavailable - fallback activated");
        response.put("fallback", true);
        response.put("userId", request.get("userId"));
        response.put("category", request.get("category"));
        response.put("amount", request.get("amount"));
        return response;
    }

    @Override
    public Map<String, Object> checkTransaction(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("hasBudget", false);
        response.put("message", "Budget service unavailable - cannot check budget");
        response.put("fallback", true);
        response.put("userId", request.get("userId"));
        response.put("category", request.get("category"));
        response.put("transactionAmount", request.get("amount"));
        response.put("alertLevel", "UNKNOWN");
        return response;
    }
}