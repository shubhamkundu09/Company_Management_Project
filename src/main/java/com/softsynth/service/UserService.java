package com.softsynth.service;

import com.softsynth.dto.PasswordChangeRequest;
import com.softsynth.dto.UserCreateRequest;
import com.softsynth.entity.*;
import com.softsynth.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Create user with temporary password
    @Transactional
    public User createUser(UserCreateRequest request, String password) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user;

        switch (request.getRole()) {
            case ADMIN:
                Admin admin = new Admin();
                user = admin;
                break;

            case MANAGER:
                Manager manager = new Manager();
                user = manager;
                break;

            case EMPLOYEE:
                Employee employee = new Employee();
                if (request.getManagerId() != null) {
                    Manager managerRef = managerRepository.findById(request.getManagerId())
                            .orElseThrow(() -> new RuntimeException("Manager not found with ID: " + request.getManagerId()));
                    employee.setManager(managerRef);
                }
                employee.setEmployeeId(generateEmployeeId());
                employee.setDepartment(request.getDepartment() != null ? request.getDepartment() : "General");
                user = employee;
                break;

            default:
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        user.setVerified(true);

        User savedUser = userRepository.save(user);

        // Send registration email
        emailService.sendRegistrationEmail(
                request.getEmail(),
                request.getRole().toString(),
                password
        );

        return savedUser;
    }

    private String generateEmployeeId() {
        return "EMP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    @Transactional
    public User updateUserStatus(Long userId, boolean isActive) {
        User user = getUserById(userId);
        user.setActive(isActive);
        return userRepository.save(user);
    }

    @Transactional
    public boolean changePassword(String email, PasswordChangeRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Check if new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return true;
    }


    public boolean changePasswordForAdmin(PasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAdminEmail = authentication.getName();

        Optional<User> adminOpt = userRepository.findByEmail(currentAdminEmail);
        if (adminOpt.isEmpty() || adminOpt.get().getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Unauthorized access");
        }

        return changePassword(currentAdminEmail, request);
    }

    public List<Employee> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerIdWithManager(managerId);  // Use the new method
    }

    public Manager getManagerForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        return employee.getManager();
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User updateProfile(String email, UserCreateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        // Update role-specific fields
        if (user instanceof Employee && request.getDepartment() != null) {
            ((Employee) user).setDepartment(request.getDepartment());
        }

        return userRepository.save(user);
    }

    public List<Manager> getAllManagers() {
        return managerRepository.findAll();
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeByIdAndManager(Long employeeId, Long managerId) {
        return employeeRepository.findById(employeeId)
                .filter(emp -> emp.getManager() != null && emp.getManager().getId().equals(managerId))
                .orElseThrow(() -> new RuntimeException("Employee not found or does not belong to this manager"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public boolean isUserActive(String email) {
        return userRepository.findByEmail(email)
                .map(User::isActive)
                .orElse(false);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUser(Long userId, UserCreateRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        // Only admin can change roles
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin && request.getRole() != null) {
            user.setRole(request.getRole());

            // Handle role-specific updates
            if (user instanceof Employee && request.getManagerId() != null) {
                Manager manager = managerRepository.findById(request.getManagerId())
                        .orElseThrow(() -> new RuntimeException("Manager not found"));
                ((Employee) user).setManager(manager);
            }

            if (request.getDepartment() != null && user instanceof Employee) {
                ((Employee) user).setDepartment(request.getDepartment());
            }
        }

        return userRepository.save(user);
    }
}