package com.ntd.csdl.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffApplicationSchemaUpgrade implements ApplicationRunner {
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        if (hasSubmissionTime()) return;
        try {
            jdbc.execute("ALTER TABLE staff_applications ADD COLUMN submitted_at DATETIME(6) NULL");
        } catch (DataAccessException failure) {
            if (!hasSubmissionTime()) throw failure;
        }
    }

    private boolean hasSubmissionTime() {
        return jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='staff_applications' AND COLUMN_NAME='submitted_at'
                """, Integer.class) > 0;
    }
}
