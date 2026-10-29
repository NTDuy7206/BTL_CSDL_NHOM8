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
    private final com.ntd.csdl.service.LandlordService landlords;
    @PostMapping public Contract create(@RequestBody(required=false) RentalDTO dto,Authentication auth){throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Khách thuê phải tự gửi yêu cầu. Chủ nhà chỉ được duyệt yêu cầu thuê"); }
    @GetMapping("/accounts") public List<Map<String,Object>> accounts(){throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.GONE,"Không còn chức năng tạo hợp đồng thay tài khoản khách");}
    @GetMapping("/me") public Tenant me(Authentication auth){return rentals.myTenant(auth.getName());}
}

