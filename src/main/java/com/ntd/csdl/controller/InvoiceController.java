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
    private final com.ntd.csdl.service.RentalWorkflowService rentals;
    private final com.ntd.csdl.repo.InvoiceRepository invoices;

    @GetMapping
    public ResponseEntity<List<Invoice>> getAll(org.springframework.security.core.Authentication auth) {
        String tenantId=rentals.tenantId(auth.getName());
        return ResponseEntity.ok(
                com.ntd.csdl.service.RentalWorkflowService.admin(auth) ? invoiceService.getAll() : (tenantId==null ? List.of() : invoices.findByContractTenantTenantId(tenantId))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getById(
            @PathVariable String id, org.springframework.security.core.Authentication auth) {
        Invoice invoice=invoiceService.getById(id);
        if(!com.ntd.csdl.service.RentalWorkflowService.admin(auth)) rentals.requireOwner(auth.getName(),invoice.getContract());
        return ResponseEntity.ok(invoice);
    }

    @PostMapping
    public ResponseEntity<Invoice> create(
            @Valid @RequestBody InvoiceDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> update(
            @PathVariable String id,
            @Valid @RequestBody InvoiceDTO dto) {

        return ResponseEntity.ok(
                invoiceService.update(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id) {

        invoiceService.delete(id);

        return ResponseEntity.noContent().build();
    }
}