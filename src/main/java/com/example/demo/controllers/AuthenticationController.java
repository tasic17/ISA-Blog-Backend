package com.example.demo.controllers;

import com.example.demo.models.JwtAuthenticationModel;
import com.example.demo.models.UserSignInModel;
import com.example.demo.models.UserSignUpModel;
import com.example.demo.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationModel> signUp(@Valid @RequestBody UserSignUpModel input) {
        JwtAuthenticationModel response = authenticationService.signUp(input);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationModel> signIn(@Valid @RequestBody UserSignInModel input) {
        JwtAuthenticationModel response = authenticationService.signIn(input);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationModel> refreshToken(@RequestParam String refreshToken) {
        JwtAuthenticationModel response = authenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}