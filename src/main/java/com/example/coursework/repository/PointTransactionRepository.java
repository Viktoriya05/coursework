package com.example.coursework.repository;

import com.example.coursework.model.PointTransaction;
import com.example.coursework.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByUser(User user);

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.createdAt BETWEEN :start AND :end ORDER BY pt.createdAt DESC")
    List<PointTransaction> findByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(pt.points) FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.createdAt BETWEEN :start AND :end")
    Integer sumPointsByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(pt) FROM PointTransaction pt WHERE pt.user.id = :userId AND pt.createdAt BETWEEN :start AND :end")
    Long countByUserAndDateRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
