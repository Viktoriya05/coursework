package com.example.coursework.repository;

import com.example.coursework.model.PlanItem;
import com.example.coursework.model.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {
    List<PlanItem> findByPlan(WeeklyPlan plan);
    void deleteByPlan(WeeklyPlan plan);
}
