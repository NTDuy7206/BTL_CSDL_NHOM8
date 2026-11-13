package com.ntd.csdl;

import com.ntd.csdl.service.LandlordService;
import com.ntd.csdl.service.StayService;
import com.ntd.csdl.service.VipRentalService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VipRentalTests {
    @Test
    void depositUsesAgreedPeriodAndCanOnlyBeUsedOnceByThePayingTenant() throws Exception {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;NON_KEYWORDS=USER;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE user(id BIGINT PRIMARY KEY)");
        jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,room_type VARCHAR(50),base_price DECIMAL(18,2))");
        jdbc.execute("CREATE TABLE contract_requests(request_id BIGINT PRIMARY KEY)");
        String schema = Files.readString(Path.of("src/main/resources/schema.sql"));
        int table = schema.indexOf("CREATE TABLE IF NOT EXISTS vip_rental_deposits");
        jdbc.execute(schema.substring(table, schema.indexOf(';', table))
                .replace("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci", ""));
        jdbc.update("INSERT INTO user VALUES (1),(2)");
        jdbc.update("INSERT INTO rooms VALUES ('V1','Phòng VIP',5000000),('V2','Studio',3000000)");
        jdbc.update("INSERT INTO contract_requests VALUES (10),(11)");
        var landlords = mock(LandlordService.class);
        when(landlords.accountId("alice")).thenReturn(1L);
        var vip = new VipRentalService(jdbc, landlords);
        var month = YearMonth.from(StayService.today()).plusMonths(1);
        var start = month.atEndOfMonth();
        var end = start.plusDays(2);
        assertThrows(ResponseStatusException.class, () -> vip.quote("V2", start, end));
        var quote = vip.quote("V1", start, end);
        assertEquals(new BigDecimal("10000000.00"), quote.totalRent());
        assertEquals(new BigDecimal("5000000.00"), quote.depositAmount());
        assertThrows(ResponseStatusException.class, () -> vip.simulate("alice", "V1", start, end, BigDecimal.ONE));
        var token = vip.simulate("alice", "V1", start, end, quote.depositAmount());
        assertEquals(token, vip.simulate("alice", "V1", start, end, quote.depositAmount()));
        assertThrows(ResponseStatusException.class, () -> vip.attach(null, 1, "V1", start, end, 10, quote.monthlyRent()));
        assertThrows(ResponseStatusException.class, () -> vip.attach(token, 2, "V1", start, end, 10, quote.monthlyRent()));
        assertThrows(ResponseStatusException.class, () -> vip.attach(token, 1, "V1", start, end.minusDays(1), 10, quote.monthlyRent()));
        vip.attach(token, 1, "V1", start, end, 10, quote.monthlyRent());
        jdbc.update("UPDATE rooms SET base_price=6000000 WHERE room_id='V1'");
        assertEquals(quote, vip.forRequest(10));
        assertThrows(ResponseStatusException.class, () -> vip.attach(token, 1, "V1", start, end, 11, quote.monthlyRent()));
    }
}
