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
 public List<Map<String,Object>> landlords(){return jdbc.queryForList("SELECT u.id AS landlordId,u.username AS landlordName,p.room_limit AS roomCount FROM landlord_properties p JOIN user u ON u.id=p.user_id WHERE u.role=1 ORDER BY u.username");}
 public Long accountId(String username){return users.findByUsername(username).orElseThrow().getId();}
 public boolean owns(String username,String room){return Objects.equals(accountId(username),owner(room));}
 public Long owner(String room){return jdbc.query("SELECT user_id FROM room_landlords WHERE room_id=?",(rs,n)->rs.getLong(1),room).stream().findFirst().orElse(null);}
 public void requireOwner(String username,String room){Long id=owner(room);if(id==null||!id.equals(users.findByUsername(username).orElseThrow().getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Phòng không thuộc chủ nhà đang đăng nhập");}
 public void requireAssigned(String room){if(owner(room)==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Phòng chưa được liên kết với chủ nhà. Vui lòng chọn phòng khác");}
 public List<Room> mine(String username){Long id=users.findByUsername(username).orElseThrow().getId();return rooms.findAllById(jdbc.query("SELECT room_id FROM room_landlords WHERE user_id=?",(rs,n)->rs.getString(1),id));}
 public List<Map<String,Object>> catalog(){return jdbc.queryForList("SELECT r.room_id AS roomId,r.floor,r.area,r.room_type AS roomType,r.status,r.base_price AS basePrice,u.id AS landlordId,u.username AS landlordName FROM rooms r JOIN room_landlords l ON l.room_id=r.room_id JOIN user u ON u.id=l.user_id WHERE u.role=1 ORDER BY u.username,r.room_id");}
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
