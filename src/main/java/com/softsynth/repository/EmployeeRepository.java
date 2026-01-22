package com.softsynth.repository;

import com.softsynth.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Add @Query annotation to ensure proper query
    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId")
    List<Employee> findByManagerId(@Param("managerId") Long managerId);

    List<Employee> findByIsActiveTrue();
    Optional<Employee> findByEmployeeId(String employeeId);

    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId AND e.isActive = true")
    List<Employee> findActiveEmployeesByManager(@Param("managerId") Long managerId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.manager.id = :managerId")
    long countByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.manager.id = :managerId")
    List<Employee> findByManagerIdWithManager(@Param("managerId") Long managerId);

}