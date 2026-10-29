package com.ntd.csdl.service;
import com.ntd.csdl.dto.*;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
@Service @RequiredArgsConstructor
public class ContractRequestService {
    private final ContractRequestRepository requestRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenants;
    private final RentalWorkflowService rentals;
    private final JdbcTemplate jdbc;
    private final LandlordService landlords;
    private ContractRequestResponseDTO response(ContractRequest request) {
        ContractRequestResponseDTO dto=new ContractRequestResponseDTO(request);
        jdbc.query("SELECT tenant_id,requested_end_date,contract_id FROM rental_request_details WHERE request_id=?",
            (org.springframework.jdbc.core.RowCallbackHandler)rs->{dto.setTenant(tenants.findById(rs.getString(1)).orElse(null));dto.setRequestedEndDate(rs.getDate(2).toLocalDate());dto.setContractId(rs.getString(3));},
            request.getRequestId());
        return dto;
    }
    @Transactional(readOnly=true)
    public List<ContractRequestResponseDTO> getForAccount(String username,boolean tenant) {
        var requests=tenant?requestRepository.findByApplicantUsernameOrderByCreatedAtDesc(username):requestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().filter(r->tenant||Objects.equals(landlords.owner(r.getRoom().getRoomId()),userRepository.findByUsername(username).orElseThrow().getId())).map(this::response).toList();
    }
    @Transactional
    public ContractRequestResponseDTO create(String username,ContractRequestCreateDTO dto) {
        landlords.requireSelected(username,dto.getRoomId());
        RentalWorkflowService.dates(dto.getRequestedStartDate(),dto.getRequestedEndDate());
        if(dto.getRequestedStartDate().isBefore(LocalDate.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ngày bắt đầu không được trong quá khứ");
        User applicant=userRepository.findByUsername(username).orElseThrow();
        RentalDTO rental=new RentalDTO();rental.setTenant(dto.getTenant());
        Tenant tenant=rentals.saveTenant(rental,username,true);
        if(requestRepository.findByApplicantUsernameOrderByCreatedAtDesc(username).stream().anyMatch(r->
            r.getStatus()==ContractRequestStatus.PENDING && r.getRoom().getRoomId().equals(dto.getRoomId())))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Bạn đã có yêu cầu đang chờ cho phòng này");
        landlords.requireAssigned(dto.getRoomId());
        Room room=roomRepository.findById(dto.getRoomId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy phòng"));
        if(!RentalWorkflowService.available(room.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng không còn trống");
        ContractRequest request=new ContractRequest();request.setApplicant(applicant);request.setRoom(room);
        request.setRequestedStartDate(dto.getRequestedStartDate());request.setMessage(dto.getMessage());
        request.setStatus(ContractRequestStatus.PENDING);request.setCreatedAt(LocalDateTime.now());
        request=requestRepository.saveAndFlush(request);
        jdbc.update("INSERT INTO rental_request_details(request_id,tenant_id,requested_end_date) VALUES (?,?,?)",
            request.getRequestId(),tenant.getTenantId(),dto.getRequestedEndDate());
        return response(request);
    }
    @Transactional
    public ContractRequestResponseDTO updateStatus(Long id,ContractRequestStatusDTO dto,String username) {
        String status=dto.getStatus().trim().toUpperCase(Locale.ROOT);
        if(!Set.of("APPROVED","REJECTED").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Chỉ chọn APPROVED hoặc REJECTED");
        jdbc.queryForList("SELECT request_id FROM contract_requests WHERE request_id=? FOR UPDATE",id);
        ContractRequest request=requestRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy yêu cầu"));
        landlords.requireOwner(username,request.getRoom().getRoomId());
        if(request.getStatus()!=ContractRequestStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Yêu cầu đã được xử lý");
        if(status.equals("APPROVED")) {
            ContractRequestResponseDTO details=response(request);
            if(details.getTenant()==null || details.getRequestedEndDate()==null)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Yêu cầu cũ thiếu thông tin khách thuê. Vui lòng từ chối và yêu cầu gửi lại");
            if(details.getRequestedEndDate().isBefore(LocalDate.now()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Thời gian thuê đã hết, vui lòng gửi yêu cầu mới");
            String linked=rentals.tenantId(request.getApplicant().getUsername());
            if(!Objects.equals(linked,details.getTenant().getTenantId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Hồ sơ yêu cầu không khớp tài khoản gửi. Khách cần gửi yêu cầu mới");
            Contract contract=rentals.book(details.getTenant(),request.getRoom().getRoomId(),
                request.getRequestedStartDate(),details.getRequestedEndDate(),BigDecimal.ZERO);
            jdbc.update("UPDATE rental_request_details SET contract_id=? WHERE request_id=?",contract.getContractId(),id);
        }
        request.setStatus(ContractRequestStatus.valueOf(status));
        return response(request);
    }
}
