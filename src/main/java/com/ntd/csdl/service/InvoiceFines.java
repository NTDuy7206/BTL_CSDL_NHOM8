package com.ntd.csdl.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public final class InvoiceFines {
    private InvoiceFines() {}

    public record Charge(String violationId, String name, LocalDate date, BigDecimal amount) {}

    public static List<Charge> charges(JdbcTemplate jdbc, String invoiceId) {
        return jdbc.query("""
                SELECT violation_id, violation_name, violation_date, amount
                FROM invoice_violation_charges WHERE invoice_id=? ORDER BY violation_date, violation_id
                """, InvoiceFines::charge, invoiceId);
    }

    static List<Charge> pending(JdbcTemplate jdbc, String contractId, String roomId, YearMonth month) {
        return jdbc.query("""
                SELECT v.violation_id,v.violation_name,v.violation_date,v.actual_fine
                FROM violations v
                WHERE v.room_id=? AND v.violation_date>=? AND v.violation_date<? AND v.actual_fine>0
                  AND NOT EXISTS (SELECT 1 FROM invoice_violation_charges f WHERE f.violation_id=v.violation_id)
                  AND ? = (%s)
                ORDER BY v.violation_date,v.violation_id
                """.formatted(RentalRecordAccess.VIOLATION_CONTRACT), InvoiceFines::charge, roomId,
                month.atDay(1), month.plusMonths(1).atDay(1), contractId);
    }

    static BigDecimal total(List<Charge> charges) {
        return charges.stream().map(Charge::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static void save(JdbcTemplate jdbc, String invoiceId, List<Charge> charges) {
        for (var charge : charges) {
            jdbc.update("""
                    INSERT INTO invoice_violation_charges(violation_id,invoice_id,violation_name,violation_date,amount)
                    VALUES (?,?,?,?,?)
                    """, charge.violationId(), invoiceId, charge.name(), charge.date(), charge.amount());
        }
    }

    static boolean append(JdbcTemplate jdbc, String invoiceId, BigDecimal existingFine, List<Charge> charges) {
        if (charges.isEmpty()) return false;
        // An older manual total may already include the same fines without itemized entries.
        if (existingFine != null && existingFine.signum()!=0 && charges(jdbc,invoiceId).isEmpty()) return false;
        if (!jdbc.queryForList("SELECT payment_report_id FROM payment_reports WHERE invoice_id=? AND status IN ('PENDING','PAID')",
                invoiceId).isEmpty()) return false;
        BigDecimal amount = total(charges);
        save(jdbc, invoiceId, charges);
        jdbc.update("""
                UPDATE invoices SET total_amount=COALESCE(rent_amount,0)+COALESCE(electricity_amount,0)+
                    COALESCE(water_amount,0)+COALESCE(fine_amount,0)+?,fine_amount=COALESCE(fine_amount,0)+?
                WHERE invoice_id=?
                """, amount, amount, invoiceId);
        return true;
    }

    private static Charge charge(java.sql.ResultSet row, int index) throws java.sql.SQLException {
        return new Charge(row.getString(1), row.getString(2), row.getObject(3, LocalDate.class), row.getBigDecimal(4));
    }
}
