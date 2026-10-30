package com.ntd.csdl.service;
import com.ntd.csdl.entity.*;import com.ntd.csdl.dto.PaymentReportResponseDTO;import com.ntd.csdl.repo.PaymentReportRepository;import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.server.ResponseStatusException;import org.springframework.http.HttpStatus;import java.util.*;
@Service @RequiredArgsConstructor public class PaymentRoutingService {
 private final JdbcTemplate jdbc;private final LandlordService landlords;private final PaymentReportRepository reports;
 private Map<String,Object> recipient(PaymentReport report){String room=report.getInvoice().getContract().getRoom().getRoomId();Long owner=landlords.owner(room);var rows=owner!=null?jdbc.queryForList("SELECT id,username FROM user WHERE id=? AND role=1",owner):jdbc.queryForList("SELECT u.id,u.username FROM payment_report_routes p JOIN user u ON u.id=p.landlord_id WHERE p.payment_report_id=? AND u.role=1",report.getPaymentReportId());return rows.isEmpty()?Map.of():rows.getFirst();}
 public boolean assignedTo(String username,PaymentReport report){var recipient=recipient(report);return !recipient.isEmpty()&&Objects.equals(recipient.get("username"),username);}
 public void describe(PaymentReport report,PaymentReportResponseDTO dto){var recipient=recipient(report);if(!recipient.isEmpty())dto.recipient(((Number)recipient.get("id")).longValue(),(String)recipient.get("username"),landlords.owner(dto.getRoomId())==null?"LEGACY_ROUTED":"ROOM_OWNER");}
 @Transactional public void assign(String username,Long reportId,Long landlordId){
  var report=reports.lockById(reportId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có báo thanh toán"));
  if(!report.getPayer().getUsername().equals(username))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Chỉ người gửi mới được chọn người nhận cho khoản cũ");
  if(report.getStatus()!=PaymentReportStatus.PENDING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Khoản đã xử lý không được đổi người nhận");
  if(landlords.owner(report.getInvoice().getContract().getRoom().getRoomId())!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Hóa đơn đã có chủ nhà theo phòng; không được chuyển cho người khác");
  if(landlordId==null||jdbc.queryForList("SELECT id FROM user WHERE id=? AND role=1",landlordId).isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Chọn chủ nhà hợp lệ");
  jdbc.update("DELETE FROM payment_report_routes WHERE payment_report_id=?",reportId);jdbc.update("INSERT INTO payment_report_routes(payment_report_id,landlord_id) VALUES (?,?)",reportId,landlordId);
 }
}
