package com.example.coursework.repository;

import com.example.coursework.model.WeeklyPlan;
import com.example.coursework.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, Long> {
    Optional<WeeklyPlan> findByUserAndWeekStart(User user, LocalDate weekStart);

    @Query("SELECT wp FROM WeeklyPlan wp WHERE wp.user.id = :userId AND wp.weekStart <= :date AND wp.weekEnd >= :date")
    Optional<WeeklyPlan> findPlanContainingDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
