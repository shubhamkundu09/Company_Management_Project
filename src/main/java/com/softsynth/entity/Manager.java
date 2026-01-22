package com.softsynth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "managers")
@Data
@EqualsAndHashCode(callSuper = true)
public class Manager extends User {

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonIgnore  // Add this to avoid circular references
    private List<Employee> employees = new ArrayList<>();

    public Manager() {
        this.setRole(Role.MANAGER);
    }
}