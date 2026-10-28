package com.ntd.csdl;
import com.ntd.csdl.service.LandlordService;
import com.ntd.csdl.controller.DemoDataController;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.repo.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
class LandlordInventoryTests {
 JdbcTemplate jdbc;LandlordService service;User a,b;TransactionTemplate tx;
 @BeforeEach void init(){
  var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa","");jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
  jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY,username VARCHAR(100),role INT)");
  jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,floor INT,area REAL,room_type VARCHAR(50),status VARCHAR(50),base_price DECIMAL(18,2))");
  jdbc.execute("CREATE TABLE landlord_properties(user_id BIGINT PRIMARY KEY REFERENCES user(id),room_limit INT)");
  jdbc.execute("CREATE TABLE room_landlords(room_id VARCHAR(20) PRIMARY KEY REFERENCES rooms(room_id),user_id BIGINT REFERENCES landlord_properties(user_id))");
  jdbc.update("INSERT INTO user VALUES(1,'A',1),(2,'B',1)");
  a=new User();a.setId(1L);a.setUsername("A");a.setRole(UserEnum.ADMIN);b=new User();b.setId(2L);b.setUsername("B");b.setRole(UserEnum.ADMIN);
  UserRepository users=mock(UserRepository.class);when(users.findByUsername("A")).thenReturn(Optional.of(a));when(users.findByUsername("B")).thenReturn(Optional.of(b));
  RoomRepository rooms=mock(RoomRepository.class);
  when(rooms.saveAndFlush(any(Room.class))).thenAnswer(i->{Room r=i.getArgument(0);jdbc.update("INSERT INTO rooms VALUES(?,?,?,?,?,?)",r.getRoomId(),r.getFloor(),r.getArea(),r.getRoomType(),r.getStatus(),r.getBasePrice());return r;});
  when(rooms.lockById(anyString())).thenAnswer(i->jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=?",(Object)i.getArgument(0)).isEmpty()?Optional.empty():Optional.of(Room.builder().roomId(i.getArgument(0)).build()));
  service=new LandlordService(jdbc,users,rooms);
 }
 int count(){return jdbc.queryForObject("SELECT COUNT(*) FROM rooms",Integer.class);}
 @Test void inventoryIsFixedAndDifferentOwnersHaveSeparateRooms(){
  tx.executeWithoutResult(t->service.setup(a,2,List.of()));tx.executeWithoutResult(t->service.setup(b,3,List.of()));
  assertEquals(5,count());assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM room_landlords WHERE user_id=1",Integer.class));
  assertThrows(ResponseStatusException.class,()->tx.executeWithoutResult(t->service.setup(a,4,List.of())));assertEquals(5,count());
  String room=jdbc.queryForObject("SELECT room_id FROM room_landlords WHERE user_id=1 LIMIT 1",String.class);
  service.requireOwner("A",room);assertThrows(ResponseStatusException.class,()->service.requireOwner("B",room));assertEquals(5,service.catalog().size());
 }
 @Test void adoptingOldRoomsDoesNotDuplicateThemAndCannotSteal(){
  jdbc.update("INSERT INTO rooms(room_id,status) VALUES('OLD','AVAILABLE')");
  tx.executeWithoutResult(t->service.setup(a,1,List.of("OLD")));assertEquals(1,count());assertEquals(1L,service.owner("OLD"));
  assertThrows(ResponseStatusException.class,()->tx.executeWithoutResult(t->service.setup(b,1,List.of("OLD"))));assertEquals(1,count());assertEquals(false,service.property("B").get("configured"));
 }
 @Test void invalidCapacityAndDuplicateIdsLeaveNoInventory(){
  for(Integer n:Arrays.asList(null,0,-1,201))assertThrows(ResponseStatusException.class,()->tx.executeWithoutResult(t->service.setup(a,n,List.of())));
  assertThrows(ResponseStatusException.class,()->tx.executeWithoutResult(t->service.setup(a,2,List.of("X","X"))));assertEquals(0,count());
 }
 @Test void oldDemoEndpointCannotCreateRooms(){
  var e=assertThrows(ResponseStatusException.class,()->new DemoDataController().myRental());assertEquals(410,e.getStatusCode().value());assertEquals(0,count());
 }
}
