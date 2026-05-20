package com.example.coursework.service;

import com.example.coursework.exception.BusinessLogicException;
import com.example.coursework.exception.ResourceNotFoundException;
import com.example.coursework.model.User;
import com.example.coursework.model.Family;
import com.example.coursework.model.UserRole;
import com.example.coursework.model.PointTransaction;
import com.example.coursework.model.Chore;
import com.example.coursework.repository.UserRepository;
import com.example.coursework.repository.FamilyRepository;
import com.example.coursework.repository.PointTransactionRepository;
import com.example.coursework.repository.ChoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ChoreRepository choreRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String username, String email, String password,
                         String firstName, String lastName, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessLogicException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessLogicException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName != null ? firstName : "");
        user.setLastName(lastName != null ? lastName : "");
        user.setRole(UserRole.valueOf(role));
        user.setPoints(0);

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional
    public User createFamily(Long userId, String familyName) {
        User user = findById(userId);

        if (user.getFamily() != null) {
            throw new BusinessLogicException("User already belongs to a family");
        }

        Family family = new Family();
        family.setName(familyName);
        family = familyRepository.save(family);

        user.setFamily(family);
        return userRepository.save(user);
    }

    @Transactional
    public User joinFamily(Long userId, String inviteCode) {
        User user = findById(userId);

        Family family = familyRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        user.setFamily(family);

        // Роль назначает создатель семьи (родитель), а присоединяющийся получает роль CHILD по умолчанию
        // Если у пользователя еще нет роли, назначаем CHILD
        if (user.getRole() == null) {
            user.setRole(UserRole.CHILD);
        }

        return userRepository.save(user);
    }

    public List<User> getFamilyMembers(Long userId) {
        User user = findById(userId);
        if (user.getFamily() == null) {
            return List.of();
        }
        return userRepository.findByFamily(user.getFamily());
    }

    public Page<User> getFamilyMembersPaged(Long userId, Pageable pageable) {
        User user = findById(userId);
        if (user.getFamily() == null) {
            return Page.empty();
        }
        return userRepository.findByFamilyPaged(user.getFamily().getId(), pageable);
    }

    @Transactional
    public void addPoints(Long userId, int points) {
        User user = findById(userId);
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }

    @Transactional
    public void subtractPoints(Long userId, int points) {
        User user = findById(userId);
        user.setPoints(Math.max(0, user.getPoints() - points));
        userRepository.save(user);
    }

    public List<User> getChildrenOrderedByPoints(Long familyId) {
        return userRepository.findChildrenByFamilyOrderedByPoints(familyId);
    }
}
