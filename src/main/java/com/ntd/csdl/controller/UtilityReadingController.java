package com.ntd.csdl.controller;

import com.ntd.csdl.dto.UtilityReadingDTO;
import com.ntd.csdl.entity.UtilityReading;
import com.ntd.csdl.service.UtilityReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utility-readings")
@RequiredArgsConstructor
public class UtilityReadingController {

    private final UtilityReadingService utilityReadingService;

    @GetMapping
    public ResponseEntity<List<UtilityReading>> getAll() {
        return ResponseEntity.ok(
                utilityReadingService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilityReading> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                utilityReadingService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UtilityReading> create(
            @Valid @RequestBody UtilityReadingDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utilityReadingService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilityReading> update(
            @PathVariable String id,
            @Valid @RequestBody UtilityReadingDTO dto) {

        return ResponseEntity.ok(
                utilityReadingService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        utilityReadingService.delete(id);

        return ResponseEntity.noContent().build();
    }
}