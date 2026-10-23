package com.ntd.csdl.service;

import com.ntd.csdl.dto.ContractRequestCreateDTO;
import com.ntd.csdl.dto.ContractRequestResponseDTO;
import com.ntd.csdl.dto.ContractRequestStatusDTO;
import com.ntd.csdl.entity.ContractRequest;
import com.ntd.csdl.entity.ContractRequestStatus;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.entity.User;
import com.ntd.csdl.repo.ContractRequestRepository;
import com.ntd.csdl.repo.RoomRepository;
import com.ntd.csdl.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractRequestService {
    private final ContractRequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ContractRequestResponseDTO> getForAccount(String username, boolean tenant) {
        List<ContractRequest> requests = tenant
                ? requestRepository.findByApplicantUsernameOrderByCreatedAtDesc(username)
                : requestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().map(ContractRequestResponseDTO::new).toList();
    }

    @Transactional
    public ContractRequestResponseDTO create(String username, ContractRequestCreateDTO dto) {
        if (dto.getRequestedStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày bắt đầu thuê không được ở trong quá khứ");
        }
        User applicant = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        Room room = roomRepository.findById(dto.getRoomId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng"));
        if (!isAvailable(room.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng này hiện không được đánh dấu là còn trống");
        }

        ContractRequest request = new ContractRequest();
        request.setApplicant(applicant);
        request.setRoom(room);
        request.setRequestedStartDate(dto.getRequestedStartDate());
        request.setMessage(dto.getMessage() == null ? null : dto.getMessage().trim());
        request.setStatus(ContractRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        return new ContractRequestResponseDTO(requestRepository.save(request));
    }

    @Transactional
    public ContractRequestResponseDTO updateStatus(Long requestId, ContractRequestStatusDTO dto) {
        ContractRequestStatus status;
        try {
            status = ContractRequestStatus.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái chỉ được là APPROVED hoặc REJECTED");
        }
        if (status == ContractRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể chuyển yêu cầu về trạng thái chờ");
        }

        ContractRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu thuê"));
        if (request.getStatus() != ContractRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Yêu cầu này đã được xử lý");
        }
        request.setStatus(status);
        return new ContractRequestResponseDTO(request);
    }

    private boolean isAvailable(String status) {
        if (status == null) return false;
        String normalized = status.toLowerCase().replace('đ', 'd').replaceAll("[^a-z0-9 ]", " ").trim();
        if (normalized.contains("not available") || normalized.contains("not vacant")
                || normalized.contains("not occupied") || normalized.contains("occupied")
                || normalized.contains("rented") || normalized.contains("leased")
                || normalized.contains("khong trong") || normalized.contains("da thue")
                || normalized.contains("dang thue")) {
            return false;
        }
        return normalized.contains("available") || normalized.contains("vacant")
                || normalized.contains("unoccupied") || normalized.contains("empty")
                || normalized.contains("trong") || normalized.contains("chua thue")
                || normalized.contains("chua cho thue");
    }
}
