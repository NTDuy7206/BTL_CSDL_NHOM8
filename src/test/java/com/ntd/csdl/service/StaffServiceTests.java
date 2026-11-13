package com.ntd.csdl.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;

class StaffServiceTests {
    @Test
    void approvalCreatesProfileWithTheSelectedSalaryAndClosesOtherApplications() {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(ds);
        var tx = new org.springframework.transaction.support.TransactionTemplate(new org.springframework.jdbc.datasource.DataSourceTransactionManager(ds));
        jdbc.execute("CREATE TABLE `user`(id BIGINT PRIMARY KEY, role INT, username VARCHAR(30))");
        jdbc.execute("CREATE TABLE user_profiles(user_id BIGINT, display_name VARCHAR(30))");
        jdbc.execute("CREATE TABLE employees(employee_id VARCHAR(20) PRIMARY KEY, full_name VARCHAR(50), position VARCHAR(50), base_salary DECIMAL(18,2), seniority INT)");
        jdbc.execute("CREATE TABLE employee_accounts(employee_id VARCHAR(20) PRIMARY KEY, landlord_id BIGINT, account_id BIGINT UNIQUE, linked_on DATE)");
        jdbc.execute("CREATE TABLE staff_applications(application_id BIGINT AUTO_INCREMENT PRIMARY KEY, applicant_id BIGINT, landlord_id BIGINT, status VARCHAR(12) DEFAULT 'PENDING', message VARCHAR(500), submitted_on DATE, submitted_at DATETIME(6))");
        jdbc.execute("CREATE TABLE staff_application_jobs(application_id BIGINT PRIMARY KEY, job_code VARCHAR(16))");
        jdbc.update("INSERT INTO `user` VALUES (1,1,'owner'),(2,1,'other'),(3,2,'staff')");
        var landlords = mock(LandlordService.class);
        when(landlords.accountId("owner")).thenReturn(1L);
        when(landlords.accountId("other")).thenReturn(2L);
        when(landlords.accountId("staff")).thenReturn(3L);
        var service = new StaffService(jdbc, landlords);
        tx.executeWithoutResult(s -> service.apply("staff", 1, "Xin việc", "GUARD"));
        assertThrows(ResponseStatusException.class, () -> tx.executeWithoutResult(s -> service.apply("staff", 1, null, "CLEANER")));
        tx.executeWithoutResult(s -> service.apply("staff", 2, null, "MANAGER"));
        assertEquals(2, service.applications("staff", true).size());
        assertEquals(1, service.applications("owner", false).size());
        assertThrows(ResponseStatusException.class, () -> tx.executeWithoutResult(s -> service.decide("other", 1, true, "E2")));
        var application = service.applications("owner", false).getFirst();
        assertEquals("PENDING", application.status());
        assertEquals(application.submittedOn(), service.applications("staff", true).getLast().submittedOn());
        assertEquals(application.submittedAt(), service.applications("staff", true).getLast().submittedAt());
        var vnNow = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        assertTrue(java.time.Duration.between(java.time.LocalDateTime.parse(application.submittedAt()), vnNow).abs().toSeconds() < 60);
        jdbc.update("UPDATE staff_applications SET submitted_at=NULL WHERE application_id=2");
        assertNull(service.applications("other", false).getFirst().submittedAt());
        tx.executeWithoutResult(s -> service.decide("owner", 1, true, "MANAGER"));
        assertEquals(3L, jdbc.queryForObject("SELECT account_id FROM employee_accounts WHERE landlord_id=1", Long.class));
        assertEquals(new BigDecimal("6000000.00"), jdbc.queryForObject("SELECT base_salary FROM employees", BigDecimal.class));
        assertEquals(List.of("APPROVED", "CLOSED"), jdbc.queryForList("SELECT status FROM staff_applications ORDER BY application_id", String.class));
        assertThrows(ResponseStatusException.class, () -> tx.executeWithoutResult(s -> service.apply("staff", 2, null, "MANAGER")));
    }

    @Test
    void employeeCanOnlyFinishWorkAssignedInTheirLandlordsRooms() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE employee_accounts(employee_id VARCHAR(20), landlord_id BIGINT, account_id BIGINT)");
        jdbc.execute("CREATE TABLE room_landlords(room_id VARCHAR(20), user_id BIGINT)");
        jdbc.execute("CREATE TABLE cleaning_supports(support_id VARCHAR(20), room_id VARCHAR(20), employee_id VARCHAR(20), is_completed BOOLEAN)");
        jdbc.execute("CREATE TABLE incidents(incident_id VARCHAR(20), room_id VARCHAR(20), status VARCHAR(20))");
        jdbc.execute("CREATE TABLE incident_resolutions(resolution_id VARCHAR(20), incident_id VARCHAR(20), employee_id VARCHAR(20), is_completed BOOLEAN, result VARCHAR(500), resolution_date DATE)");
        jdbc.update("INSERT INTO employee_accounts VALUES ('E1',1,10),('E2',1,20)");
        jdbc.update("INSERT INTO room_landlords VALUES ('R1',1),('R2',2)");
        jdbc.update("INSERT INTO cleaning_supports VALUES ('C1','R1','E1',FALSE),('C2','R1','E2',FALSE),('C3','R2','E1',FALSE)");
        jdbc.update("INSERT INTO incidents VALUES ('I1','R1','PENDING')");
        jdbc.update("INSERT INTO incident_resolutions VALUES ('S1','I1','E1',FALSE,NULL,NULL),('S2','I1','E2',FALSE,NULL,NULL)");
        var landlords = mock(LandlordService.class);
        when(landlords.accountId("staff1")).thenReturn(10L);
        when(landlords.accountId("staff2")).thenReturn(20L);
        var staff = new StaffService(jdbc, landlords);

        assertThrows(ResponseStatusException.class, () -> staff.finishCleaning("staff1", "C2"));
        assertThrows(ResponseStatusException.class, () -> staff.finishCleaning("staff1", "C3"));
        staff.finishCleaning("staff1", "C1");
        assertThrows(ResponseStatusException.class, () -> staff.finishCleaning("staff1", "C1"));
        assertThrows(ResponseStatusException.class, () -> staff.finishIncident("staff2", "S1", "Đã sửa"));
        staff.finishIncident("staff1", "S1", "Đã sửa bước một");
        assertEquals("PENDING", jdbc.queryForObject("SELECT status FROM incidents", String.class));
        staff.finishIncident("staff2", "S2", "Đã kiểm tra lại");
        assertEquals("COMPLETED", jdbc.queryForObject("SELECT status FROM incidents", String.class));
    }

    @Test
    void payrollUsesStrictThresholdsAndCannotBePaidTwiceOrByAnotherLandlord() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE employees(employee_id VARCHAR(20) PRIMARY KEY, full_name VARCHAR(50), position VARCHAR(50), base_salary DECIMAL(18,2), seniority INT)");
        jdbc.execute("CREATE TABLE employee_accounts(employee_id VARCHAR(20) PRIMARY KEY, landlord_id BIGINT, account_id BIGINT, linked_on DATE)");
        jdbc.execute("CREATE TABLE employee_payroll(employee_id VARCHAR(20), period DATE, base_salary DECIMAL(18,2), seniority INT, multiplier DECIMAL(3,2), paid_on DATE, PRIMARY KEY(employee_id,period))");
        var landlords = mock(LandlordService.class);
        when(landlords.accountId("owner")).thenReturn(1L);
        when(landlords.accountId("other")).thenReturn(2L);
        var staff = new StaffService(jdbc, landlords);
        var month = YearMonth.from(StayService.today()).minusMonths(1);
        var start = month.atDay(1);
        for (int years : List.of(3, 4, 5, 6, 10, 11)) {
            jdbc.update("INSERT INTO employees VALUES (?,?,?,?,?)", "E" + years, "Nhân viên " + years, "Test", 10_000_000, years);
            jdbc.update("INSERT INTO employee_accounts VALUES (?,?,?,?)", "E" + years, 1, 100 + years, start);
        }
        staff.preparePayroll(month.atEndOfMonth().minusDays(1));
        assertTrue(history(staff, "owner").isEmpty());
        staff.preparePayroll(month.atEndOfMonth());
        staff.preparePayroll(month.atEndOfMonth());
        assertEquals(6, history(staff, "owner").size());
        assertTrue(history(staff, "other").isEmpty());
        var rates = Map.of(3,"1.00",4,"1.10",5,"1.10",6,"1.30",10,"1.30",11,"1.50");
        for (var row : history(staff, "owner")) {
            assertEquals(new BigDecimal(rates.get(row.get("seniority"))), row.get("multiplier"));
        }
        assertThrows(ResponseStatusException.class, () -> staff.pay("other", "E11", start));
        assertThrows(ResponseStatusException.class, () -> staff.pay("owner", "E11", LocalDate.now().plusMonths(2).withDayOfMonth(1)));
        staff.pay("owner", "E11", start);
        assertThrows(ResponseStatusException.class, () -> staff.pay("owner", "E11", start));
        jdbc.update("UPDATE employees SET base_salary=20000000 WHERE employee_id='E11'");
        staff.preparePayroll(month.atEndOfMonth());
        assertEquals(new BigDecimal("10000000.00"), jdbc.queryForObject("SELECT base_salary FROM employee_payroll WHERE employee_id='E11'", BigDecimal.class));
    }
    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> history(StaffService staff, String admin) {
        return (List<Map<String,Object>>) staff.adminPayroll(admin).get("history");
    }
}
