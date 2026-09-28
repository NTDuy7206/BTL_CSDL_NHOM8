package com.ntd.csdl.repo;

import com.ntd.csdl.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, String> {

    // Derived Query
    List<Contract> findByRoomRoomId(String roomId);

    List<Contract> findByTenantTenantId(String tenantId);

    List<Contract> findByStartDateAfter(LocalDate date);

    List<Contract> findByEndDateBefore(LocalDate date);

    // JPQL
    @Query("""
        SELECT c
        FROM Contract c
        WHERE c.endDate < :currentDate
    """)
    List<Contract> findExpiredContracts(LocalDate currentDate);

    // Truy vấn nhiều bảng
    @Query("""
        SELECT c
        FROM Contract c
        JOIN FETCH c.room
        JOIN FETCH c.tenant
        WHERE c.contractId = :contractId
    """)
    Contract findContractDetail(String contractId);
}