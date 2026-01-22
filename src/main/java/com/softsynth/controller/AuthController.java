package com.softsynth.controller;

import com.softsynth.config.JwtUtil;
import com.softsynth.dto.*;
import com.softsynth.entity.Otp;
import com.softsynth.entity.User;
import com.softsynth.service.OtpService;
import com.softsynth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);

            Optional<User> userOpt = userService.getUserByEmail(authRequest.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String fullName = user.getFirstName() + " " + user.getLastName();
                Map<String, Object> response = new HashMap<>();
                response.put("token", jwt);
                response.put("email", user.getEmail());
                response.put("role", user.getRole());
                response.put("name", fullName);
                response.put("userId", user.getId());
                return ResponseEntity.ok(response);
            }

            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        Optional<Otp> otpOpt = otpService.validateOtp(request.getEmail(), request.getOtp());

        if (otpOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid or expired OTP");
            return ResponseEntity.badRequest().body(error);
        }

        Otp otp = otpOpt.get();

        if (userService.emailExists(request.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User already registered");
            return ResponseEntity.badRequest().body(error);
        }

        otpService.markOtpAsUsed(otp);

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setEmail(request.getEmail());
        userCreateRequest.setRole(otp.getRole());
        userCreateRequest.setManagerId(otp.getManagerId());
        userCreateRequest.setFirstName("New");
        userCreateRequest.setLastName("User");

        if (otp.getRole() == User.Role.EMPLOYEE) {
            userCreateRequest.setDepartment("To be updated");
        }

        try {
            User user = userService.createUser(userCreateRequest, tempPassword);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful! Check your email for credentials.");
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}