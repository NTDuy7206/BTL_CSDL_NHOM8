package com.ntd.csdl.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public final class InvoiceFines {
    private InvoiceFines() {}

    static final String MONTHLY_ONLY = """
            NOT EXISTS (SELECT 1 FROM standalone_fine_invoices f WHERE f.invoice_id=invoices.invoice_id)
            """;

    public record Charge(String violationId, String name, LocalDate date, BigDecimal amount) {}

    static boolean standalone(JdbcTemplate jdbc, String invoiceId) {
        return !jdbc.queryForList("SELECT invoice_id FROM standalone_fine_invoices WHERE invoice_id=?", invoiceId).isEmpty();
    }

    static boolean linked(JdbcTemplate jdbc, String violationId) {
        return !jdbc.queryForList("SELECT invoice_id FROM invoice_violation_charges WHERE violation_id=?", violationId).isEmpty();
    }

    static void issue(JdbcTemplate jdbc, com.ntd.csdl.entity.Violation violation, boolean existingViolation) {
        BigDecimal amount = violation.getActualFine();
        if (amount == null || amount.signum() == 0) return;
        if (amount.signum() < 0 || violation.getViolationDate() == null || violation.getViolationDate().isAfter(StayService.today())) {
            throw new IllegalArgumentException("Kiểm tra số tiền phạt và ngày vi phạm");
        }

        String id = violation.getViolationId();
        var candidates = jdbc.queryForList("""
                SELECT (%s) FROM violations v WHERE v.violation_id=?
                """.formatted(RentalRecordAccess.VIOLATION_CONTRACT), String.class, id);
        if (candidates.isEmpty() || candidates.getFirst() == null) {
            throw new IllegalArgumentException("Cần xác định đúng một hợp đồng tại ngày vi phạm để lập hóa đơn phạt");
        }
        String contractId = candidates.getFirst();
        jdbc.queryForList("SELECT contract_id FROM contracts WHERE contract_id=? FOR UPDATE", contractId);
        if (linked(jdbc,id)) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,"Vi phạm đã được ghi vào hóa đơn");
        var date = violation.getViolationDate();
        if (existingViolation && !jdbc.queryForList("""
                SELECT invoice_id FROM invoices WHERE contract_id=? AND year=? AND month=?
                  AND COALESCE(fine_amount,0)>0 AND %s
                  AND NOT EXISTS (SELECT 1 FROM invoice_violation_charges c WHERE c.invoice_id=invoices.invoice_id)
                """.formatted(MONTHLY_ONLY), contractId, date.getYear(), date.getMonthValue()).isEmpty())
            throw new IllegalArgumentException("Hóa đơn tháng đã có tiền phạt chưa tách chi tiết. Cần đối soát trước khi lập hóa đơn phạt riêng");

        String invoiceId = RentalWorkflowService.id("VP");
        jdbc.update("""
                INSERT INTO invoices(invoice_id,month,year,rent_amount,electricity_amount,water_amount,fine_amount,total_amount,contract_id)
                VALUES (?,?,?,0,0,0,?,?,?)
                """, invoiceId, date.getMonthValue(), date.getYear(), amount, amount, contractId);
        jdbc.update("INSERT INTO standalone_fine_invoices(invoice_id) VALUES (?)",invoiceId);
        save(jdbc,invoiceId,List.of(new Charge(id,violation.getViolationName(),date,amount)));
        jdbc.update("""
                INSERT INTO invoice_notifications(invoice_id,user_id,message,seen)
                SELECT ?,t.user_id,?,FALSE FROM tenant_accounts t JOIN contracts c ON c.tenant_id=t.tenant_id
                WHERE c.contract_id=?
                """,invoiceId,"Hóa đơn tiền phạt mới: "+invoiceId,contractId);
    }

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
