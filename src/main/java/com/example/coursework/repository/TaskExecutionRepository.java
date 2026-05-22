package com.example.coursework.repository;

import com.example.coursework.model.TaskExecution;
import com.example.coursework.model.User;
import com.example.coursework.model.Chore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    @Query("SELECT te FROM TaskExecution te WHERE te.user.id = :userId AND te.status = 'ACTIVE'")
    TaskExecution findActiveExecution(@Param("userId") Long userId);

    @Query("SELECT te FROM TaskExecution te WHERE te.user.id = :userId AND te.startTime BETWEEN :start AND :end")
    List<TaskExecution> findByUserAndStartTimeBetween(@Param("userId") Long userId,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    List<TaskExecution> findByUser(User user);

    @Query("SELECT te FROM TaskExecution te WHERE te.chore.id = :choreId AND te.user.id = :userId AND te.status = 'COMPLETED' ORDER BY te.endTime DESC")
    List<TaskExecution> findLastNExecutions(@Param("choreId") Long choreId,
                                            @Param("userId") Long userId,
                                            Pageable pageable);

    @Query("SELECT te FROM TaskExecution te WHERE te.chore.id = :choreId AND te.status = 'COMPLETED' ORDER BY te.endTime DESC")
    List<TaskExecution> findLastNExecutionsForChore(@Param("choreId") Long choreId, Pageable pageable);

    @Query("SELECT te FROM TaskExecution te WHERE te.chore.id = :choreId ORDER BY te.createdAt DESC")
    List<TaskExecution> findTopByChoreIdOrderByCreatedAtDesc(@Param("choreId") Long choreId, Pageable pageable);

    @Query("SELECT te FROM TaskExecution te WHERE te.user = :user AND te.chore = :chore AND te.status = 'COMPLETED' ORDER BY te.endTime DESC")
    List<TaskExecution> findLastNCompletionsByUserAndChore(@Param("user") User user,
                                                           @Param("chore") Chore chore,
                                                           Pageable pageable);
}