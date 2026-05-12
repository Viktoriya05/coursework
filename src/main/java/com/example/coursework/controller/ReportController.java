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
        User user = userService.findByUsername(userDetails.getUsername());

        LocalDate startDate = weekStart != null ? LocalDate.parse(weekStart) : LocalDate.now();
        StatisticsDto weeklyStats = statisticsService.getWeeklyStatistics(user.getId(), startDate);
        Map<String, Object> userStats = statisticsService.getUserStats(user.getId());

        model.addAttribute("weeklyStats", weeklyStats);
        model.addAttribute("userStats", userStats);
        model.addAttribute("currentWeek", startDate);
        model.addAttribute("previousWeek", startDate.minusWeeks(1));
        model.addAttribute("nextWeek", startDate.plusWeeks(1));

        return "reports";
    }
}