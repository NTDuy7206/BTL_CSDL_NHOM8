package com.ntd.csdl.controller;

import com.ntd.csdl.dto.TenantDTO;
import com.ntd.csdl.entity.Tenant;
import com.ntd.csdl.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public ResponseEntity<List<Tenant>> getAll() {
        return ResponseEntity.ok(tenantService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                tenantService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Tenant> create(
            @Valid @RequestBody TenantDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tenantService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> update(
            @PathVariable String id,
            @Valid @RequestBody TenantDTO dto) {

        return ResponseEntity.ok(
                tenantService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        tenantService.delete(id);

        return ResponseEntity.noContent().build();
    }
}