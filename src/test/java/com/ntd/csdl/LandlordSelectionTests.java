package com.ntd.csdl;
import com.ntd.csdl.service.*;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.dto.*;
import com.ntd.csdl.repo.*;
import com.ntd.csdl.controller.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.*;
import java.math.BigDecimal;
class LandlordSelectionTests {
 JdbcTemplate jdbc;LandlordService service;RoomRepository rooms;User a,b;
 @BeforeEach void init(){
  jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY,username VARCHAR(100),role INT)");
  jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,floor INT,area REAL,room_type VARCHAR(50),status VARCHAR(50),base_price DECIMAL(18,2))");
  jdbc.execute("CREATE TABLE landlord_properties(user_id BIGINT PRIMARY KEY REFERENCES user(id),room_limit INT)");
  jdbc.execute("CREATE TABLE room_landlords(room_id VARCHAR(20) PRIMARY KEY REFERENCES rooms(room_id),user_id BIGINT REFERENCES landlord_properties(user_id))");
  jdbc.execute("CREATE TABLE contract_stays(contract_id VARCHAR(20),check_in DATE,check_out DATE)");
  jdbc.execute("CREATE TABLE tenant_landlord_selection(user_id BIGINT PRIMARY KEY REFERENCES user(id),landlord_id BIGINT REFERENCES landlord_properties(user_id))");
  jdbc.execute("CREATE TABLE tenant_landlords(tenant_id VARCHAR(20),user_id BIGINT,PRIMARY KEY(tenant_id,user_id))");
  jdbc.execute("CREATE TABLE contracts(contract_id VARCHAR(20),tenant_id VARCHAR(20),room_id VARCHAR(20),start_date DATE,end_date DATE)");
  jdbc.update("INSERT INTO user VALUES(1,'A',1),(2,'B',1),(3,'user1',0),(4,'user2',0)");
  UserRepository users=mock(UserRepository.class);for(int i=1;i<=4;i++){User u=new User();u.setId((long)i);u.setUsername(i==1?"A":i==2?"B":"user"+(i-2));u.setRole(i<=2?UserEnum.ADMIN:UserEnum.USER);when(users.findByUsername(u.getUsername())).thenReturn(Optional.of(u));if(i==1)a=u;if(i==2)b=u;}
  rooms=mock(RoomRepository.class);when(rooms.saveAndFlush(any(Room.class))).thenAnswer(i->{Room q=i.getArgument(0);jdbc.update("INSERT INTO rooms VALUES(?,?,?,?,?,?)",q.getRoomId(),q.getFloor(),q.getArea(),q.getRoomType(),q.getStatus(),q.getBasePrice());return q;});
  when(rooms.findAllById(any())).thenAnswer(i->{List<Room> result=new ArrayList<>();for(String id:(Iterable<String>)i.getArgument(0))result.add(Room.builder().roomId(id).build());return result;});service=new LandlordService(jdbc,users,rooms);
  service.declareRooms(a,List.of(room("101"),room("102")));service.declareRooms(b,List.of(room("101")));
 }
 RoomDTO room(String code){return new RoomDTO(code,1,20f,"Phòng đơn","AVAILABLE",new BigDecimal("2000000"));}
 @Test void roomsDeclareTheirOwnerWithoutRequiringAnAdminSelection(){
  var catalog=service.browseRooms();assertEquals(3,catalog.size());
  assertEquals(2,catalog.stream().filter(room->"A".equals(room.getLandlordName())).count());
  assertEquals(1,catalog.stream().filter(room->"B".equals(room.getLandlordName())).count());
  service.requireListed("L1-101");service.requireListed("L2-101");
  assertThrows(ResponseStatusException.class,()->service.requireListed("UNASSIGNED"));
  assertEquals(2,service.mine("A").size());assertEquals(1,service.mine("B").size());
  assertThrows(ResponseStatusException.class,()->service.requireOwner("A","L2-101"));
 }
 @Test void allAdminAccountsAppearIncludingThoseWithoutRooms(){
  jdbc.update("INSERT INTO user VALUES(5,'C',1)");
  var all=service.landlords();assertEquals(3,all.size());
  var empty=all.stream().filter(row->"C".equals(row.get("landlordName"))).findFirst().orElseThrow();
  assertEquals(0,((Number)empty.get("roomCount")).intValue());assertEquals(false,empty.get("configured"));
  assertEquals(3,service.browseRooms().size(),"Do not invent ownership for an admin without rooms");
 }
 @Test void tenantsAreCountedOnceOnlyForTheirLandlord(){
  jdbc.update("INSERT INTO contracts(tenant_id,room_id) VALUES('T1','L1-101'),('T1','L1-102'),('T2','L2-101')");service.linkTenant("A","T3");
  assertEquals(Set.of("T1","T3"),service.tenantIds("A"));assertEquals(Set.of("T2"),service.tenantIds("B"));
  service.requireTenant("A","T1");assertThrows(ResponseStatusException.class,()->service.requireTenant("B","T1"));
  jdbc.update("INSERT INTO contracts(tenant_id,room_id) VALUES('T1','L2-101')");assertThrows(ResponseStatusException.class,()->service.requireExclusiveTenant("A","T1"));
 }
 @Test void invalidRoomDeclarationCannotCreatePartialInventory(){
  User u=new User();u.setId(3L);u.setRole(UserEnum.ADMIN);RoomDTO bad=room("202");bad.setArea(-1f);
  assertThrows(ResponseStatusException.class,()->service.declareRooms(u,List.of(room("201"),bad)));
  assertTrue(jdbc.queryForList("SELECT user_id FROM landlord_properties WHERE user_id=3").isEmpty());
  assertThrows(ResponseStatusException.class,()->service.declareRooms(u,List.of(room("201"),room("201"))));
  assertThrows(ResponseStatusException.class,()->service.declareRooms(u,List.of()));
 }
 @Test void roomDetailsAllowEitherPublishedOwnerButRejectUnassignedRooms(){
  var controller=new RoomController(mock(RoomService.class),service);var auth=new UsernamePasswordAuthenticationToken("user1",null,List.of());
  assertEquals("A",controller.getById("L1-101",auth).getBody().getLandlordName());
  assertEquals("B",controller.getById("L2-101",auth).getBody().getLandlordName());
  assertThrows(ResponseStatusException.class,()->controller.getById("UNASSIGNED",auth));
 }
 @Test void tenantEndpointsFilterListAndRejectOtherOwnersEdits(){
  jdbc.update("INSERT INTO contracts(tenant_id,room_id) VALUES('T1','L1-101'),('T2','L2-101')");
  Tenant t1=new Tenant();t1.setTenantId("T1");Tenant t2=new Tenant();t2.setTenantId("T2");
  TenantService tenants=mock(TenantService.class);when(tenants.getAll()).thenReturn(List.of(t1,t2));
  var controller=new TenantController(tenants,service);var auth=new UsernamePasswordAuthenticationToken("A",null,List.of());
  assertEquals(List.of(t1),controller.getAll(auth).getBody());
  assertThrows(ResponseStatusException.class,()->controller.getById("T2",auth));
  assertThrows(ResponseStatusException.class,()->controller.update("T2",new TenantDTO(),auth));
  assertThrows(ResponseStatusException.class,()->controller.delete("T2",auth));
 }
 @Test void publicOccupancyMatchesContractDatesWithoutExposingTenantData(){
  jdbc.update("INSERT INTO contracts(contract_id,tenant_id,room_id,start_date,end_date) VALUES('C1','T1','L1-101',?,?),('C2','T2','L2-101',?,?)",com.ntd.csdl.service.StayService.today().minusDays(5),com.ntd.csdl.service.StayService.today().plusDays(5),com.ntd.csdl.service.StayService.today().minusDays(10),com.ntd.csdl.service.StayService.today().minusDays(1));
  jdbc.update("UPDATE rooms SET status='UNCONFIGURED' WHERE room_id='L1-102'");
  var catalog=service.catalog();var occupied=catalog.stream().filter(row->row.get("roomId").equals("L1-101")).findFirst().orElseThrow();
  assertEquals(true,occupied.get("occupied"));assertEquals(false,occupied.get("availableForRent"));assertThrows(ResponseStatusException.class,()->service.requireRentable("L1-101"));
  var empty=catalog.stream().filter(row->row.get("roomId").equals("L1-102")).findFirst().orElseThrow();assertEquals(false,empty.get("occupied"));assertEquals(true,empty.get("occupancyKnown"));assertEquals(false,empty.get("availableForRent"));
  assertFalse(occupied.containsKey("tenantId"));assertFalse(occupied.containsKey("contractId"));
  service.requireRentable("L2-101");
  assertEquals(1,service.mine("A").stream().filter(room->Boolean.TRUE.equals(room.getOccupied())).count());
  jdbc.update("INSERT INTO contract_stays VALUES('C1',NULL,?)",com.ntd.csdl.service.StayService.today());
  assertFalse(service.browseRooms().stream().filter(room->room.getRoomId().equals("L1-101")).findFirst().orElseThrow().getOccupied());
 }
}
