package com.ntd.csdl.service;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.server.ResponseStatusException;import org.springframework.http.HttpStatus;import java.time.*;import java.util.*;import com.ntd.csdl.entity.Contract;
@Service @RequiredArgsConstructor public class StayService {
 private final JdbcTemplate jdbc;private final LandlordService landlords;
 public static LocalDate today(){return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));}
 public static LocalDate date(Object value){return value==null?null:LocalDate.parse(value.toString());}
 public static String activeSql(){return "COALESCE(s.check_in,c.start_date)<=? AND ((s.check_out IS NULL AND c.end_date>=?) OR s.check_out>?)";}
 public Map<String,Object> stay(String id){var rows=jdbc.queryForList("SELECT * FROM contract_stays WHERE contract_id=?",id);return rows.isEmpty()?Map.of():rows.getFirst();}
 public Contract enrich(Contract c){var s=stay(c.getContractId());c.setAgreedRent(s.get("monthly_rent")==null?null:new java.math.BigDecimal(s.get("monthly_rent").toString()));c.setActualCheckIn(date(s.get("check_in")));c.setActualCheckOut(date(s.get("check_out")));return c;}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public Map<String,Object> record(String id,String username,boolean checkIn,LocalDate day,Integer electricity,Integer water){
  if(day==null||day.isAfter(today())||electricity==null||water==null||electricity<0||water<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Nhập ngày thực tế không sau hôm nay và chỉ số công tơ không âm");
  var rows=jdbc.queryForList("SELECT * FROM contracts WHERE contract_id=? FOR UPDATE",id);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy hợp đồng");var c=rows.getFirst();String room=(String)c.get("room_id");landlords.requireOwner(username,room);jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE",room);var s=stay(id);
  if(s.get("check_out")!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Hợp đồng đã check-out");
  LocalDate start=date(c.get("start_date")),end=date(c.get("end_date")),actual=date(s.get("check_in"));
  if(checkIn&&(actual!=null||day.isAfter(end)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Đã check-in hoặc hợp đồng đã hết hạn");
  if(!checkIn&&day.isBefore(actual==null?start:actual))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Check-out không được trước ngày nhận phòng");
  LocalDate from=checkIn?day:(actual==null?start:actual),through=checkIn?end:day.minusDays(1);
  Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id WHERE c.room_id=? AND c.contract_id<>? AND COALESCE(s.check_in,c.start_date)<=? AND ((s.check_out IS NULL AND c.end_date>=?) OR s.check_out>?)",Integer.class,room,id,through,from,from);
  if(count!=null&&count>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Ngày thực tế chồng lấn hợp đồng khác trong phòng");
  if(!checkIn&&s.get("check_in_electric")!=null&&(electricity<((Number)s.get("check_in_electric")).intValue()||water<((Number)s.get("check_in_water")).intValue()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Chỉ số trả phòng không được nhỏ hơn lúc nhận");
  if(s.isEmpty())jdbc.update("INSERT INTO contract_stays(contract_id) VALUES (?)",id);
  String prefix=checkIn?"check_in":"check_out";jdbc.update("UPDATE contract_stays SET "+prefix+"=?,"+prefix+"_electric=?,"+prefix+"_water=? WHERE contract_id=?",day,electricity,water,id);refreshRoom(room,today());
  for(var invoice:jdbc.queryForList("SELECT year,month FROM invoices WHERE contract_id=? AND (year>? OR (year=? AND month>=?))",id,day.getYear(),day.getYear(),day.getMonthValue())){Integer year=((Number)invoice.get("year")).intValue(),month=((Number)invoice.get("month")).intValue();String message="Ngày nhận/trả phòng đã thay đổi sau khi có hóa đơn. Chủ nhà cần đối soát kỳ này; hệ thống giữ nguyên hóa đơn đã lập.";if(jdbc.queryForList("SELECT contract_id FROM billing_alerts WHERE contract_id=? AND year=? AND month=?",id,year,month).isEmpty())jdbc.update("INSERT INTO billing_alerts(contract_id,year,month,message) VALUES (?,?,?,?)",id,year,month,message);else jdbc.update("UPDATE billing_alerts SET message=? WHERE contract_id=? AND year=? AND month=?",message,id,year,month);}
  return stay(id);
 }
 @Transactional public void setRent(String id,String username,java.math.BigDecimal rate){if(rate==null||rate.signum()<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Giá thuê không được âm");var rows=jdbc.queryForList("SELECT room_id FROM contracts WHERE contract_id=? FOR UPDATE",id);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có hợp đồng");landlords.requireOwner(username,(String)rows.getFirst().get("room_id"));var current=stay(id);if(current.get("monthly_rent")!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Giá thuê đã được chốt cho hợp đồng");if(current.isEmpty())jdbc.update("INSERT INTO contract_stays(contract_id,monthly_rent) VALUES (?,?)",id,rate);else jdbc.update("UPDATE contract_stays SET monthly_rent=? WHERE contract_id=?",rate,id);}
 public void refreshRoom(String room,LocalDate day){
  String current=jdbc.queryForObject("SELECT status FROM rooms WHERE room_id=?",String.class,room);String normalized=java.text.Normalizer.normalize(current==null?"":current.toLowerCase(java.util.Locale.ROOT),java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').trim();
  if(!RentalWorkflowService.available(current)&&!java.util.Set.of("occupied","rented","leased","da thue","dang thue","dang su dung","co khach").contains(normalized))return;
  Integer active=jdbc.queryForObject("SELECT COUNT(*) FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id WHERE c.room_id=? AND "+activeSql(),Integer.class,room,day,day,day);
  Integer unknown=jdbc.queryForObject("SELECT COUNT(*) FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id WHERE c.room_id=? AND s.check_out IS NULL AND (COALESCE(s.check_in,c.start_date) IS NULL OR c.end_date IS NULL)",Integer.class,room);
  if((active==null||active==0)&&unknown!=null&&unknown>0)return;
  jdbc.update("UPDATE rooms SET status=? WHERE room_id=?",active!=null&&active>0?"OCCUPIED":"AVAILABLE",room);
 }
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public void refreshAll(){for(String id:jdbc.query("SELECT DISTINCT room_id FROM contracts",(rs,n)->rs.getString(1))) {jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE",id);refreshRoom(id,today());}}
}
