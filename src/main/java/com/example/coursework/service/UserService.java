package com.example.coursework.service;

import com.example.coursework.model.User;
import com.example.coursework.model.Family;
import com.example.coursework.model.UserRole;
import com.example.coursework.repository.UserRepository;
import com.example.coursework.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User register(String username, String email, String password, String firstName, String lastName, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(UserRole.valueOf(role));
        user.setPoints(0);

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User createFamily(Long userId, String familyName) {
        User user = findById(userId);

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
        return userRepository.save(user);
    }

    public List<User> getFamilyMembers(Long userId) {
        User user = findById(userId);
        if (user.getFamily() == null) {
            throw new RuntimeException("User is not in a family");
        }
        return userRepository.findByFamily(user.getFamily());
    }

    @Transactional
    public void addPoints(Long userId, int points) {
        User user = findById(userId);
        user.setPoints(user.getPoints() + points);
        userRepository.save(user);
    }
}