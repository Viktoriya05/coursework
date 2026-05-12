package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final UserService userService;

    @GetMapping
    public String familyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());

        if (user.getFamily() != null) {
            model.addAttribute("family", user.getFamily());
            model.addAttribute("members", userService.getFamilyMembers(user.getId()));
            model.addAttribute("inviteCode", user.getFamily().getInviteCode());
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
                             @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        userService.joinFamily(user.getId(), inviteCode);
        return "redirect:/family";
    }
}