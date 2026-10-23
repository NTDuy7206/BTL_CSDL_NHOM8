package com.ntd.csdl.dto;

import com.ntd.csdl.entity.User;
import lombok.Getter;

@Getter
public class AccountSummaryDTO {
    private final Long id;
    private final String username;
    private final String role;

    public AccountSummaryDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().name();
    }
}
