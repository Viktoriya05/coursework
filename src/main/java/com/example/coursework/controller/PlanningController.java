package com.example.coursework.controller;
import java.util.*;
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
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;
    private final UserService userService;
    private final ChoreService choreService;

    @PostMapping("/create-and-add")
    public String createAndAddToPlan(@RequestParam Long planId,
                                     @RequestParam String name,
                                     @RequestParam String description,
                                     @RequestParam Integer points,
                                     @RequestParam String scheduledDate,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam(required = false) String weekStart) {
        User user = userService.findByUsername(userDetails.getUsername());

        // Создаем новую задачу
        Chore newChore = choreService.createChore(
                name,
                description,
                points,
                null,
                user.getId(),
                null,
                LocalDate.parse(scheduledDate)
        );

        // Добавляем в план
        planningService.addChoreToPlan(planId, newChore.getId(), LocalDate.parse(scheduledDate));

        return "redirect:/planning?weekStart=" + (weekStart != null ? weekStart : LocalDate.now().toString());
    }
    @GetMapping
    public String planningPage(@RequestParam(required = false) String weekStart,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        LocalDate startDate = weekStart != null ? LocalDate.parse(weekStart) :
                LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        WeeklyPlan plan = planningService.getWeeklyPlan(user.getId(), startDate);
        Map<String, Object> summary = planningService.getWeeklySummary(user.getId(), startDate);

        // Получаем ВСЕ задачи пользователя + базовые задачи
        List<Chore> userChores = choreService.getUserChores(user.getId());
        List<Chore> defaultChores = choreService.getDefaultChores();

        // Объединяем списки без дубликатов
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
        model.addAttribute("weekStart", startDate);
        model.addAttribute("weekEnd", startDate.plusDays(6));
        model.addAttribute("userChores", allChores);  // Все задачи
        model.addAttribute("user", user);

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
                                 @RequestParam String scheduledDate,
                                 @RequestParam(required = false) String weekStart) {
        planningService.addChoreToPlan(planId, choreId, LocalDate.parse(scheduledDate));
        return "redirect:/planning?weekStart=" + (weekStart != null ? weekStart : LocalDate.now().toString());
    }

    @PostMapping("/item/{itemId}/complete")
    public String completePlanItem(@PathVariable Long itemId,
                                   @RequestParam(required = false) String weekStart) {
        planningService.markPlanItemCompleted(itemId);
        return "redirect:/planning?weekStart=" + (weekStart != null ? weekStart : LocalDate.now().toString());
    }

    @PostMapping("/item/{itemId}/remove")
    public String removePlanItem(@PathVariable Long itemId,
                                 @RequestParam(required = false) String weekStart) {
        planningService.removePlanItem(itemId);
        return "redirect:/planning?weekStart=" + (weekStart != null ? weekStart : LocalDate.now().toString());
    }
}
