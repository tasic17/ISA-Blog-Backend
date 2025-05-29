package com.example.demo.controllers;

import com.example.demo.models.JwtAuthenticationModel;
import com.example.demo.models.UserSignInModel;
import com.example.demo.models.UserSignUpModel;
import com.example.demo.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationModel> signUp(@Valid @RequestBody UserSignUpModel input) {
        log.info("Received signup request for email: {}", input.getEmail());
        log.debug("Signup request details: {}", input);
        
        try {
            JwtAuthenticationModel response = authenticationService.signUp(input);
            log.info("Successfully registered user with email: {}", input.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationModel> signIn(@Valid @RequestBody UserSignInModel input) {
        log.info("Received signin request for email: {}", input.getEmail());
        
        try {
            JwtAuthenticationModel response = authenticationService.signIn(input);
            log.info("Successfully authenticated user with email: {}", input.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during user authentication: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationModel> refreshToken(@RequestParam String refreshToken) {
        log.info("Received token refresh request");
        
        try {
            JwtAuthenticationModel response = authenticationService.refreshToken(refreshToken);
            log.info("Successfully refreshed token");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            throw e;
        }
    }
}