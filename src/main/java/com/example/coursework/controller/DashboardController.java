package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.model.Chore;
import com.example.coursework.service.UserService;
import com.example.coursework.service.ChoreService;
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

    private final UserService userService;
    private final ChoreService choreService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername());
        List<Chore> pendingChores = choreService.getPendingChores(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("pendingChores", pendingChores);
        model.addAttribute("pendingCount", pendingChores.size());

        // УБРАНО: отображение роли на дашборде
        // Очки показываем только для детей
        if ("CHILD".equals(user.getRole().name())) {
            model.addAttribute("showPoints", true);
            model.addAttribute("totalPoints", user.getPoints());
        } else {
            model.addAttribute("showPoints", false);
        }

        return "dashboard";
    }
}