package com.ntd.csdl.controller;

import com.ntd.csdl.dto.EmployeeDTO;
import com.ntd.csdl.entity.Employee;
import com.ntd.csdl.service.EmployeeService;
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

    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(
                employeeService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                employeeService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Employee> create(
            @Valid @RequestBody EmployeeDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(employeeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable String id,
            @Valid @RequestBody EmployeeDTO dto) {

        return ResponseEntity.ok(
                employeeService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        employeeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchByName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                employeeService.searchByName(name)
        );
    }
}