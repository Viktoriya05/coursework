package com.example.coursework.controller.api;

import com.example.coursework.dto.ApiResponse;
import com.example.coursework.dto.ChoreDto;
import com.example.coursework.model.Chore;
import com.example.coursework.model.User;
import com.example.coursework.service.ChoreService;
import com.example.coursework.service.UserService;
import com.example.coursework.service.TimerService;
import com.example.coursework.converter.ChoreConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chores")
@RequiredArgsConstructor
public class ChoreApiController {

    private final ChoreService choreService;
    private final UserService userService;
    private final TimerService timerService;
    private final ChoreConverter choreConverter;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChoreDto>>> getMyChores(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<ChoreDto> chores = choreService.getUserChores(user.getId()).stream()
                .map(choreConverter::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(chores));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ChoreDto>>> getPendingChores(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<ChoreDto> chores = choreService.getPendingChores(user.getId()).stream()
                .map(choreConverter::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(chores));
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<List<ChoreDto>>> getPendingReview(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<ChoreDto> chores = choreService.getChoresForReview(user.getId()).stream()
                .map(choreConverter::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(chores));
    }

    @PostMapping("/{choreId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmTask(@PathVariable Long choreId,
                                                         @RequestParam boolean approve,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());
        // Находим execution для этого chore (последний активный)
        Chore chore = choreService.findById(choreId);
        if (chore.getExecutions() != null && !chore.getExecutions().isEmpty()) {
            Long executionId = chore.getExecutions().get(chore.getExecutions().size() - 1).getId();
            timerService.confirmAndAwardPoints(executionId, parent.getId(), approve);
        }
        return ResponseEntity.ok(ApiResponse.success("Task " + (approve ? "approved" : "rejected"), null));
    }
}
