package com.example.coursework.repository;
import com.example.coursework.model.Chore;
import com.example.coursework.model.User;
import com.example.coursework.model.ChoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {
    List<Chore> findByUser(User user);
    List<Chore> findByUserAndStatus(User user, ChoreStatus status);
    List<Chore> findByAssignedBy(User parent);

    @Query("SELECT c FROM Chore c WHERE c.user.id = :userId AND c.status = :status AND c.dueDate <= :dueDate")
    List<Chore> findOverdueTasks(@Param("userId") Long userId, @Param("status") ChoreStatus status, @Param("dueDate") LocalDateTime dueDate);

    @Query("SELECT AVG(t.durationSeconds) FROM TaskExecution t WHERE t.chore.id = :choreId AND t.status = 'COMPLETED'")
    Double getAverageExecutionTime(@Param("choreId") Long choreId);
}
