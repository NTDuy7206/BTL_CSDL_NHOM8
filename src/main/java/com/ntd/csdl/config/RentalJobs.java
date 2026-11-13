package com.ntd.csdl.config;

import com.ntd.csdl.service.BillingCalendar;
import com.ntd.csdl.service.MeterSimulationService;
import com.ntd.csdl.service.MonthlyBillingService;
import com.ntd.csdl.service.StayService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "app.rental-jobs.enabled", havingValue = "true", matchIfMissing = true)
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RentalJobs {
    private final StayService stays;
    private final MonthlyBillingService billing;
    private final JdbcTemplate jdbc;
    private final MeterSimulationService meters;

    @Scheduled(initialDelay = 30000, fixedDelay = 60000)
    public void tick() {
        LocalDate today = StayService.today();
        try {
            stays.refreshAll();
            YearMonth current = YearMonth.from(today);
            YearMonth firstBillingMonth = firstMonth(1, current);
            YearMonth firstSimulationMonth = firstMonth(2, current);
            var roomIds = meters.rooms();
            var contracts = jdbc.queryForList("SELECT contract_id FROM contracts", String.class);
            for (YearMonth month = firstBillingMonth; !month.isAfter(current); month = month.plusMonths(1)) {
                if (!month.isBefore(firstSimulationMonth)) {
                    LocalDate cutoff = month.equals(current) ? today : month.atEndOfMonth();
                    for (String room : roomIds) meters.update(room, cutoff);
                }
                for (String id : contracts) {
                    try {
                        billing.billDue(id, month, today);
                    } catch (Exception e) {
                        log("Chưa tính được hóa đơn " + id + " kỳ " + month, e);
                    }
                }
            }
        } catch (Exception e) {
            log("Tác vụ thuê phòng sẽ thử lại", e);
        }
    }

    private YearMonth firstMonth(int id, YearMonth current) {
        jdbc.update("INSERT INTO billing_start(id,start_month) VALUES (?,?) ON DUPLICATE KEY UPDATE id=billing_start.id",
                id,current.atDay(1));
        return YearMonth.from(jdbc.queryForObject("SELECT start_month FROM billing_start WHERE id=?",LocalDate.class,id));
    }

    private void log(String message, Exception e) {
        System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, message + ": " + e.getClass().getSimpleName());
    }

    public static YearMonth dueMonth(LocalDateTime now) {
        return BillingCalendar.latestDue(now.toLocalDate());
    }
}
