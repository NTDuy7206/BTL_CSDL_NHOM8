package com.ntd.csdl.service;

import com.ntd.csdl.entity.Incident;
import com.ntd.csdl.entity.Violation;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RentalRecordAccess {
    private final JdbcTemplate jdbc;
    private final LandlordService landlords;

    static final String VIOLATION_CONTRACT = """
            SELECT CASE WHEN COUNT(*)=1 THEN MIN(c.contract_id) END
            FROM contracts c LEFT JOIN contract_stays s ON s.contract_id=c.contract_id
            WHERE c.room_id=v.room_id AND COALESCE(s.check_in,c.start_date)<=v.violation_date
              AND ((s.check_out IS NOT NULL AND s.check_out>v.violation_date)
                  OR (s.check_out IS NULL AND (c.end_date>v.violation_date
                      OR (c.end_date=v.violation_date AND NOT EXISTS
                          (SELECT 1 FROM nightly_contract_rates n WHERE n.contract_id=c.contract_id)))))
            """;

    public List<Incident> incidents(Authentication auth, List<Incident> rows) {
        return visible(auth, rows, row -> row.getRoom() == null ? null : row.getRoom().getRoomId(),
                Incident::getIncidentId, "SELECT incident_id FROM incident_reporters WHERE user_id=?");
    }

    public List<Violation> violations(Authentication auth, List<Violation> rows) {
        return visible(auth, rows, row -> row.getRoom() == null ? null : row.getRoom().getRoomId(),
                Violation::getViolationId, """
                SELECT v.violation_id FROM violations v
                LEFT JOIN invoice_violation_charges f ON f.violation_id=v.violation_id
                LEFT JOIN invoices i ON i.invoice_id=f.invoice_id
                JOIN contracts charged ON charged.contract_id=CASE WHEN f.violation_id IS NULL
                    THEN (%s) ELSE i.contract_id END
                JOIN tenant_accounts t ON t.tenant_id=charged.tenant_id
                WHERE t.user_id=?
                """.formatted(VIOLATION_CONTRACT));
    }

    private <T> List<T> visible(Authentication auth, List<T> rows, Function<T,String> room,
                                Function<T,String> id, String tenantQuery) {
        if (rows.isEmpty()) return rows;
        long account = landlords.accountId(auth.getName());
        if (RentalWorkflowService.admin(auth)) {
            var owned = new HashSet<>(jdbc.queryForList("SELECT room_id FROM room_landlords WHERE user_id=?", String.class, account));
            return rows.stream().filter(row -> owned.contains(room.apply(row))).toList();
        }
        var allowed = new HashSet<>(jdbc.queryForList(tenantQuery, String.class, account));
        return rows.stream().filter(row -> allowed.contains(id.apply(row))).toList();
    }

    public static <T> T requireVisible(List<T> rows) {
        return rows.stream().findFirst().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy bản ghi"));
    }
}
