package com.example.coursework.service;

import com.example.coursework.dto.TimerResponse;
import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import com.example.coursework.repository.UserRepository;
@Service
@RequiredArgsConstructor
public class TimerService {

    private final TaskExecutionRepository executionRepository;
    private final ChoreRepository choreRepository;
    private final UserService userService;
    private final PlanningService planningService;
    private final UserRepository userRepository;
    @Transactional
    public TimerResponse startTimer(Long userId, Long choreId) {
        TaskExecution active = executionRepository.findActiveExecution(userId);
        if (active != null) {
            throw new RuntimeException("You have an active task. Please complete or cancel it first.");
        }

        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));

        TaskExecution execution = new TaskExecution();
        execution.setChore(chore);
        execution.setUser(chore.getUser());
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(ExecutionStatus.ACTIVE);

        chore.setStatus(ChoreStatus.IN_PROGRESS);
        choreRepository.save(chore);

        execution = executionRepository.save(execution);

        TimerResponse response = new TimerResponse();
        response.setExecutionId(execution.getId());
        response.setStartTime(execution.getStartTime());
        response.setStatus("started");
        response.setChoreName(chore.getName());

        return response;
    }

    @Transactional
    public TimerResponse pauseTimer(Long userId, Long executionId) {
        TaskExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() != ExecutionStatus.ACTIVE) {
            throw new RuntimeException("Timer is not active");
        }

        Duration currentDuration = Duration.between(execution.getStartTime(), LocalDateTime.now());
        int currentSeconds = (int) currentDuration.getSeconds();
        execution.setPausedDuration(execution.getPausedDuration() + currentSeconds);
        execution.setStatus(ExecutionStatus.PAUSED);

        execution = executionRepository.save(execution);

        TimerResponse response = new TimerResponse();
        response.setExecutionId(execution.getId());
        response.setPausedDuration(execution.getPausedDuration());
        response.setStatus("paused");

        return response;
    }

    @Transactional
    public TimerResponse resumeTimer(Long userId, Long executionId) {
        TaskExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() != ExecutionStatus.PAUSED) {
            throw new RuntimeException("Timer is not paused");
        }

        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(ExecutionStatus.ACTIVE);

        execution = executionRepository.save(execution);

        TimerResponse response = new TimerResponse();
        response.setExecutionId(execution.getId());
        response.setStartTime(execution.getStartTime());
        response.setStatus("resumed");

        return response;
    }

    @Transactional
    public TimerResponse stopTimer(Long userId, Long executionId) {
        TaskExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (!execution.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to stop this timer");
        }

        if (execution.getStatus() != ExecutionStatus.ACTIVE && execution.getStatus() != ExecutionStatus.PAUSED) {
            throw new RuntimeException("Timer is not active or paused");
        }

        LocalDateTime endTime = LocalDateTime.now();
        execution.setEndTime(endTime);

        Duration duration;
        if (execution.getStatus() == ExecutionStatus.PAUSED) {
            duration = Duration.between(execution.getStartTime(), endTime);
            int currentSessionSeconds = (int) duration.getSeconds();
            execution.setDurationSeconds(execution.getPausedDuration() + currentSessionSeconds);
        } else {
            duration = Duration.between(execution.getStartTime(), endTime);
            execution.setDurationSeconds((int) duration.getSeconds());
        }

        execution.setStatus(ExecutionStatus.COMPLETED);
        execution = executionRepository.save(execution);

        Chore chore = execution.getChore();
        User child = chore.getUser();

        // Находим родителя (кто создал задачу или родитель в семье)
        User parent = findParentForChild(child);

        // Для родителя - сразу начисляем очки и завершаем задачу
        if (child.getRole() == UserRole.PARENT) {
            chore.setStatus(ChoreStatus.COMPLETED);
            if (chore.getPoints() != null && chore.getPoints() > 0) {
                userService.addPoints(child.getId(), chore.getPoints());
            }
            chore.setCompletedAt(endTime);
            choreRepository.save(chore);
            planningService.syncTaskCompletion(chore.getId(), true);
        }
        // Для ребенка - всегда отправляем на проверку родителю (даже если задачу создал сам ребенок)
        else if (child.getRole() == UserRole.CHILD) {
            // Если у задачи нет assignedBy (ребенок взял сам), все равно отправляем на проверку
            chore.setStatus(ChoreStatus.NEEDS_REVIEW);
            chore.setCompletedAt(endTime);
            choreRepository.save(chore);

            // Логируем для отладки
            System.out.println("Task completed by child. Parent to review: " +
                    (parent != null ? parent.getUsername() : "No parent found"));
        }

        TimerResponse response = new TimerResponse();
        response.setExecutionId(execution.getId());
        response.setDurationSeconds(execution.getDurationSeconds());
        response.setStatus("completed");
        response.setPointsAwarded(chore.getPoints());

        if (child.getRole() == UserRole.PARENT) {
            response.setMessage("Task completed! You earned " + chore.getPoints() + " points!");
        } else {
            response.setMessage("Task completed! Waiting for parent approval.");
        }

        return response;
    }

    // Добавить вспомогательный метод для поиска родителя
    private User findParentForChild(User child) {
        if (child.getFamily() == null) {
            return null;
        }

        // Ищем родителя в той же семье
        List<User> familyMembers = userRepository.findByFamily(child.getFamily());
        return familyMembers.stream()
                .filter(member -> member.getRole() == UserRole.PARENT)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public TimerResponse confirmAndAwardPoints(Long executionId, Long parentId, boolean approve) {
        TaskExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (approve) {
            Chore chore = execution.getChore();
            // Меняем статус с NEEDS_REVIEW на COMPLETED
            chore.setStatus(ChoreStatus.COMPLETED);
            choreRepository.save(chore);

            // Начисляем очки
            if (chore.getPoints() != null && chore.getPoints() > 0) {
                userService.addPoints(chore.getUser().getId(), chore.getPoints());
            }

            // Синхронизируем с планом
            planningService.syncTaskCompletion(chore.getId(), true);

            TimerResponse response = new TimerResponse();
            response.setStatus("approved");
            response.setPointsAwarded(chore.getPoints());
            return response;
        } else {
            Chore chore = execution.getChore();
            // Возвращаем статус в PENDING (задача не выполнена)
            chore.setStatus(ChoreStatus.PENDING);
            chore.setCompletedAt(null);
            choreRepository.save(chore);

            // Синхронизируем с планом
            planningService.syncTaskCompletion(chore.getId(), false);

            TimerResponse response = new TimerResponse();
            response.setStatus("rejected");
            return response;
        }
    }

    public TaskExecution getActiveExecution(Long userId) {
        return executionRepository.findActiveExecution(userId);
    }
}