package com.ntd.csdl;
import com.ntd.csdl.service.RoomSamplePhotoService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.UUID;

class RoomSamplePhotoTests {
 @Test void addsPersistentReadablePhotosOnlyToAssignedEmptyRoomsAndDoesNotReseed() throws Exception {
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("CREATE TABLE demo_seed_history(seed_key VARCHAR(100) PRIMARY KEY,completed BOOLEAN)");
  jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY)");
  jdbc.execute("CREATE TABLE room_landlords(room_id VARCHAR(20) PRIMARY KEY,user_id BIGINT)");
  jdbc.execute("CREATE TABLE room_photos(id VARCHAR(36) PRIMARY KEY,room_id VARCHAR(20),mime_type VARCHAR(30),image_data BLOB,thumbnail_data BLOB)");
  jdbc.update("INSERT INTO rooms VALUES('empty'),('real'),('unassigned')");
  jdbc.update("INSERT INTO room_landlords VALUES('empty',1),('real',2)");
  jdbc.update("INSERT INTO room_photos VALUES('owner-photo','real','image/jpeg',?,?)",new byte[]{1,2},new byte[]{3,4});
  var service=new RoomSamplePhotoService(jdbc);
  assertEquals(1,service.seed());
  var row=jdbc.queryForMap("SELECT id,mime_type,image_data,thumbnail_data FROM room_photos WHERE room_id='empty'");
  assertTrue(row.get("id").toString().startsWith("demo-"));
  assertEquals("image/jpeg",row.get("mime_type"));
  var full=ImageIO.read(new ByteArrayInputStream((byte[])row.get("image_data")));
  var thumb=ImageIO.read(new ByteArrayInputStream((byte[])row.get("thumbnail_data")));
  assertTrue(full.getWidth()<=1200&&full.getHeight()<=800);
  assertTrue(thumb.getWidth()<=640&&thumb.getHeight()<=480);
  assertArrayEquals(new byte[]{1,2},jdbc.queryForObject("SELECT image_data FROM room_photos WHERE room_id='real'",byte[].class));
  assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM room_photos",Integer.class));
  jdbc.update("DELETE FROM room_photos WHERE room_id='empty'");
  assertEquals(0,service.seed());
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM room_photos",Integer.class));
 }
}
