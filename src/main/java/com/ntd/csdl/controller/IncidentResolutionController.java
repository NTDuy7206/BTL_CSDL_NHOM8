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
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/incident-resolutions")
@RequiredArgsConstructor
public class IncidentResolutionController {

    private final IncidentResolutionService incidentResolutionService;

    private final com.ntd.csdl.service.LandlordService landlords;
    private final com.ntd.csdl.service.StaffService staff;
    private final com.ntd.csdl.service.IncidentService incidents;

    private IncidentResolution owned(String id, Authentication auth) {
        var row = incidentResolutionService.getById(id);
        landlords.requireOwner(auth.getName(), row.getIncident().getRoom().getRoomId());
        return row;
    }

    @PutMapping("/{id}/complete")
    public IncidentResolution complete(@PathVariable String id,org.springframework.security.core.Authentication auth) {
        owned(id, auth);
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
            @PathVariable String id, Authentication auth) {

        return ResponseEntity.ok(
                owned(id, auth)
        );
    }

    @PostMapping
    public ResponseEntity<IncidentResolution> create(
            @Valid @RequestBody IncidentResolutionDTO dto, Authentication auth) {
        landlords.requireOwner(auth.getName(), incidents.getById(dto.getIncidentId()).getRoom().getRoomId());
        staff.checkAssignment(auth.getName(), dto.getEmployeeId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(incidentResolutionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncidentResolution> update(
            @PathVariable String id,
            @Valid @RequestBody IncidentResolutionDTO dto, Authentication auth) {
        var current = owned(id, auth);
        landlords.requireOwner(auth.getName(), incidents.getById(dto.getIncidentId()).getRoom().getRoomId());
        if (current.getEmployee() == null || !current.getEmployee().getEmployeeId().equals(dto.getEmployeeId())) {
            staff.checkAssignment(auth.getName(), dto.getEmployeeId());
        }

        return ResponseEntity.ok(
                incidentResolutionService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id, Authentication auth) {
        owned(id, auth);

        incidentResolutionService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
