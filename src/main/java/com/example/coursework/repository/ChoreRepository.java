package com.example.coursework.repository;

import com.example.coursework.model.Chore;
import com.example.coursework.model.User;
import com.example.coursework.model.ChoreStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<Chore> findByUserIsNull();
    List<Chore> findByCategoryName(String categoryName);
    Page<Chore> findByUser(User user, Pageable pageable);
    List<Chore> findByAssignedByAndStatus(User parent, ChoreStatus status);

    @Query("SELECT c FROM Chore c WHERE c.user.id = :userId AND c.status = :status AND c.dueDate <= CURRENT_DATE")
    List<Chore> findOverdueTasks(@Param("userId") Long userId, @Param("status") ChoreStatus status);

    @Query("SELECT c FROM Chore c WHERE c.assignedBy.id = :parentId AND c.status = 'NEEDS_REVIEW' ORDER BY c.completedAt DESC")
    List<Chore> findPendingReviewByParent(@Param("parentId") Long parentId);

    @Query("SELECT AVG(t.durationSeconds) FROM TaskExecution t WHERE t.chore.id = :choreId AND t.status = 'COMPLETED'")
    Double getAverageExecutionTime(@Param("choreId") Long choreId);

    @Query("SELECT COUNT(c) FROM Chore c WHERE c.user.id = :userId AND c.status = 'COMPLETED'")
    Long countCompletedByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(c.points) FROM Chore c WHERE c.user.id = :userId AND c.status = 'COMPLETED'")
    Integer sumPointsByUser(@Param("userId") Long userId);
}
