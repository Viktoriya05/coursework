package com.example.coursework.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("error", ex.getMessage());
        mav.addObject("path", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("error", ex.getMessage());
        mav.addObject("path", request.getRequestURI());
        mav.addObject("status", 500);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("error", "An unexpected error occurred: " + ex.getMessage());
        mav.addObject("path", request.getRequestURI());
        mav.addObject("status", 500);
        return mav;
    }
}