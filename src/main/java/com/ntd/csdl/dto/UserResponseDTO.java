package com.ntd.csdl.dto;

import com.ntd.csdl.entity.User;

public class UserResponseDTO {
    private final Long id;
    private final String username;
    private final String role;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole() == null ? null : user.getRole().name();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
