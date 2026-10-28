package com.ntd.csdl.controller;
import com.ntd.csdl.dto.AccountSummaryDTO;
import com.ntd.csdl.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin/accounts") @RequiredArgsConstructor
public class AdminAccountController {
    private final UserRepository users;
    @GetMapping public List<AccountSummaryDTO> accounts() {
        return users.findAll().stream().map(AccountSummaryDTO::new).toList();
    }
}
