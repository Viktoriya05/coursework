package com.example.coursework.controller;

import com.example.coursework.dto.ChoreDto;
import com.example.coursework.model.Category;
import com.example.coursework.model.Chore;
import com.example.coursework.model.User;
import com.example.coursework.model.ChoreStatus;
import com.example.coursework.repository.CategoryRepository;
import com.example.coursework.service.ChoreService;
import com.example.coursework.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chores")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreService choreService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String listChores(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Chore> chores = choreService.getUserChores(user.getId());

        model.addAttribute("chores", chores);
        model.addAttribute("user", user);
        return "tasks";
    }

    @GetMapping("/create")
    public String createForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("chore", new ChoreDto());
        model.addAttribute("categories", categoryRepository.findByFamily(user.getFamily()));
        return "chore-form";
    }

    @PostMapping("/create")
    public String createChore(@Valid @ModelAttribute ChoreDto choreDto,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());

        choreService.createChore(
                choreDto.getName(),
                choreDto.getDescription(),
                choreDto.getPoints(),
                choreDto.getCategoryId(),
                user.getId(),
                null,
                choreDto.getDueDate()
        );

        return "redirect:/chores";
    }

    @GetMapping("/assign")
    public String assignForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());
        List<User> children = userService.getFamilyMembers(parent.getId()).stream()
                .filter(u -> u.getRole().name().equals("CHILD"))
                .toList();
        List<Chore> availableChores = choreService.getUserChores(parent.getId());

        model.addAttribute("children", children);
        model.addAttribute("chores", availableChores);
        return "assign-chore";
    }

    @PostMapping("/assign")
    public String assignChore(@RequestParam Long choreId, @RequestParam Long childId,
                              @AuthenticationPrincipal UserDetails userDetails) {
        User parent = userService.findByUsername(userDetails.getUsername());
        choreService.assignChoreToChild(choreId, childId, parent.getId());
        return "redirect:/chores";
    }

    @PostMapping("/{id}/complete")
    public String completeChore(@PathVariable Long id) {
        choreService.updateChoreStatus(id, ChoreStatus.COMPLETED);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/request-review")
    public String requestReview(@PathVariable Long id) {
        choreService.requestReview(id);
        return "redirect:/dashboard";
    }
}
