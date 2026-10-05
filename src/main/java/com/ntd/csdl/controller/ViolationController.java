package com.ntd.csdl.controller;

import com.ntd.csdl.dto.ViolationDTO;
import com.ntd.csdl.entity.Violation;
import com.ntd.csdl.service.ViolationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping
    public ResponseEntity<List<Violation>> getAll() {
        return ResponseEntity.ok(
                violationService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Violation> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                violationService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Violation> create(
            @Valid @RequestBody ViolationDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(violationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Violation> update(
            @PathVariable String id,
            @Valid @RequestBody ViolationDTO dto) {

        return ResponseEntity.ok(
                violationService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        violationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}