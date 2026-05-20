package com.example.coursework.repository;

import com.example.coursework.model.User;
import com.example.coursework.model.Family;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByFamily(Family family);
    List<User> findByRole(String role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.family.id = :familyId AND u.role = 'CHILD' ORDER BY u.points DESC")
    List<User> findChildrenByFamilyOrderedByPoints(@Param("familyId") Long familyId);

    @Query("SELECT u FROM User u WHERE u.family.id = :familyId ORDER BY u.role, u.firstName")
    Page<User> findByFamilyPaged(@Param("familyId") Long familyId, Pageable pageable);
}
