package com.example.coursework.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimerResponse {
    private Long executionId;
    private LocalDateTime startTime;
    private Integer durationSeconds;
    private Integer pausedDuration;
    private String status;
    private String choreName;
    private Integer pointsAwarded;
    private String message;
}