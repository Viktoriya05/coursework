package com.example.coursework.repository;

import com.example.coursework.model.Category;
import com.example.coursework.model.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByFamily(Family family);
    List<Category> findByFamilyIsNull();
    Optional<Category> findByName(String name);
    List<Category> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE c.family IS NULL OR c.family.id = :familyId ORDER BY c.name")
    List<Category> findAvailableCategories(@Param("familyId") Long familyId);
}
