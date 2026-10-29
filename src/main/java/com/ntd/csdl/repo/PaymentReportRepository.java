package com.ntd.csdl.repo;

import com.ntd.csdl.entity.PaymentReport;
import com.ntd.csdl.entity.PaymentReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentReportRepository extends JpaRepository<PaymentReport, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from PaymentReport p where p.paymentReportId = :id")
    java.util.Optional<PaymentReport> lockById(@org.springframework.data.repository.query.Param("id") Long id);
    List<PaymentReport> findAllByOrderByCreatedAtDesc();
    List<PaymentReport> findByPayerUsernameOrderByCreatedAtDesc(String username);
    boolean existsByPayerUsernameAndInvoiceInvoiceIdAndStatusIn(
            String username,
            String invoiceId,
            List<PaymentReportStatus> statuses
    );
}
