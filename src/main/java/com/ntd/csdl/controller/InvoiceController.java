package com.ntd.csdl.controller;

import com.ntd.csdl.dto.InvoiceDTO;
import com.ntd.csdl.entity.Invoice;
import com.ntd.csdl.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final com.ntd.csdl.service.LandlordService landlords;
    private final com.ntd.csdl.repo.ContractRepository contracts;
    private final com.ntd.csdl.service.RentalWorkflowService rentals;
    private final com.ntd.csdl.repo.InvoiceRepository invoices;

    @GetMapping
    public ResponseEntity<List<Invoice>> getAll(org.springframework.security.core.Authentication auth) {
        String tenantId=rentals.tenantId(auth.getName());
        return ResponseEntity.ok(
                com.ntd.csdl.service.RentalWorkflowService.admin(auth) ? invoiceService.getAll().stream().filter(i->landlords.owns(auth.getName(),i.getContract().getRoom().getRoomId())).toList() : (tenantId==null ? List.of() : invoices.findByContractTenantTenantId(tenantId))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getById(
            @PathVariable String id, org.springframework.security.core.Authentication auth) {
        Invoice invoice=invoiceService.getById(id);
        if(com.ntd.csdl.service.RentalWorkflowService.admin(auth))landlords.requireOwner(auth.getName(),invoice.getContract().getRoom().getRoomId());
        else rentals.requireOwner(auth.getName(),invoice.getContract());
        return ResponseEntity.ok(invoice);
    }

    private String ownedId(String id,String name){landlords.requireOwner(name,invoiceService.getById(id).getContract().getRoom().getRoomId());return id;}

    @PostMapping
    public ResponseEntity<Invoice> create(
            @Valid @RequestBody InvoiceDTO dto,org.springframework.security.core.Authentication auth) {
        landlords.requireOwner(auth.getName(),contracts.findById(dto.getContractId()).orElseThrow().getRoom().getRoomId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> update(
            @PathVariable String id,
            @Valid @RequestBody InvoiceDTO dto,org.springframework.security.core.Authentication auth) {
        landlords.requireOwner(auth.getName(),contracts.findById(dto.getContractId()).orElseThrow().getRoom().getRoomId());

        return ResponseEntity.ok(
                invoiceService.update(ownedId(id,auth.getName()), dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,org.springframework.security.core.Authentication auth) {
        invoiceService.delete(ownedId(id,auth.getName()));

        return ResponseEntity.noContent().build();
    }
}