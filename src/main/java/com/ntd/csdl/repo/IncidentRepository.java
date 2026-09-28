package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, String> {

    // Derived Query
    List<Incident> findByRoomRoomId(String roomId);

    List<Incident> findByStatus(String status);

    List<Incident> findByReportDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // JPQL
    @Query("""
        SELECT i
        FROM Incident i
        WHERE i.status = :status
        ORDER BY i.reportDate DESC
    """)
    List<Incident> findIncidentsByStatus(String status);

    // Truy vấn nhiều bảng
    @Query("""
        SELECT i
        FROM Incident i
        JOIN FETCH i.room
        WHERE i.incidentId = :incidentId
    """)
    Incident findIncidentDetail(String incidentId);
}