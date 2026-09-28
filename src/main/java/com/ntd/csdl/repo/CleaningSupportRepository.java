package com.ntd.csdl.repo;

import com.ntd.csdl.entity.CleaningSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CleaningSupportRepository
        extends JpaRepository<CleaningSupport, String> {

    // Derived Query
    List<CleaningSupport> findByRoomRoomId(String roomId);

    List<CleaningSupport> findByEmployeeEmployeeId(String employeeId);

    List<CleaningSupport> findByIsCompleted(String isCompleted);

    List<CleaningSupport> findBySupportDate(LocalDate date);

    // JPQL
    @Query("""
        SELECT c
        FROM CleaningSupport c
        WHERE c.supportDate BETWEEN :startDate AND :endDate
    """)
    List<CleaningSupport> findByDateRange(
            LocalDate startDate,
            LocalDate endDate
    );

    // Truy vấn nhiều bảng
    @Query("""
        SELECT c
        FROM CleaningSupport c
        JOIN FETCH c.room
        JOIN FETCH c.employee
        WHERE c.supportId = :supportId
    """)
    CleaningSupport findSupportDetail(String supportId);
}