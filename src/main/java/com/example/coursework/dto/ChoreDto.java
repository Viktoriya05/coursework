package com.example.coursework.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoreDto {
    private Long id;

    @NotBlank(message = "Task name is required")
    private String name;

    private String description;

    @Min(value = 1, message = "Estimated time must be at least 1 minute")
    private Integer estimatedMinutes;

    @Min(value = 0, message = "Points cannot be negative")
    private Integer points;

    private Long categoryId;
    private Long userId;
    private Long assignedById;
    private String status;
    private LocalDate dueDate;
}
