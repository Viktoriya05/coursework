package com.example.coursework.service;

import com.example.coursework.exception.BusinessLogicException;
import com.example.coursework.exception.ResourceNotFoundException;
import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TaskExecutionRepository executionRepository;

    @Transactional
    public Chore createChore(String name, String description, Integer points, 
                             Long categoryId, Long userId, Long assignedById, LocalDate dueDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Chore chore = new Chore();
        chore.setName(name);
        chore.setDescription(description);
        chore.setPoints(points);
        chore.setUser(user);
        chore.setStatus(ChoreStatus.PENDING);
        chore.setDueDate(dueDate);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
            chore.setCategory(category);
        }

        if (assignedById != null) {
            User assignedBy = userRepository.findById(assignedById)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedById));
            chore.setAssignedBy(assignedBy);
        }

        return choreRepository.save(chore);
    }

    public List<Chore> getDefaultChores() {
        return choreRepository.findByUserIsNull();
    }

    @Transactional
    public Chore assignChoreToChild(Long choreId, Long childId, Long parentId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new ResourceNotFoundException("Chore", "id", choreId));
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", childId));
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", parentId));

        if (child.getRole() != UserRole.CHILD) {
            throw new BusinessLogicException("Selected user is not a child");
        }

        chore.setUser(child);
        chore.setAssignedBy(parent);
        chore.setStatus(ChoreStatus.PENDING);

        return choreRepository.save(chore);
    }

    public List<Chore> getUserChores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return choreRepository.findByUser(user);
    }

    public Page<Chore> getUserChoresPaged(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return choreRepository.findByUser(user, pageable);
    }

    public List<Chore> getPendingChores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return choreRepository.findByUserAndStatus(user, ChoreStatus.PENDING);
    }
    public List<Chore> getChoresForReview(Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        // Если у родителя есть семья - ищем ВСЕ задачи детей в этой семье на проверку
        if (parent.getFamily() != null) {
            return choreRepository.findPendingReviewByFamily(parent.getFamily().getId());
        }

        // Fallback: ищем только задачи, назначенные этим родителем
        return choreRepository.findByAssignedByAndStatus(parent, ChoreStatus.NEEDS_REVIEW);
    }
    @Transactional
    public Chore takeDefaultChore(Long choreId, Long childId) {
        Chore defaultChore = choreRepository.findById(choreId)
                .orElseThrow(() -> new ResourceNotFoundException("Chore", "id", choreId));

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", childId));

        if (child.getRole() != UserRole.CHILD) {
            throw new BusinessLogicException("Only children can take tasks");
        }

        // Создаем копию задачи для ребенка
        Chore childChore = new Chore();
        childChore.setName(defaultChore.getName());
        childChore.setDescription(defaultChore.getDescription());
        childChore.setPoints(defaultChore.getPoints());
        childChore.setCategory(defaultChore.getCategory());
        childChore.setUser(child);
        childChore.setStatus(ChoreStatus.PENDING);
        childChore.setDueDate(LocalDate.now().plusDays(7));

        // Важно: НЕ устанавливаем assignedBy, чтобы показать, что ребенок взял задачу сам
        // childChore.setAssignedBy(null); - это и так null

        return choreRepository.save(childChore);
    }
    @Transactional
    public Chore updateChoreStatus(Long choreId, ChoreStatus status) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));

        System.out.println("Updating chore " + chore.getId() + " from " + chore.getStatus() + " to " + status);

        chore.setStatus(status);

        if (status == ChoreStatus.COMPLETED) {
            chore.setCompletedAt(LocalDateTime.now());
        }

        return choreRepository.save(chore);
    }

    @Transactional
    public void requestReview(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new ResourceNotFoundException("Chore", "id", choreId));
        chore.setStatus(ChoreStatus.NEEDS_REVIEW);
        choreRepository.save(chore);
    }

    public Chore findById(Long id) {
        return choreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chore", "id", id));
    }

    public Double getAverageExecutionTime(Long choreId) {
        return choreRepository.getAverageExecutionTime(choreId);
    }

    public List<Chore> getOverdueTasks(Long userId) {
        return choreRepository.findOverdueTasks(userId, ChoreStatus.PENDING);
    }
}
