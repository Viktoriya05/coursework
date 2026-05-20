package com.example.coursework.validation;

import com.example.coursework.dto.ChoreDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChoreValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ChoreDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChoreDto dto = (ChoreDto) target;

        if (dto.getName() != null && dto.getName().length() < 2) {
            errors.rejectValue("name", "chore.name.tooShort", "Task name must be at least 2 characters");
        }

        if (dto.getPoints() != null && dto.getPoints() > 1000) {
            errors.rejectValue("points", "chore.points.tooHigh", "Points cannot exceed 1000");
        }

        if (dto.getEstimatedMinutes() != null && dto.getEstimatedMinutes() > 1440) {
            errors.rejectValue("estimatedMinutes", "chore.time.tooLong", "Estimated time cannot exceed 24 hours");
        }
    }
}
