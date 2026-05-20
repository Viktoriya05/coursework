package com.example.coursework.controller;

import com.example.coursework.dto.StatisticsDto;
import com.example.coursework.model.User;
import com.example.coursework.service.StatisticsService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @GetMapping
    public String reportsPage(@RequestParam(required = false) String weekStart,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());

            LocalDate startDate;
            if (weekStart != null) {
                startDate = LocalDate.parse(weekStart);
            } else {
                // Неделя начинается с понедельника
                startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }

            StatisticsDto weeklyStats = statisticsService.getWeeklyStatistics(user.getId(), startDate);
            Map<String, Object> userStats = statisticsService.getUserStats(user.getId());

            model.addAttribute("weeklyStats", weeklyStats);
            model.addAttribute("userStats", userStats);
            model.addAttribute("user", user);
            model.addAttribute("currentWeek", startDate);

            return "reports";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "dashboard";
        }
    }
}
