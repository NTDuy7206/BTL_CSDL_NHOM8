package com.ntd.csdl.controller;

import com.ntd.csdl.dto.IncidentResolutionDTO;
import com.ntd.csdl.entity.IncidentResolution;
import com.ntd.csdl.service.IncidentResolutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incident-resolutions")
@RequiredArgsConstructor
public class IncidentResolutionController {

    private final IncidentResolutionService incidentResolutionService;

    private final com.ntd.csdl.service.LandlordService landlords;

    @PutMapping("/{id}/complete")
    public IncidentResolution complete(@PathVariable String id,org.springframework.security.core.Authentication auth) {
        landlords.requireOwner(auth.getName(),incidentResolutionService.getById(id).getIncident().getRoom().getRoomId());
        return incidentResolutionService.complete(id);
    }

    @GetMapping
    public ResponseEntity<List<IncidentResolution>> getAll(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(
                incidentResolutionService.getAll().stream().filter(r->landlords.owns(auth.getName(),r.getIncident().getRoom().getRoomId())).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentResolution> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                incidentResolutionService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<IncidentResolution> create(
            @Valid @RequestBody IncidentResolutionDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(incidentResolutionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResolution> update(
            @PathVariable String id,
            @Valid @RequestBody IncidentResolutionDTO dto) {

        return ResponseEntity.ok(
                incidentResolutionService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        incidentResolutionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
