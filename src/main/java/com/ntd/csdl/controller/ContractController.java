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
    private final com.ntd.csdl.service.StayService stays;
    private final com.ntd.csdl.service.LandlordService landlords;
    private final com.ntd.csdl.service.RentalWorkflowService rentals;

    @GetMapping
    public ResponseEntity<List<Contract>> getAll(org.springframework.security.core.Authentication auth) {
        return ResponseEntity.ok(
                com.ntd.csdl.service.RentalWorkflowService.admin(auth) ? contractService.getAll().stream().filter(c->landlords.owns(auth.getName(),c.getRoom().getRoomId())).map(stays::enrich).toList() : rentals.mine(auth.getName()).stream().map(stays::enrich).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getById(
            @PathVariable String id, org.springframework.security.core.Authentication auth) {
        Contract contract=contractService.getById(id);
        if(com.ntd.csdl.service.RentalWorkflowService.admin(auth))landlords.requireOwner(auth.getName(),contract.getRoom().getRoomId());
        else rentals.requireOwner(auth.getName(),contract);
        return ResponseEntity.ok(stays.enrich(contract));
    }

    private String ownedId(String id,String name){landlords.requireOwner(name,contractService.getById(id).getRoom().getRoomId());return id;}

    @PostMapping public ResponseEntity<Contract> create(@RequestBody(required=false) ContractDTO dto){
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,"Khách thuê phải tự gửi yêu cầu. Chủ nhà chỉ được duyệt yêu cầu thuê");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> update(
            @PathVariable String id,
            @Valid @RequestBody ContractDTO dto,org.springframework.security.core.Authentication auth) {
        Contract existing=contractService.getById(ownedId(id,auth.getName()));
        if(!java.util.Objects.equals(existing.getTenant().getTenantId(),dto.getTenantId())||!java.util.Objects.equals(existing.getRoom().getRoomId(),dto.getRoomId()))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT,"Không được đổi khách thuê hoặc phòng của hợp đồng; khách phải gửi yêu cầu mới");

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