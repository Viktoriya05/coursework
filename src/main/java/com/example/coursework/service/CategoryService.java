package com.example.coursework.service;

import com.example.coursework.model.Category;
import com.example.coursework.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAvailableCategories(Long familyId) {
        if (familyId != null) {
            return categoryRepository.findAvailableCategories(familyId);
        }
        return categoryRepository.findByFamilyIsNull();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}