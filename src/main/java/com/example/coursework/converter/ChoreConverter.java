package com.example.coursework.converter;

import com.example.coursework.dto.ChoreDto;
import com.example.coursework.model.Chore;
import com.example.coursework.model.ChoreStatus;
import org.springframework.stereotype.Component;

@Component
public class ChoreConverter {

    public ChoreDto toDto(Chore chore) {
        if (chore == null) return null;

        ChoreDto dto = new ChoreDto();
        dto.setId(chore.getId());
        dto.setName(chore.getName());
        dto.setDescription(chore.getDescription());
        dto.setEstimatedMinutes(chore.getEstimatedMinutes());
        dto.setPoints(chore.getPoints());
        dto.setStatus(chore.getStatus() != null ? chore.getStatus().name() : null);
        dto.setDueDate(chore.getDueDate());

        if (chore.getCategory() != null) {
            dto.setCategoryId(chore.getCategory().getId());
        }
        if (chore.getUser() != null) {
            dto.setUserId(chore.getUser().getId());
        }
        if (chore.getAssignedBy() != null) {
            dto.setAssignedById(chore.getAssignedBy().getId());
        }

        return dto;
    }

    public Chore toEntity(ChoreDto dto) {
        if (dto == null) return null;

        Chore chore = new Chore();
        chore.setId(dto.getId());
        chore.setName(dto.getName());
        chore.setDescription(dto.getDescription());
        chore.setEstimatedMinutes(dto.getEstimatedMinutes());
        chore.setPoints(dto.getPoints());
        chore.setDueDate(dto.getDueDate());

        if (dto.getStatus() != null) {
            chore.setStatus(ChoreStatus.valueOf(dto.getStatus()));
        }

        return chore;
    }
}
