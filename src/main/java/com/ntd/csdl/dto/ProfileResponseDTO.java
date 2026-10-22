package com.ntd.csdl.dto;

public record ProfileResponseDTO(
        String username,
        String displayName,
        String email,
        String phone,
        String avatarUrl
) {
}
