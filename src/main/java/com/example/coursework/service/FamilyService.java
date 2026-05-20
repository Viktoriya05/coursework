package com.example.coursework.service;

import com.example.coursework.model.Family;
import com.example.coursework.model.User;
import com.example.coursework.model.UserRole;
import com.example.coursework.repository.FamilyRepository;
import com.example.coursework.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;

    @Transactional
    public void assignRoleToMember(Long familyId, Long memberId, UserRole role) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!member.getFamily().getId().equals(familyId)) {
            throw new RuntimeException("User is not in this family");
        }

        member.setRole(role);
        userRepository.save(member);
    }

    @Transactional
    public Family createFamilyWithParent(Long userId, String familyName) {
        User parent = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Family family = new Family();
        family.setName(familyName);
        family = familyRepository.save(family);

        parent.setFamily(family);
        parent.setRole(UserRole.PARENT);  // Создатель семьи становится родителем
        userRepository.save(parent);

        return family;
    }
}