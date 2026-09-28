package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

    List<Employee> findByFullNameContainingIgnoreCase(String fullName);

    List<Employee> findByPosition(String position);

    List<Employee> findBySeniorityGreaterThan(Integer seniority);
}