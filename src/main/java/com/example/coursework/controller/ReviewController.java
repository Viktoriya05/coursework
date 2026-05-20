package com.example.coursework.controller;

import com.example.coursework.model.Chore;
import com.example.coursework.model.ChoreStatus;
import com.example.coursework.model.User;
import com.example.coursework.service.ChoreService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ChoreService choreService;
    private final UserService userService;

    @GetMapping
    public String reviewPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if ("PARENT".equals(user.getRole().name())) {
            // Ищем задачи со статусом NEEDS_REVIEW
            List<Chore> pendingReview = choreService.getChoresForReview(user.getId());
            System.out.println("=== REVIEW PAGE ===");
            System.out.println("Pending review tasks: " + pendingReview.size());
            for (Chore c : pendingReview) {
                System.out.println("  - " + c.getName() + " (status: " + c.getStatus() + ")");
            }
            model.addAttribute("pendingReview", pendingReview);
        }

        model.addAttribute("user", user);
        return "review";
    }

    @PostMapping("/{choreId}/approve")
    public String approveChore(@PathVariable Long choreId) {
        System.out.println("=== APPROVING CHORE ===");
        System.out.println("Chore ID: " + choreId);

        Chore chore = choreService.findById(choreId);
        System.out.println("Before approve - Status: " + chore.getStatus());

        // Меняем статус с NEEDS_REVIEW на COMPLETED
        chore.setStatus(ChoreStatus.COMPLETED);
        choreService.updateChoreStatus(choreId, ChoreStatus.COMPLETED);

        System.out.println("After approve - Status: " + chore.getStatus());

        // Начисляем очки ребенку
        if (chore.getPoints() != null && chore.getPoints() > 0) {
            userService.addPoints(chore.getUser().getId(), chore.getPoints());
            System.out.println("Added " + chore.getPoints() + " points to user " + chore.getUser().getUsername());
        }

        System.out.println("Redirecting to /review");
        return "redirect:/review";
    }

    @PostMapping("/{choreId}/reject")
    public String rejectChore(@PathVariable Long choreId) {
        System.out.println("=== REJECTING CHORE ===");
        System.out.println("Chore ID: " + choreId);

        Chore chore = choreService.findById(choreId);
        System.out.println("Before reject - Status: " + chore.getStatus());

        // Меняем статус с NEEDS_REVIEW на PENDING
        chore.setStatus(ChoreStatus.PENDING);
        choreService.updateChoreStatus(choreId, ChoreStatus.PENDING);

        System.out.println("After reject - Status: " + chore.getStatus());

        return "redirect:/review";
    }
}