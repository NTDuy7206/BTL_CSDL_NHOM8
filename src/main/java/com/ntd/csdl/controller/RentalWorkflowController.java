package com.ntd.csdl.controller;
import com.ntd.csdl.dto.RentalDTO;
import com.ntd.csdl.service.RentalWorkflowService;
import com.ntd.csdl.entity.Contract;
import com.ntd.csdl.entity.Tenant;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.*;
@RestController @RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalWorkflowController {
    private final RentalWorkflowService rentals;
    @PostMapping public Contract create(@Valid @RequestBody RentalDTO dto){ return rentals.create(dto); }
    @GetMapping("/accounts") public List<Map<String,Object>> accounts(){return rentals.accounts();}
    @GetMapping("/me") public Tenant me(Authentication auth){return rentals.myTenant(auth.getName());}
}

