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
import org.springframework.security.core.Authentication;
import com.ntd.csdl.service.LandlordService;

@RestController
@RequestMapping("/api/utility-readings")
@RequiredArgsConstructor
public class UtilityReadingController {

    private final UtilityReadingService utilityReadingService;
    private final LandlordService landlords;

    @GetMapping
    public ResponseEntity<List<UtilityReading>> getAll(Authentication auth) {
        return ResponseEntity.ok(
                utilityReadingService.getAll().stream().filter(reading ->
                    reading.getRoom()!=null && landlords.owns(auth.getName(),reading.getRoom().getRoomId())).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilityReading> getById(
            @PathVariable String id, Authentication auth) {

        return ResponseEntity.ok(
                owned(id,auth)
        );
    }

    @PostMapping
    public ResponseEntity<UtilityReading> create(
            @Valid @RequestBody UtilityReadingDTO dto, Authentication auth) {
        landlords.requireOwner(auth.getName(), dto.getRoomId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utilityReadingService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilityReading> update(
            @PathVariable String id,
            @Valid @RequestBody UtilityReadingDTO dto, Authentication auth) {
        owned(id,auth);
        landlords.requireOwner(auth.getName(), dto.getRoomId());

        return ResponseEntity.ok(
                utilityReadingService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id, Authentication auth) {
        owned(id,auth);

        utilityReadingService.delete(id);

        return ResponseEntity.noContent().build();
    }

    private UtilityReading owned(String id, Authentication auth) {
        var reading=utilityReadingService.getById(id);
        landlords.requireOwner(auth.getName(),reading.getRoom()==null?null:reading.getRoom().getRoomId());
        return reading;
    }
}
