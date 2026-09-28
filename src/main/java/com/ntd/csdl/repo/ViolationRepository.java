package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ViolationRepository extends JpaRepository<Violation, String> {

    // Derived Query
    List<Violation> findByRoomRoomId(String roomId);

    List<Violation> findByRuleRuleId(String ruleId);

    List<Violation> findByViolationDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    List<Violation> findByActualFineGreaterThan(BigDecimal amount);

    // JPQL
    @Query("""
        SELECT v
        FROM Violation v
        WHERE v.room.roomId = :roomId
    """)
    List<Violation> findViolationsByRoom(String roomId);

    // Truy vấn nhiều bảng
    @Query("""
        SELECT v
        FROM Violation v
        JOIN FETCH v.room
        JOIN FETCH v.rule
        WHERE v.violationId = :violationId
    """)
    Violation findViolationDetail(String violationId);

    // Thống kê số lần vi phạm theo phòng
    @Query("""
        SELECT v.room.roomId, COUNT(v)
        FROM Violation v
        GROUP BY v.room.roomId
    """)
    List<Object[]> countViolationsByRoom();
}