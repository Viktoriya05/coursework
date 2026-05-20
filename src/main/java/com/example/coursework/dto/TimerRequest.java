package com.example.coursework.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimerRequest {
    private Long choreId;
    private Long executionId;
}
