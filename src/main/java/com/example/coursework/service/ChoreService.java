package com.example.coursework.service;

import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Chore createChore(String name, String description, Integer points, Long categoryId,
                             Long userId, Long assignedById, LocalDateTime dueDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chore chore = new Chore();
        chore.setName(name);
        chore.setDescription(description);
        chore.setPoints(points);
        chore.setUser(user);
        chore.setStatus(ChoreStatus.PENDING);
        chore.setDueDate(dueDate);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            chore.setCategory(category);
        }

        if (assignedById != null) {
            User assignedBy = userRepository.findById(assignedById)
                    .orElseThrow(() -> new RuntimeException("Assigned by user not found"));
            chore.setAssignedBy(assignedBy);
        }

        return choreRepository.save(chore);
    }

    @Transactional
    public Chore assignChoreToChild(Long choreId, Long childId, Long parentId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        chore.setUser(child);
        chore.setAssignedBy(parent);
        chore.setStatus(ChoreStatus.PENDING);

        return choreRepository.save(chore);
    }

    public List<Chore> getUserChores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return choreRepository.findByUser(user);
    }

    public List<Chore> getPendingChores(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return choreRepository.findByUserAndStatus(user, ChoreStatus.PENDING);
    }

    @Transactional
    public Chore updateChoreStatus(Long choreId, ChoreStatus status) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));
        chore.setStatus(status);

        if (status == ChoreStatus.COMPLETED) {
            chore.setCompletedAt(LocalDateTime.now());
        }

        return choreRepository.save(chore);
    }

    @Transactional
    public void requestReview(Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new RuntimeException("Chore not found"));
        chore.setStatus(ChoreStatus.NEEDS_REVIEW);
        choreRepository.save(chore);
    }

    public List<Chore> getChoresForReview(Long parentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
        return choreRepository.findByAssignedBy(parent);
    }

    public Chore findById(Long id) {
        return choreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chore not found"));
    }
}