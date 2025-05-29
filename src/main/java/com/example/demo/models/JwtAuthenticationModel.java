package com.example.demo.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtAuthenticationModel {
    private String accessToken;
    private String refreshToken;
    private UserModel user;
}