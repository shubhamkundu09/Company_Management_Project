package com.softsynth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnore  // Prevent serialization of manager's employees to avoid circular reference
    private Manager manager;

    @Column(name = "employee_id", unique = true)
    private String employeeId;

    private String department;

    public Employee() {
        this.setRole(Role.EMPLOYEE);
    }
}