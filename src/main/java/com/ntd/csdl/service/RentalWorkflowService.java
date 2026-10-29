package com.ntd.csdl.service;
import com.ntd.csdl.dto.RentalDTO;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;

@Service @RequiredArgsConstructor
public class RentalWorkflowService {
    private final JdbcTemplate jdbc;
    private final TenantRepository tenants;
    private final ContractRepository contracts;
    private final RoomRepository rooms;
    private final UserRepository users;

    public static boolean admin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    public static String id(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-","").substring(0,16);
    }
    public static boolean available(String status) {
        if(status==null) return false;
        String s=Normalizer.normalize(status.toLowerCase(Locale.ROOT),Normalizer.Form.NFD)
            .replaceAll("\\p{M}","").replace('đ','d').trim();
        return Set.of("available","vacant","unoccupied","not occupied","empty","trong","con trong","chua thue","chua cho thue").contains(s);
    }
    public static void dates(LocalDate start, LocalDate end) {
        if(start==null || end==null || !end.isAfter(start))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ngày kết thúc phải sau ngày bắt đầu");
    }
    public String tenantId(String username) {
        return jdbc.query("SELECT ta.tenant_id FROM tenant_accounts ta JOIN user u ON u.id=ta.user_id WHERE u.username=?",
            (rs,n)->rs.getString(1),username).stream().findFirst().orElse(null);
    }
    public void requireOwner(String username, Contract contract) {
        String id=tenantId(username);
        if(id==null || contract==null || contract.getTenant()==null || !id.equals(contract.getTenant().getTenantId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Bạn chỉ được truy cập hợp đồng và hóa đơn của mình");
    }
    @Transactional(readOnly=true)
    public List<Contract> mine(String username) {
        String id=tenantId(username);
        return id==null?List.of():contracts.findByTenantTenantId(id);
    }
    @Transactional(readOnly=true)
    public Tenant myTenant(String username) {
        String id=tenantId(username);
        return id==null?null:tenants.findById(id).orElse(null);
    }
    public List<Map<String,Object>> accounts() {
        return jdbc.queryForList("SELECT u.username, ta.tenant_id AS tenantId FROM user u LEFT JOIN tenant_accounts ta ON ta.user_id=u.id WHERE u.role=0 ORDER BY u.username");
    }
    public void requireRoom(String username,String roomId) {
        LocalDate now=LocalDate.now();
        if(mine(username).stream().noneMatch(c->c.getRoom()!=null && c.getRoom().getRoomId().equals(roomId)
            && c.getStartDate()!=null && !c.getStartDate().isAfter(now) && c.getEndDate()!=null && !c.getEndDate().isBefore(now)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Chỉ được báo cáo sự cố tại phòng bạn đang thuê");
    }
    // Called inside the same transaction as the request or the final contract.
    public Tenant saveTenant(RentalDTO dto, String username, boolean self) {
        User account=null;
        if(username!=null && !username.isBlank()) {
            account=users.findByUsername(username.trim()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy tài khoản khách thuê"));
            jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",account.getId());
            if(account.getRole()!=UserEnum.USER)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Chọn tài khoản có vai trò USER");
        }
        String linked=account==null?null:tenantId(account.getUsername());
        String selected=self?linked:dto.getTenantId();
        if(linked!=null && selected!=null && !selected.isBlank() && !linked.equals(selected))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Tài khoản đã liên kết với khách thuê khác");
        if((selected==null || selected.isBlank()) && linked!=null) selected=linked;
        Tenant tenant=selected==null || selected.isBlank()?new Tenant():tenants.findById(selected)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy khách thuê"));
        RentalDTO.Person p=dto.getTenant();
        Tenant duplicate=tenants.findByCccd(p.getCccd().trim());
        if(duplicate!=null && !Objects.equals(duplicate.getTenantId(),tenant.getTenantId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"CCCD đã có hồ sơ. Vui lòng nhờ chủ nhà liên kết đúng tài khoản");
        if(tenant.getTenantId()==null) tenant.setTenantId(id("KT"));
        tenant.setCccd(p.getCccd().trim()); tenant.setFullName(p.getFullName().trim());
        tenant.setDateOfBirth(p.getDateOfBirth());tenant.setPermanentAddress(p.getPermanentAddress().trim());
        tenant.setPhoneNumber(p.getPhoneNumber().trim());
        tenant=tenants.saveAndFlush(tenant);
        if(account!=null && linked==null) {
            if(!jdbc.queryForList("SELECT user_id FROM tenant_accounts WHERE tenant_id=?",tenant.getTenantId()).isEmpty())
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Khách thuê đã được gắn với tài khoản khác");
            jdbc.update("INSERT INTO tenant_accounts(user_id,tenant_id) VALUES (?,?)",account.getId(),tenant.getTenantId());
        }
        return tenant;
    }
    public Contract book(Tenant tenant,String roomId,LocalDate start,LocalDate end,BigDecimal deposit) {
        dates(start,end);
        Room room=rooms.lockById(roomId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy phòng"));
        // A request may have loaded the Room earlier into Hibernate's first-level cache.
        // Read its current locked status, not that potentially stale managed entity.
        String currentStatus=jdbc.queryForObject("SELECT status FROM rooms WHERE room_id=? FOR UPDATE",String.class,roomId);
        if(!available(currentStatus)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng không còn trống");
        boolean overlap=contracts.findByRoomRoomId(roomId).stream().anyMatch(c ->
            c.getStartDate()==null || c.getEndDate()==null || (!c.getStartDate().isAfter(end) && !c.getEndDate().isBefore(start)));
        if(overlap) throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng đã có hợp đồng trong thời gian này");
        Contract contract=Contract.builder().contractId(id("HD")).tenant(tenant).room(room).startDate(start).endDate(end).deposit(deposit).build();
        contract=contracts.saveAndFlush(contract);
        room.setStatus("OCCUPIED");rooms.save(room);
        return contract;
    }
}
