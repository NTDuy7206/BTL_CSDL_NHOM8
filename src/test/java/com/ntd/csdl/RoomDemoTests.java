package com.ntd.csdl;

import com.ntd.csdl.service.RoomReadinessMigration;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoomDemoTests {
    @Test
    void changesTheOldDefaultOnExistingRoomsByTypeAndPreservesAgreedPrices() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE demo_seed_history(seed_key VARCHAR(100) PRIMARY KEY,completed BOOLEAN)");
        jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,room_type VARCHAR(50),base_price INT,area INT)");
        jdbc.execute("CREATE TABLE room_landlords(room_id VARCHAR(20) PRIMARY KEY)");
        jdbc.execute("CREATE TABLE contract_requests(room_id VARCHAR(20),status VARCHAR(20))");
        jdbc.execute("CREATE TABLE contract_stays(contract_id VARCHAR(20),monthly_rent INT)");
        jdbc.update("""
                INSERT INTO rooms VALUES ('P1','Phòng tiêu chuẩn',2000000,25),('P2','Căn hộ 1 phòng ngủ',2000000,35),
                    ('P3','Phòng VIP',2000000,40),('P4','Studio',2750000,25),('P5','VIP',2000000,40),
                    ('P6','Studio',2000000,0),('P7','Studio',2000000,25),('P8','Chưa cấu hình',2000000,25)
                """);
        jdbc.update("INSERT INTO room_landlords VALUES ('P1'),('P2'),('P3'),('P4'),('P5'),('P6'),('P8')");
        jdbc.update("INSERT INTO contract_requests VALUES ('P5','PENDING')");
        jdbc.update("INSERT INTO contract_stays VALUES ('HD1',2000000)");
        var migration = new RoomReadinessMigration(jdbc, null);

        assertEquals(3, migration.applyListedRates());
        assertEquals(java.util.List.of(2500000,3600000,5500000,2750000,2000000,2000000,2000000,2000000),
                jdbc.queryForList("SELECT base_price FROM rooms ORDER BY room_id", Integer.class));
        assertEquals(2_000_000, jdbc.queryForObject("SELECT monthly_rent FROM contract_stays", Integer.class));
        jdbc.update("UPDATE rooms SET base_price=2000000 WHERE room_id='P1'");
        assertEquals(0, migration.applyListedRates());
        assertEquals(2_000_000, jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='P1'", Integer.class));
    }

    @Test
    void eachRoomTypeHasAReadableIllustration() throws Exception {
        for (String type : new String[]{"studio", "apartment", "vip"}) {
            var source = Path.of("frontend/public/room-images", type + ".jpg");
            var built = Path.of("src/main/resources/static/room-images", type + ".jpg");
            assertNotNull(ImageIO.read(source.toFile()));
            assertEquals(-1, Files.mismatch(source, built));
        }
    }

    @Test
    void demoPricingLeavesEditedAndContractedRoomsAlone() {
        var jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE demo_seed_history(seed_key VARCHAR(100) PRIMARY KEY,completed BOOLEAN)");
        jdbc.execute("CREATE TABLE rooms(room_id VARCHAR(20) PRIMARY KEY,room_type VARCHAR(50),base_price INT,status VARCHAR(30))");
        jdbc.execute("CREATE TABLE contracts(room_id VARCHAR(20))");
        jdbc.execute("CREATE TABLE contract_requests(room_id VARCHAR(20))");
        jdbc.execute("CREATE TABLE vip_rental_deposits(room_id VARCHAR(20))");
        jdbc.update("INSERT INTO rooms VALUES ('DEMO_P11','Studio',3100000,'AVAILABLE'),('DEMO_P12','Căn hộ 1 phòng ngủ',3500000,'AVAILABLE'),('DEMO_P13','Studio',3300000,'AVAILABLE'),('REAL','Studio',3300000,'AVAILABLE')");
        jdbc.update("INSERT INTO contracts VALUES ('DEMO_P11')");
        var migration = new RoomReadinessMigration(jdbc, null);
        migration.applyDemoRates();
        assertEquals(3_100_000, jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='DEMO_P11'", Integer.class));
        assertEquals(3_500_000, jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='DEMO_P12'", Integer.class));
        assertEquals("VIP", jdbc.queryForObject("SELECT room_type FROM rooms WHERE room_id='DEMO_P13'", String.class));
        assertEquals(5_500_000, jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='DEMO_P13'", Integer.class));
        assertEquals(3_300_000, jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='REAL'", Integer.class));
        jdbc.update("DELETE FROM contracts");
        migration.applyDemoRates();
        assertEquals(3_100_000, jdbc.queryForObject("SELECT base_price FROM rooms WHERE room_id='DEMO_P11'", Integer.class));
    }
}
