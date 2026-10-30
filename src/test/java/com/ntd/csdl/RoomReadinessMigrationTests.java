package com.ntd.csdl;
import com.ntd.csdl.service.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import java.math.BigDecimal;

class RoomReadinessMigrationTests {
 @Test void opensExistingVacanciesOnceWithoutChangingTenanciesMaintenanceOrValidPrices() {
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("CREATE TABLE demo_seed_history(seed_key VARCHAR(100) PRIMARY KEY,completed BOOLEAN)");
  jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,area REAL,base_price DECIMAL(18,2),room_type VARCHAR(50),floor INT,status VARCHAR(50))");
  jdbc.execute("CREATE TABLE contracts(contract_id VARCHAR(20),room_id VARCHAR(20),end_date DATE)");
  jdbc.execute("CREATE TABLE contract_stays(contract_id VARCHAR(20),check_out DATE)");
  for(String id:List.of("blank","priced","occupied","repair","unknown","future","unassigned"))jdbc.update("INSERT INTO rooms VALUES(?,0,0,'Chưa cấu hình',NULL,'UNCONFIGURED')",id);
  jdbc.update("UPDATE rooms SET area=35,base_price=523251,room_type='VIP',floor=3 WHERE room_id='priced'");
  jdbc.update("UPDATE rooms SET status='MAINTENANCE' WHERE room_id='repair'");
  jdbc.update("INSERT INTO contracts VALUES('F','future',?)",StayService.today().plusMonths(2));
  var owners=mock(LandlordService.class);
  var catalog=new ArrayList<Map<String,Object>>();
  for(String id:List.of("blank","priced","occupied","repair","unknown","future"))catalog.add(Map.of("roomId",id,"status",id.equals("repair")?"MAINTENANCE":"UNCONFIGURED","occupied",id.equals("occupied"),"occupancyKnown",!id.equals("unknown")));
  when(owners.catalog()).thenReturn(catalog);
  var migration=new RoomReadinessMigration(jdbc,owners);
  assertEquals(2,migration.apply());
  assertEquals(25f,jdbc.queryForObject("SELECT area FROM rooms WHERE room_id='blank'",Float.class));
  assertEquals(new BigDecimal("2000000.00"),jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='blank'",BigDecimal.class));
  assertEquals(new BigDecimal("523251.00"),jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='priced'",BigDecimal.class));
  assertEquals("VIP",jdbc.queryForObject("SELECT room_type FROM rooms WHERE room_id='priced'",String.class));
  for(String id:List.of("occupied","unknown","future","unassigned"))assertEquals("UNCONFIGURED",jdbc.queryForObject("SELECT status FROM rooms WHERE room_id=?",String.class,id));
  assertEquals("MAINTENANCE",jdbc.queryForObject("SELECT status FROM rooms WHERE room_id='repair'",String.class));
  jdbc.update("UPDATE rooms SET status='UNCONFIGURED' WHERE room_id='blank'");
  assertEquals(0,migration.apply());
  assertEquals("UNCONFIGURED",jdbc.queryForObject("SELECT status FROM rooms WHERE room_id='blank'",String.class));
  assertEquals(7,jdbc.queryForObject("SELECT COUNT(*) FROM rooms",Integer.class));
 }
}
