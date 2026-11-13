package com.ntd.csdl.controller;

import com.ntd.csdl.dto.IncidentDTO;
import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.service.IncidentService;
import com.ntd.csdl.service.RentalRecordAccess;
import com.ntd.csdl.service.RentalWorkflowService;
import com.ntd.csdl.service.StayService;
import org.springframework.security.core.Authentication;
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
    private final RentalRecordAccess access;

    @PutMapping("/{id}/complete")
    public Incident complete(@PathVariable String id,org.springframework.security.core.Authentication auth){return incidentService.complete(id,auth.getName());}

    // GET ALL
    @GetMapping
    public ResponseEntity<List<Incident>> getAll(org.springframework.security.core.Authentication auth) {

        return ResponseEntity.ok(
                access.incidents(auth,incidentService.getAll())
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Incident> getById(
            @PathVariable String id, Authentication auth) {

        return ResponseEntity.ok(
                RentalRecordAccess.requireVisible(access.incidents(auth,List.of(incidentService.getById(id))))
        );
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Incident> create(
            @Valid @RequestBody IncidentDTO dto, org.springframework.security.core.Authentication auth) {
        boolean admin=RentalWorkflowService.admin(auth);
        if(!admin) {
            rentals.requireRoom(auth.getName(),dto.getRoomId());
            if(dto.getIncidentContent()==null || dto.getIncidentContent().isBlank() || dto.getIncidentContent().length()>500)
                throw new IllegalArgumentException("Nội dung sự cố cần có từ 1 đến 500 ký tự");
            dto.setIncidentId(RentalWorkflowService.id("SC"));
            dto.setReportDate(StayService.today());dto.setStatus("PENDING");
        } else landlords.requireOwner(auth.getName(),dto.getRoomId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        admin ? incidentService.create(dto) : incidentService.report(dto,auth.getName())
                );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Incident> update(
            @PathVariable String id,
            @Valid @RequestBody IncidentDTO dto, Authentication auth) {
        requireOwned(id,auth);
        landlords.requireOwner(auth.getName(),dto.getRoomId());

        return ResponseEntity.ok(
                incidentService.update(id, dto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id, Authentication auth) {

        requireOwned(id,auth);
        incidentService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // GET BY ROOM
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Incident>> getByRoom(
            @PathVariable String roomId, Authentication auth) {

        return ResponseEntity.ok(
                access.incidents(auth,incidentService.getByRoom(roomId))
        );
    }

    // GET BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Incident>> getByStatus(
            @PathVariable String status, Authentication auth) {

        return ResponseEntity.ok(
                access.incidents(auth,incidentService.getByStatus(status))
        );
    }

    private void requireOwned(String id, Authentication auth) {
        var incident=incidentService.getById(id);
        landlords.requireOwner(auth.getName(),incident.getRoom()==null?null:incident.getRoom().getRoomId());
    }
}
