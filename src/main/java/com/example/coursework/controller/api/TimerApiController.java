package com.example.coursework.controller.api;

import com.example.coursework.dto.ApiResponse;
import com.example.coursework.dto.TimerRequest;
import com.example.coursework.dto.TimerResponse;
import com.example.coursework.model.*;
import com.example.coursework.service.TimerService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timer")
@RequiredArgsConstructor
public class TimerApiController {

    private final TimerService timerService;
    private final UserService userService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TimerResponse>> startTimer(@RequestBody TimerRequest request,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.startTimer(user.getId(), request.getChoreId());
        return ResponseEntity.ok(ApiResponse.success("Timer started", response));
    }

    @PostMapping("/pause")
    public ResponseEntity<ApiResponse<TimerResponse>> pauseTimer(@RequestBody TimerRequest request,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.pauseTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(ApiResponse.success("Timer paused", response));
    }

    @PostMapping("/resume")
    public ResponseEntity<ApiResponse<TimerResponse>> resumeTimer(@RequestBody TimerRequest request,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.resumeTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(ApiResponse.success("Timer resumed", response));
    }

    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<TimerResponse>> stopTimer(@RequestBody TimerRequest request,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.stopTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @PostMapping("/{executionId}/confirm")
    public ResponseEntity<ApiResponse<TimerResponse>> confirmExecution(@PathVariable Long executionId,
                                                                       @RequestParam boolean approve,
                                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.confirmAndAwardPoints(executionId, parent.getId(), approve);
        return ResponseEntity.ok(ApiResponse.success(
                approve ? "Task approved" : "Task rejected", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<TimerResponse>> getActiveTimer(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        var execution = timerService.getActiveExecution(user.getId());
        if (execution != null) {
            TimerResponse response = new TimerResponse();
            response.setExecutionId(execution.getId());
            response.setStartTime(execution.getStartTime());
            response.setStatus("active");
            response.setChoreName(execution.getChore().getName());
            return ResponseEntity.ok(ApiResponse.success(response));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
