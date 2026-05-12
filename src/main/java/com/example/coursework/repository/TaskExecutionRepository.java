package com.example.coursework.repository;
import com.example.coursework.model.TaskExecution;
import com.example.coursework.model.User;
import com.example.coursework.model.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
    List<TaskExecution> findByUser(User user);
    List<TaskExecution> findByChore(Chore chore);
    List<TaskExecution> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM TaskExecution t WHERE t.user.id = :userId AND t.status = 'ACTIVE'")
    TaskExecution findActiveExecution(@Param("userId") Long userId);

    @Query("SELECT AVG(t.durationSeconds) FROM TaskExecution t WHERE t.chore.id = :choreId AND t.user.id = :userId AND t.status = 'COMPLETED' ORDER BY t.createdAt DESC LIMIT :limit")
    Double getAverageLastNExecutions(@Param("choreId") Long choreId, @Param("userId") Long userId, @Param("limit") int limit);
}