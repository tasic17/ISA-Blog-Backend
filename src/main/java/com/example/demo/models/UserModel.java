package com.example.demo.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserModel {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String bio;
    private String profilePictureUrl;
    private List<String> roles;
    private LocalDateTime createdAt;
}