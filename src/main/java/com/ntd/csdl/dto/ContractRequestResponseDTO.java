package com.ntd.csdl.dto;

import com.ntd.csdl.entity.ContractRequest;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ContractRequestResponseDTO {
    private final Long requestId;
    private final String username;
    private final String roomId;
    private final LocalDate requestedStartDate;
    private final String message;
    private final String status;
    private final LocalDateTime createdAt;

    public ContractRequestResponseDTO(ContractRequest request) {
        this.requestId = request.getRequestId();
        this.username = request.getApplicant().getUsername();
        this.roomId = request.getRoom().getRoomId();
        this.requestedStartDate = request.getRequestedStartDate();
        this.message = request.getMessage();
        this.status = request.getStatus().name();
        this.createdAt = request.getCreatedAt();
    }
}
