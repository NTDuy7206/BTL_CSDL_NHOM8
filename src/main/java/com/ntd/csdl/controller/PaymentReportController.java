package com.ntd.csdl.controller;

import com.ntd.csdl.dto.PaymentReportCreateDTO;
import com.ntd.csdl.dto.PaymentReportResponseDTO;
import com.ntd.csdl.dto.PaymentReportStatusDTO;
import com.ntd.csdl.service.PaymentReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-reports")
@RequiredArgsConstructor
public class PaymentReportController {
    private final PaymentReportService service;

    @GetMapping
    public List<PaymentReportResponseDTO> get(Authentication authentication) {
        boolean tenant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_USER"::equals);
        return service.getForAccount(authentication.getName(), tenant);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentReportResponseDTO create(Authentication authentication, @Valid @RequestBody PaymentReportCreateDTO dto) {
        return service.create(authentication.getName(), dto);
    }

    @PatchMapping("/{id}/status")
    public PaymentReportResponseDTO updateStatus(@PathVariable Long id, @Valid @RequestBody PaymentReportStatusDTO dto) {
        return service.updateStatus(id, dto);
    }
}
