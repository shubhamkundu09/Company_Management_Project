package com.softsynth.dto;

import com.softsynth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String firstName;

    private String lastName;

    private User.Role role;

    private Long managerId; // For employee creation
    private String department;
}