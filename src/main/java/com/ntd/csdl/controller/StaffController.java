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

    public record Link(String username) {}
    public record Result(String result) {}
    public record Application(long landlordId, String message) {}
    public record Decision(String status, String employeeId) {}

    @GetMapping("/me/applications")
    public Map<String, Object> myApplications(Authentication auth) { return staff.applicationPage(auth.getName()); }

    @PostMapping("/me/applications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apply(Authentication auth, @RequestBody Application body) {
        staff.apply(auth.getName(), body.landlordId(), body.message());
    }

    @GetMapping("/applications")
    public Map<String, Object> applications(Authentication auth) {
        return Map.of("applications", staff.applications(auth.getName(), false), "profiles", staff.directory(auth.getName()));
    }

    @PutMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decide(Authentication auth, @PathVariable long id, @RequestBody Decision body) {
        if (!"APPROVED".equals(body.status()) && !"REJECTED".equals(body.status())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Chọn duyệt hoặc từ chối đơn");
        }
        staff.decide(auth.getName(), id, "APPROVED".equals(body.status()), body.employeeId());
    }

    @GetMapping("/directory")
    public List<Map<String, Object>> directory(Authentication auth) { return staff.directory(auth.getName()); }

    @PutMapping("/directory/{id}/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void link(Authentication auth, @PathVariable String id, @RequestBody Link body) {
        staff.link(auth.getName(), id, body.username());
    }

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

    @GetMapping("/payroll")
    public List<Map<String, Object>> payroll(Authentication auth) { return staff.payroll(auth.getName()); }

    @PutMapping("/payroll/{id}/{period}/paid")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pay(Authentication auth, @PathVariable String id, @PathVariable LocalDate period) {
        staff.pay(auth.getName(), id, period);
    }
}
