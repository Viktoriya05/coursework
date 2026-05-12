package com.example.coursework.config;

import com.example.coursework.model.*;
import com.example.coursework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create default categories if none exist
        if (categoryRepository.count() == 0) {
            Category cleaning = new Category();
            cleaning.setName("Cleaning");
            cleaning.setIcon("🧹");
            cleaning.setColor("#4CAF50");
            cleaning.setIsDefault(true);
            categoryRepository.save(cleaning);

            Category cooking = new Category();
            cooking.setName("Cooking");
            cooking.setIcon("🍳");
            cooking.setColor("#FF9800");
            cooking.setIsDefault(true);
            categoryRepository.save(cooking);

            Category laundry = new Category();
            laundry.setName("Laundry");
            laundry.setIcon("👕");
            laundry.setColor("#2196F3");
            laundry.setIsDefault(true);
            categoryRepository.save(laundry);

            Category dishes = new Category();
            dishes.setName("Dishes");
            dishes.setIcon("🍽️");
            dishes.setColor("#9C27B0");
            dishes.setIsDefault(true);
            categoryRepository.save(dishes);

            System.out.println("Default categories created");
        }

        // Create test users if none exist
        if (userRepository.count() == 0) {
            User parent = new User();
            parent.setUsername("parent");
            parent.setEmail("parent@example.com");
            parent.setPassword(passwordEncoder.encode("password"));
            parent.setFirstName("John");
            parent.setLastName("Doe");
            parent.setRole(UserRole.PARENT);
            parent.setPoints(0);
            userRepository.save(parent);

            User child = new User();
            child.setUsername("child");
            child.setEmail("child@example.com");
            child.setPassword(passwordEncoder.encode("password"));
            child.setFirstName("Tommy");
            child.setLastName("Doe");
            child.setRole(UserRole.CHILD);
            child.setPoints(0);
            userRepository.save(child);

            System.out.println("Test users created - Login with: parent/password or child/password");
        }
    }
}