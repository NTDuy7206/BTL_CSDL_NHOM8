package com.ntd.csdl;
import com.ntd.csdl.service.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.*;
import java.math.BigDecimal;
import java.util.*;
class DepositServiceTests {
 JdbcTemplate jdbc;DepositService service;String key="test-only-not-a-live-key-01234567890123456789";
 @BeforeEach void init() throws Exception {
  jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY)");jdbc.update("INSERT INTO user VALUES(1),(2)");
  jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,base_price DECIMAL(18,2))");jdbc.update("INSERT INTO rooms VALUES('R1',2000000)");
  jdbc.execute("CREATE TABLE contracts(contract_id VARCHAR(20) PRIMARY KEY,room_id VARCHAR(20),start_date DATE,end_date DATE)");
  jdbc.execute("CREATE TABLE contract_stays(contract_id VARCHAR(20),check_in DATE,check_out DATE)");
  jdbc.execute("CREATE TABLE contract_requests(request_id BIGINT PRIMARY KEY,user_id BIGINT,room_id VARCHAR(20),requested_start_date DATE,status VARCHAR(20))");
  jdbc.update("INSERT INTO contract_requests VALUES(10,2,'R1','2026-12-01','PENDING'),(11,2,'R1','2026-12-02','PENDING')");
  jdbc.execute("CREATE TABLE rental_request_details(request_id BIGINT PRIMARY KEY,requested_end_date DATE)");jdbc.update("INSERT INTO rental_request_details VALUES(10,'2027-12-01'),(11,'2027-12-02')");
  for(String sql:Files.readString(Path.of("src/main/resources/deposits-schema.sql")).split(";"))if(!sql.isBlank())jdbc.execute(sql);
  var owners=mock(LandlordService.class);when(owners.accountId("A")).thenReturn(1L);when(owners.accountId("B")).thenReturn(2L);when(owners.owner("R1")).thenReturn(1L);
  service=new DepositService(jdbc,owners);service.saveBank("A",new DepositService.Bank("MBBank","123456789","NGUYEN VAN A",key));service.initialize(10,"R1");
 }
 DepositService.Event event(long id,String account,String bank,String direction,String code,String money){return new DepositService.Event(id,bank,account,null,null,code,direction,new BigDecimal(money));}
 String code(){return service.details(10).get("paymentCode").toString();}
 @Test void snapshotDepositAndApproveOnlyAfterVerifiedTransfer(){
  assertEquals(new BigDecimal("2000000.00"),service.amount(10));assertFalse(service.prepareApproval(10));
  assertTrue(service.details(10).get("qrUrl").toString().contains("amount=2000000&"));
  jdbc.update("UPDATE rooms SET base_price=3000000 WHERE room_id='R1'");assertEquals(new BigDecimal("2000000.00"),service.amount(10));
  assertEquals("PAID",service.webhook(1,"Apikey "+key,event(1,"123456789","MBBank","in",code(),"2000000")).get("result"));
  assertTrue(service.prepareApproval(10));assertEquals("DUPLICATE",service.webhook(1,"Apikey "+key,event(1,"123456789","MBBank","in",code(),"2000000")).get("result"));
  assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM deposit_bank_events",Integer.class));
  assertThrows(ResponseStatusException.class,()->service.reject(10));
 }
 @Test void mismatchesAndAnotherOwnerCannotMarkDepositPaid(){
  service.prepareApproval(10);
  assertThrows(ResponseStatusException.class,()->service.webhook(1,"Apikey wrong",event(1,"123456789","MBBank","in",code(),"2000000")));
  assertThrows(ResponseStatusException.class,()->service.webhook(2,"Apikey "+key,event(1,"123456789","MBBank","in",code(),"2000000")));
  assertEquals("NEEDS_REVIEW",service.webhook(1,"Apikey "+key,event(2,"999","MBBank","in",code(),"2000000")).get("result"));
  assertEquals("NEEDS_REVIEW",service.webhook(1,"Apikey "+key,event(3,"123456789","VCB","in",code(),"2000000")).get("result"));
  assertEquals("NEEDS_REVIEW",service.webhook(1,"Apikey "+key,event(4,"123456789","MBBank","out",code(),"2000000")).get("result"));
  assertEquals("NEEDS_REVIEW",service.webhook(1,"Apikey "+key,event(5,"123456789","MBBank","in",code(),"1")).get("result"));
  assertEquals("AWAITING_PAYMENT",service.details(10).get("status"));assertFalse(service.prepareApproval(10));
 }
 @Test void noBankSetupNoPaymentAndOverlappingRequestsCannotBothCollect(){
  service.initialize(11,"R1");assertFalse(service.prepareApproval(10));assertThrows(ResponseStatusException.class,()->service.prepareApproval(11));
  service.reject(10);assertFalse(service.prepareApproval(11));
  assertFalse(service.ownBank("B").containsKey("webhook_key_hash"));assertTrue(service.events("B").isEmpty());
 }
 @Test void existingContractBlocksCollectionBeforeMoneyIsTransferred(){
  jdbc.update("INSERT INTO contracts VALUES('OLD','R1','2026-11-01','2027-01-01')");
  assertThrows(ResponseStatusException.class,()->service.prepareApproval(10));
  assertEquals("REVIEW",service.details(10).get("status"));
 }

 @Test void depositInvoiceIsVisibleOnlyToItsTenantAndLandlordAndCanBeConfirmedOnce() {
  assertTrue(service.invoices("B",false).isEmpty());
  jdbc.update("UPDATE landlord_bank_accounts SET webhook_key_hash=NULL WHERE user_id=1");
  assertFalse(service.prepareApproval(10));
  assertEquals(1,service.invoices("B",false).size());
  assertTrue(service.invoices("A",false).isEmpty());
  assertTrue(service.invoices("B",true).isEmpty());
  assertThrows(ResponseStatusException.class,()->service.confirmReceived("A",10));
  assertThrows(ResponseStatusException.class,()->service.reportTransfer("A",10,"BANK-123"));

  service.reportTransfer("B",10,"BANK-123");
  service.reportTransfer("B",10,"BANK-456");
  assertEquals("BANK-123",service.invoices("A",true).getFirst().get("bankReference"));
  assertEquals("AWAITING_PAYMENT",service.details(10).get("status"));
  assertFalse(service.prepareApproval(10));
  assertThrows(ResponseStatusException.class,()->service.confirmReceived("B",10));

  service.confirmReceived("A",10);
  Object confirmedAt=service.details(10).get("confirmedAt");
  assertNotNull(confirmedAt);
  service.confirmReceived("A",10);
  assertEquals(confirmedAt,service.details(10).get("confirmedAt"));
  assertEquals("PAID",service.invoices("B",false).getFirst().get("status"));
  assertTrue(service.prepareApproval(10));
 }

 @Test void incorrectBankSnapshotCanBeRefreshedBeforeTransfer() {
  service.prepareApproval(10);
  jdbc.update("UPDATE rental_deposits SET bank_name='12345' WHERE request_id=10");
  assertFalse((Boolean)service.details(10).get("bankReady"));
  assertNull(service.details(10).get("qrUrl"));
  assertThrows(ResponseStatusException.class,()->service.reportTransfer("B",10,"BANK-123"));
  assertFalse(service.prepareApproval(10));
  assertEquals("MBBank",service.details(10).get("bankName"));
  assertNotNull(service.details(10).get("qrUrl"));
 }

 @Test void oldUnpaidDepositDoesNotBlockRentalApproval() {
  service.prepareApproval(10);
  assertEquals(BigDecimal.ZERO, service.closeForRentalApproval(10));
  assertEquals("CANCELLED", service.details(10).get("status"));
 }
}
