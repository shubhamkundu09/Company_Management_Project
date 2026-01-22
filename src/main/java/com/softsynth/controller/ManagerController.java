package com.softsynth.controller;

import com.softsynth.dto.EmployeeDTO;
import com.softsynth.dto.PasswordChangeRequest;
import com.softsynth.dto.UserCreateRequest;
import com.softsynth.entity.Employee;
import com.softsynth.entity.User;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/managers")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerController {

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

    // Team Management
    @GetMapping("/team")
    public ResponseEntity<?> getTeam() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Manager not found");
            return ResponseEntity.badRequest().body(error);
        }

        Long managerId = userOpt.get().getId();
        List<Employee> employees = userService.getEmployeesByManager(managerId);

        // Convert to DTO to avoid circular references
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(employeeDTOs);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setEmail(employee.getEmail());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setDepartment(employee.getDepartment());
        dto.setActive(employee.isActive());
        dto.setVerified(employee.isVerified());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());

        // Add manager info if needed
        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerName(employee.getManager().getFirstName() + " " +
                    employee.getManager().getLastName());
        }

        return dto;
    }

    @GetMapping("/team/{employeeId}")
    public ResponseEntity<?> getTeamMember(@PathVariable Long employeeId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Manager not found");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Employee employee = userService.getEmployeeByIdAndManager(employeeId, userOpt.get().getId());
            EmployeeDTO dto = convertToDTO(employee);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}