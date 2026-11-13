package com.ntd.csdl;

import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.entity.Room;
import com.ntd.csdl.entity.Violation;
import com.ntd.csdl.service.LandlordService;
import com.ntd.csdl.service.RentalRecordAccess;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalRecordAccessTests {
    @Test
    void sharedRoomDoesNotExposeOtherTenantsRecords() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        for (String sql : List.of(
                "CREATE TABLE incident_reporters(incident_id VARCHAR(20),user_id BIGINT)",
                "CREATE TABLE violations(violation_id VARCHAR(20),room_id VARCHAR(20),violation_date DATE)",
                "CREATE TABLE contracts(contract_id VARCHAR(20),room_id VARCHAR(20),tenant_id VARCHAR(20),start_date DATE,end_date DATE)",
                "CREATE TABLE contract_stays(contract_id VARCHAR(20),check_in DATE,check_out DATE)",
                "CREATE TABLE nightly_contract_rates(contract_id VARCHAR(20))",
                "CREATE TABLE tenant_accounts(tenant_id VARCHAR(20),user_id BIGINT)",
                "CREATE TABLE invoice_violation_charges(violation_id VARCHAR(20),invoice_id VARCHAR(20))",
                "CREATE TABLE invoices(invoice_id VARCHAR(20),contract_id VARCHAR(20))",
                "CREATE TABLE room_landlords(room_id VARCHAR(20),user_id BIGINT)")) jdbc.execute(sql);
        jdbc.update("INSERT INTO incident_reporters VALUES ('A',1),('B',2)");
        jdbc.update("INSERT INTO tenant_accounts VALUES ('TA',1),('TB',2)");
        jdbc.update("INSERT INTO room_landlords VALUES ('R1',3),('R2',4)");
        jdbc.update("""
                INSERT INTO contracts VALUES ('CA','R1','TA','2026-11-10','2026-11-12'),
                                             ('CB','R1','TB','2026-11-12','2026-11-14')
                """);
        jdbc.update("INSERT INTO nightly_contract_rates VALUES ('CA'),('CB')");
        jdbc.update("INSERT INTO violations VALUES ('A','R1','2026-11-11'),('B','R1','2026-11-12'),('U','R1','2026-11-15'),('S','R1','2026-11-12')");
        jdbc.update("INSERT INTO invoices VALUES ('I1','CA')");
        jdbc.update("INSERT INTO invoice_violation_charges VALUES ('S','I1')");
        var landlords = mock(LandlordService.class);
        when(landlords.accountId("alice")).thenReturn(1L);
        when(landlords.accountId("bob")).thenReturn(2L);
        when(landlords.accountId("owner")).thenReturn(3L);
        var access = new RentalRecordAccess(jdbc, landlords);
        var alice = auth("alice","USER");
        var bob = auth("bob","USER");
        var room = Room.builder().roomId("R1").build();
        var incidents = List.of(Incident.builder().incidentId("A").room(room).build(),
                Incident.builder().incidentId("B").room(room).build(),
                Incident.builder().incidentId("OLD").room(room).build(),
                Incident.builder().incidentId("OTHER").room(Room.builder().roomId("R2").build()).build());
        assertEquals(List.of("A"), access.incidents(alice,incidents).stream().map(Incident::getIncidentId).toList());
        assertEquals(List.of("B"), access.incidents(bob,incidents).stream().map(Incident::getIncidentId).toList());
        assertEquals(3, access.incidents(auth("owner","ADMIN"),incidents).size());
        assertThrows(ResponseStatusException.class, () ->
                RentalRecordAccess.requireVisible(access.incidents(alice,List.of(incidents.get(1)))));
        var violations = List.of("A","B","U","S").stream()
                .map(id -> Violation.builder().violationId(id).room(room).build()).toList();
        assertEquals(List.of("A","S"), access.violations(alice,violations).stream().map(Violation::getViolationId).toList());
        assertEquals(List.of("B"), access.violations(bob,violations).stream().map(Violation::getViolationId).toList());
    }

    private static UsernamePasswordAuthenticationToken auth(String username,String role) {
        return new UsernamePasswordAuthenticationToken(username,null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));
    }
}
