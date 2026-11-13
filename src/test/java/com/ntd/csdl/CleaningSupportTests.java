package com.ntd.csdl;

import com.ntd.csdl.service.CleaningSupportService;
import com.ntd.csdl.service.LandlordService;
import com.ntd.csdl.service.RentalWorkflowService;
import com.ntd.csdl.service.StayService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CleaningSupportTests {
    @Test
    void onlyCurrentTenantCanRequestCleaningAndHistoryFollowsTheSender() throws Exception {
        var source = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY, username VARCHAR(30))");
        jdbc.execute("CREATE TABLE tenant_accounts(user_id BIGINT, tenant_id VARCHAR(20))");
        jdbc.execute("CREATE TABLE nightly_contract_rates(contract_id VARCHAR(20) PRIMARY KEY,nightly_rate DECIMAL(18,2))");
        jdbc.execute("CREATE TABLE contracts(contract_id VARCHAR(20), tenant_id VARCHAR(20), room_id VARCHAR(20), start_date DATE, end_date DATE)");
        jdbc.execute("CREATE TABLE contract_stays(contract_id VARCHAR(20), check_in DATE, check_out DATE)");
        jdbc.execute("CREATE TABLE cleaning_supports(support_id VARCHAR(20) PRIMARY KEY, support_date DATE, task_content VARCHAR(500), is_completed BOOLEAN, room_id VARCHAR(20), employee_id VARCHAR(20))");
        String schema = Files.readString(Path.of("src/main/resources/schema.sql"));
        jdbc.execute(schema.substring(schema.indexOf("CREATE TABLE IF NOT EXISTS cleaning_support_requesters"),
                schema.indexOf("CREATE TABLE IF NOT EXISTS contract_requests"))
                .replace("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci", ""));
        jdbc.update("INSERT INTO user VALUES (1,'alice'),(2,'bob'),(3,'owner')");
        jdbc.update("INSERT INTO tenant_accounts VALUES (1,'T1'),(2,'T2')");
        var today = StayService.today();
        jdbc.update("INSERT INTO contracts VALUES ('C1','T1','R1',?,?),('C2','T2','R1',?,?)",
                today.minusDays(2), today.plusDays(2), today.plusDays(3), today.plusDays(10));
        var landlords = mock(LandlordService.class);
        when(landlords.owner("R1")).thenReturn(3L);
        when(landlords.accountId("alice")).thenReturn(1L);
        when(landlords.accountId("bob")).thenReturn(2L);
        var rentals = new RentalWorkflowService(jdbc, null, null, null, null);
        var cleaning = new CleaningSupportService(null, null, null, rentals, landlords, jdbc);

        var request = cleaning.requestCleaning("alice", "R1", " Lau sàn ");
        assertFalse(request.isCompleted());
        assertEquals("Lau sàn", request.taskContent());
        assertEquals(request, cleaning.mine("alice").getFirst());
        assertTrue(cleaning.mine("bob").isEmpty());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ResponseStatusException.class,
                () -> cleaning.requestCleaning("bob", "R1", "Lau sàn")).getStatusCode());
        jdbc.update("UPDATE cleaning_supports SET is_completed=true WHERE support_id=?", request.supportId());
        jdbc.update("UPDATE contracts SET end_date=? WHERE contract_id='C1'", today.minusDays(1));
        assertTrue(cleaning.mine("alice").getFirst().isCompleted());
    }
}
