package com.fintrack.transaction_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "budget-service",
        fallback = BudgetServiceFallback.class
)
@Primary  // ADD THIS
public interface BudgetServiceClient {

    // Add spending to budget
    @PostMapping("/api/budgets/add-spending")
    Map<String, Object> addSpending(@RequestBody Map<String, Object> request);

    // Check transaction against budget
    @PostMapping("/api/budgets/check-transaction")
    Map<String, Object> checkTransaction(@RequestBody Map<String, Object> request);
}