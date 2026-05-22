package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.model.Chore;
import com.example.coursework.model.UserRole;
import com.example.coursework.service.UserService;
import com.example.coursework.service.ChoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final UserService userService;
    private final ChoreService choreService;

    @GetMapping
    public String familyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        System.out.println("=== FAMILY PAGE DEBUG ===");
        System.out.println("User: " + user.getUsername());
        System.out.println("User role: " + user.getRole());
        System.out.println("User family: " + (user.getFamily() != null ? user.getFamily().getName() : "null"));

        if (user.getFamily() != null) {
            model.addAttribute("family", user.getFamily());
            List<User> members = userService.getFamilyMembers(user.getId());
            model.addAttribute("members", members);
            model.addAttribute("inviteCode", user.getFamily().getInviteCode());

            System.out.println("Family members count: " + members.size());

            // Для родителей - добавляем базовые задачи для назначения
            if ("PARENT".equals(user.getRole().name())) {
                // Получаем всех детей в семье
                List<User> children = members.stream()
                        .filter(m -> "CHILD".equals(m.getRole().name()))
                        .collect(Collectors.toList());
                model.addAttribute("children", children);
                System.out.println("Children count: " + children.size());

                // Получаем базовые задачи (без привязки к пользователю)
                List<Chore> defaultChores = choreService.getDefaultChores();
                model.addAttribute("defaultChores", defaultChores);
                System.out.println("Default chores count: " + defaultChores.size());

                // Получаем задачи, созданные родителем
                List<Chore> parentChores = choreService.getUserChores(user.getId());
                model.addAttribute("parentChores", parentChores);
                System.out.println("Parent chores count: " + parentChores.size());
            } else {
                System.out.println("User is not PARENT, role = " + user.getRole());
            }
        } else {
            System.out.println("User has no family");
        }

        model.addAttribute("user", user);
        return "family";
    }

    @PostMapping("/create")
    public String createFamily(@RequestParam String familyName,
                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        userService.createFamily(user.getId(), familyName);
        return "redirect:/family";
    }

    @PostMapping("/join")
    public String joinFamily(@RequestParam String inviteCode,
                             @RequestParam(required = false) String role,  // Добавлен параметр role
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        userService.joinFamily(user.getId(), inviteCode);

        // Если указана роль и пользователь присоединяется как ребенок
        if ("CHILD".equals(role) && user.getRole() == UserRole.PARENT) {
            user.setRole(UserRole.CHILD);
            userService.updateUser(user);  // Нужно добавить этот метод в UserService
        }

        return "redirect:/family";
    }

    @PostMapping("/assign-task")
    public String assignTask(@RequestParam Long choreId,
                             @RequestParam Long childId,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());
        choreService.assignChoreToChild(choreId, childId, parent.getId());
        return "redirect:/family";
    }

    @PostMapping("/create-and-assign")
    public String createAndAssign(@RequestParam String name,
                                  @RequestParam String description,
                                  @RequestParam Integer points,
                                  @RequestParam Long childId,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());

        choreService.createChore(
                name,
                description,
                points,
                null,
                childId,
                parent.getId(),
                null
        );

        return "redirect:/family";
    }
}