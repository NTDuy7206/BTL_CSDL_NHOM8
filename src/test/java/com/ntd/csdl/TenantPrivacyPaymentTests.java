package com.ntd.csdl;
import com.ntd.csdl.entity.*;
import com.ntd.csdl.dto.*;
import com.ntd.csdl.repo.*;
import com.ntd.csdl.service.*;
import com.ntd.csdl.controller.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
class TenantPrivacyPaymentTests {
 StayService stays;RentalWorkflowService rentals;Contract c1,c2;Invoice invoice;InvoiceRepository invoices;UserRepository users;PaymentReportRepository reports;LandlordService landlords;PaymentReportService payments;
 org.springframework.security.core.Authentication auth(String username){return new UsernamePasswordAuthenticationToken(username,null,List.of(new SimpleGrantedAuthority("ROLE_USER")));}
 @BeforeEach void init(){
  stays=mock(StayService.class);when(stays.enrich(any(Contract.class))).thenAnswer(i->i.getArgument(0));
  var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1","sa",""));
  jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY,username VARCHAR(100) UNIQUE)");jdbc.execute("CREATE TABLE tenant_accounts(user_id BIGINT PRIMARY KEY,tenant_id VARCHAR(20) UNIQUE)");jdbc.update("INSERT INTO user VALUES(1,'user1'),(2,'user2'),(3,'user3')");jdbc.update("INSERT INTO tenant_accounts VALUES(1,'T1'),(2,'T2')");
  var contracts=mock(ContractRepository.class);users=mock(UserRepository.class);invoices=mock(InvoiceRepository.class);reports=mock(PaymentReportRepository.class);landlords=mock(LandlordService.class);
  rentals=new RentalWorkflowService(jdbc,mock(TenantRepository.class),contracts,mock(RoomRepository.class),users);
  Tenant t1=new Tenant();t1.setTenantId("T1");Tenant t2=new Tenant();t2.setTenantId("T2");
  c1=Contract.builder().contractId("C1").tenant(t1).room(Room.builder().roomId("R1").build()).startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusMonths(6)).build();
  c2=Contract.builder().contractId("C2").tenant(t2).room(Room.builder().roomId("R2").build()).startDate(LocalDate.now().minusDays(1)).endDate(LocalDate.now().plusMonths(6)).build();
  when(contracts.findByTenantTenantId("T1")).thenReturn(List.of(c1));when(contracts.findByTenantTenantId("T2")).thenReturn(List.of(c2));
  invoice=Invoice.builder().invoiceId("I1").contract(c1).totalAmount(new BigDecimal("3000000")).build();when(invoices.lockById("I1")).thenReturn(Optional.of(invoice));
  for(int i=1;i<=3;i++){User u=new User();u.setId((long)i);u.setUsername("user"+i);u.setRole(UserEnum.USER);when(users.findByUsername(u.getUsername())).thenReturn(Optional.of(u));}
  when(reports.save(any(PaymentReport.class))).thenAnswer(i->{PaymentReport r=i.getArgument(0);r.setPaymentReportId(1L);return r;});
  invoice.setYear(StayService.today().minusMonths(1).getYear());invoice.setMonth(StayService.today().minusMonths(1).getMonthValue());
  payments=new PaymentReportService(reports,invoices,users,rentals,landlords,mock(FinanceService.class),mock(PaymentRoutingService.class));
 }
 @Test void eachAccountOnlyGetsItsOwnContract(){assertEquals("C1",rentals.mine("user1").getFirst().getContractId());assertEquals("C2",rentals.mine("user2").getFirst().getContractId());assertTrue(rentals.mine("user3").isEmpty());assertThrows(ResponseStatusException.class,()->rentals.requireOwner("user2",c1));}
 @Test void contractEndpointRejectsAnotherAccountsId(){var service=mock(ContractService.class);when(service.getById("C1")).thenReturn(c1);var ctl=new ContractController(service,stays,landlords,rentals);assertEquals("C1",ctl.getById("C1",auth("user1")).getBody().getContractId());assertEquals(403,assertThrows(ResponseStatusException.class,()->ctl.getById("C1",auth("user2"))).getStatusCode().value());}
 @Test void invoiceEndpointRejectsAnotherAccountsId(){var service=mock(InvoiceService.class);when(service.getById("I1")).thenReturn(invoice);var ctl=new InvoiceController(service,landlords,mock(ContractRepository.class),rentals,invoices);assertEquals(403,assertThrows(ResponseStatusException.class,()->ctl.getById("I1",auth("user2"))).getStatusCode().value());}
 @Test void landlordCannotCreateRentalOrContractDirectly(){var ctl=new RentalWorkflowController(rentals,landlords);assertEquals(403,assertThrows(ResponseStatusException.class,()->ctl.create(null,auth("owner"))).getStatusCode().value());var contracts=new ContractController(mock(ContractService.class),stays,landlords,rentals);assertEquals(403,assertThrows(ResponseStatusException.class,()->contracts.create(null)).getStatusCode().value());}
 PaymentReportCreateDTO dto(String method){var dto=new PaymentReportCreateDTO();dto.setInvoiceId("I1");dto.setPaymentMethod(method);dto.setTransactionReference("Reference 01");return dto;}
 @Test void bothMethodsPersistWithPendingStatusAndCorrectPayer(){for(String method:List.of("BANK_TRANSFER","CASH")){var r=payments.create("user1",dto(method));assertEquals(method,r.getPaymentMethod());assertEquals("PENDING",r.getStatus());assertEquals("user1",r.getUsername());assertEquals(new BigDecimal("3000000"),r.getAmount());}}
 @Test void anotherTenantCannotPayThisInvoice(){assertEquals(403,assertThrows(ResponseStatusException.class,()->payments.create("user2",dto("CASH"))).getStatusCode().value());verify(reports,never()).save(any());}
 @Test void unknownOrMissingPaymentMethodIsRejected(){for(String m:Arrays.asList(null,"","CARD"))assertEquals(400,assertThrows(ResponseStatusException.class,()->payments.create("user1",dto(m))).getStatusCode().value());verify(reports,never()).save(any());}
 @Test void duplicatePendingPaymentIsRejected(){when(reports.existsByPayerUsernameAndInvoiceInvoiceIdAndStatusIn(anyString(),anyString(),anyList())).thenReturn(true);assertEquals(409,assertThrows(ResponseStatusException.class,()->payments.create("user1",dto("BANK_TRANSFER"))).getStatusCode().value());verify(reports,never()).save(any());}
 @Test void pendingPaymentsAppearOnlyForTheOwningLandlord(){
  PaymentReport first=new PaymentReport();first.setPaymentReportId(11L);first.setInvoice(invoice);first.setPayer(users.findByUsername("user1").orElseThrow());first.setAmount(new BigDecimal("3000000"));
  PaymentReport second=new PaymentReport();second.setPaymentReportId(12L);second.setInvoice(Invoice.builder().invoiceId("I2").contract(c2).build());second.setPayer(users.findByUsername("user2").orElseThrow());second.setAmount(new BigDecimal("2000000"));
  when(reports.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first,second));when(landlords.owns("ownerA","R1")).thenReturn(true);when(landlords.owns("ownerB","R2")).thenReturn(true);
  assertEquals(List.of(11L),payments.getForAccount("ownerA",false).stream().map(PaymentReportResponseDTO::getPaymentReportId).toList());assertEquals(List.of(12L),payments.getForAccount("ownerB",false).stream().map(PaymentReportResponseDTO::getPaymentReportId).toList());
  when(reports.lockById(11L)).thenReturn(Optional.of(first));doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Wrong owner")).when(landlords).requireOwner("ownerB","R1");var update=new PaymentReportStatusDTO();update.setStatus("PAID");assertThrows(ResponseStatusException.class,()->payments.updateStatus(11L,update,"ownerB"));assertEquals(PaymentReportStatus.PENDING,first.getStatus());
 }
 @Test void tenantCannotSubmitOrSeePayableInvoiceBeforeServerMonthEnd(){
  invoice.setYear(2026);invoice.setMonth(10);when(invoices.findByContractTenantTenantId("T1")).thenReturn(List.of(invoice));
  try(var clock=mockStatic(StayService.class)){clock.when(StayService::today).thenReturn(LocalDate.of(2026,10,30));assertTrue(payments.payableInvoices("user1").isEmpty());assertThrows(ResponseStatusException.class,()->payments.create("user1",dto("CASH")));clock.when(StayService::today).thenReturn(LocalDate.of(2026,10,31));assertEquals(1,payments.payableInvoices("user1").size());assertEquals("PENDING",payments.create("user1",dto("CASH")).getStatus());when(reports.existsByPayerUsernameAndInvoiceInvoiceIdAndStatusIn(anyString(),anyString(),anyList())).thenReturn(true);assertTrue(payments.payableInvoices("user1").isEmpty());}
 }
}
