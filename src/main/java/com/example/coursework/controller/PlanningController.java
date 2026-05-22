package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.model.WeeklyPlan;
import com.example.coursework.model.Chore;
import com.example.coursework.service.PlanningService;
import com.example.coursework.service.UserService;
import com.example.coursework.service.ChoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.*;

@Controller
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final UserService userService;
    private final ChoreService choreService;

    @GetMapping
    public String planningPage(@RequestParam(required = false) String weekStart,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());

            LocalDate startDate;
            if (weekStart != null && !weekStart.isEmpty()) {
                startDate = LocalDate.parse(weekStart);
            } else {
                startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String weekStartStr = startDate.format(formatter);
            String weekEndStr = startDate.plusDays(6).format(formatter);

            WeeklyPlan plan = planningService.getWeeklyPlan(user.getId(), startDate);
            Map<String, Object> summary = planningService.getWeeklySummary(user.getId(), startDate);

            List<Chore> userChores = choreService.getUserChores(user.getId());
            List<Chore> defaultChores = choreService.getDefaultChores();

            Set<Long> choreIds = new HashSet<>();
            List<Chore> allChores = new ArrayList<>();

            for (Chore chore : userChores) {
                if (!choreIds.contains(chore.getId())) {
                    choreIds.add(chore.getId());
                    allChores.add(chore);
                }
            }

            for (Chore chore : defaultChores) {
                if (!choreIds.contains(chore.getId())) {
                    choreIds.add(chore.getId());
                    allChores.add(chore);
                }
            }

            model.addAttribute("plan", plan);
            model.addAttribute("summary", summary);
            model.addAttribute("weekStartDate", startDate);
            model.addAttribute("weekStart", weekStartStr);
            model.addAttribute("weekEnd", weekEndStr);
            model.addAttribute("userChores", allChores);
            model.addAttribute("user", user);

            return "planning";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/create")
    public String createPlan(@RequestParam(required = false) String weekStart,
                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            LocalDate startDate;
            if (weekStart != null && !weekStart.isEmpty()) {
                startDate = LocalDate.parse(weekStart);
            } else {
                startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }
            planningService.createWeeklyPlan(user.getId(), startDate);
            return "redirect:/planning?weekStart=" + startDate;
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/add-chore")
    public String addChoreToPlan(@RequestParam Long planId,
                                 @RequestParam Long choreId,
                                 @RequestParam String scheduledDate,
                                 @RequestParam(required = false) String weekStart) {
        try {
            planningService.addChoreToPlan(planId, choreId, LocalDate.parse(scheduledDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/planning";
    }

    @PostMapping("/item/{itemId}/complete")
    public String completePlanItem(@PathVariable Long itemId) {
        try {
            planningService.markPlanItemCompleted(itemId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/planning";
    }

    @PostMapping("/item/{itemId}/remove")
    public String removePlanItem(@PathVariable Long itemId) {
        try {
            planningService.removePlanItem(itemId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/planning";
    }
}