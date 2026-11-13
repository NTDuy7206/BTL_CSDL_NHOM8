package com.ntd.csdl.controller;

import com.ntd.csdl.dto.CleaningSupportDTO;
import com.ntd.csdl.entity.CleaningSupport;
import com.ntd.csdl.service.CleaningSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaning-supports")
@RequiredArgsConstructor
public class CleaningSupportController {

    private final CleaningSupportService cleaningSupportService;
    private final com.ntd.csdl.service.LandlordService landlords;
    private final com.ntd.csdl.service.StaffService staff;

    public record TenantCleaningRequest(String roomId, String taskContent) {}

    @GetMapping("/mine")
    public List<CleaningSupportService.TenantRequest> mine(Authentication auth) {
        return cleaningSupportService.mine(auth.getName());
    }

    @PostMapping("/mine")
    @ResponseStatus(HttpStatus.CREATED)
    public CleaningSupportService.TenantRequest requestCleaning(@RequestBody TenantCleaningRequest body, Authentication auth) {
        return cleaningSupportService.requestCleaning(auth.getName(), body.roomId(), body.taskContent());
    }

    private CleaningSupport owned(String id, Authentication auth) {
        CleaningSupport support = cleaningSupportService.getById(id);
        if (support.getRoom() == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        landlords.requireOwner(auth.getName(), support.getRoom().getRoomId());
        return support;
    }

    private List<CleaningSupport> owned(List<CleaningSupport> supports, Authentication auth) {
        return supports.stream().filter(support -> support.getRoom() != null &&
                landlords.owns(auth.getName(), support.getRoom().getRoomId())).toList();
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CleaningSupport>> getAll(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(
                owned(cleaningSupportService.getAll(), auth)
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CleaningSupport> getById(
            @PathVariable String id, Authentication auth) {

        return ResponseEntity.ok(
                owned(id, auth)
        );
    }

    // CREATE
    @PostMapping
    public ResponseEntity<CleaningSupport> create(
            @Valid @RequestBody CleaningSupportDTO dto, Authentication auth) {
        landlords.requireOwner(auth.getName(), dto.getRoomId());
        staff.checkAssignment(auth.getName(), dto.getEmployeeId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cleaningSupportService.create(dto));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CleaningSupport> update(
            @PathVariable String id,
            @Valid @RequestBody CleaningSupportDTO dto, Authentication auth) {
        var current = owned(id, auth);
        landlords.requireOwner(auth.getName(), dto.getRoomId());
        if (current.getEmployee() == null || !current.getEmployee().getEmployeeId().equals(dto.getEmployeeId())) {
            staff.checkAssignment(auth.getName(), dto.getEmployeeId());
        }

        return ResponseEntity.ok(
                cleaningSupportService.update(id, dto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id, Authentication auth) {
        owned(id, auth);

        cleaningSupportService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // GET BY ROOM
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<CleaningSupport>> getByRoom(
            @PathVariable String roomId, Authentication auth) {
        landlords.requireOwner(auth.getName(), roomId);

        return ResponseEntity.ok(
                cleaningSupportService.getByRoom(roomId)
        );
    }

    // GET BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<CleaningSupport>> getByEmployee(
            @PathVariable String employeeId, Authentication auth) {

        return ResponseEntity.ok(
                owned(cleaningSupportService.getByEmployee(employeeId), auth)
        );
    }

    // GET UNCOMPLETED
    @GetMapping("/uncompleted")
    public ResponseEntity<List<CleaningSupport>> getUncompleted(Authentication auth) {

        return ResponseEntity.ok(
                owned(cleaningSupportService.getUncompleted(), auth)
        );
    }

    // COMPLETE
    @PutMapping("/{id}/complete")
    public ResponseEntity<CleaningSupport> complete(
            @PathVariable String id,org.springframework.security.core.Authentication auth) {
        owned(id, auth);

        return ResponseEntity.ok(
                cleaningSupportService.complete(id)
        );
    }
}
