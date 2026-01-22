package com.softsynth.controller;

import com.softsynth.dto.PasswordChangeRequest;
import com.softsynth.dto.UserCreateRequest;
import com.softsynth.entity.Employee;
import com.softsynth.entity.Manager;
import com.softsynth.entity.User;
import com.softsynth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER', 'ADMIN')")
public class EmployeeController {

    @Autowired
    private UserService userService;

    // Profile Management
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            boolean success = userService.changePassword(email, request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Manager Information
    @GetMapping("/manager")
    public ResponseEntity<?> getManager() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Employee not found");
            return ResponseEntity.badRequest().body(error);
        }

        if (!(userOpt.get() instanceof Employee)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User is not an employee");
            return ResponseEntity.badRequest().body(error);
        }

        Employee employee = (Employee) userOpt.get();
        Manager manager = employee.getManager();

        if (manager == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No manager assigned");
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok(manager);
    }

    // Team Members (Other employees under same manager)
    @GetMapping("/team-mates")
    public ResponseEntity<?> getTeamMates() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Employee not found");
            return ResponseEntity.badRequest().body(error);
        }

        if (!(userOpt.get() instanceof Employee)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User is not an employee");
            return ResponseEntity.badRequest().body(error);
        }

        Employee employee = (Employee) userOpt.get();
        if (employee.getManager() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Employee has no manager assigned");
            return ResponseEntity.badRequest().body(error);
        }

        Long managerId = employee.getManager().getId();
        return ResponseEntity.ok(userService.getEmployeesByManager(managerId));
    }
}