package com.softsynth.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
@Data
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Enumerated(EnumType.STRING)
    private User.Role role;

    private Long managerId; // For employee creation

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean isUsed = false;
}