package com.example.coursework.repository;
import com.example.coursework.model.Category;
import com.example.coursework.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByFamily(Family family);
    List<Category> findByFamilyIsNull();
}
