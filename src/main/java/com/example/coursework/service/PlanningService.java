package com.example.coursework.service;

import com.example.coursework.exception.BusinessLogicException;
import com.example.coursework.exception.ResourceNotFoundException;
import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
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
    public PlanItem addChoreToPlan(Long planId, Long choreId, LocalDate scheduledDate) {
        WeeklyPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));

        boolean alreadyExists = plan.getItems().stream()
                .anyMatch(item -> item.getChore().getId().equals(choreId) &&
                        item.getScheduledDate().equals(scheduledDate));
        if (alreadyExists) {
            throw new RuntimeException("This task is already planned for this day");
        }

        if (chore.getUser() == null || !chore.getUser().getId().equals(plan.getUser().getId())) {
            Chore userChore = new Chore();
            userChore.setName(chore.getName());
            userChore.setDescription(chore.getDescription());
            userChore.setPoints(chore.getPoints());
            userChore.setCategory(chore.getCategory());
            userChore.setUser(plan.getUser());
            userChore.setStatus(ChoreStatus.PENDING);
            userChore.setDueDate(scheduledDate);
            chore = choreRepository.save(userChore);
        } else {
            chore.setDueDate(scheduledDate);
            chore = choreRepository.save(chore);
        }

        PlanItem item = new PlanItem();
        item.setPlan(plan);
        item.setChore(chore);
        item.setScheduledDate(scheduledDate);

        Integer estimatedMinutes = calculateEstimatedTime(chore.getId(), plan.getUser().getId());
        item.setEstimatedMinutes(estimatedMinutes != null ? estimatedMinutes : 30);

        item.setOrderNumber(plan.getItems().size());
        item.setCompleted(false);

        return planItemRepository.save(item);
    }

    @Transactional
    public WeeklyPlan createWeeklyPlan(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<WeeklyPlan> existingPlan = planRepository.findByUserAndWeekStart(user, weekStart);
        if (existingPlan.isPresent()) {
            return existingPlan.get();
        }

        WeeklyPlan plan = new WeeklyPlan();
        plan.setUser(user);
        plan.setWeekStart(weekStart);
        plan.setWeekEnd(weekStart.plusDays(6));

        return planRepository.save(plan);
    }

    private Integer calculateEstimatedTime(Long choreId, Long userId) {
        List<TaskExecution> userExecutions = executionRepository.findLastNExecutions(
                choreId, userId, PageRequest.of(0, 5));

        if (!userExecutions.isEmpty()) {
            double avg = userExecutions.stream()
                    .filter(e -> e.getDurationSeconds() != null)
                    .mapToInt(TaskExecution::getDurationSeconds)
                    .average().orElse(0);
            if (avg > 0) return (int) (avg / 60);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getFamily() != null) {
            List<User> familyMembers = userRepository.findByFamily(user.getFamily());
            List<Integer> familyAverages = new ArrayList<>();

            for (User member : familyMembers) {
                List<TaskExecution> memberExecutions = executionRepository.findLastNExecutions(
                        choreId, member.getId(), PageRequest.of(0, 5));
                if (!memberExecutions.isEmpty()) {
                    double avg = memberExecutions.stream()
                            .filter(e -> e.getDurationSeconds() != null)
                            .mapToInt(TaskExecution::getDurationSeconds)
                            .average().orElse(0);
                    if (avg > 0) familyAverages.add((int) (avg / 60));
                }
            }

            if (!familyAverages.isEmpty()) {
                return (int) familyAverages.stream().mapToInt(Integer::intValue).average().orElse(0);
            }
        }

        return null;
    }

    public WeeklyPlan getWeeklyPlan(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return planRepository.findByUserAndWeekStart(user, weekStart).orElse(null);
    }

    public Map<String, Object> getWeeklySummary(Long userId, LocalDate weekStart) {
        WeeklyPlan plan = getWeeklyPlan(userId, weekStart);
        Map<String, Object> summary = new LinkedHashMap<>();

        if (plan != null) {
            int totalEstimatedMinutes = plan.getItems().stream()
                    .mapToInt(item -> item.getEstimatedMinutes() != null ? item.getEstimatedMinutes() : 0)
                    .sum();
            long completedCount = plan.getItems().stream().filter(PlanItem::getCompleted).count();

            summary.put("hasPlan", true);
            summary.put("totalChores", plan.getItems().size());
            summary.put("completedChores", completedCount);
            summary.put("totalEstimatedMinutes", totalEstimatedMinutes);
            summary.put("totalHours", totalEstimatedMinutes / 60);
            summary.put("completionRate", plan.getItems().isEmpty() ? 0 : (int)(completedCount * 100 / plan.getItems().size()));
        } else {
            summary.put("hasPlan", false);
            summary.put("message", "No plan created for this week");
        }

        return summary;
    }

    @Transactional
    public void markPlanItemCompleted(Long itemId) {
        PlanItem item = planItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("PlanItem", "id", itemId));
        item.setCompleted(true);
        planItemRepository.save(item);
    }

    @Transactional
    public void syncTaskCompletion(Long choreId, boolean completed) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));

        // Обновляем статус задачи
        if (completed) {
            if (chore.getStatus() != ChoreStatus.COMPLETED) {
                chore.setStatus(ChoreStatus.COMPLETED);
                chore.setCompletedAt(LocalDateTime.now());
                choreRepository.save(chore);
            }
        } else {
            if (chore.getStatus() != ChoreStatus.PENDING) {
                chore.setStatus(ChoreStatus.PENDING);
                chore.setCompletedAt(null);
                choreRepository.save(chore);
            }
        }

        // Обновляем статус во всех планах, где есть эта задача
        List<WeeklyPlan> plans = planRepository.findPlansContainingChore(choreId);
        for (WeeklyPlan plan : plans) {
            for (PlanItem item : plan.getItems()) {
                if (item.getChore().getId().equals(choreId)) {
                    item.setCompleted(completed);
                    planItemRepository.save(item);
                }
            }
        }
    }

    @Transactional
    public void removePlanItem(Long itemId) {
        planItemRepository.deleteById(itemId);
    }
}