package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.mappers.UserMapper;
import com.example.demo.models.UserModel;
import com.example.demo.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class UserProfileController {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<UserModel> getCurrentUserProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(UserMapper.toModel(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserModel> updateProfile(@RequestBody Map<String, String> profileData) {
        User user = getCurrentUser();

        log.info("Updating profile for user: {}", user.getEmail());
        log.info("Profile data: {}", profileData);

        // Update user fields
        if (profileData.containsKey("firstName")) {
            user.setFirstName(profileData.get("firstName"));
        }
        if (profileData.containsKey("lastName")) {
            user.setLastName(profileData.get("lastName"));
        }
        if (profileData.containsKey("contactNumber")) {
            user.setContactNumber(profileData.get("contactNumber"));
        }
        if (profileData.containsKey("bio")) {
            user.setBio(profileData.get("bio"));
        }
        if (profileData.containsKey("profilePictureUrl")) {
            user.setProfilePictureUrl(profileData.get("profilePictureUrl"));
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", user.getEmail());

        return ResponseEntity.ok(UserMapper.toModel(updatedUser));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> passwordData) {
        User user = getCurrentUser();

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        log.info("Password change request for user: {}", user.getEmail());

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Invalid current password for user: {}", user.getEmail());
            return ResponseEntity.badRequest()
                    .body(Map.of("detail", "Trenutna lozinka nije tačna"));
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());

        return ResponseEntity.ok(Map.of("message", "Lozinka je uspešno promenjena"));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}