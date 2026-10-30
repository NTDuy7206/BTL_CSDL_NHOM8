package com.ntd.csdl;
import com.ntd.csdl.service.*;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.dto.*;
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
import java.util.*;import java.time.*;import java.math.BigDecimal;import java.nio.file.*;
class FinanceTests {
 JdbcTemplate jdbc;FinanceService finance;TransactionTemplate tx;LandlordService owners;
 BigDecimal amount(String value){return new BigDecimal(value);}
 LocalDate date(String value){return LocalDate.parse(value);}
 @BeforeEach void setup() throws Exception {
  var ds=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;NON_KEYWORDS=USER,YEAR,MONTH;DB_CLOSE_DELAY=-1","sa","");jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
  jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY)");jdbc.execute("CREATE TABLE room_landlords(room_id VARCHAR(20) PRIMARY KEY,user_id BIGINT)");jdbc.execute("CREATE TABLE contracts(contract_id VARCHAR(20) PRIMARY KEY,room_id VARCHAR(20))");
  jdbc.execute("CREATE TABLE invoices(invoice_id VARCHAR(20) PRIMARY KEY,contract_id VARCHAR(20),year INT,month INT,electricity_amount DECIMAL(18,2),water_amount DECIMAL(18,2))");
  jdbc.execute("CREATE TABLE payment_reports(payment_report_id BIGINT PRIMARY KEY,invoice_id VARCHAR(20),amount DECIMAL(18,2),status VARCHAR(20),created_at TIMESTAMP)");
  String schema=Files.readString(Path.of("src/main/resources/schema.sql"));schema=schema.substring(schema.indexOf("CREATE TABLE IF NOT EXISTS finance_receipts"));
  for(String statement:schema.split(";")){if(!statement.isBlank())jdbc.execute(statement.replaceAll("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci", ""));}
  jdbc.update("INSERT INTO user VALUES(1),(2),(3)");jdbc.update("INSERT INTO room_landlords VALUES('R1',1),('R2',2)");jdbc.update("INSERT INTO contracts VALUES('C1','R1'),('C2','R2')");
  jdbc.update("INSERT INTO invoices VALUES('I1','C1',2026,10,100,50),('I2','C1',2026,10,200,75),('I3','C2',2026,10,900,600),('I4','C1',2026,11,400,125),('I5','C1',2025,12,30,15)");
  jdbc.update("INSERT INTO payment_reports VALUES(1,'I1',1000,'PAID','2026-09-01 10:00:00'),(2,'I2',2000,'PAID','2026-09-01 10:00:00'),(3,'I3',5000,'PAID','2026-09-01 10:00:00'),(4,'I4',9000,'PENDING','2026-09-01 10:00:00'),(5,'I5',300,'PAID','2025-12-01 10:00:00'),(6,'I1',1000,'PAID','2026-09-01 10:00:00')");
  owners=mock(LandlordService.class);when(owners.accountId("A")).thenReturn(1L);when(owners.accountId("B")).thenReturn(2L);finance=new FinanceService(jdbc,owners);
 }
 @SuppressWarnings("unchecked") Map<String,Object> totals(String owner,String from,String to,String group){return (Map<String,Object>)finance.report(owner,date(from),date(to),group).get("totals");}
 void eq(String expected,Object actual){assertEquals(0,amount(expected).compareTo(new BigDecimal(actual.toString())));}
 @Test void billedUtilitiesAreOwnedAndAreNotExpensesUntilPaid(){
  var a=finance.utilities("A",2026,10);var billed=(Map<?,?>)a.get("billed");eq("300",billed.get("electricity"));eq("125",billed.get("water"));eq("425",billed.get("totalUtilities"));eq("2",billed.get("invoiceCount"));
  var b=(Map<?,?>)finance.utilities("B",2026,10).get("billed");eq("900",b.get("electricity"));
  eq("0",totals("A","2026-10-01","2026-10-31","month").get("expenses"));
 }
 @Test void exactIncomeExpenseAndNegativeProfitGroupedByActualDayMonthYear(){
  finance.allocateReceipt("A",1L,date("2026-10-05"));finance.allocateReceipt("A",2L,date("2026-10-06"));finance.allocateReceipt("B",3L,date("2026-10-05"));finance.allocateReceipt("A",5L,date("2025-12-31"));
  tx.executeWithoutResult(t->finance.payUtility("A",2026,9,"ELECTRICITY",amount("250.25"),date("2026-10-05"),"Điện lực","E1"));
  tx.executeWithoutResult(t->finance.payUtility("A",2026,9,"WATER",amount("100.10"),date("2026-10-06"),"Cấp nước","W1"));
  tx.executeWithoutResult(t->finance.expense("A",amount("50.05"),date("2026-10-05"),"Sửa vòi nước","X1"));
  var day=totals("A","2026-10-05","2026-10-05","day");eq("1000",day.get("income"));eq("699.70",day.get("profit"));
  var month=totals("A","2026-10-01","2026-10-31","month");eq("3000",month.get("income"));eq("400.40",month.get("expenses"));eq("2599.60",month.get("profit"));
  var summary=finance.summary("A",date("2026-10-05"));eq("699.70",((Map<?,?>)summary.get("day")).get("profit"));eq("2599.60",((Map<?,?>)summary.get("month")).get("profit"));
  eq("3000",totals("A","2026-01-01","2026-12-31","year").get("income"));eq("300",totals("A","2025-01-01","2025-12-31","year").get("income"));
  eq("5000",totals("B","2026-10-01","2026-10-31","month").get("profit"));
  eq("0",totals("A","2026-09-01","2026-09-30","day").get("income"));
  tx.executeWithoutResult(t->finance.expense("A",amount("1200"),date("2026-10-07"),"Sửa máy lạnh","X2"));eq("-1200",totals("A","2026-10-07","2026-10-07","day").get("profit"));
  assertEquals(3,((List<?>)finance.report("A",date("2026-10-05"),date("2026-10-07"),"day").get("rows")).size());
 }
 @Test void pendingOtherOwnerAndDuplicateReceiptsCannotCountAsIncome(){
  assertThrows(ResponseStatusException.class,()->finance.allocateReceipt("A",4L,date("2026-10-05")));
  assertThrows(ResponseStatusException.class,()->finance.allocateReceipt("A",3L,date("2026-10-05")));
  finance.allocateReceipt("A",1L,date("2026-10-05"));assertThrows(ResponseStatusException.class,()->finance.allocateReceipt("A",1L,date("2026-10-05")));assertThrows(ResponseStatusException.class,()->finance.allocateReceipt("A",6L,date("2026-10-05")));
  eq("1000",totals("A","2026-10-01","2026-10-31","month").get("income"));
 }
 @Test void duplicateUtilityAndExpenseAreRejectedWithoutDoubleCosts(){
  tx.executeWithoutResult(t->finance.payUtility("A",2026,9,"ELECTRICITY",amount("1"),date("2026-10-05"),"EVN","E1"));
  assertThrows(ResponseStatusException.class,()->tx.executeWithoutResult(t->finance.payUtility("A",2026,9,"ELECTRICITY",amount("1"),date("2026-10-05"),"EVN","E2")));
  tx.executeWithoutResult(t->finance.expense("A",amount("1"),date("2026-10-05"),"Chi phí","X1"));assertThrows(ResponseStatusException.class,()->tx.executeWithoutResult(t->finance.expense("A",amount("1"),date("2026-10-06"),"Chi phí","X1")));
  tx.executeWithoutResult(t->finance.expense("B",amount("1"),date("2026-10-05"),"Chi phí","X1"));
  eq("2",totals("A","2026-10-01","2026-10-31","month").get("expenses"));assertEquals(1,finance.expenses("A").size());
 }
 @Test void legacyReceiptsAreFlaggedInsteadOfAssignedFakeDates(){
  assertEquals(4,finance.unallocated("A").size());eq("0",totals("A","2026-09-01","2026-09-30","day").get("income"));
  assertThrows(ResponseStatusException.class,()->finance.allocateReceipt("A",1L,null));finance.allocateReceipt("A",1L,date("2026-10-05"));assertEquals(2,finance.unallocated("A").size(),"Duplicate reports for one invoice must not request a second allocation");
 }
 @Test void invalidAmountsDatesAndReportRangesAreRejected(){
  for(String value:List.of("0","-1","0.001","10000000000000000"))assertThrows(ResponseStatusException.class,()->finance.expense("A",amount(value),StayService.today(),"Chi phí","test"));
  assertThrows(ResponseStatusException.class,()->finance.expense("A",amount("1"),StayService.today().plusDays(1),"Chi phí","test"));
  assertThrows(ResponseStatusException.class,()->finance.payUtility("A",2026,13,"ELECTRICITY",amount("1"),StayService.today(),"EVN","E1"));
  assertThrows(ResponseStatusException.class,()->finance.report("A",date("2026-01-01"),date("2026-12-31"),"invalid"));assertThrows(ResponseStatusException.class,()->finance.report("A",date("2025-01-01"),date("2026-12-31"),"day"));
  assertThrows(ResponseStatusException.class,()->finance.expense("A",amount("1"),StayService.today(),"","test"));
 }
 @Test void confirmationLocksReportAndWritesReceiptExactlyOnce(){
  var repo=mock(PaymentReportRepository.class);var ledger=mock(FinanceService.class);var service=new PaymentReportService(repo,mock(InvoiceRepository.class),mock(UserRepository.class),mock(RentalWorkflowService.class),owners,ledger);
  PaymentReport p=new PaymentReport();p.setPaymentReportId(1L);p.setAmount(amount("1000"));p.setStatus(PaymentReportStatus.PENDING);User payer=new User();payer.setId(3L);payer.setUsername("tenant");p.setPayer(payer);p.setInvoice(Invoice.builder().invoiceId("I1").contract(Contract.builder().room(Room.builder().roomId("R1").build()).build()).build());
  when(repo.lockById(1L)).thenReturn(Optional.of(p));var dto=new PaymentReportStatusDTO();dto.setStatus("PAID");dto.setReceivedOn(date("2026-10-05"));service.updateStatus(1L,dto,"A");verify(ledger).confirmReceipt("A",p,date("2026-10-05"));assertEquals(PaymentReportStatus.PAID,p.getStatus());
  assertThrows(ResponseStatusException.class,()->service.updateStatus(1L,dto,"A"));verify(ledger,times(1)).confirmReceipt(anyString(),any(),any());
 }
 @Test void concurrentUtilityPaymentCannotCreateTwoExpenses() throws Exception {
  var pool=java.util.concurrent.Executors.newFixedThreadPool(2);var start=new java.util.concurrent.CountDownLatch(1);
  java.util.concurrent.Callable<Boolean> task=()->{start.await();try{tx.executeWithoutResult(t->finance.payUtility("A",2026,9,"ELECTRICITY",amount("10"),date("2026-10-05"),"EVN","E1"));return true;}catch(ResponseStatusException e){assertEquals(409,e.getStatusCode().value());return false;}};
  try{var one=pool.submit(task);var two=pool.submit(task);start.countDown();assertNotEquals(one.get(10,java.util.concurrent.TimeUnit.SECONDS),two.get(10,java.util.concurrent.TimeUnit.SECONDS));eq("10",totals("A","2026-10-01","2026-10-31","month").get("expenses"));}finally{pool.shutdownNow();}
 }
 @Test void concurrentDuplicateLegacyReceiptsCannotDoubleIncome() throws Exception {
  var pool=java.util.concurrent.Executors.newFixedThreadPool(2);var start=new java.util.concurrent.CountDownLatch(1);
  java.util.function.Function<Long,java.util.concurrent.Callable<Boolean>> task=id->()->{start.await();try{tx.executeWithoutResult(t->finance.allocateReceipt("A",id,date("2026-10-05")));return true;}catch(ResponseStatusException e){assertEquals(409,e.getStatusCode().value());return false;}};
  try{var one=pool.submit(task.apply(1L));var two=pool.submit(task.apply(6L));start.countDown();assertNotEquals(one.get(10,java.util.concurrent.TimeUnit.SECONDS),two.get(10,java.util.concurrent.TimeUnit.SECONDS));eq("1000",totals("A","2026-10-01","2026-10-31","month").get("income"));}finally{pool.shutdownNow();}
 }
 @Test void utilityCardsOpenOnlyAtMonthEndAndDisappearIntoHistoryAfterPayment(){
  try(var clock=mockStatic(StayService.class)){
   clock.when(StayService::today).thenReturn(date("2026-10-30"));
   var before=finance.utilities("A",2026,10);assertEquals(false,before.get("due"));assertEquals(0,((List<Map<String,Object>>)before.get("pending")).stream().filter(i->((Number)i.get("year")).intValue()==2026&&((Number)i.get("month")).intValue()==10).count());
   assertThrows(ResponseStatusException.class,()->finance.payUtility("A",2026,10,"ELECTRICITY",amount("300"),date("2026-10-30"),"EVN","E1"));
   clock.when(StayService::today).thenReturn(date("2026-10-31"));
   var due=finance.utilities("A",2026,10);assertEquals(true,due.get("due"));assertEquals(2,((List<Map<String,Object>>)due.get("pending")).stream().filter(i->((Number)i.get("year")).intValue()==2026&&((Number)i.get("month")).intValue()==10).count());
   assertThrows(ResponseStatusException.class,()->finance.payUtility("A",2026,10,"ELECTRICITY",amount("300"),date("2026-10-30"),"EVN","E1"));
   tx.executeWithoutResult(t->finance.payUtility("A",2026,10,"ELECTRICITY",amount("300"),date("2026-10-31"),"EVN","E1"));
   var paid=finance.utilities("A",2026,10);assertEquals(1,((List<?>)paid.get("payments")).size());assertEquals(1,((List<Map<String,Object>>)paid.get("pending")).stream().filter(i->((Number)i.get("year")).intValue()==2026&&((Number)i.get("month")).intValue()==10).count());
   assertFalse(((List<Map<String,Object>>)paid.get("pending")).stream().anyMatch(i->((Number)i.get("year")).intValue()==2026&&((Number)i.get("month")).intValue()==10&&"ELECTRICITY".equals(i.get("utilityType"))));
   clock.when(StayService::today).thenReturn(date("2026-11-01"));var later=finance.utilities("A",2026,11);assertFalse(((List<Map<String,Object>>)later.get("pending")).stream().anyMatch(i->((Number)i.get("year")).intValue()==2026&&((Number)i.get("month")).intValue()==10&&"ELECTRICITY".equals(i.get("utilityType"))));
  }
 }
 @Test void localDateRulesCoverLeapFebruaryAndYearRollover(){assertFalse(BillingCalendar.due(2028,2,date("2028-02-28")));assertTrue(BillingCalendar.due(2028,2,date("2028-02-29")));assertTrue(BillingCalendar.due(2026,12,date("2027-01-01")));assertFalse(BillingCalendar.due(2026,13,date("2027-01-01")));}
}
