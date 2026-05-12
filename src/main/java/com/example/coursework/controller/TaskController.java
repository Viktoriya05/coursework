package com.example.coursework.controller;

import com.example.coursework.service.TimerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chores")
@RequiredArgsConstructor
public class TaskController {

    private final TimerService timerService;

    @PostMapping("/{choreId}/confirm")
    public ResponseEntity<?> confirmTask(@PathVariable Long choreId,
                                         @RequestParam boolean approve) {
        // This should call timerService.confirmAndAwardPoints with the execution ID
        // For now, return success
        return ResponseEntity.ok().build();
    }
}
