package com.ntd.csdl.controller;

import com.ntd.csdl.dto.RuleDTO;
import com.ntd.csdl.entity.Rule;
import com.ntd.csdl.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<List<Rule>> getAll() {
        return ResponseEntity.ok(
                ruleService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rule> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                ruleService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<Rule> create(
            @Valid @RequestBody RuleDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ruleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Rule> update(
            @PathVariable String id,
            @Valid @RequestBody RuleDTO dto) {

        return ResponseEntity.ok(
                ruleService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        ruleService.delete(id);

        return ResponseEntity.noContent().build();
    }
}