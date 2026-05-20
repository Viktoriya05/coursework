package com.example.coursework.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Map<String, Integer> userTotalMinutes = new HashMap<>();
    private Map<String, Map<String, Integer>> userCategoryMinutes = new HashMap<>();
    private Integer familyTotalMinutes = 0;
    private Map<String, Integer> userPoints = new HashMap<>();
}
