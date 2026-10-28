package com.ntd.csdl.service;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.dto.RentalDTO;
import com.ntd.csdl.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Service @RequiredArgsConstructor
public class DemoDataService {
 private final JdbcTemplate jdbc;
 private final RentalWorkflowService rentals;
 private final UserRepository users;
 private final RoomRepository rooms;
 private final InvoiceRepository invoices;
 @Transactional public void seed() {
   jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES ('coursework-v1',false) ON DUPLICATE KEY UPDATE seed_key=seed_key");
   Boolean done=jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key='coursework-v1' FOR UPDATE",Boolean.class);
   if(Boolean.TRUE.equals(done))return;
   ResourceDatabasePopulator script=new ResourceDatabasePopulator(new ClassPathResource("demo-data.sql"));
   script.setSqlScriptEncoding("UTF-8");script.execute(Objects.requireNonNull(jdbc.getDataSource()));
   jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key='coursework-v1'");
 }
 @Transactional public Map<String,Object> myRental(String username) {
   User user=users.findByUsername(username).orElseThrow();
   jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",user.getId());
   var links=jdbc.queryForList("SELECT tenant_id FROM tenant_accounts WHERE user_id=? FOR UPDATE",user.getId());
   if(!links.isEmpty())return Map.of("created",false,"message","Tài khoản đã có hồ sơ khách thuê. Xem Hợp đồng của tôi và Hóa đơn của tôi.");
   String roomId=RentalWorkflowService.id("DM");
   Room room=new Room();room.setRoomId(roomId);room.setFloor(1);room.setArea(25f);
   room.setRoomType("Phòng mẫu cá nhân");room.setStatus("AVAILABLE");room.setBasePrice(new BigDecimal("2500000"));rooms.saveAndFlush(room);
   RentalDTO dto=new RentalDTO();RentalDTO.Person p=new RentalDTO.Person();
   String name="Khách mẫu - "+username;p.setFullName(name.substring(0,Math.min(50,name.length())));
   p.setCccd(RentalWorkflowService.id("DM"));p.setDateOfBirth(LocalDate.of(2000,1,1));
   p.setPhoneNumber("0900000000");p.setPermanentAddress("Địa chỉ giả lập cho bài tập lớn");
   dto.setTenant(p);
   Tenant tenant=rentals.saveTenant(dto,username,true);
   Contract contract=rentals.book(tenant,roomId,LocalDate.now().minusDays(1),LocalDate.now().plusMonths(6),BigDecimal.ZERO);
   Invoice invoice=Invoice.builder().invoiceId(RentalWorkflowService.id("DM")).contract(contract)
     .month(LocalDate.now().getMonthValue()).year(LocalDate.now().getYear())
     .rentAmount(new BigDecimal("2500000")).electricityAmount(new BigDecimal("105000"))
     .waterAmount(new BigDecimal("75000")).fineAmount(BigDecimal.ZERO).totalAmount(new BigDecimal("2680000")).build();
   invoices.saveAndFlush(invoice);
   return Map.of("created",true,"contractId",contract.getContractId(),"roomId",roomId,"message","Đã tạo hồ sơ, phòng, hợp đồng và hóa đơn mẫu cho tài khoản của bạn.");
 }
}
