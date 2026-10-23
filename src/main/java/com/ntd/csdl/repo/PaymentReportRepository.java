package com.ntd.csdl.repo;

import com.ntd.csdl.entity.PaymentReport;
import com.ntd.csdl.entity.PaymentReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentReportRepository extends JpaRepository<PaymentReport, Long> {
    List<PaymentReport> findAllByOrderByCreatedAtDesc();
    List<PaymentReport> findByPayerUsernameOrderByCreatedAtDesc(String username);
    boolean existsByPayerUsernameAndInvoiceInvoiceIdAndStatusIn(
            String username,
            String invoiceId,
            List<PaymentReportStatus> statuses
    );
}
