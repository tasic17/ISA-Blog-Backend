package com.example.demo.controllers;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.mappers.UserMapper;
import com.example.demo.models.UserModel;
import com.example.demo.repositories.IRoleRepository;
import com.example.demo.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminUserController {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Admin fetching all users - page: {}, size: {}", page, size);

        try {
            Sort sort = sortDir.equals("desc") ?
                    Sort.by(sortBy).descending() :
                    Sort.by(sortBy).ascending();

            Page<User> userPage = userRepository.findAll(PageRequest.of(page, size, sort));

            List<UserModel> users = userPage.getContent().stream()
                    .map(UserMapper::toModel)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("currentPage", page);
            response.put("pageSize", size);

            log.info("Successfully fetched {} users", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching users", e);
            throw new RuntimeException("Error fetching users: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable Integer id) {
        log.info("Admin fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(UserMapper.toModel(user));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<UserModel> assignRole(
            @PathVariable Integer id,
            @RequestParam String roleName
    ) {
        log.info("Admin assigning role {} to user {}", roleName, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
            log.info("Successfully assigned role {} to user {}", roleName, id);
        } else {
            log.info("User {} already has role {}", id, roleName);
        }

        return ResponseEntity.ok(UserMapper.toModel(user));
    }

    @DeleteMapping("/{id}/roles")
    public ResponseEntity<UserModel> removeRole(
            @PathVariable Integer id,
            @RequestParam String roleName
    ) {
        log.info("Admin removing role {} from user {}", roleName, id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().remove(role);
        userRepository.save(user);

        log.info("Successfully removed role {} from user {}", roleName, id);
        return ResponseEntity.ok(UserMapper.toModel(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        log.info("Admin deleting user: {}", id);
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
        log.info("Successfully deleted user: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserModel>> searchUsers(@RequestParam String query) {
        log.info("Admin searching users with query: {}", query);

        List<User> users = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContaining(
                query, query, query);

        List<UserModel> userModels = users.stream()
                .map(UserMapper::toModel)
                .toList();

        return ResponseEntity.ok(userModels);
    }
}