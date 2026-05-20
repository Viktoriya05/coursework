package com.example.coursework.converter;

import com.example.coursework.dto.UserDto;
import com.example.coursework.model.User;
import com.example.coursework.model.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setPoints(user.getPoints());

        return dto;
    }

    public User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPoints(dto.getPoints() != null ? dto.getPoints() : 0);

        if (dto.getRole() != null) {
            user.setRole(UserRole.valueOf(dto.getRole()));
        }

        return user;
    }
}
