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
