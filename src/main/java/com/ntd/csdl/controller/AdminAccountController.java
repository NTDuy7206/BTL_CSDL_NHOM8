package com.ntd.csdl.controller;

import com.ntd.csdl.dto.AccountSummaryDTO;
import com.ntd.csdl.entity.User;
import com.ntd.csdl.entity.UserEnum;
import com.ntd.csdl.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {
    private final UserRepository userRepository;

    @GetMapping("/pending-owners")
    public List<AccountSummaryDTO> pendingOwners() {
        return userRepository.findByRoleOrderByIdAsc(UserEnum.OWNER_PENDING)
                .stream().map(AccountSummaryDTO::new).toList();
    }

    @PatchMapping("/{id}/approve-owner")
    public AccountSummaryDTO approveOwner(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        if (user.getRole() != UserEnum.OWNER_PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tài khoản này không ở trạng thái chờ duyệt");
        }
        user.setRole(UserEnum.OWNER);
        return new AccountSummaryDTO(userRepository.save(user));
    }
}
