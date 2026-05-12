package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.model.WeeklyPlan;
import com.example.coursework.service.PlanningService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final UserService userService;

    @GetMapping
    public String planningPage(@RequestParam(required = false) String weekStart,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        LocalDate startDate = weekStart != null ? LocalDate.parse(weekStart) : LocalDate.now();
        WeeklyPlan plan = planningService.getWeeklyPlan(user.getId(), startDate);
        Map<String, Object> summary = planningService.getWeeklySummary(user.getId(), startDate);

        model.addAttribute("plan", plan);
        model.addAttribute("summary", summary);
        model.addAttribute("weekStart", startDate);
        model.addAttribute("weekEnd", startDate.plusDays(6));

        return "planning";
    }

    @PostMapping("/create")
    public String createPlan(@RequestParam(required = false) String weekStart,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        LocalDate startDate = weekStart != null ? LocalDate.parse(weekStart) : LocalDate.now();
        planningService.createWeeklyPlan(user.getId(), startDate);
        return "redirect:/planning?weekStart=" + startDate;
    }

    @PostMapping("/add-chore")
    public String addChoreToPlan(@RequestParam Long planId,
                                 @RequestParam Long choreId,
                                 @RequestParam String scheduledDate) {
        planningService.addChoreToPlan(planId, choreId, LocalDate.parse(scheduledDate));
        return "redirect:/planning";
    }

    @PostMapping("/item/{itemId}/complete")
    public String completePlanItem(@PathVariable Long itemId) {
        planningService.markPlanItemCompleted(itemId);
        return "redirect:/planning";
    }
}
