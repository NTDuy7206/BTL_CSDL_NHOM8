package com.ntd.csdl.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeterSimulationService {
    static final int ELECTRICITY_PER_DAY = 3;
    static final int WATER_PER_DAY = 1;
    static final int ELECTRICITY_PRICE = 3500;
    static final int WATER_PRICE = 15000;
    private final JdbcTemplate jdbc;

    public List<String> rooms() {
        return jdbc.queryForList("SELECT room_id FROM rooms", String.class);
    }

    @Transactional
    public void update(String room, LocalDate today) {
        if (jdbc.queryForList("SELECT room_id FROM rooms WHERE room_id=? FOR UPDATE", room).isEmpty()) return;
        YearMonth period = YearMonth.from(today);
        int year = period.getYear(), month = period.getMonthValue();
        var simulated = jdbc.queryForList("""
                SELECT reading_id,simulated_from FROM simulated_meter_periods
                WHERE room_id=? AND reading_year=? AND reading_month=?
                """, room, year, month);
        String id;
        LocalDate from;
        if (simulated.isEmpty()) {
            if (!jdbc.queryForList("SELECT reading_id FROM utility_readings WHERE room_id=? AND year=? AND month=?",
                    room, year, month).isEmpty()) return;
            var previous = jdbc.queryForList("""
                    SELECT electricity_end_index,water_end_index FROM utility_readings
                    WHERE room_id=? AND (year<? OR (year=? AND month<?))
                    ORDER BY year DESC,month DESC,reading_id DESC LIMIT 1
                    """, room, year, year, month);
            int electricity = previous.isEmpty() ? 0 : index(previous.getFirst().get("electricity_end_index"));
            int water = previous.isEmpty() ? 0 : index(previous.getFirst().get("water_end_index"));
            id = RentalWorkflowService.id("CS");
            from = period.atDay(1);
            jdbc.update("""
                    INSERT INTO utility_readings(reading_id,room_id,year,month,electricity_start_index,
                        electricity_end_index,water_start_index,water_end_index,electricity_price,water_price)
                    VALUES (?,?,?,?,?,?,?,?,?,?)
                    """, id, room, year, month, electricity, electricity, water, water, ELECTRICITY_PRICE, WATER_PRICE);
            jdbc.update("INSERT INTO simulated_meter_periods VALUES (?,?,?,?,?)", room, year, month, id, from);
        } else {
            id = (String) simulated.getFirst().get("reading_id");
            from = StayService.date(simulated.getFirst().get("simulated_from"));
        }
        var stays = jdbc.queryForList("""
                SELECT COALESCE(s.check_in,c.start_date) AS arrival,c.end_date,s.check_out,n.nightly_rate
                FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id
                LEFT JOIN nightly_contract_rates n ON n.contract_id=c.contract_id
                WHERE c.room_id=? AND COALESCE(s.check_in,c.start_date)<=?
                  AND COALESCE(s.check_out,c.end_date)>=?
                """, room, today, from);
        int occupied = 0;
        for (LocalDate day = from; !day.isAfter(today); day = day.plusDays(1)) {
            LocalDate current = day;
            if (stays.stream().anyMatch(stay -> {
                LocalDate start = StayService.date(stay.get("arrival"));
                LocalDate checkout = StayService.date(stay.get("check_out"));
                LocalDate end = StayService.date(stay.get("end_date"));
                if (checkout == null && end == null) return false;
                LocalDate departure = checkout != null ? checkout
                        : stay.get("nightly_rate") != null ? end : end.plusDays(1);
                return !current.isBefore(start) && current.isBefore(departure);
            })) occupied++;
        }
        int electricityUsed = occupied * ELECTRICITY_PER_DAY;
        int waterUsed = occupied * WATER_PER_DAY;
        jdbc.update("""
                UPDATE utility_readings SET electricity_end_index=electricity_start_index+?,
                    water_end_index=water_start_index+? WHERE reading_id=?
                    AND (electricity_end_index<>electricity_start_index+? OR water_end_index<>water_start_index+?)
                """, electricityUsed, waterUsed, id, electricityUsed, waterUsed);
    }

    private static int index(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
