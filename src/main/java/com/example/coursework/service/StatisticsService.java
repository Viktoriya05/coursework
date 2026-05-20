package com.example.coursework.service;

import com.example.coursework.dto.StatisticsDto;
import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.coursework.exception.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TaskExecutionRepository executionRepository;
    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ChoreRepository choreRepository;

    public StatisticsDto getWeeklyStatistics(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atStartOfDay();

        StatisticsDto dto = new StatisticsDto();
        dto.setWeekStart(weekStart);
        dto.setWeekEnd(weekStart.plusDays(6));

        List<User> members = new ArrayList<>();
        if (user.getFamily() != null) {
            members = userRepository.findByFamily(user.getFamily());
        } else {
            members.add(user);
        }

        int familyTotalMinutes = 0;

        for (User member : members) {
            List<TaskExecution> executions = executionRepository.findByUserAndStartTimeBetween(
                    member.getId(), start, end);

            int totalMinutes = 0;
            Map<String, Integer> categoryMinutes = new LinkedHashMap<>();

            for (TaskExecution execution : executions) {
                if (execution.getDurationSeconds() != null && execution.getStatus() == ExecutionStatus.COMPLETED) {
                    int minutes = execution.getDurationSeconds() / 60;
                    totalMinutes += minutes;

                    String categoryName = execution.getChore().getCategory() != null
                            ? execution.getChore().getCategory().getName()
                            : "Uncategorized";
                    categoryMinutes.put(categoryName,
                            categoryMinutes.getOrDefault(categoryName, 0) + minutes);
                }
            }

            String memberName = member.getFullName().trim().isEmpty() ? member.getUsername() : member.getFullName();
            dto.getUserTotalMinutes().put(memberName, totalMinutes);
            dto.getUserCategoryMinutes().put(memberName, categoryMinutes);
            familyTotalMinutes += totalMinutes;

            Integer pointsEarned = pointTransactionRepository.sumPointsByUserAndDateRange(
                    member.getId(), start, end);
            dto.getUserPoints().put(memberName, pointsEarned != null ? pointsEarned : 0);
        }

        dto.setFamilyTotalMinutes(familyTotalMinutes);
        return dto;
    }

    public Map<String, Object> getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Map<String, Object> stats = new LinkedHashMap<>();

        Long totalChoresCompleted = choreRepository.countCompletedByUser(userId);
        Integer totalPointsFromChores = choreRepository.sumPointsByUser(userId);

        List<TaskExecution> executions = executionRepository.findByUser(user);

        int totalSeconds = executions.stream()
                .filter(e -> e.getStatus() == ExecutionStatus.COMPLETED && e.getDurationSeconds() != null)
                .mapToInt(TaskExecution::getDurationSeconds)
                .sum();

        Map<String, Integer> categoryTimeMap = new LinkedHashMap<>();
        for (TaskExecution ex : executions) {
            if (ex.getStatus() == ExecutionStatus.COMPLETED && ex.getDurationSeconds() != null) {
                String category = ex.getChore().getCategory() != null
                        ? ex.getChore().getCategory().getName()
                        : "Uncategorized";
                categoryTimeMap.put(category,
                        categoryTimeMap.getOrDefault(category, 0) + ex.getDurationSeconds() / 60);
            }
        }

        stats.put("user", user);
        stats.put("totalChoresCompleted", totalChoresCompleted != null ? totalChoresCompleted : 0);
        stats.put("totalPoints", user.getPoints());
        stats.put("totalPointsFromChores", totalPointsFromChores != null ? totalPointsFromChores : 0);
        stats.put("totalHoursSpent", totalSeconds / 3600);
        stats.put("totalMinutesSpent", totalSeconds / 60);
        stats.put("categoryTimeMap", categoryTimeMap);

        return stats;
    }
}
