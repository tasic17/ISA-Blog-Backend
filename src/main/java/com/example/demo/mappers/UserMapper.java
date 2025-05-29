package com.example.demo.mappers;

import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.models.UserModel;

import java.util.stream.Collectors;

public class UserMapper {

    public static UserModel toModel(User entity) {
        if (entity == null) return null;

        return UserModel.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .contactNumber(entity.getContactNumber())
                .bio(entity.getBio())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .roles(entity.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}