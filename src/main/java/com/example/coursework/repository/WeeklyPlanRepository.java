package com.example.coursework.repository;
import com.example.coursework.model.WeeklyPlan;
import com.example.coursework.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, Long> {
    Optional<WeeklyPlan> findByUserAndWeekStart(User user, LocalDate weekStart);
}
