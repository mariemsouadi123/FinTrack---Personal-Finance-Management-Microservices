package com.fintrack.transaction_service.repositories;


import com.fintrack.transaction_service.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByUserIdAndCategory(Long userId, String category);
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
