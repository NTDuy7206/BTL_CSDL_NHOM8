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
    private final DepositService deposits;
    private final VipRentalService vipRentals;
    private ContractRequestResponseDTO response(ContractRequest request) {
        ContractRequestResponseDTO dto=new ContractRequestResponseDTO(request);
        jdbc.query("SELECT tenant_id,requested_end_date,contract_id FROM rental_request_details WHERE request_id=?",
            (org.springframework.jdbc.core.RowCallbackHandler)rs->{dto.setTenant(tenants.findById(rs.getString(1)).orElse(null));dto.setRequestedEndDate(rs.getDate(2).toLocalDate());dto.setContractId(rs.getString(3));},
            request.getRequestId());
        dto.setVipPayment(vipRentals.forRequest(request.getRequestId()));
        jdbc.query("SELECT nightly_rate FROM nightly_request_rates WHERE request_id=?",
            (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
                var rate = rs.getBigDecimal(1);
                dto.setNightlyRate(rate);
                dto.setTotalRent(BillingMath.nightlyRent(rate, dto.getRequestedStartDate(), dto.getRequestedEndDate()));
            }, request.getRequestId());
        return dto;
    }
    @Transactional(readOnly=true)
    public List<ContractRequestResponseDTO> getForAccount(String username,boolean tenant) {
        var requests=tenant?requestRepository.findByApplicantUsernameOrderByCreatedAtDesc(username):requestRepository.findAllByOrderByCreatedAtDesc();
        return requests.stream().filter(r->tenant||Objects.equals(landlords.owner(r.getRoom().getRoomId()),userRepository.findByUsername(username).orElseThrow().getId())).map(this::response).toList();
    }
    @Transactional
    public ContractRequestResponseDTO create(String username,ContractRequestCreateDTO dto) {
        landlords.requireRentable(dto.getRoomId());
        RentalWorkflowService.dates(dto.getRequestedStartDate(),dto.getRequestedEndDate());
        if(dto.getRequestedStartDate().isBefore(StayService.today()) || dto.getRequestedEndDate().getYear()>2100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ngày thuê phải từ hôm nay đến hết năm 2100");
        User applicant=userRepository.findByUsername(username).orElseThrow();
        RentalDTO rental=new RentalDTO();rental.setTenant(dto.getTenant());
        Tenant tenant=rentals.saveTenant(rental,username,true);
        if(requestRepository.findByApplicantUsernameOrderByCreatedAtDesc(username).stream().anyMatch(r->
            r.getStatus()==ContractRequestStatus.PENDING && r.getRoom().getRoomId().equals(dto.getRoomId())))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Bạn đã có yêu cầu đang chờ cho phòng này");
        landlords.requireAssigned(dto.getRoomId());
        Room room=roomRepository.lockById(dto.getRoomId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy phòng"));
        if(!RentalWorkflowService.available(room.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng không còn trống");
        BigDecimal nightlyRate = room.getBasePrice();
        if (nightlyRate == null || nightlyRate.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng chưa có giá theo đêm");
        if (dto.getExpectedNightlyRate() != null && nightlyRate.compareTo(dto.getExpectedNightlyRate()) != 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Giá phòng đã thay đổi. Vui lòng tải lại báo giá");
        ContractRequest request=new ContractRequest();request.setApplicant(applicant);request.setRoom(room);
        request.setRequestedStartDate(dto.getRequestedStartDate());request.setMessage(dto.getMessage());
        request.setStatus(ContractRequestStatus.PENDING);request.setCreatedAt(LocalDateTime.now());
        request=requestRepository.saveAndFlush(request);
        if(VipRentalService.isVip(room.getRoomType())) {
            vipRentals.attach(dto.getVipDepositId(),applicant.getId(),room.getRoomId(),
                dto.getRequestedStartDate(),dto.getRequestedEndDate(),request.getRequestId(),nightlyRate);
        } else if(dto.getVipDepositId()!=null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng đã đổi loại. Vui lòng tải lại thông tin trước khi gửi");
        }
        jdbc.update("INSERT INTO rental_request_details(request_id,tenant_id,requested_end_date) VALUES (?,?,?)",
            request.getRequestId(),tenant.getTenantId(),dto.getRequestedEndDate());
        jdbc.update("INSERT INTO nightly_request_rates(request_id,nightly_rate) VALUES (?,?)", request.getRequestId(),nightlyRate);
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
        if(status.equals("REJECTED"))deposits.reject(id);
        if(status.equals("APPROVED")) {
            ContractRequestResponseDTO details=response(request);
            if(details.getTenant()==null || details.getRequestedEndDate()==null)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Yêu cầu cũ thiếu thông tin khách thuê. Vui lòng từ chối và yêu cầu gửi lại");
            if(details.getRequestedEndDate().isBefore(StayService.today())
                    || (details.getNightlyRate()!=null && details.getRequestedEndDate().equals(StayService.today())))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Thời gian thuê đã hết, vui lòng gửi yêu cầu mới");
            String linked=rentals.tenantId(request.getApplicant().getUsername());
            if(!Objects.equals(linked,details.getTenant().getTenantId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Hồ sơ yêu cầu không khớp tài khoản gửi. Khách cần gửi yêu cầu mới");
            var vip=details.getVipPayment();
            BigDecimal deposit=vip==null?deposits.closeForRentalApproval(id):vip.depositAmount();
            BigDecimal legacyRate = vip != null ? vip.monthlyRent() : details.getNightlyRate() != null ? null
                : jdbc.queryForList("SELECT monthly_rate FROM legacy_room_monthly_rates WHERE room_id=?",
                    BigDecimal.class,request.getRoom().getRoomId()).stream().findFirst().orElse(null);
            Contract contract=rentals.book(details.getTenant(),request.getRoom().getRoomId(),
                request.getRequestedStartDate(),details.getRequestedEndDate(),deposit,legacyRate,details.getNightlyRate());
            jdbc.update("UPDATE rental_request_details SET contract_id=? WHERE request_id=?",contract.getContractId(),id);
        }
        request.setStatus(ContractRequestStatus.valueOf(status));
        return response(request);
    }
}
