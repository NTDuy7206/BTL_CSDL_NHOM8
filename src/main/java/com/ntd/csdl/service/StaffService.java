package com.ntd.csdl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final JdbcTemplate jdbc;
    private final LandlordService landlords;

    public void claimNewProfile(String admin, String employeeId) {
        jdbc.update("INSERT INTO employee_accounts(employee_id,landlord_id) VALUES (?,?)", employeeId, landlords.accountId(admin));
    }

    public List<Map<String, Object>> directory(String username) {
        return jdbc.queryForList("""
                SELECT e.employee_id AS employeeId, e.full_name AS fullName, u.username
                FROM employees e LEFT JOIN employee_accounts a ON a.employee_id=e.employee_id
                LEFT JOIN `user` u ON u.id=a.account_id
                WHERE a.landlord_id=? ORDER BY e.full_name,e.employee_id
                """, landlords.accountId(username));
    }

    @Transactional
    public void link(String admin, String employeeId, String username) {
        long owner = landlords.accountId(admin);
        if (username == null || username.isBlank()) throw error(HttpStatus.BAD_REQUEST, "Nhập tên tài khoản nhân viên");
        if (jdbc.queryForList("SELECT employee_id FROM employees WHERE employee_id=? FOR UPDATE", employeeId).isEmpty()) {
            throw error(HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ nhân viên");
        }
        var accounts = jdbc.queryForList("SELECT id FROM `user` WHERE username=? AND role=2 FOR UPDATE", Long.class, username.trim());
        if (accounts.isEmpty()) throw error(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản vai trò Nhân viên");
        long account = accounts.getFirst();
        var link = jdbc.queryForList("SELECT employee_id,landlord_id,account_id FROM employee_accounts WHERE employee_id=? OR account_id=?",
                employeeId, account);
        if (!link.isEmpty()) {
            var row = link.getFirst();
            if (link.size() == 1 && employeeId.equals(row.get("employee_id")) && Long.valueOf(owner).equals(row.get("landlord_id"))) {
                if (Long.valueOf(account).equals(row.get("account_id"))) return;
                if (row.get("account_id") == null) {
                    jdbc.update("UPDATE employee_accounts SET account_id=?,linked_on=? WHERE employee_id=?",
                            account, StayService.today(), employeeId);
                    return;
                }
            }
            throw error(HttpStatus.CONFLICT, "Tài khoản hoặc hồ sơ đã được liên kết");
        }
        throw error(HttpStatus.FORBIDDEN, "Hãy tạo hồ sơ nhân viên thuộc chủ nhà này trước khi liên kết");
    }

    public void checkProfileAccess(String admin, String employeeId) {
        if (!jdbc.queryForList("SELECT employee_id FROM employee_accounts WHERE employee_id=? AND landlord_id<>?",
                employeeId, landlords.accountId(admin)).isEmpty()) {
            throw error(HttpStatus.FORBIDDEN, "Hồ sơ nhân viên thuộc chủ nhà khác");
        }
    }

    public void checkAssignment(String admin, String employeeId) {
        if (employeeId == null) return;
        if (jdbc.queryForList("SELECT employee_id FROM employee_accounts WHERE employee_id=? AND landlord_id=? AND account_id IS NOT NULL",
                employeeId, landlords.accountId(admin)).isEmpty()) {
            throw error(HttpStatus.BAD_REQUEST, "Hãy liên kết tài khoản nhân viên trong mục Quản lý nhân viên trước khi phân công");
        }
    }

    public List<com.ntd.csdl.entity.Employee> visibleProfiles(String admin, List<com.ntd.csdl.entity.Employee> employees) {
        var hidden = new HashSet<>(jdbc.queryForList("SELECT employee_id FROM employee_accounts WHERE landlord_id<>?", String.class,
                landlords.accountId(admin)));
        return employees.stream().filter(employee -> !hidden.contains(employee.getEmployeeId())).toList();
    }

    public record PublicEmployee(String employeeId, String fullName, String position) {
        public static PublicEmployee of(com.ntd.csdl.entity.Employee employee) {
            return new PublicEmployee(employee.getEmployeeId(), employee.getFullName(), employee.getPosition());
        }
    }

    public Map<String, Object> mine(String username) {
        long account = landlords.accountId(username);
        var identity = jdbc.queryForList("""
                SELECT e.employee_id AS employeeId,e.full_name AS fullName,e.base_salary AS baseSalary,e.seniority,
                       %s AS multiplier,ROUND(e.base_salary*(%s),2) AS estimatedSalary
                FROM employee_accounts a JOIN employees e ON e.employee_id=a.employee_id WHERE a.account_id=?
                """.formatted(MULTIPLIER, MULTIPLIER), account);
        if (identity.isEmpty()) return Map.of("linked", false);
        return Map.of("linked", true, "employee", identity.getFirst(), "payroll", payroll(account, "account_id"),
                "cleaning", jdbc.queryForList("""
                        SELECT c.support_id AS id,c.room_id AS roomId,c.support_date AS workDate,c.task_content AS content,c.is_completed AS completed
                        FROM cleaning_supports c JOIN employee_accounts a ON a.employee_id=c.employee_id
                        JOIN room_landlords r ON r.room_id=c.room_id AND r.user_id=a.landlord_id
                        WHERE a.account_id=? ORDER BY c.support_date DESC,c.support_id
                        """, account),
                "incidents", jdbc.queryForList("""
                        SELECT s.resolution_id AS id,i.room_id AS roomId,s.resolution_date AS workDate,
                               i.incident_content AS content,s.result,s.is_completed AS completed
                        FROM incident_resolutions s JOIN incidents i ON i.incident_id=s.incident_id
                        JOIN employee_accounts a ON a.employee_id=s.employee_id
                        JOIN room_landlords r ON r.room_id=i.room_id AND r.user_id=a.landlord_id
                        WHERE a.account_id=? ORDER BY s.resolution_date DESC,s.resolution_id
                        """, account));
    }

    @Transactional
    public void finishCleaning(String username, String id) {
        int count = jdbc.update("""
                UPDATE cleaning_supports c SET is_completed=TRUE WHERE c.support_id=? AND COALESCE(c.is_completed,FALSE)=FALSE
                AND EXISTS (SELECT 1 FROM employee_accounts a JOIN room_landlords r ON r.user_id=a.landlord_id
                            WHERE a.employee_id=c.employee_id AND r.room_id=c.room_id AND a.account_id=?)
                """, id, landlords.accountId(username));
        if (count != 1) throw error(HttpStatus.CONFLICT, "Công việc không còn chờ xử lý hoặc không được giao cho bạn");
    }

    @Transactional
    public void finishIncident(String username, String id, String result) {
        if (result == null || result.isBlank() || result.length() > 500) {
            throw error(HttpStatus.BAD_REQUEST, "Nhập kết quả xử lý từ 1 đến 500 ký tự");
        }
        var incidents = jdbc.queryForList("""
                SELECT i.incident_id FROM incidents i JOIN incident_resolutions s ON i.incident_id=s.incident_id
                JOIN employee_accounts a ON a.employee_id=s.employee_id
                JOIN room_landlords r ON r.room_id=i.room_id AND r.user_id=a.landlord_id
                WHERE s.resolution_id=? AND a.account_id=? FOR UPDATE
                """, String.class, id, landlords.accountId(username));
        if (incidents.isEmpty()) throw error(HttpStatus.NOT_FOUND, "Không có việc xử lý sự cố được giao này");
        if (jdbc.update("UPDATE incident_resolutions SET is_completed=TRUE,result=?,resolution_date=? WHERE resolution_id=? AND COALESCE(is_completed,FALSE)=FALSE",
                result.trim(), StayService.today(), id) != 1) throw error(HttpStatus.CONFLICT, "Công việc đã hoàn thành");
        String incident = incidents.getFirst();
        jdbc.update("""
                UPDATE incidents SET status='COMPLETED' WHERE incident_id=? AND NOT EXISTS
                    (SELECT 1 FROM incident_resolutions WHERE incident_id=? AND COALESCE(is_completed,FALSE)=FALSE)
                """, incident, incident);
    }

    public List<Map<String, Object>> payroll(String admin) {
        return payroll(landlords.accountId(admin), "landlord_id");
    }

    private List<Map<String, Object>> payroll(long id, String scope) {
        return jdbc.queryForList("""
                SELECT p.employee_id AS employeeId,e.full_name AS fullName,p.period,p.base_salary AS baseSalary,
                       p.seniority,p.multiplier,ROUND(p.base_salary*p.multiplier,2) AS amount,p.paid_on AS paidOn
                FROM employee_payroll p JOIN employee_accounts a ON a.employee_id=p.employee_id
                JOIN employees e ON e.employee_id=p.employee_id WHERE a.%s=? ORDER BY p.period DESC,e.full_name
                """.formatted(scope), id);
    }

    private static final String MULTIPLIER = "CASE WHEN e.seniority IS NULL OR e.seniority<0 THEN NULL "
            + "WHEN e.seniority>10 THEN 1.50 WHEN e.seniority>5 THEN 1.30 WHEN e.seniority>3 THEN 1.10 ELSE 1.00 END";

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    public void preparePayroll() {
        preparePayroll(StayService.today());
    }

    void preparePayroll(LocalDate today) {
        LocalDate first = jdbc.queryForObject("SELECT MIN(linked_on) FROM employee_accounts", LocalDate.class);
        if (first == null) return;
        for (YearMonth month = YearMonth.from(first); !month.atEndOfMonth().isAfter(today); month = month.plusMonths(1)) {
            LocalDate period = month.atDay(1);
            jdbc.update("""
                    INSERT INTO employee_payroll(employee_id,period,base_salary,seniority,multiplier)
                    SELECT e.employee_id,?,e.base_salary,e.seniority,%s
                    FROM employee_accounts a JOIN employees e ON e.employee_id=a.employee_id
                    WHERE a.linked_on<=? AND e.base_salary>0 AND e.seniority>=0
                    AND NOT EXISTS (SELECT 1 FROM employee_payroll p WHERE p.employee_id=e.employee_id AND p.period=?)
                    ON DUPLICATE KEY UPDATE paid_on=employee_payroll.paid_on
                    """.formatted(MULTIPLIER), period, month.atEndOfMonth(), period);
        }
    }

    @Transactional
    public void pay(String admin, String employeeId, LocalDate period) {
        if (period == null || period.getDayOfMonth() != 1 || YearMonth.from(period).atEndOfMonth().isAfter(StayService.today())) {
            throw error(HttpStatus.BAD_REQUEST, "Chỉ được ghi nhận trả lương từ ngày cuối tháng");
        }
        int count = jdbc.update("""
                UPDATE employee_payroll SET paid_on=? WHERE employee_id=? AND period=? AND paid_on IS NULL
                AND employee_id IN (SELECT employee_id FROM employee_accounts WHERE landlord_id=?)
                """, StayService.today(), employeeId, period, landlords.accountId(admin));
        if (count != 1) throw error(HttpStatus.CONFLICT, "Kỳ lương không tồn tại, đã trả hoặc thuộc chủ nhà khác");
    }

    private static ResponseStatusException error(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
