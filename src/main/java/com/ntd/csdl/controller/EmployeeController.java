package com.ntd.csdl.controller;

import com.ntd.csdl.dto.EmployeeDTO;
import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.service.EmployeeService;
import com.ntd.csdl.service.StaffService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final StaffService staff;

    @GetMapping
    public List<?> getAll(Authentication auth) {
        return visible(auth, employeeService.getAll());
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable String id, Authentication auth) {
        if (com.ntd.csdl.service.RentalWorkflowService.admin(auth)) {
            staff.checkProfileAccess(auth.getName(), id);
            return employeeService.getById(id);
        }
        return StaffService.PublicEmployee.of(employeeService.getById(id));
    }

    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Employee> create(
            @Valid @RequestBody EmployeeDTO dto, Authentication auth) {
        Employee employee = employeeService.create(dto);
        staff.claimNewProfile(auth.getName(), employee.getEmployeeId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable String id,
            @Valid @RequestBody EmployeeDTO dto, Authentication auth) {
        staff.checkProfileAccess(auth.getName(), id);

        return ResponseEntity.ok(
                employeeService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id, Authentication auth) {

        staff.checkProfileAccess(auth.getName(), id);
        employeeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<?> searchByName(@RequestParam String name, Authentication auth) {
        return visible(auth, employeeService.searchByName(name));
    }

    private List<?> visible(Authentication auth, List<Employee> employees) {
        if (com.ntd.csdl.service.RentalWorkflowService.admin(auth)) return staff.visibleProfiles(auth.getName(), employees);
        return employees.stream().map(StaffService.PublicEmployee::of).toList();
    }
}
