package com.example.coursework.exception;

import com.example.coursework.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Определяет, является ли запрос API-запросом
     */
    private boolean isApiRequest(HttpServletRequest request) {
        if (request == null) return false;
        String path = request.getRequestURI();
        String accept = request.getHeader("Accept");

        // API запросы: /api/** или ожидают JSON
        return (path != null && path.startsWith("/api/")) ||
                (accept != null && accept.contains("application/json"));
    }

    // ==================== ResourceNotFoundException ====================
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("error", ex.getMessage());
            mav.addObject("status", HttpStatus.NOT_FOUND.value());
            mav.setViewName("404");
            return mav;
        }
    }

    // ==================== BusinessLogicException ====================
    @ExceptionHandler(BusinessLogicException.class)
    public Object handleBusinessLogic(BusinessLogicException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("error", ex.getMessage());
            mav.addObject("status", HttpStatus.BAD_REQUEST.value());
            mav.setViewName("error");
            return mav;
        }
    }

    // ==================== TimerActiveException ====================
    @ExceptionHandler(TimerActiveException.class)
    public Object handleTimerActive(TimerActiveException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage()));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("error", ex.getMessage());
            mav.addObject("status", HttpStatus.CONFLICT.value());
            mav.setViewName("error");
            return mav;
        }
    }

    // ==================== AccessDeniedException ====================
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("error", "Access denied");
            mav.addObject("status", HttpStatus.FORBIDDEN.value());
            mav.setViewName("error");
            return mav;
        }
    }

    // ==================== Validation Exceptions ====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        if (isApiRequest(request)) {
            ApiResponse<Map<String, String>> response = new ApiResponse<>();
            response.setSuccess(false);
            response.setMessage("Validation failed");
            response.setData(errors);
            response.setTimestamp(LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("errors", errors);
            mav.addObject("error", "Validation failed");
            mav.setViewName("error");
            return mav;
        }
    }

    // ==================== Все остальные исключения ====================
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request) {
        ex.printStackTrace(); // Для отладки

        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
        } else {
            ModelAndView mav = new ModelAndView();
            mav.addObject("error", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
            mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            mav.setViewName("error");
            return mav;
        }
    }
}
