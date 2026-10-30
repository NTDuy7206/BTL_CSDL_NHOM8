package com.ntd.csdl.service;
import com.ntd.csdl.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
@Service @RequiredArgsConstructor
public class RoomListingService {
 private final JdbcTemplate jdbc;private final LandlordService landlords;
 private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
 @Transactional(readOnly=true) public List<Map<String,Object>> list(String username,boolean admin){
  var rooms=admin?landlords.mine(username):landlords.browseRooms();var descriptions=new HashMap<String,Map<String,Object>>();
  for(var row:jdbc.queryForList("SELECT room_id,title,description,address FROM room_listings"))descriptions.put((String)row.get("room_id"),row);
  var photos=new HashMap<String,List<Map<String,Object>>>();for(var row:jdbc.queryForList("SELECT id,room_id FROM room_photos ORDER BY id")){String id=(String)row.get("id");photos.computeIfAbsent((String)row.get("room_id"),k->new ArrayList<>()).add(Map.of("id",id,"url","/api/room-listings/photos/"+id,"thumbnailUrl","/api/room-listings/photos/"+id+"/thumbnail"));}
  return rooms.stream().map(room->{Map<String,Object> row=new LinkedHashMap<>();row.put("roomId",room.getRoomId());row.put("landlordId",room.getLandlordId());row.put("landlordName",room.getLandlordName());row.put("floor",room.getFloor());row.put("area",room.getArea());row.put("roomType",room.getRoomType());row.put("status",room.getStatus());row.put("basePrice",room.getBasePrice());row.put("occupied",room.getOccupied());row.put("occupancyKnown",room.getOccupancyKnown());row.put("availableForRent",room.getAvailableForRent());var info=descriptions.getOrDefault(room.getRoomId(),Map.of());row.put("title",info.getOrDefault("title",room.getRoomId()));row.put("description",info.getOrDefault("description",""));row.put("address",info.getOrDefault("address",""));row.put("photos",photos.getOrDefault(room.getRoomId(),List.of()));return row;}).toList();
 }
 public Map<String,Object> detail(String username,boolean admin,String id){return list(username,admin).stream().filter(r->id.equals(r.get("roomId"))).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy phòng trong danh sách của bạn"));}
 private void lock(String username,String room){landlords.requireOwner(username,room);jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE",room);}
 @Transactional public void describe(String username,String room,String title,String description,String address){
  lock(username,room);if(title==null||title.isBlank()||title.length()>120||description==null||description.length()>4000||address==null||address.length()>250)throw bad("Tên phòng tối đa 120 ký tự, mô tả tối đa 4000, địa chỉ tối đa 250");
  if(jdbc.queryForList("SELECT room_id FROM room_listings WHERE room_id=?",room).isEmpty())jdbc.update("INSERT INTO room_listings(room_id,title,description,address) VALUES (?,?,?,?)",room,title.trim(),description.trim(),address.trim());else jdbc.update("UPDATE room_listings SET title=?,description=?,address=? WHERE room_id=?",title.trim(),description.trim(),address.trim(),room);
 }
 @Transactional public String upload(String username,String room,MultipartFile file){
  lock(username,room);if(file==null||file.isEmpty()||file.getSize()>5L*1024*1024)throw bad("Chọn ảnh JPG/PNG tối đa 5 MB");
  if(jdbc.queryForObject("SELECT COUNT(*) FROM room_photos WHERE room_id=?",Integer.class,room)>=6)throw new ResponseStatusException(HttpStatus.CONFLICT,"Mỗi phòng tối đa 6 ảnh");
  try{byte[] bytes=file.getBytes();String mime;BufferedImage image;
   try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw bad("Tệp không phải ảnh JPG/PNG hợp lệ");var reader=readers.next();try{reader.setInput(input);String format=reader.getFormatName().toLowerCase(Locale.ROOT);if(!Set.of("jpeg","jpg","png").contains(format))throw bad("Chỉ hỗ trợ JPG/PNG");int width=reader.getWidth(0),height=reader.getHeight(0);if(width<=0||height<=0||width>8000||height>8000||(long)width*height>25000000)throw bad("Kích thước ảnh quá lớn");image=reader.read(0);mime=format.equals("png")?"image/png":"image/jpeg";}finally{reader.dispose();}}
   double scale=Math.min(1,Math.min(640d/image.getWidth(),480d/image.getHeight()));var thumb=new BufferedImage(Math.max(1,(int)(image.getWidth()*scale)),Math.max(1,(int)(image.getHeight()*scale)),BufferedImage.TYPE_INT_RGB);var g=thumb.createGraphics();try{g.setColor(Color.WHITE);g.fillRect(0,0,thumb.getWidth(),thumb.getHeight());g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(image,0,0,thumb.getWidth(),thumb.getHeight(),null);}finally{g.dispose();}
   var output=new ByteArrayOutputStream();ImageIO.write(thumb,"jpg",output);String id=UUID.randomUUID().toString();jdbc.update("INSERT INTO room_photos(id,room_id,mime_type,image_data,thumbnail_data) VALUES (?,?,?,?,?)",id,room,mime,bytes,output.toByteArray());return id;
  }catch(ResponseStatusException e){throw e;}catch(org.springframework.dao.DataAccessException e){throw e;}catch(IOException|RuntimeException e){throw bad("Không đọc được ảnh JPG/PNG");}
 }
 public record Photo(byte[] bytes,String mime){}
 @Transactional(readOnly=true) public Photo photo(String username,boolean admin,String id,boolean thumbnail){
  var metadata=jdbc.queryForList("SELECT room_id,mime_type FROM room_photos WHERE id=?",id);if(metadata.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có ảnh");String room=(String)metadata.getFirst().get("room_id");if(admin)landlords.requireOwner(username,room);else if(jdbc.queryForList("SELECT l.room_id FROM room_landlords l JOIN user u ON u.id=l.user_id WHERE l.room_id=? AND u.role=1",room).isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Phòng chưa được niêm yết");
  return new Photo(jdbc.queryForObject("SELECT "+(thumbnail?"thumbnail_data":"image_data")+" FROM room_photos WHERE id=?",byte[].class,id),thumbnail?"image/jpeg":(String)metadata.getFirst().get("mime_type"));
 }
 @Transactional public void deletePhoto(String username,String room,String photo){lock(username,room);if(jdbc.update("DELETE FROM room_photos WHERE id=? AND room_id=?",photo,room)==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không có ảnh thuộc phòng này");}
}
