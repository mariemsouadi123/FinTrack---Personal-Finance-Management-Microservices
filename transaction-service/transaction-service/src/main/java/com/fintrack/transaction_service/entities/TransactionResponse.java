package com.fintrack.transaction_service.entities;

import java.time.LocalDateTime;
import java.util.Map;

public class TransactionResponse {

    private Long id;
    private Double amount;
    private String description;
    private String category;
    private LocalDateTime date;
    private Long userId;
    private String status;
    private String message;
    private Map<String, Object> budgetInfo;  // NEW FIELD


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getBudgetInfo() {
        return budgetInfo;
    }

    public void setBudgetInfo(Map<String, Object> budgetInfo) {
        this.budgetInfo = budgetInfo;
    }
}
