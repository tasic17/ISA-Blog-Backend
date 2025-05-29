package com.example.demo.services;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.mappers.UserMapper;
import com.example.demo.models.JwtAuthenticationModel;
import com.example.demo.models.UserSignInModel;
import com.example.demo.models.UserSignUpModel;
import com.example.demo.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtAuthenticationModel signUp(UserSignUpModel input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        User user = new User();
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setContactNumber(input.getContactNumber());
        user.setPassword(passwordEncoder.encode(input.getPassword()));

        // Dodeli READER rolu po defaultu
        Role readerRole = new Role();
        readerRole.setId(1); // READER role ID
        readerRole.setName("READER");
        user.setRoles(new ArrayList<>());
        user.getRoles().add(readerRole);

        User savedUser = userRepository.save(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", savedUser.getId());
        extraClaims.put("fullName", savedUser.getFullName());

        String accessToken = jwtService.generateToken(extraClaims, savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return JwtAuthenticationModel.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserMapper.toModel(savedUser))
                .build();
    }

    public JwtAuthenticationModel signIn(UserSignInModel input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("fullName", user.getFullName());

        String accessToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return JwtAuthenticationModel.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserMapper.toModel(user))
                .build();
    }

    public JwtAuthenticationModel refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (jwtService.isTokenValid(refreshToken, user)) {
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("fullName", user.getFullName());

            String accessToken = jwtService.generateToken(extraClaims, user);

            return JwtAuthenticationModel.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserMapper.toModel(user))
                    .build();
        }

        throw new ValidationException("Invalid refresh token");
    }
}