package com.softsynth.controller;

import com.softsynth.dto.OtpRequest;
import com.softsynth.dto.PasswordChangeRequest;
import com.softsynth.dto.UserCreateRequest;
import com.softsynth.entity.Admin;
import com.softsynth.entity.Employee;
import com.softsynth.entity.Manager;
import com.softsynth.entity.User;
import com.softsynth.service.OtpService;
import com.softsynth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserService userService;

    // Manager Registration
    @PostMapping("/managers/initiate-registration")
    public ResponseEntity<?> initiateManagerRegistration(@Valid @RequestBody OtpRequest otpRequest) {
        if (userService.emailExists(otpRequest.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already registered");
            return ResponseEntity.badRequest().body(error);
        }

        otpRequest.setRole(User.Role.MANAGER);
        otpService.generateOtp(otpRequest.getEmail(), otpRequest.getRole(), null);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to manager's email for registration");
        return ResponseEntity.ok(response);
    }

    // Employee Registration
    @PostMapping("/employees/initiate-registration")
    public ResponseEntity<?> initiateEmployeeRegistration(@Valid @RequestBody OtpRequest otpRequest) {
        if (userService.emailExists(otpRequest.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already registered");
            return ResponseEntity.badRequest().body(error);
        }

        if (otpRequest.getManagerId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Manager ID is required for employee registration");
            return ResponseEntity.badRequest().body(error);
        }

        otpRequest.setRole(User.Role.EMPLOYEE);
        otpService.generateOtp(otpRequest.getEmail(), otpRequest.getRole(), otpRequest.getManagerId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to employee's email for registration");
        return ResponseEntity.ok(response);
    }

    // User Management
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, @RequestParam boolean active) {
        try {
            User user = userService.updateUserStatus(userId, active);
            String status = active ? "activated" : "deactivated";
            Map<String, String> response = new HashMap<>();
            response.put("message", "User " + status + " successfully");
            response.put("userId", userId.toString());
            response.put("status", String.valueOf(active));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Manager Management
    @GetMapping("/managers")
    public ResponseEntity<?> getAllManagers() {
        List<Manager> managers = userService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    // Employee Management
    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees() {
        List<Employee> employees = userService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/managers/{managerId}/employees")
    public ResponseEntity<?> getEmployeesByManager(@PathVariable Long managerId) {
        List<Employee> employees = userService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(employees);
    }

    // Admin Profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Admin not found");
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok(userOpt.get());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserCreateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            User updatedUser = userService.updateProfile(email, request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        try {
            boolean success = userService.changePasswordForAdmin(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}