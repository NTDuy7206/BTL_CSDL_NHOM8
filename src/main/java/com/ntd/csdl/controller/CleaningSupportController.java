package com.ntd.csdl.controller;

import com.ntd.csdl.dto.CleaningSupportDTO;
import com.ntd.csdl.entity.CleaningSupport;
import com.ntd.csdl.service.CleaningSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaning-supports")
@RequiredArgsConstructor
public class CleaningSupportController {

    private final CleaningSupportService cleaningSupportService;

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CleaningSupport>> getAll() {
        return ResponseEntity.ok(
                cleaningSupportService.getAll()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CleaningSupport> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                cleaningSupportService.getById(id)
        );
    }

    // CREATE
    @PostMapping
    public ResponseEntity<CleaningSupport> create(
            @Valid @RequestBody CleaningSupportDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cleaningSupportService.create(dto));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CleaningSupport> update(
            @PathVariable String id,
            @Valid @RequestBody CleaningSupportDTO dto) {

        return ResponseEntity.ok(
                cleaningSupportService.update(id, dto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        cleaningSupportService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // GET BY ROOM
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<CleaningSupport>> getByRoom(
            @PathVariable String roomId) {

        return ResponseEntity.ok(
                cleaningSupportService.getByRoom(roomId)
        );
    }

    // GET BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<CleaningSupport>> getByEmployee(
            @PathVariable String employeeId) {

        return ResponseEntity.ok(
                cleaningSupportService.getByEmployee(employeeId)
        );
    }

    // GET UNCOMPLETED
    @GetMapping("/uncompleted")
    public ResponseEntity<List<CleaningSupport>> getUncompleted() {

        return ResponseEntity.ok(
                cleaningSupportService.getUncompleted()
        );
    }

    // COMPLETE
    @PutMapping("/{id}/complete")
    public ResponseEntity<CleaningSupport> complete(
            @PathVariable String id) {

        return ResponseEntity.ok(
                cleaningSupportService.complete(id)
        );
    }
}