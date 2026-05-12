
package com.example.coursework.controller;

import com.example.coursework.dto.TimerRequest;
import com.example.coursework.dto.TimerResponse;
import com.example.coursework.model.User;
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
public class TimerController {

    private final TimerService timerService;
    private final UserService userService;

    @PostMapping("/start")
    public ResponseEntity<TimerResponse> startTimer(@RequestBody TimerRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.startTimer(user.getId(), request.getChoreId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pause")
    public ResponseEntity<TimerResponse> pauseTimer(@RequestBody TimerRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.pauseTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resume")
    public ResponseEntity<TimerResponse> resumeTimer(@RequestBody TimerRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.resumeTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<TimerResponse> stopTimer(@RequestBody TimerRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.stopTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{executionId}/confirm")
    public ResponseEntity<TimerResponse> confirmExecution(@PathVariable Long executionId,
                                                          @RequestParam boolean approve,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.confirmAndAwardPoints(executionId, parent.getId(), approve);
        return ResponseEntity.ok(response);
    }
}
