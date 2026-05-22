package com.example.coursework.controller;

import com.example.coursework.dto.ChoreDto;
import com.example.coursework.model.Category;
import com.example.coursework.model.User;
import com.example.coursework.service.CategoryService;
import com.example.coursework.service.ChoreService;
import com.example.coursework.service.UserService;
import com.example.coursework.validation.ChoreValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chores")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreService choreService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ChoreValidator choreValidator;

    @InitBinder("chore")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(choreValidator);
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());

        ChoreDto choreDto = new ChoreDto();
        model.addAttribute("chore", choreDto);

        // Добавляем категории для выбора
        List<Category> categories = categoryService.getAvailableCategories(user.getFamily() != null ? user.getFamily().getId() : null);
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);

        return "chore-form";
    }

    @PostMapping("/create")
    public String createChore(@Valid @ModelAttribute("chore") ChoreDto choreDto,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAvailableCategories(user.getFamily() != null ? user.getFamily().getId() : null);
            model.addAttribute("categories", categories);
            model.addAttribute("user", user);
            return "chore-form";
        }

        try {
            choreService.createChore(
                    choreDto.getName(),
                    choreDto.getDescription(),
                    choreDto.getPoints(),
                    choreDto.getCategoryId(),
                    user.getId(),
                    null,  // assignedBy - null, так как пользователь создает задачу для себя
                    choreDto.getDueDate()
            );
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            List<Category> categories = categoryService.getAvailableCategories(user.getFamily() != null ? user.getFamily().getId() : null);
            model.addAttribute("categories", categories);
            model.addAttribute("user", user);
            return "chore-form";
        }
    }

    @GetMapping
    public String listChores(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        List<com.example.coursework.model.Chore> userChores = choreService.getUserChores(user.getId());
        List<com.example.coursework.model.Chore> defaultChores = choreService.getDefaultChores();

        model.addAttribute("userChores", userChores);
        model.addAttribute("defaultChores", defaultChores);
        model.addAttribute("user", user);

        return "chores";
    }

    @PostMapping("/assign-default/{choreId}")
    public String takeDefaultChore(@PathVariable Long choreId,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());

        // Создаем копию задачи для пользователя
        com.example.coursework.model.Chore defaultChore = choreService.findById(choreId);

        choreService.createChore(
                defaultChore.getName(),
                defaultChore.getDescription(),
                defaultChore.getPoints(),
                defaultChore.getCategory() != null ? defaultChore.getCategory().getId() : null,
                user.getId(),
                null,  // assignedBy = null (ребенок взял задачу сам)
                null
        );

        return "redirect:/chores";
    }

    @PostMapping("/{choreId}/request-review")
    public String requestReview(@PathVariable Long choreId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        com.example.coursework.model.Chore chore = choreService.findById(choreId);

        // Проверяем, что задача принадлежит пользователю
        if (chore.getUser().getId().equals(user.getId())) {
            choreService.requestReview(choreId);
        }

        return "redirect:/chores";
    }
}
