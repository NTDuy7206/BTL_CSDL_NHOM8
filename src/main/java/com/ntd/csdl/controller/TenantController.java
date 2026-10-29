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
    private final com.ntd.csdl.service.LandlordService landlords;

    @GetMapping
    public ResponseEntity<List<Tenant>> getAll(org.springframework.security.core.Authentication auth) {
        var ids=landlords.tenantIds(auth.getName());
        return ResponseEntity.ok(tenantService.getAll().stream().filter(t->ids.contains(t.getTenantId())).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getById(
            @PathVariable String id, org.springframework.security.core.Authentication auth) {
        landlords.requireTenant(auth.getName(),id);

        return ResponseEntity.ok(
                tenantService.getById(id)
        );
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping
    public ResponseEntity<Tenant> create(
            @Valid @RequestBody TenantDTO dto,org.springframework.security.core.Authentication auth) {

        Tenant created=tenantService.create(dto);landlords.linkTenant(auth.getName(),created.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> update(
            @PathVariable String id,
            @Valid @RequestBody TenantDTO dto,org.springframework.security.core.Authentication auth) {
        landlords.requireExclusiveTenant(auth.getName(),id);

        return ResponseEntity.ok(
                tenantService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,org.springframework.security.core.Authentication auth) {
        landlords.requireExclusiveTenant(auth.getName(),id);

        tenantService.delete(id);

        return ResponseEntity.noContent().build();
    }
}