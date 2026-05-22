package com.example.coursework.controller;

import com.example.coursework.dto.TimerRequest;
import com.example.coursework.dto.TimerResponse;
import com.example.coursework.model.Chore;
import com.example.coursework.model.User;
import com.example.coursework.service.ChoreService;
import com.example.coursework.service.TimerService;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TimerController {

    private final TimerService timerService;
    private final UserService userService;
    private final ChoreService choreService;

    @PostMapping("/stop")
    public ResponseEntity<TimerResponse> stopTimer(@RequestBody TimerRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        TimerResponse response = timerService.stopTimer(user.getId(), request.getExecutionId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timer")
    public String timerPage(@RequestParam Long choreId,
                            Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        Chore chore = choreService.findById(choreId);

        model.addAttribute("choreId", choreId);
        model.addAttribute("choreName", chore.getName());
        model.addAttribute("points", chore.getPoints());
        model.addAttribute("user", user);

        return "timer";
    }
}