package com.softsynth.dto;

import com.softsynth.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String email;
    private User.Role role;
    private String name;
    private String message;

    public AuthResponse(String token, String email, User.Role role, String name) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.name = name;
    }

    public AuthResponse(String message) {
        this.message = message;
    }
}