package com.ntd.csdl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/** Repairs collisions in generated employee records without changing real contacts. */
@Service @RequiredArgsConstructor
public class DemoPhoneRepairService {
    private final JdbcTemplate jdbc;
    private static final String KEY = "demo-phone-repair-20261030-v1";

    static String normalized(Object phone) {
        String digits = phone == null ? "" : phone.toString().replaceAll("[^0-9]", "");
        return digits.startsWith("84") && digits.length()==11 ? "0"+digits.substring(2) : digits;
    }

    @Transactional
    public int repair() {
        jdbc.update("INSERT INTO demo_seed_history(seed_key,completed) VALUES (?,false) ON DUPLICATE KEY UPDATE seed_key=seed_key", KEY);
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT completed FROM demo_seed_history WHERE seed_key=? FOR UPDATE", Boolean.class, KEY))) return 0;
        var employees = jdbc.queryForList("SELECT employee_id,phone_number FROM employees ORDER BY employee_id FOR UPDATE");
        var tenants = jdbc.queryForList("SELECT phone_number FROM tenants ORDER BY tenant_id FOR UPDATE");
        var counts = new HashMap<String,Integer>();
        for (var row : tenants) {
            String phone=normalized(row.get("phone_number"));
            if (!phone.isEmpty()) counts.merge(phone,1,Integer::sum);
        }
        for (var row : employees) {
            String phone=normalized(row.get("phone_number"));
            if (!phone.isEmpty()) counts.merge(phone,1,Integer::sum);
        }
        var used=new HashSet<>(counts.keySet());
        int changed=0,next=1;
        for (var row : employees) {
            String id=row.get("employee_id").toString(),phone=normalized(row.get("phone_number"));
            if (!id.matches("(?:DEMO_NV|SEED_E)[0-9]{2}") || phone.isEmpty() || counts.getOrDefault(phone,0)<2) continue;
            String replacement;
            do { replacement=String.format(Locale.ROOT,"0908%06d",next++); } while (used.contains(replacement));
            changed+=jdbc.update("UPDATE employees SET phone_number=? WHERE employee_id=?",replacement,id);
            used.add(replacement);
            counts.merge(phone,-1,Integer::sum);
        }
        jdbc.update("UPDATE demo_seed_history SET completed=true WHERE seed_key=?", KEY);
        return changed;
    }
}
