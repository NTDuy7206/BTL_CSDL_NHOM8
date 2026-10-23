package com.ntd.csdl.dto;

import com.ntd.csdl.entity.PaymentReport;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PaymentReportResponseDTO {
    private final Long paymentReportId;
    private final String username;
    private final Long userId;
    private final String invoiceId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String transactionReference;
    private final String status;
    private final LocalDateTime createdAt;

    public PaymentReportResponseDTO(PaymentReport report) {
        this.paymentReportId = report.getPaymentReportId();
        this.username = report.getPayer().getUsername();
        this.userId = report.getPayer().getId();
        this.invoiceId = report.getInvoice().getInvoiceId();
        this.amount = report.getAmount();
        this.paymentMethod = report.getPaymentMethod();
        this.transactionReference = report.getTransactionReference();
        this.status = report.getStatus().name();
        this.createdAt = report.getCreatedAt();
    }
}
