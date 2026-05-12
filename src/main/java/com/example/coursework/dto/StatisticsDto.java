package com.example.coursework.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Map<String, Integer> userTotalMinutes;
    private Map<String, Map<String, Integer>> userCategoryMinutes;
    private Integer familyTotalMinutes;
    private Map<String, Integer> userPoints;
}