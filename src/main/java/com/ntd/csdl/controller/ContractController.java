package com.ntd.csdl.controller;

import com.ntd.csdl.dto.ContractDTO;
import com.ntd.csdl.entity.Contract;
import com.ntd.csdl.service.ContractService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public ResponseEntity<List<Contract>> getAll() {
        return ResponseEntity.ok(
                contractService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                contractService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Contract> create(
            @Valid @RequestBody ContractDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contractService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> update(
            @PathVariable String id,
            @Valid @RequestBody ContractDTO dto) {

        return ResponseEntity.ok(
                contractService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        contractService.delete(id);

        return ResponseEntity.noContent().build();
    }
}