package com.example.coursework.controller.api;

import com.example.coursework.dto.ApiResponse;
import com.example.coursework.dto.StatisticsDto;
import com.example.coursework.model.User;
import com.example.coursework.service.StatisticsService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsApiController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<StatisticsDto>> getWeeklyStats(
            @RequestParam(required = false) String weekStart,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        LocalDate startDate = weekStart != null ? LocalDate.parse(weekStart) : LocalDate.now();
        StatisticsDto stats = statisticsService.getWeeklyStatistics(user.getId(), startDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/personal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPersonalStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Map<String, Object> stats = statisticsService.getUserStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
