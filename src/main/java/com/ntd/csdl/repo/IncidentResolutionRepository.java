package com.ntd.csdl.repo;

import com.ntd.csdl.entity.IncidentResolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncidentResolutionRepository
        extends JpaRepository<IncidentResolution, String> {

    // Derived Query
    List<IncidentResolution> findByIncidentIncidentId(String incidentId);

    List<IncidentResolution> findByEmployeeEmployeeId(String employeeId);

    List<IncidentResolution> findByIsCompleted(String isCompleted);

    // JPQL
    @Query("""
        SELECT r
        FROM IncidentResolution r
        WHERE r.isCompleted = 'NO'
    """)
    List<IncidentResolution> findUncompletedResolutions();

    // Truy vấn nhiều bảng
    @Query("""
        SELECT r
        FROM IncidentResolution r
        JOIN FETCH r.incident
        JOIN FETCH r.employee
        WHERE r.resolutionId = :resolutionId
    """)
    IncidentResolution findResolutionDetail(String resolutionId);
}