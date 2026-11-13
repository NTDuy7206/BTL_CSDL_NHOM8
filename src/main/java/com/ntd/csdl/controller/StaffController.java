package com.ntd.csdl.controller;

import com.ntd.csdl.service.StaffService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staff;

    public record Result(String result) {}
    public record Application(long landlordId, String message, String jobCode) {}
    public record Decision(String status, String jobCode) {}

    @GetMapping("/pending-work")
    public StaffService.PendingWork pendingWork(Authentication auth) {
        return staff.pendingWork(auth.getName(),
                auth.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN")));
    }

    @GetMapping("/me/applications")
    public Map<String, Object> myApplications(Authentication auth) { return staff.applicationPage(auth.getName()); }

    @PostMapping("/me/applications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apply(Authentication auth, @RequestBody Application body) {
        staff.apply(auth.getName(), body.landlordId(), body.message(), body.jobCode());
    }

    @GetMapping("/applications")
    public Map<String, Object> applications(Authentication auth) {
        return Map.of("applications", staff.applications(auth.getName(), false), "jobs", staff.jobs());
    }

    @PutMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decide(Authentication auth, @PathVariable long id, @RequestBody Decision body) {
        if (!"APPROVED".equals(body.status()) && !"REJECTED".equals(body.status())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Chọn duyệt hoặc từ chối đơn");
        }
        staff.decide(auth.getName(), id, "APPROVED".equals(body.status()), body.jobCode());
    }

    @GetMapping("/directory")
    public List<Map<String, Object>> directory(Authentication auth) { return staff.directory(auth.getName()); }

    @GetMapping("/me")
    public Map<String, Object> mine(Authentication auth) { return staff.mine(auth.getName()); }

    @PutMapping("/me/cleaning/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishCleaning(Authentication auth, @PathVariable String id) { staff.finishCleaning(auth.getName(), id); }

    @PutMapping("/me/incidents/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishIncident(Authentication auth, @PathVariable String id, @RequestBody Result body) {
        staff.finishIncident(auth.getName(), id, body.result());
    }

    @PutMapping("/me/reported-incidents/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishReportedIncident(Authentication auth, @PathVariable String id, @RequestBody Result body) {
        staff.finishReportedIncident(auth.getName(), id, body.result());
    }

    @GetMapping("/payroll")
    public Map<String, Object> payroll(Authentication auth) { return staff.adminPayroll(auth.getName()); }

    @PutMapping("/payroll/{id}/{period}/paid")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pay(Authentication auth, @PathVariable String id, @PathVariable LocalDate period) {
        staff.pay(auth.getName(), id, period);
    }
}
