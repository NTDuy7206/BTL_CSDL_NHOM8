package com.ntd.csdl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/** One-time, requested completion of the existing rental inventory. */
@Service @RequiredArgsConstructor
public class RoomReadinessMigration {
    private final JdbcTemplate jdbc;
    private final LandlordService landlords;
    private static final String KEY = "room-readiness-20261030-v1";
    private static final int OLD_DEFAULT_RATE = 2_000_000;

    @Transactional
    public int applyListedRates() {
        String seed = "listed-room-default-rates-v1";
        jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES (?,false) ON DUPLICATE KEY UPDATE seed_key=seed_key", seed);
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key=? FOR UPDATE", Boolean.class, seed))) return 0;

        int changed = 0;
        for (var room : jdbc.queryForList("""
                SELECT r.room_id,r.room_type FROM rooms r JOIN room_landlords l ON l.room_id=r.room_id
                WHERE r.area>0 AND r.base_price=? AND r.room_type IS NOT NULL
                ORDER BY r.room_id
                """, OLD_DEFAULT_RATE)) {
            String id = (String)room.get("room_id");
            String type = (String)room.get("room_type");
            int newRate = listedRate(type);
            if (newRate == 0) continue;

            changed += jdbc.update("""
                    UPDATE rooms SET base_price=?
                    WHERE room_id=? AND area>0 AND room_type=? AND base_price=?
                      AND NOT EXISTS (SELECT 1 FROM contract_requests WHERE room_id=? AND status='PENDING')
                    """, newRate, id, type, OLD_DEFAULT_RATE, id);
        }
        jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key=?", seed);
        return changed;
    }

    private static int listedRate(String type) {
        if (VipRentalService.isVip(type)) return 5_500_000;
        String normalized = Normalizer.normalize(type.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').trim().replaceAll("\\s+", " ");
        return switch (normalized) {
            case "studio", "phong studio", "phong tieu chuan", "tieu chuan", "standard", "phong thuong", "thuong" -> 2_500_000;
            case "can ho", "can ho 1 phong ngu", "1 phong ngu", "1pn", "1br" -> 3_600_000;
            default -> 0;
        };
    }

    @Transactional
    public void applyDemoRates() {
        String seed = "demo-room-types-and-rates-v1";
        jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES (?,false) ON DUPLICATE KEY UPDATE seed_key=seed_key", seed);
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key=? FOR UPDATE", Boolean.class, seed))) return;
        updateDemoRate("DEMO_P11", "Studio", 3_100_000, "Studio", 2_500_000);
        updateDemoRate("DEMO_P12", "Căn hộ 1 phòng ngủ", 3_200_000, "Căn hộ 1 phòng ngủ", 3_600_000);
        updateDemoRate("DEMO_P13", "Studio", 3_300_000, "VIP", 5_500_000);
        jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key=?", seed);
    }

    private void updateDemoRate(String id, String oldType, int oldRate, String newType, int newRate) {
        jdbc.update("""
                UPDATE rooms SET room_type=?,base_price=?
                WHERE room_id=? AND room_type=? AND base_price=? AND status='AVAILABLE'
                  AND NOT EXISTS (SELECT 1 FROM contracts WHERE room_id=?)
                  AND NOT EXISTS (SELECT 1 FROM contract_requests WHERE room_id=?)
                  AND NOT EXISTS (SELECT 1 FROM vip_rental_deposits WHERE room_id=?)
                """, newType, newRate, id, oldType, oldRate, id, id, id);
    }

    @Transactional
    public int apply() {
        jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES (?,false) ON DUPLICATE KEY UPDATE seed_key=seed_key", KEY);
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key=? FOR UPDATE", Boolean.class, KEY))) return 0;
        // Use the same room locks as rental approval, then re-evaluate occupancy.
        jdbc.queryForList("SELECT room_id FROM rooms ORDER BY room_id FOR UPDATE");
        int changed = 0;
        for (var room : landlords.catalog()) {
            if (Boolean.TRUE.equals(room.get("occupied")) || !Boolean.TRUE.equals(room.get("occupancyKnown"))) continue;
            String status = room.get("status") == null ? "" : room.get("status").toString();
            String normalized = Normalizer.normalize(status.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").replace('đ', 'd').trim();
            if (!RentalWorkflowService.available(status) && !Set.of("", "unconfigured", "draft", "unlisted", "chua cau hinh", "chua mo cho thue").contains(normalized)) continue;
            // Keep rooms with future or incomplete contracts out of this bulk change too.
            var blocked = jdbc.queryForList("SELECT c.contract_id FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id WHERE c.room_id=? AND s.check_out IS NULL AND (c.end_date IS NULL OR c.end_date>=?)", room.get("roomId"), StayService.today());
            if (!blocked.isEmpty()) continue;
            changed += jdbc.update("UPDATE rooms SET area=CASE WHEN area IS NULL OR area<=0 THEN 25 ELSE area END, base_price=CASE WHEN base_price IS NULL OR base_price<=0 THEN 2000000 ELSE base_price END, room_type=CASE WHEN room_type IS NULL OR TRIM(room_type)='' OR room_type='Chưa cấu hình' THEN 'Phòng tiêu chuẩn' ELSE room_type END, floor=COALESCE(floor,1), status='AVAILABLE' WHERE room_id=?", room.get("roomId"));
        }
        jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key=?", KEY);
        return changed;
    }
}
