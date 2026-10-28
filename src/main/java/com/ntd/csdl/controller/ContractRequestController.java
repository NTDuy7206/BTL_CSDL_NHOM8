package com.ntd.csdl.controller;

import com.ntd.csdl.dto.ContractRequestCreateDTO;
import com.ntd.csdl.dto.ContractRequestResponseDTO;
import com.ntd.csdl.dto.ContractRequestStatusDTO;
import com.ntd.csdl.service.ContractRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract-requests")
@RequiredArgsConstructor
public class ContractRequestController {
    private final ContractRequestService service;

    @GetMapping
    public List<ContractRequestResponseDTO> get(Authentication authentication) {
        boolean tenant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_USER"::equals);
        return service.getForAccount(authentication.getName(), tenant);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractRequestResponseDTO create(Authentication authentication, @Valid @RequestBody ContractRequestCreateDTO dto) {
        return service.create(authentication.getName(), dto);
    }

    @PatchMapping("/{id}/status")
    public ContractRequestResponseDTO updateStatus(@PathVariable Long id, @Valid @RequestBody ContractRequestStatusDTO dto,Authentication auth) {
        return service.updateStatus(id, dto,auth.getName());
    }
}
