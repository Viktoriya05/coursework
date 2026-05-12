package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.model.Chore;
import com.example.coursework.service.ChoreService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ChoreService choreService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        List<Chore> pendingChores = choreService.getPendingChores(user.getId());
        List<Chore> completedChores = choreService.getUserChores(user.getId()).stream()
                .filter(c -> c.getStatus().name().equals("COMPLETED") || c.getStatus().name().equals("NEEDS_REVIEW"))
                .limit(5)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("pendingChores", pendingChores);
        model.addAttribute("completedChores", completedChores);
        model.addAttribute("pendingCount", pendingChores.size());
        model.addAttribute("totalPoints", user.getPoints());

        return "dashboard";
    }
}
