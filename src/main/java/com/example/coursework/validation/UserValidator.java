package com.example.coursework.validation;

import com.example.coursework.dto.UserDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDto dto = (UserDto) target;

        if (dto.getPassword() != null) {
            if (!dto.getPassword().matches(".*[A-Z].*")) {
                errors.rejectValue("password", "user.password.noUppercase", 
                    "Password must contain at least one uppercase letter");
            }
            if (!dto.getPassword().matches(".*[0-9].*")) {
                errors.rejectValue("password", "user.password.noDigit", 
                    "Password must contain at least one digit");
            }
        }

        if (dto.getFirstName() != null && dto.getFirstName().matches(".*\\d.*")) {
            errors.rejectValue("firstName", "user.firstName.hasDigits", 
                "First name cannot contain digits");
        }
    }
}
