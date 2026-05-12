package com.example.coursework.service;

import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final WeeklyPlanRepository planRepository;
    private final PlanItemRepository planItemRepository;
    private final ChoreRepository choreRepository;
    private final UserRepository userRepository;
    private final TaskExecutionRepository executionRepository;

    @Transactional
    public WeeklyPlan createWeeklyPlan(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WeeklyPlan existingPlan = planRepository.findByUserAndWeekStart(user, weekStart).orElse(null);
        if (existingPlan != null) {
            return existingPlan;
        }

        WeeklyPlan plan = new WeeklyPlan();
        plan.setUser(user);
        plan.setWeekStart(weekStart);
        plan.setWeekEnd(weekStart.plusDays(6));

        return planRepository.save(plan);
    }

    @Transactional
    public PlanItem addChoreToPlan(Long planId, Long choreId, LocalDate scheduledDate) {
        WeeklyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));

        PlanItem item = new PlanItem();
        item.setPlan(plan);
        item.setChore(chore);
        item.setScheduledDate(scheduledDate);

        Integer estimatedMinutes = calculateEstimatedTime(chore.getId(), chore.getUser().getId());
        item.setEstimatedMinutes(estimatedMinutes != null ? estimatedMinutes : 30);

        item.setOrderNumber(plan.getItems().size());
        item.setCompleted(false);

        return planItemRepository.save(item);
    }

    private Integer calculateEstimatedTime(Long choreId, Long userId) {
        // Get last 5 executions average
        Double avgTime = executionRepository.getAverageLastNExecutions(choreId, userId, 5);
        if (avgTime != null && avgTime > 0) {
            return (int) (avgTime / 60); // Convert seconds to minutes
        }

        // If no data for this user, try family average
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getFamily() != null) {
            List<User> familyMembers = userRepository.findByFamily(user.getFamily());
            List<Double> familyAverages = new ArrayList<>();
            for (User member : familyMembers) {
                Double memberAvg = executionRepository.getAverageLastNExecutions(choreId, member.getId(), 5);
                if (memberAvg != null) {
                    familyAverages.add(memberAvg);
                }
            }
            if (!familyAverages.isEmpty()) {
                double familyAvg = familyAverages.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                return (int) (familyAvg / 60);
            }
        }

        return null; // Not enough data
    }

    public WeeklyPlan getWeeklyPlan(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return planRepository.findByUserAndWeekStart(user, weekStart).orElse(null);
    }

    public Map<String, Object> getWeeklySummary(Long userId, LocalDate weekStart) {
        WeeklyPlan plan = getWeeklyPlan(userId, weekStart);
        Map<String, Object> summary = new HashMap<>();

        if (plan != null) {
            int totalEstimatedMinutes = plan.getItems().stream()
                    .mapToInt(item -> item.getEstimatedMinutes() != null ? item.getEstimatedMinutes() : 0)
                    .sum();
            long completedCount = plan.getItems().stream().filter(PlanItem::getCompleted).count();

            summary.put("totalChores", plan.getItems().size());
            summary.put("completedChores", completedCount);
            summary.put("totalEstimatedMinutes", totalEstimatedMinutes);
            summary.put("totalHours", totalEstimatedMinutes / 60);
            summary.put("completionRate", plan.getItems().isEmpty() ? 0 : (completedCount * 100 / plan.getItems().size()));
        } else {
            summary.put("hasPlan", false);
            summary.put("message", "No plan created for this week");
        }

        return summary;
    }

    @Transactional
    public void markPlanItemCompleted(Long itemId) {
        PlanItem item = planItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Plan item not found"));
        item.setCompleted(true);
        planItemRepository.save(item);
    }
}