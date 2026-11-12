package com.ntd.csdl.controller;

import com.ntd.csdl.dto.ContractRequestCreateDTO;
import com.ntd.csdl.dto.ContractRequestResponseDTO;
import com.ntd.csdl.dto.ContractRequestStatusDTO;
import com.ntd.csdl.service.ContractRequestService;
import com.ntd.csdl.service.VipRentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/contract-requests")
@RequiredArgsConstructor
public class ContractRequestController {
    private final ContractRequestService service;
    private final VipRentalService vipRentals;

    public record VipDeposit(String roomId, LocalDate start, LocalDate end, BigDecimal amount) {}

    @GetMapping("/vip-deposit")
    public VipRentalService.Quote quote(@RequestParam String roomId, @RequestParam LocalDate start, @RequestParam LocalDate end) {
        return vipRentals.quote(roomId, start, end);
    }

    @PostMapping("/vip-deposit")
    public Map<String, String> simulate(Authentication auth, @RequestBody VipDeposit body) {
        return Map.of("depositId", vipRentals.simulate(auth.getName(), body.roomId(), body.start(), body.end(), body.amount()));
    }

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
