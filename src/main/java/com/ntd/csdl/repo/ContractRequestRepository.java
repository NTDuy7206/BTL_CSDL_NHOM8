package com.ntd.csdl.repo;

import com.ntd.csdl.entity.ContractRequest;
import com.ntd.csdl.entity.ContractRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRequestRepository extends JpaRepository<ContractRequest, Long> {
    List<ContractRequest> findAllByOrderByCreatedAtDesc();
    List<ContractRequest> findByApplicantUsernameOrderByCreatedAtDesc(String username);
    long countByStatus(ContractRequestStatus status);
}
