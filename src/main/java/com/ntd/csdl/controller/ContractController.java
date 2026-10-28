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
    private final com.ntd.csdl.service.LandlordService landlords;
    private final com.ntd.csdl.service.RentalWorkflowService rentals;

    @GetMapping
    public ResponseEntity<List<Contract>> getAll(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(
                com.ntd.csdl.service.RentalWorkflowService.admin(auth) ? contractService.getAll().stream().filter(c->landlords.owns(auth.getName(),c.getRoom().getRoomId())).toList() : rentals.mine(auth.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getById(
            @PathVariable String id, org.springframework.security.core.Authentication auth) {
        Contract contract=contractService.getById(id);
        if(com.ntd.csdl.service.RentalWorkflowService.admin(auth))landlords.requireOwner(auth.getName(),contract.getRoom().getRoomId());
        else rentals.requireOwner(auth.getName(),contract);
        return ResponseEntity.ok(contract);
    }

    private String ownedId(String id,String name){landlords.requireOwner(name,contractService.getById(id).getRoom().getRoomId());return id;}

    @PostMapping
    public ResponseEntity<Contract> create(
            @Valid @RequestBody ContractDTO dto,org.springframework.security.core.Authentication auth) {
        landlords.requireOwner(auth.getName(),dto.getRoomId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contractService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> update(
            @PathVariable String id,
            @Valid @RequestBody ContractDTO dto,org.springframework.security.core.Authentication auth) {
        landlords.requireOwner(auth.getName(),dto.getRoomId());

        return ResponseEntity.ok(
                contractService.update(ownedId(id,auth.getName()), dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,org.springframework.security.core.Authentication auth) {
        contractService.delete(ownedId(id,auth.getName()));

        return ResponseEntity.noContent().build();
    }
}