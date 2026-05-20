package com.example.coursework.repository;

import com.example.coursework.model.PlanItem;
import com.example.coursework.model.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {
    List<PlanItem> findByPlan(WeeklyPlan plan);
    void deleteByPlan(WeeklyPlan plan);

    @Query("SELECT pi FROM PlanItem pi WHERE pi.plan.user.id = :userId AND pi.scheduledDate = :date ORDER BY pi.orderNumber")
    List<PlanItem> findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
