package com.example.coursework.repository;
import com.example.coursework.model.PointTransaction;
import com.example.coursework.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByUser(User user);
    List<PointTransaction> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    Integer countByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}