package com.ntd.csdl.service;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.math.BigDecimal;
@Service @RequiredArgsConstructor
public class LandlordService {
 private final JdbcTemplate jdbc;
 private final UserRepository users;
 private final RoomRepository rooms;
 public List<Map<String,Object>> landlords(){return jdbc.queryForList("SELECT u.id AS landlordId,u.username AS landlordName,(SELECT COUNT(*) FROM room_landlords l WHERE l.user_id=u.id) AS roomCount,CASE WHEN p.user_id IS NULL THEN FALSE ELSE TRUE END AS configured FROM user u LEFT JOIN landlord_properties p ON p.user_id=u.id WHERE u.role=1 ORDER BY u.username");}
 public void requireListed(String room){if(catalog().stream().noneMatch(r->Objects.equals(r.get("roomId"),room)))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Phòng chưa có chủ nhà cho thuê hoặc không còn được niêm yết");}
 public List<Room> browseRooms(){return materialize(catalog());}
 private List<Room> materialize(List<Map<String,Object>> catalog){var byId=new HashMap<String,Map<String,Object>>();for(var row:catalog)byId.put((String)row.get("roomId"),row);return rooms.findAllById(byId.keySet()).stream().map(room->{var row=byId.get(room.getRoomId());room.setLandlordId(((Number)row.get("landlordId")).longValue());room.setLandlordName((String)row.get("landlordName"));room.setOccupied((Boolean)row.get("occupied"));room.setOccupancyKnown((Boolean)row.get("occupancyKnown"));room.setAvailableForRent((Boolean)row.get("availableForRent"));return room;}).toList();}
 public Set<String> tenantIds(String username){Long id=accountId(username);return new HashSet<>(jdbc.query("SELECT tenant_id FROM tenant_landlords WHERE user_id=? UNION SELECT c.tenant_id FROM contracts c JOIN room_landlords l ON l.room_id=c.room_id WHERE l.user_id=?",(rs,n)->rs.getString(1),id,id));}
 public void requireTenant(String username,String tenant){if(!tenantIds(username).contains(tenant))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Khách thuê không thuộc chủ nhà đang đăng nhập");}
 public void linkTenant(String username,String tenant){jdbc.update("INSERT INTO tenant_landlords(tenant_id,user_id) VALUES (?,?)",tenant,accountId(username));}
 public void requireExclusiveTenant(String username,String tenant){requireTenant(username,tenant);Long id=accountId(username);if(!jdbc.queryForList("SELECT user_id FROM tenant_landlords WHERE tenant_id=? AND user_id<>? UNION SELECT l.user_id FROM contracts c JOIN room_landlords l ON l.room_id=c.room_id WHERE c.tenant_id=? AND l.user_id<>?",tenant,id,tenant,id).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Hồ sơ dùng trong hợp đồng của nhiều chủ nhà; không được sửa hoặc xóa ảnh hưởng hợp đồng khác");}
 @Transactional public void declareRooms(User user,List<com.ntd.csdl.dto.RoomDTO> list){
  if(user.getRole()!=UserEnum.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Chỉ chủ nhà được khai báo phòng");
  if(list==null||list.isEmpty()||list.size()>200)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Khai báo từ 1 đến 200 phòng");
  Set<String> codes=new HashSet<>();
  for(var d:list){String code=d.getRoomId()==null?"":d.getRoomId().trim();String id="L"+user.getId()+"-"+code;
   if(!code.matches("[A-Za-z0-9_-]{1,12}")||id.length()>20||!codes.add(code.toLowerCase(Locale.ROOT))||d.getFloor()==null||d.getFloor()<0||d.getArea()==null||!Float.isFinite(d.getArea())||d.getArea()<=0||d.getRoomType()==null||d.getRoomType().isBlank()||d.getRoomType().length()>50||d.getBasePrice()==null||d.getBasePrice().signum()<=0||d.getBasePrice().precision()>18||d.getBasePrice().scale()>2)
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Phòng cần mã riêng không trùng (1–12 ký tự), tầng từ 0, diện tích và giá dương, loại phòng tối đa 50 ký tự");
  }
  jdbc.update("INSERT INTO landlord_properties(user_id,room_limit) VALUES (?,?)",user.getId(),list.size());
  for(var d:list){Room room=Room.builder().roomId("L"+user.getId()+"-"+d.getRoomId().trim()).floor(d.getFloor()).area(d.getArea()).roomType(d.getRoomType().trim()).basePrice(d.getBasePrice()).status("AVAILABLE").build();rooms.saveAndFlush(room);jdbc.update("INSERT INTO room_landlords(room_id,user_id) VALUES (?,?)",room.getRoomId(),user.getId());}
 }
 public Long accountId(String username){return users.findByUsername(username).orElseThrow().getId();}
 public boolean owns(String username,String room){return Objects.equals(accountId(username),owner(room));}
 public Long owner(String room){return jdbc.query("SELECT user_id FROM room_landlords WHERE room_id=?",(rs,n)->rs.getLong(1),room).stream().findFirst().orElse(null);}
 public void requireOwner(String username,String room){Long id=owner(room);if(id==null||!id.equals(users.findByUsername(username).orElseThrow().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Phòng không thuộc chủ nhà đang đăng nhập");}
 public void requireAssigned(String room){if(owner(room)==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng chưa được liên kết với chủ nhà. Vui lòng chọn phòng khác");}
 public List<Room> mine(String username){Long id=accountId(username);return materialize(catalog().stream().filter(row->Objects.equals(((Number)row.get("landlordId")).longValue(),id)).toList());}
 public List<Map<String,Object>> catalog(){var catalog=jdbc.queryForList("SELECT r.room_id AS roomId,r.floor,r.area,r.room_type AS roomType,r.status,r.base_price AS basePrice,u.id AS landlordId,u.username AS landlordName FROM rooms r JOIN room_landlords l ON l.room_id=r.room_id JOIN user u ON u.id=l.user_id WHERE u.role=1 ORDER BY u.username,r.room_id");
  var today=StayService.today();var occupancy=new HashMap<String,Map<String,Object>>();
  for(var row:jdbc.queryForList("SELECT c.room_id,SUM(CASE WHEN "+StayService.activeSql()+" THEN 1 ELSE 0 END) AS active_count,SUM(CASE WHEN s.check_out IS NULL AND (COALESCE(s.check_in,c.start_date) IS NULL OR c.end_date IS NULL) THEN 1 ELSE 0 END) AS unknown_count FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id GROUP BY c.room_id",today,today,today))occupancy.put((String)row.get("room_id"),row);
  for(var row:catalog){var counts=occupancy.get(row.get("roomId"));boolean occupied=counts!=null&&((Number)counts.get("active_count")).intValue()>0,known=occupied||counts==null||((Number)counts.get("unknown_count")).intValue()==0;
   row.put("occupied",occupied);row.put("occupancyKnown",known);row.put("availableForRent",known&&!occupied&&RentalWorkflowService.available((String)row.get("status"))&&row.get("area") instanceof Number&&((Number)row.get("area")).doubleValue()>0&&row.get("basePrice")!=null&&new BigDecimal(row.get("basePrice").toString()).signum()>0);
  }return catalog;
 }
 public void requireRentable(String room){var match=catalog().stream().filter(r->Objects.equals(r.get("roomId"),room)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Phòng chưa được niêm yết"));if(!Boolean.TRUE.equals(match.get("availableForRent")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng đang có khách, chưa mở cho thuê hoặc cần xác minh hợp đồng");}

 public Map<String,Object> property(String username){Long id=users.findByUsername(username).orElseThrow().getId();var rows=jdbc.queryForList("SELECT room_limit AS roomCount FROM landlord_properties WHERE user_id=?",id);return rows.isEmpty()?Map.of("configured",false):Map.of("configured",true,"roomCount",rows.getFirst().get("roomCount"));}
 public List<Room> unassigned(){return rooms.findAllById(jdbc.query("SELECT r.room_id FROM rooms r LEFT JOIN room_landlords l ON l.room_id=r.room_id WHERE l.room_id IS NULL ORDER BY r.room_id",(rs,n)->rs.getString(1)));}
 @Transactional public void setup(String username,Integer count,List<String> existing){setup(users.findByUsername(username).orElseThrow(),count,existing);}
 public void setup(User user,Integer count,List<String> existing){
  if(user.getRole()!=UserEnum.ADMIN)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Chỉ chủ nhà được khai báo phòng");
  if(count==null||count<1||count>200)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Số phòng phải từ 1 đến 200");
  jdbc.queryForList("SELECT id FROM user WHERE id=? FOR UPDATE",user.getId());
  if(!jdbc.queryForList("SELECT user_id FROM landlord_properties WHERE user_id=?",user.getId()).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Số phòng đã được thiết lập cố định");
  List<String> ids=existing==null?List.of():existing;
  if(new HashSet<>(ids).size()!=ids.size()||ids.size()>count)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Danh sách phòng cũ không hợp lệ");
  for(String id:ids.stream().sorted().toList()){
   rooms.lockById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy phòng "+id));
   if(owner(id)!=null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng "+id+" đã thuộc chủ nhà khác");
  }
  jdbc.update("INSERT INTO landlord_properties(user_id,room_limit) VALUES (?,?)",user.getId(),count);
  for(String id:ids)jdbc.update("INSERT INTO room_landlords(room_id,user_id) VALUES (?,?)",id,user.getId());
  for(int i=ids.size();i<count;i++){
   Room room=Room.builder().roomId(RentalWorkflowService.id("P")).floor(1).area(0f).roomType("Chưa cấu hình").status("UNCONFIGURED").basePrice(BigDecimal.ZERO).build();
   rooms.saveAndFlush(room);jdbc.update("INSERT INTO room_landlords(room_id,user_id) VALUES (?,?)",room.getRoomId(),user.getId());
  }
 }
}
