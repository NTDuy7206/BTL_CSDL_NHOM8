package com.ntd.csdl.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.sql.Statement;
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
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
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

    public record Job(String code, String label, int baseSalary) {}

    private static final List<Job> JOBS = List.of(new Job("CLEANER", "Lao công", 5_000_000),
            new Job("GUARD", "Bảo vệ", 6_000_000), new Job("MANAGER", "Quản lý", 10_000_000));

    public List<Job> jobs() { return JOBS; }

    private Job requireJob(String code) {
        return JOBS.stream().filter(job -> job.code().equals(code)).findFirst()
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Hãy chọn công việc trong danh sách"));
    }

    private boolean hasEmployeeProfile(long account) {
        return !jdbc.queryForList("SELECT employee_id FROM employee_accounts WHERE account_id=? FOR UPDATE", account).isEmpty();
    }

    public List<Map<String, Object>> hiringLandlords() {
        return jdbc.queryForList("""
                SELECT u.id,u.username,COALESCE(NULLIF(p.display_name,''),u.username) AS displayName
                FROM `user` u LEFT JOIN user_profiles p ON p.user_id=u.id WHERE u.role=1
                ORDER BY displayName,u.id
                """);
    }

    public Map<String, Object> applicationPage(String username) {
        boolean linked = !jdbc.queryForList("SELECT employee_id FROM employee_accounts WHERE account_id=?",
                landlords.accountId(username)).isEmpty();
        return Map.of("linked", linked, "landlords", linked ? List.of() : hiringLandlords(), "jobs", JOBS,
                "applications", applications(username, true));
    }

    public record ApplicationRow(long id, String status, String message, String submittedOn, String submittedAt,
                                 String username, String displayName, String jobCode) {}

    public List<ApplicationRow> applications(String username, boolean applicant) {
        return jdbc.query("""
                SELECT a.application_id AS id, a.status, a.message, a.submitted_on AS submittedOn, a.submitted_at AS submittedAt,
                       u.username, COALESCE(NULLIF(p.display_name,''),u.username) AS displayName,j.job_code AS jobCode
                FROM staff_applications a JOIN `user` u ON u.id=a.%s LEFT JOIN user_profiles p ON p.user_id=u.id
                LEFT JOIN staff_application_jobs j ON j.application_id=a.application_id
                WHERE a.%s=? ORDER BY CASE WHEN a.status='PENDING' THEN 0 ELSE 1 END, a.application_id DESC
                """.formatted(applicant ? "landlord_id" : "applicant_id", applicant ? "applicant_id" : "landlord_id"),
                (rs, index) -> {
                    var submittedAt = rs.getObject("submittedAt", LocalDateTime.class);
                    return new ApplicationRow(rs.getLong("id"), rs.getString("status"), rs.getString("message"),
                            rs.getObject("submittedOn", LocalDate.class).toString(),
                            submittedAt == null ? null : submittedAt.toString(), rs.getString("username"),
                            rs.getString("displayName"), rs.getString("jobCode"));
                }, landlords.accountId(username));
    }

    @Transactional
    public void apply(String username, long owner, String message, String jobCode) {
        Job job = requireJob(jobCode);
        long account = landlords.accountId(username);
        jdbc.queryForList("SELECT id FROM `user` WHERE id=? FOR UPDATE", account);
        if (hasEmployeeProfile(account)) throw error(HttpStatus.CONFLICT, "Bạn đã liên kết với chủ nhà");
        if (jdbc.queryForList("SELECT id FROM `user` WHERE id=? AND role=1", owner).isEmpty()) {
            throw error(HttpStatus.NOT_FOUND, "Không tìm thấy chủ nhà");
        }
        if (message != null && message.length() > 500) throw error(HttpStatus.BAD_REQUEST, "Lời nhắn tối đa 500 ký tự");
        if (!jdbc.queryForList("SELECT application_id FROM staff_applications WHERE applicant_id=? AND landlord_id=? AND status='PENDING'",
                account, owner).isEmpty()) throw error(HttpStatus.CONFLICT, "Bạn đã gửi đơn cho chủ nhà này");
        var key = new org.springframework.jdbc.support.GeneratedKeyHolder();
        var submittedAt = LocalDateTime.now(VIETNAM).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("INSERT INTO staff_applications(applicant_id,landlord_id,message,submitted_on,submitted_at) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, account);
            statement.setLong(2, owner);
            statement.setString(3, message == null ? null : message.trim());
            statement.setObject(4, submittedAt.toLocalDate());
            statement.setObject(5, submittedAt);
            return statement;
        }, key);
        jdbc.update("INSERT INTO staff_application_jobs(application_id,job_code) VALUES (?,?)", key.getKey().longValue(), job.code());
    }

    @Transactional
    public void decide(String admin, long id, boolean approve, String legacyJobCode) {
        long owner = landlords.accountId(admin);
        var applicants = jdbc.queryForList("SELECT applicant_id FROM staff_applications WHERE application_id=? AND landlord_id=?",
                Long.class, id, owner);
        if (applicants.isEmpty()) throw error(HttpStatus.NOT_FOUND, "Không tìm thấy đơn xin việc của chủ nhà này");
        long account = applicants.getFirst();
        jdbc.queryForList("SELECT id FROM `user` WHERE id=? FOR UPDATE", account);
        int changed = jdbc.update("UPDATE staff_applications SET status=? WHERE application_id=? AND landlord_id=? AND status='PENDING'",
                approve ? "APPROVED" : "REJECTED", id, owner);
        if (changed != 1) throw error(HttpStatus.CONFLICT, "Đơn đã được xử lý");
        if (approve) {
            if (hasEmployeeProfile(account)) throw error(HttpStatus.CONFLICT, "Nhân viên đã liên kết với chủ nhà");
            var codes = jdbc.queryForList("SELECT job_code FROM staff_application_jobs WHERE application_id=?", String.class, id);
            Job job = requireJob(codes.isEmpty() ? legacyJobCode : codes.getFirst());
            if (codes.isEmpty()) jdbc.update("INSERT INTO staff_application_jobs(application_id,job_code) VALUES (?,?)", id, job.code());
            String employeeId = RentalWorkflowService.id("NV");
            String name = jdbc.queryForObject("""
                    SELECT LEFT(COALESCE(NULLIF(TRIM(p.display_name),''),u.username),50)
                    FROM `user` u LEFT JOIN user_profiles p ON p.user_id=u.id WHERE u.id=?
                    """, String.class, account);
            jdbc.update("INSERT INTO employees(employee_id,full_name,position,base_salary,seniority) VALUES (?,?,?,?,0)",
                    employeeId, name, job.label(), job.baseSalary());
            jdbc.update("INSERT INTO employee_accounts(employee_id,landlord_id,account_id,linked_on) VALUES (?,?,?,?)",
                    employeeId, owner, account, StayService.today());
            jdbc.update("UPDATE staff_applications SET status='CLOSED' WHERE applicant_id=? AND status='PENDING'", account);
        }
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
            throw error(HttpStatus.BAD_REQUEST, "Chỉ được giao việc cho nhân viên đã được chủ nhà tiếp nhận");
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

    public Map<String, Object> adminPayroll(String admin) {
        long owner = landlords.accountId(admin);
        return Map.of("history", payroll(owner, "landlord_id"), "employees", jdbc.queryForList("""
                SELECT e.employee_id AS employeeId,e.full_name AS fullName,e.position,e.base_salary AS baseSalary,
                       e.seniority,%s AS multiplier,ROUND(e.base_salary*(%s),2) AS amount
                FROM employee_accounts a JOIN employees e ON e.employee_id=a.employee_id
                WHERE a.landlord_id=? ORDER BY e.full_name,e.employee_id
                """.formatted(MULTIPLIER, MULTIPLIER), owner));
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
