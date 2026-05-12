package com.example.coursework.service;

import com.example.coursework.dto.StatisticsDto;
import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TaskExecutionRepository executionRepository;
    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;

    public StatisticsDto getWeeklyStatistics(Long userId, LocalDate weekStart) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atStartOfDay();

        StatisticsDto dto = new StatisticsDto();
        dto.setWeekStart(weekStart);
        dto.setWeekEnd(weekStart.plusDays(6));

        Map<String, Integer> userTotalMinutes = new HashMap<>();
        Map<String, Map<String, Integer>> userCategoryMinutes = new HashMap<>();
        Map<String, Integer> userPoints = new HashMap<>();

        // If user has a family, get all family members
        List<User> members = new ArrayList<>();
        if (user.getFamily() != null) {
            members = userRepository.findByFamily(user.getFamily());
        } else {
            members.add(user);
        }

        int familyTotalMinutes = 0;

        for (User member : members) {
            List<TaskExecution> executions = executionRepository.findByUserAndStartTimeBetween(member, start, end);

            int totalMinutes = 0;
            Map<String, Integer> categoryMinutes = new HashMap<>();

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

            String memberName = member.getFirstName() + " " + member.getLastName();
            userTotalMinutes.put(memberName, totalMinutes);
            userCategoryMinutes.put(memberName, categoryMinutes);
            familyTotalMinutes += totalMinutes;

            // Get points earned this week
            List<PointTransaction> transactions = pointTransactionRepository.findByUserAndCreatedAtBetween(member, start, end);
            int pointsEarned = transactions.stream().mapToInt(PointTransaction::getPoints).sum();
            userPoints.put(memberName, pointsEarned);
        }

        dto.setUserTotalMinutes(userTotalMinutes);
        dto.setUserCategoryMinutes(userCategoryMinutes);
        dto.setFamilyTotalMinutes(familyTotalMinutes);
        dto.setUserPoints(userPoints);

        return dto;
    }

    public Map<String, Object> getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> stats = new HashMap<>();

        List<Chore> chores = user.getChores();
        long totalChoresCompleted = chores.stream()
                .filter(c -> c.getStatus() == ChoreStatus.COMPLETED)
                .count();

        List<TaskExecution> executions = executionRepository.findByUser(user);

        // Total time spent
        int totalSeconds = executions.stream()
                .filter(e -> e.getStatus() == ExecutionStatus.COMPLETED && e.getDurationSeconds() != null)
                .mapToInt(TaskExecution::getDurationSeconds)
                .sum();

        // Category breakdown
        Map<String, Integer> categoryTimeMap = new HashMap<>();
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
        stats.put("totalChoresCompleted", totalChoresCompleted);
        stats.put("totalPoints", user.getPoints());
        stats.put("totalHoursSpent", totalSeconds / 3600);
        stats.put("totalMinutesSpent", totalSeconds / 60);
        stats.put("categoryTimeMap", categoryTimeMap);

        return stats;
    }
}
