package com.ntd.csdl.controller;

import com.ntd.csdl.dto.IncidentDTO;
import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final com.ntd.csdl.service.LandlordService landlords;
    private final com.ntd.csdl.service.RentalWorkflowService rentals;

    @PutMapping("/{id}/complete")
    public Incident complete(@PathVariable String id,org.springframework.security.core.Authentication auth){return incidentService.complete(id,auth.getName());}

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Incident>> getAll(org.springframework.security.core.Authentication auth) {

        return ResponseEntity.ok(
                incidentService.getAll().stream().filter(r->!com.ntd.csdl.service.RentalWorkflowService.admin(auth)||landlords.owns(auth.getName(),r.getRoom().getRoomId())).toList()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Incident> getById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                incidentService.getById(id)
        );
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Incident> create(
            @Valid @RequestBody IncidentDTO dto, org.springframework.security.core.Authentication auth) {
        if(!com.ntd.csdl.service.RentalWorkflowService.admin(auth)) {
            rentals.requireRoom(auth.getName(),dto.getRoomId());
            if(dto.getIncidentContent()==null || dto.getIncidentContent().isBlank() || dto.getIncidentContent().length()>500)
                throw new IllegalArgumentException("Nội dung sự cố cần có từ 1 đến 500 ký tự");
            dto.setIncidentId(com.ntd.csdl.service.RentalWorkflowService.id("SC"));
            dto.setReportDate(java.time.LocalDate.now());dto.setStatus("PENDING");
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        incidentService.create(dto)
                );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Incident> update(
            @PathVariable String id,
            @Valid @RequestBody IncidentDTO dto) {

        return ResponseEntity.ok(
                incidentService.update(id, dto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        incidentService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // GET BY ROOM
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Incident>> getByRoom(
            @PathVariable String roomId) {

        return ResponseEntity.ok(
                incidentService.getByRoom(roomId)
        );
    }

    // GET BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Incident>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                incidentService.getByStatus(status)
        );
    }
}