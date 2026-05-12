package com.example.coursework.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoreDto {
    private Long id;

    @NotBlank(message = "Chore name is required")
    private String name;

    private String description;
    private Integer estimatedMinutes;
    private Integer points;
    private Long categoryId;
    private Long userId;
    private Long assignedById;
    private String status;
    private LocalDateTime dueDate;
}