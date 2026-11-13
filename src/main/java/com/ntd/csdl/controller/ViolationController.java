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
import org.springframework.security.core.Authentication;
import com.ntd.csdl.service.LandlordService;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;
    private final LandlordService landlords;

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
            @Valid @RequestBody ViolationDTO dto, Authentication auth) {
        landlords.requireOwner(auth.getName(),dto.getRoomId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(violationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Violation> update(
            @PathVariable String id,
            @Valid @RequestBody ViolationDTO dto, Authentication auth) {
        requireOwned(id,auth);
        landlords.requireOwner(auth.getName(),dto.getRoomId());

        return ResponseEntity.ok(
                violationService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id, Authentication auth) {

        requireOwned(id,auth);
        violationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    private void requireOwned(String id, Authentication auth) {
        var violation=violationService.getById(id);
        landlords.requireOwner(auth.getName(),violation.getRoom()==null?null:violation.getRoom().getRoomId());
    }
}
