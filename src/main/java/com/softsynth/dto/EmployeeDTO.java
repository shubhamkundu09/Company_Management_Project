package com.softsynth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String employeeId;
    private String department;
    private boolean isActive;
    private boolean isVerified;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long managerId;
    private String managerName;
}