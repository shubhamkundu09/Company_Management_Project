package com.softsynth.repository;

import com.softsynth.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
    // Find manager by email
    Optional<Manager> findByEmail(String email);

    // Correct method name - use "IsActive" not "Active"
    List<Manager> findByIsActiveTrue();

    // Count active managers
    long countByIsActiveTrue();

    // Or use custom query
    @Query("SELECT m FROM Manager m WHERE m.isActive = true")
    List<Manager> findAllActiveManagers();
}