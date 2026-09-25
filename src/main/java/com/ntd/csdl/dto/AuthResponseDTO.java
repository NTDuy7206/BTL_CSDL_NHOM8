package com.ntd.csdl.dto;

import com.ntd.task.entity.Users;

public class AuthResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    private Users user;

    public AuthResponseDTO(String accessToken, Users user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}