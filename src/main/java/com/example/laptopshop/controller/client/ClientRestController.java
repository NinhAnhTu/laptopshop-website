package com.example.laptopshop.controller.client;

import com.example.laptopshop.entity.User;
import com.example.laptopshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientRestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/check-password")
    public ResponseEntity<?> checkCurrentPassword(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) return ResponseEntity.badRequest().build();

        String oldPassword = payload.get("oldPassword");
        User currentUser = userService.getUserByEmail(principal.getName());

        if (currentUser != null && currentUser.getPassword() != null) {
            boolean isMatch = passwordEncoder.matches(oldPassword, currentUser.getPassword());
            return ResponseEntity.ok(Map.of("valid", isMatch));
        }

        return ResponseEntity.ok(Map.of("valid", false));
    }
}